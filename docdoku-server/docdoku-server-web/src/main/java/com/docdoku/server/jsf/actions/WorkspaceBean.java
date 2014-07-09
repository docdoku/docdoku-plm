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

import com.docdoku.core.common.Account;
import com.docdoku.core.common.User;
import com.docdoku.core.common.UserGroupKey;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.core.services.IWorkspaceManagerLocal;
import com.docdoku.core.util.NamingConvention;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.*;

@Named("workspaceBean")
@RequestScoped
public class WorkspaceBean {

    @EJB
    private IWorkspaceManagerLocal workspaceManager;

    @EJB
    private IUserManagerLocal userManager;

    @Inject
    private AdminStateBean adminState;

    private Map<String, Boolean> selectedGroups = new HashMap<>();
    private Map<String, Boolean> selectedLogins = new HashMap<>();
    private String loginToAdd;
    private String groupToCreate;
    private String workspaceAdmin;
    private String workspaceId;
    private String workspaceDescription;
    private boolean freezeFolders;

    public WorkspaceBean() {
    }

    public String editWorkspace() throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        Workspace wks = adminState.getCurrentWorkspace();
        this.workspaceId = wks.getId();
        this.workspaceDescription = wks.getDescription();
        this.freezeFolders = wks.isFolderLocked();
        //this.workspaceAdmin = new User();
        this.workspaceAdmin = wks.getAdmin().getLogin();

