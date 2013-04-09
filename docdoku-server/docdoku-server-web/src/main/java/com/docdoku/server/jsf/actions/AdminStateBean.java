/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,  
 * but WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
 * GNU Affero General Public License for more details.  
 *  
 * You should have received a copy of the GNU Affero General Public License  
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.  
 */
package com.docdoku.server.jsf.actions;

import com.docdoku.core.common.User;
import com.docdoku.core.common.UserGroup;
import com.docdoku.core.common.UserGroupKey;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.security.WorkspaceUserGroupMembership;
import com.docdoku.core.security.WorkspaceUserMembership;
import com.docdoku.core.services.*;
import org.apache.commons.io.FileUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.File;
import java.io.Serializable;

import java.sql.Array;
import java.util.*;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

@ManagedBean(name = "adminStateBean")
@SessionScoped
public class AdminStateBean implements Serializable {

    @EJB
    private IDocumentManagerLocal documentService;
    @EJB
    private IProductManagerLocal productService;
    @EJB
    private IUserManagerLocal userManager;

    private String selectedWorkspace;
    private String selectedGroup;
        
    public AdminStateBean() {
    }

    public User[] getUsers() throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        return documentService.getUsers(selectedWorkspace);
    }
    
    public User[] getUsersInGroup() throws UserGroupNotFoundException, WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException{
        UserGroup ug = userManager.getUserGroup(new UserGroupKey(selectedWorkspace,selectedGroup));
        return ug.getUsers().toArray(new User[ug.getUsers().size()]);
    }
    
    public User[] getUsersToManage() throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User[] users = documentService.getUsers(selectedWorkspace);
        List<User> usersToManage=new ArrayList<User>();
        Map<String, List<UserGroup>> usersGroups = getUsersGroups();
        Map<String, WorkspaceUserMembership> userMembers = getUserMembers();
        for(User u:users){
            if((usersGroups.get(u.getLogin())==null) || (userMembers.get(u.getLogin())!=null) || (u.getLogin().equals(getCurrentWorkspace().getAdmin().getLogin()))){
                usersToManage.add(u);
            }
        }
        return usersToManage.toArray(new User[usersToManage.size()]);
    }
    
    public UserGroup[] getGroups() throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        return userManager.getUserGroups(selectedWorkspace);
    }
    
    public Map<String, WorkspaceUserMembership> getUserMembers() throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {

        WorkspaceUserMembership[] userMemberships = userManager.getWorkspaceUserMemberships(selectedWorkspace);
        Map<String, WorkspaceUserMembership> userMembersMap = new HashMap<String, WorkspaceUserMembership>();
        for (WorkspaceUserMembership membership : userMemberships) {
            userMembersMap.put(membership.getMemberLogin(), membership);
        }
        return userMembersMap;
    }

    public Map<String, WorkspaceUserGroupMembership> getGroupMembers() throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        WorkspaceUserGroupMembership[] groupMemberships = userManager.getWorkspaceUserGroupMemberships(selectedWorkspace);
        Map<String, WorkspaceUserGroupMembership> groupMembersMap = new HashMap<String, WorkspaceUserGroupMembership>();
        for (WorkspaceUserGroupMembership membership : groupMemberships) {
            groupMembersMap.put(membership.getMemberId(), membership);
        }

        return groupMembersMap;
    }

    public Map<String, List<UserGroup>> getUsersGroups() throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        Map<String, List<UserGroup>> usersGroups = new HashMap<String, List<UserGroup>>();
        UserGroup[] groups = getGroups();
        for (UserGroup group : groups) {
            for (User user : group.getUsers()) {
                List<UserGroup> lstGroups = usersGroups.get(user.getLogin());
                if (lstGroups == null) {
                    lstGroups = new LinkedList<UserGroup>();
                    usersGroups.put(user.getLogin(), lstGroups);
                }
                lstGroups.add(group);
            }
        }
        return usersGroups;
    }

    public int getUsersCount() throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        return documentService.getUsers(selectedWorkspace).length;
    }

    public int getDocumentsCount() throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        return documentService.getDocumentsCountInWorkspace(getCurrentWorkspace().getId());
    }

    public int getProductsCount() throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException {
        return productService.getConfigurationItems(getCurrentWorkspace().getId()).size();
    }

    public int getPartsCount() throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException {
        return productService.getPartMastersCount(getCurrentWorkspace().getId());
    }

    public JSONObject getDiskSpaceUsageStats() throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException, AccountNotFoundException {

        Map<String, Long> diskUsage = new HashMap<String,Long>();

        Long documentDiskUsage = documentService.getDiskUsageForDocumentsInWorkspace(getCurrentWorkspace().getId());
        Long partsDiskUsage = productService.getDiskUsageForPartsInWorkspace(getCurrentWorkspace().getId());
        Long documentTemplatesDiskUsage = documentService.getDiskUsageForDocumentTemplatesInWorkspace(getCurrentWorkspace().getId());
        Long partTemplatesDiskUsage = productService.getDiskUsageForPartTemplatesInWorkspace(getCurrentWorkspace().getId());

        diskUsage.put("documents", documentDiskUsage);
        diskUsage.put("parts", partsDiskUsage);
        diskUsage.put("document-templates", documentTemplatesDiskUsage);
        diskUsage.put("part-templates", partTemplatesDiskUsage);

        return new JSONObject(diskUsage);

    }

    public JSONObject getCheckedOutDocumentsStats() throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException, AccountNotFoundException, JSONException {

        DocumentMaster[] checkedOutDocumentMasters = documentService.getAllCheckedOutDocumentMasters(getCurrentWorkspace().getId());

        JSONObject statsByUser = new JSONObject();

        for(DocumentMaster documentMaster : checkedOutDocumentMasters){

            String userLogin = documentMaster.getCheckOutUser().getLogin() ;

            JSONArray userArray;
            try {
                userArray = (JSONArray) statsByUser.get(userLogin);
            } catch (JSONException e) {
                userArray = new JSONArray();
                statsByUser.put(userLogin, userArray);
            }

            JSONObject doc = new JSONObject();
            doc.put("date",documentMaster.getCheckOutDate().getTime());
            userArray.put(doc);

        }

        return statsByUser;

    }

    public JSONObject getCheckedOutPartsStats() throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException, AccountNotFoundException, JSONException {

        PartRevision[] checkedOutPartRevisions = productService.getAllCheckedOutPartRevisions(getCurrentWorkspace().getId());

        JSONObject statsByUser = new JSONObject();

        for(PartRevision partRevision : checkedOutPartRevisions){

            String userLogin = partRevision.getCheckOutUser().getLogin() ;

            JSONArray userArray;
            try {
                userArray = (JSONArray) statsByUser.get(userLogin);
            } catch (JSONException e) {
                userArray = new JSONArray();
                statsByUser.put(userLogin, userArray);
            }

            JSONObject doc = new JSONObject();
            doc.put("date",partRevision.getCheckOutDate().getTime());
            userArray.put(doc);

        }

        return statsByUser;

    }



    public JSONArray getUsersInWorkspace() throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException, AccountNotFoundException, JSONException {

        JSONArray usersJSONArray = new JSONArray();
        User[] users = documentService.getUsers(selectedWorkspace);
        for(int i = 0 ; i< users.length ; i++ ){
            usersJSONArray.put(users[i].getLogin());
        }
        return usersJSONArray;

    }

    public Workspace getCurrentWorkspace() {
        return userManager.getWorkspace(selectedWorkspace); 
    }
    
    public String getSelectedWorkspace() {
        return selectedWorkspace;
    }

    public void setSelectedWorkspace(String selectedWorkspace) {
        this.selectedWorkspace = selectedWorkspace;
    }
    
    public String getSelectedGroup() {
        return selectedGroup;
    }

    public void setSelectedGroup(String selectedGroup) {
        this.selectedGroup = selectedGroup;
    }
    
}
