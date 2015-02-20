/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2015 DocDoku SARL
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
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.security.WorkspaceUserGroupMembership;
import com.docdoku.core.security.WorkspaceUserMembership;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.core.services.IUserManagerLocal;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.json.*;
import java.io.Serializable;
import java.util.*;

@Named("adminStateBean")
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

    public User[] getUsers() throws WorkspaceNotFoundException, AccountNotFoundException, AccessRightException, UserNotFoundException, UserNotActiveException {
        return documentService.getUsers(selectedWorkspace);
    }
    
    public User[] getUsersInGroup() throws UserGroupNotFoundException, WorkspaceNotFoundException, UserNotActiveException, UserNotFoundException, AccountNotFoundException {
        UserGroup ug = userManager.getUserGroup(new UserGroupKey(selectedWorkspace,selectedGroup));
        return ug.getUsers().toArray(new User[ug.getUsers().size()]);
    }
    
    public User[] getUsersToManage() throws WorkspaceNotFoundException, AccountNotFoundException, AccessRightException, UserNotFoundException, UserNotActiveException {
        User[] users = documentService.getUsers(selectedWorkspace);
        List<User> usersToManage=new ArrayList<>();
        Map<String, List<UserGroup>> usersGroups = getUsersGroups();
        Map<String, WorkspaceUserMembership> userMembers = getUserMembers();
        for(User u:users){
            if((usersGroups.get(u.getLogin())==null) || (userMembers.get(u.getLogin())!=null) || (u.getLogin().equals(getCurrentWorkspace().getAdmin().getLogin()))){
                usersToManage.add(u);
            }
        }
        return usersToManage.toArray(new User[usersToManage.size()]);
    }

    public UserGroup[] getGroups() throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, AccountNotFoundException {
        return userManager.getUserGroups(selectedWorkspace);
    }
    
    public Map<String, WorkspaceUserMembership> getUserMembers() throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, AccountNotFoundException {

        WorkspaceUserMembership[] userMemberships = userManager.getWorkspaceUserMemberships(selectedWorkspace);
        Map<String, WorkspaceUserMembership> userMembersMap = new HashMap<>();
        for (WorkspaceUserMembership membership : userMemberships) {
            userMembersMap.put(membership.getMemberLogin(), membership);
        }
        return userMembersMap;
    }

    public Map<String, WorkspaceUserGroupMembership> getGroupMembers() throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, AccountNotFoundException {
        WorkspaceUserGroupMembership[] groupMemberships = userManager.getWorkspaceUserGroupMemberships(selectedWorkspace);
        Map<String, WorkspaceUserGroupMembership> groupMembersMap = new HashMap<>();
        for (WorkspaceUserGroupMembership membership : groupMemberships) {
            groupMembersMap.put(membership.getMemberId(), membership);
        }

        return groupMembersMap;
    }

    public Map<String, List<UserGroup>> getUsersGroups() throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, AccountNotFoundException {
        Map<String, List<UserGroup>> usersGroups = new HashMap<>();
        UserGroup[] groups = getGroups();
        for (UserGroup group : groups) {
            for (User user : group.getUsers()) {
                List<UserGroup> lstGroups = usersGroups.get(user.getLogin());
                if (lstGroups == null) {
                    lstGroups = new LinkedList<>();
                    usersGroups.put(user.getLogin(), lstGroups);
                }
                lstGroups.add(group);
            }
        }
        return usersGroups;
    }

    public int getUsersCount() throws WorkspaceNotFoundException, AccountNotFoundException, AccessRightException, UserNotFoundException, UserNotActiveException {
        return documentService.getUsers(selectedWorkspace).length;
    }

    public int getDocumentsCount() throws WorkspaceNotFoundException, AccountNotFoundException, AccessRightException {
        return documentService.getTotalNumberOfDocuments(selectedWorkspace);
    }

    public int getProductsCount() throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        return productService.getConfigurationItems(selectedWorkspace).size();
    }

    public int getPartsCount() throws WorkspaceNotFoundException, AccountNotFoundException, AccessRightException, UserNotFoundException, UserNotActiveException {
        return productService.getTotalNumberOfParts(selectedWorkspace);
    }

    public JsonObject getDiskSpaceUsageStats() throws WorkspaceNotFoundException, AccountNotFoundException, AccessRightException {

        long documentDiskUsage = documentService.getDiskUsageForDocumentsInWorkspace(selectedWorkspace);
        long partsDiskUsage = productService.getDiskUsageForPartsInWorkspace(selectedWorkspace);
        long documentTemplatesDiskUsage = documentService.getDiskUsageForDocumentTemplatesInWorkspace(selectedWorkspace);
        long partTemplatesDiskUsage = productService.getDiskUsageForPartTemplatesInWorkspace(selectedWorkspace);

        return Json.createObjectBuilder()
        .add("documents", documentDiskUsage)
        .add("parts", partsDiskUsage)
        .add("document-templates", documentTemplatesDiskUsage)
        .add("part-templates", partTemplatesDiskUsage).build();
    }

    public JsonObject getCheckedOutDocumentsStats() throws WorkspaceNotFoundException, AccountNotFoundException, AccessRightException {

        DocumentRevision[] checkedOutDocumentRevisions = documentService.getAllCheckedOutDocumentRevisions(selectedWorkspace);
        JsonObjectBuilder statsByUserBuilder = Json.createObjectBuilder();

        Map<String, JsonArrayBuilder> userArrays=new HashMap<>();
        for(DocumentRevision documentRevision : checkedOutDocumentRevisions){

            String userLogin = documentRevision.getCheckOutUser().getLogin();
            JsonArrayBuilder userArray=userArrays.get(userLogin);
            if(userArray==null) {
                userArray = Json.createArrayBuilder();
                userArrays.put(userLogin, userArray);
            }
            userArray.add(Json.createObjectBuilder().add("date",documentRevision.getCheckOutDate().getTime()).build());
        }

        for(Map.Entry<String,JsonArrayBuilder> entry : userArrays.entrySet()){
            statsByUserBuilder.add(entry.getKey(),entry.getValue().build());
        }

        return statsByUserBuilder.build();

    }

    public JsonObject getCheckedOutPartsStats() throws WorkspaceNotFoundException, AccountNotFoundException, AccessRightException {

        PartRevision[] checkedOutPartRevisions = productService.getAllCheckedOutPartRevisions(selectedWorkspace);
        JsonObjectBuilder statsByUserBuilder = Json.createObjectBuilder();

        Map<String, JsonArrayBuilder> userArrays=new HashMap<>();
        for(PartRevision partRevision : checkedOutPartRevisions){

            String userLogin = partRevision.getCheckOutUser().getLogin();
            JsonArrayBuilder userArray=userArrays.get(userLogin);
            if(userArray==null) {
                userArray = Json.createArrayBuilder();
                userArrays.put(userLogin, userArray);
            }
            userArray.add(Json.createObjectBuilder().add("date", partRevision.getCheckOutDate().getTime()).build());
        }               

        for(Map.Entry<String,JsonArrayBuilder> entry : userArrays.entrySet()){
            statsByUserBuilder.add(entry.getKey(),entry.getValue().build());
        }
        
        return statsByUserBuilder.build();

    }



    public JsonArray getUsersInWorkspace() throws WorkspaceNotFoundException, AccountNotFoundException, AccessRightException, UserNotFoundException, UserNotActiveException {

        JsonArrayBuilder usersJSONArrayBuilder = Json.createArrayBuilder();
        User[] users = documentService.getUsers(selectedWorkspace);
        for (User user : users) {
            usersJSONArrayBuilder.add(user.getLogin());
        }
        return usersJSONArrayBuilder.build();

    }

    public JsonObject getUsersStats() throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, AccountNotFoundException, AccessRightException {

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



    public Workspace getCurrentWorkspace() throws WorkspaceNotFoundException, AccountNotFoundException {
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