        return "/admin/workspace/workspaceEditionForm.xhtml";
    }


    public String synchronizeIndexer() throws AccessRightException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        if(userManager.isCallerInRole("admin")){
            String wksId = adminState.getSelectedWorkspace();
            workspaceManager.synchronizeIndexer(wksId);
            return "/admin/workspace/workspaceIndexed.xhtml";
        }else{
            User user = userManager.checkWorkspaceReadAccess(adminState.getSelectedWorkspace());
            throw new AccessRightException(new Locale(user.getLanguage()),user);
        }
    }

    public String deleteWorkspace() throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException, IOException, StorageException, AccountNotFoundException {

        Workspace wks = adminState.getCurrentWorkspace();
        User user = null;
        boolean isRoot = userManager.isCallerInRole("admin");
        boolean canAccess = isRoot;

        if(!isRoot){
            user = userManager.checkWorkspaceReadAccess(wks.getId());
            canAccess = wks.getAdmin().getLogin().equals(user.getLogin());
        }

        if(canAccess){
            workspaceManager.deleteWorkspace(wks.getId());
            return "/admin/workspace/workspaceDeleted.xhtml";

        }else{
            throw new AccessRightException(new Locale(user.getLanguage()),user);
        }
    }

    public String createGroup() throws UserGroupAlreadyExistsException, AccessRightException, AccountNotFoundException, CreationException, WorkspaceNotFoundException {
        userManager.createUserGroup(groupToCreate, adminState.getCurrentWorkspace());
        return "/admin/workspace/manageUsers.xhtml";
    }

    
    public String addUser() throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException, UserGroupNotFoundException, NotAllowedException, AccountNotFoundException, UserAlreadyExistsException, FolderAlreadyExistsException, CreationException {
        Workspace workspace = adminState.getCurrentWorkspace();

        if (adminState.getSelectedGroup() != null && !adminState.getSelectedGroup().equals("")) {
            userManager.addUserInGroup(new UserGroupKey(workspace.getId(), adminState.getSelectedGroup()), loginToAdd);
            return "/admin/workspace/manageUsersGroup.xhtml?group=" + adminState.getSelectedGroup();
        } else {
            userManager.addUserInWorkspace(workspace.getId(), loginToAdd);
            return "/admin/workspace/manageUsers.xhtml";
        }

    }

    public String updateWorkspace() throws AccountNotFoundException, AccessRightException, WorkspaceNotFoundException {
        Workspace workspace = adminState.getCurrentWorkspace();
        Account newAdmin = userManager.getAccount(workspaceAdmin);
        workspace.setDescription(workspaceDescription);
        workspace.setFolderLocked(freezeFolders);
        workspace.setAdmin(newAdmin);
        userManager.updateWorkspace(workspace);

        return "/admin/workspace/editWorkspace.xhtml";
    }

    public String createWorkspace() throws FolderAlreadyExistsException, UserAlreadyExistsException, WorkspaceAlreadyExistsException, CreationException, NotAllowedException, AccountNotFoundException {
        String remoteUser = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();

        Account account;
        if(userManager.isCallerInRole("admin")){
            account = userManager.getAccount(loginToAdd);
        }else{
            account = userManager.getAccount(remoteUser);
        }

        if (!NamingConvention.correct(workspaceId)) {
            throw new NotAllowedException(new Locale(account.getLanguage()), "NotAllowedException9");
        }

        try {
            Workspace workspace = userManager.createWorkspace(workspaceId, account, workspaceDescription, freezeFolders);
            adminState.setSelectedWorkspace(workspace.getId());
            adminState.setSelectedGroup(null);

        } catch (ESIndexNamingException e) {
            throw new NotAllowedException(new Locale(account.getLanguage()), "NotAllowedException9");
        }
        return "/admin/workspace/createWorkspace.xhtml";
    }

    public void read() throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException {
        if (!selectedLogins.isEmpty()) {
            userManager.grantUserAccess(adminState.getSelectedWorkspace(), getLogins(), true);
        }
        if (!selectedGroups.isEmpty()) {
            userManager.grantGroupAccess(adminState.getSelectedWorkspace(), getGroupIds(), true);
        }
        selectedGroups.clear();
        selectedLogins.clear();
    }

    public void removeUserFromGroup() throws AccessRightException, UserGroupNotFoundException, AccountNotFoundException, WorkspaceNotFoundException{
        userManager.removeUserFromGroup(new UserGroupKey(adminState.getSelectedWorkspace(),adminState.getSelectedGroup()),getLogins());
        selectedGroups.clear();
        selectedLogins.clear();
    }
    
    public void remove() throws UserGroupNotFoundException, AccessRightException, UserNotFoundException, NotAllowedException, AccountNotFoundException, WorkspaceNotFoundException, FolderNotFoundException, ESServerException {
        if (!selectedLogins.isEmpty()) {
            userManager.removeUsers(adminState.getSelectedWorkspace(), getLogins());
        }
        if (!selectedGroups.isEmpty()) {
            userManager.removeUserGroups(adminState.getSelectedWorkspace(), getGroupIds());
        }
        selectedGroups.clear();
        selectedLogins.clear();
    }

    public void full() throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException {
        if (!selectedLogins.isEmpty()) {
            userManager.grantUserAccess(adminState.getSelectedWorkspace(), getLogins(), false);
        }
        if (!selectedGroups.isEmpty()) {
            userManager.grantGroupAccess(adminState.getSelectedWorkspace(), getGroupIds(), false);
        }
        selectedGroups.clear();
        selectedLogins.clear();
    }

    public void enable() throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException {
        if (!selectedLogins.isEmpty()) {
            userManager.activateUsers(adminState.getSelectedWorkspace(), getLogins());
        }
        if (!selectedGroups.isEmpty()) {
            userManager.activateUserGroups(adminState.getSelectedWorkspace(), getGroupIds());
        }
        selectedGroups.clear();
        selectedLogins.clear();
    }

    public void disable() throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException {
        if (!selectedLogins.isEmpty()) {
            userManager.passivateUsers(adminState.getSelectedWorkspace(), getLogins());
        }
        if (!selectedGroups.isEmpty()) {
            userManager.passivateUserGroups(adminState.getSelectedWorkspace(), getGroupIds());
        }
        selectedGroups.clear();
        selectedLogins.clear();
    }

    private String[] getGroupIds() {
        List<String> groupIds = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : selectedGroups.entrySet()) {
            if (entry.getValue()) {
                groupIds.add(entry.getKey());
            }
        }
        return groupIds.toArray(new String[groupIds.size()]);
    }

    private String[] getLogins() {
        List<String> logins = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : selectedLogins.entrySet()) {
            if (entry.getValue()) {
                logins.add(entry.getKey());
            }
        }
        return logins.toArray(new String[logins.size()]);
    }

    public void setWorkspaceDescription(String workspaceDescription) {
        this.workspaceDescription = workspaceDescription;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public void setFreezeFolders(boolean freezeFolders) {
        this.freezeFolders = freezeFolders;
    }

    public String getWorkspaceDescription() {
        return workspaceDescription;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public boolean isFreezeFolders() {
        return freezeFolders;
    }

    public String getWorkspaceAdmin() {
        return workspaceAdmin;
    }

    public void setWorkspaceAdmin(String workspaceAdmin) {
        this.workspaceAdmin = workspaceAdmin;
    }

    public AdminStateBean getAdminState() {
        return adminState;
    }

    public void setAdminState(AdminStateBean adminState) {
        this.adminState = adminState;
    }

    public Map<String, Boolean> getSelectedGroups() {
        return selectedGroups;
    }

    public void setSelectedGroups(Map<String, Boolean> selectedGroups) {
        this.selectedGroups = selectedGroups;
    }

    public Map<String, Boolean> getSelectedLogins() {
        return selectedLogins;
    }

    public void setSelectedLogins(Map<String, Boolean> selectedLogins) {
        this.selectedLogins = selectedLogins;
    }

    public String getLoginToAdd() {
        return loginToAdd;
    }

    public void setLoginToAdd(String loginToAdd) {
        this.loginToAdd = loginToAdd;
    }

    public String getGroupToCreate() {
        return groupToCreate;
    }

    public void setGroupToCreate(String groupToCreate) {
        this.groupToCreate = groupToCreate;
    }
    
}
