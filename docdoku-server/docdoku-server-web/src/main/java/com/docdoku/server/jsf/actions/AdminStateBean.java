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
import com.docdoku.core.exceptions.*;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.security.WorkspaceUserGroupMembership;
import com.docdoku.core.security.WorkspaceUserMembership;
import com.docdoku.core.services.*;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.json.*;
import java.io.Serializable;
import java.util.*;

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
        return documentService.getDocumentsCountInWorkspace(selectedWorkspace);
    }

    public int getProductsCount() throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException {
        return productService.getConfigurationItems(selectedWorkspace).size();
    }

    public int getPartsCount() throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException {
        return productService.getPartMastersCount(selectedWorkspace);
    }

    public JsonObject getDiskSpaceUsageStats() throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException, AccountNotFoundException {

        Long documentDiskUsage = documentService.getDiskUsageForDocumentsInWorkspace(selectedWorkspace);
        Long partsDiskUsage = productService.getDiskUsageForPartsInWorkspace(selectedWorkspace);
        Long documentTemplatesDiskUsage = documentService.getDiskUsageForDocumentTemplatesInWorkspace(selectedWorkspace);
        Long partTemplatesDiskUsage = productService.getDiskUsageForPartTemplatesInWorkspace(selectedWorkspace);

        return Json.createObjectBuilder()
        .add("documents", documentDiskUsage)
        .add("parts", partsDiskUsage)
        .add("document-templates", documentTemplatesDiskUsage)
        .add("part-templates", partTemplatesDiskUsage).build();
    }

    public JsonObject getCheckedOutDocumentsStats() throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException, AccountNotFoundException {

        DocumentMaster[] checkedOutDocumentMasters = documentService.getAllCheckedOutDocumentMasters(selectedWorkspace);
        JsonObjectBuilder statsByUserBuilder = Json.createObjectBuilder();

        Map<String, JsonArrayBuilder> userArrays=new HashMap<>();
        for(DocumentMaster documentMaster : checkedOutDocumentMasters){

            String userLogin = documentMaster.getCheckOutUser().getLogin();
            JsonArrayBuilder userArray=userArrays.get(userLogin);
            if(userArray==null) {
                userArray = Json.createArrayBuilder();
                userArrays.put(userLogin, userArray);
                statsByUserBuilder.add(userLogin, userArray);
            }
            userArray.add(Json.createObjectBuilder().add("date",documentMaster.getCheckOutDate().getTime()).build());
        }

        return statsByUserBuilder.build();

    }

    public JsonObject getCheckedOutPartsStats() throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException, AccountNotFoundException {

        PartRevision[] checkedOutPartRevisions = productService.getAllCheckedOutPartRevisions(selectedWorkspace);
        JsonObjectBuilder statsByUserBuilder = Json.createObjectBuilder();

        Map<String, JsonArrayBuilder> userArrays=new HashMap<>();
        for(PartRevision partRevision : checkedOutPartRevisions){

            String userLogin = partRevision.getCheckOutUser().getLogin();
            JsonArrayBuilder userArray=userArrays.get(userLogin);
            if(userArray==null) {
                userArray = Json.createArrayBuilder();
                userArrays.put(userLogin, userArray);
                statsByUserBuilder.add(userLogin, userArray);
            }
            userArray.add(Json.createObjectBuilder().add("date",partRevision.getCheckOutDate().getTime()).build());
        }

        return statsByUserBuilder.build();

    }



    public JsonArray getUsersInWorkspace() throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException, AccountNotFoundException {

        JsonArrayBuilder usersJSONArrayBuilder = Json.createArrayBuilder();
        User[] users = documentService.getUsers(selectedWorkspace);
        for(int i = 0 ; i< users.length ; i++ ){
            usersJSONArrayBuilder.add(users[i].getLogin());
        }
        return usersJSONArrayBuilder.build();

    }

    public JsonObject getUsersStats() throws WorkspaceNotFoundException, UserNotActiveException, UserNotFoundException {

        WorkspaceUserMembership[] workspaceUserMemberships = userManager.getWorkspaceUserMemberships(selectedWorkspace);
        WorkspaceUserGroupMembership[] workspaceUserGroupMemberships = userManager.getWorkspaceUserGroupMemberships(selectedWorkspace);

        int usersCount = getUsersCount();
        int activeUsersCount = workspaceUserMemberships.length;
        int inactiveUsersCount = usersCount - activeUsersCount;

        int groupsCount = getGroups().length;
        int activeGroupsCount = workspaceUserGroupMemberships.length;
        int inactiveGroupsCount = groupsCount - activeGroupsCount;

        return Json.createObjectBuilder()
        .add("users", usersCount)
        .add("activeusers", activeUsersCount)
        .add("inactiveusers", inactiveUsersCount)
        .add("groups", groupsCount)
        .add("activegroups", activeGroupsCount)
        .add("inactivegroups", inactiveGroupsCount).build();
    }



    public Workspace getCurrentWorkspace() throws WorkspaceNotFoundException {
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
