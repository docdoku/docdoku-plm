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
import com.docdoku.core.common.Organization;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.core.services.IWorkspaceManagerLocal;
import com.docdoku.core.util.NamingConvention;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

@Named("organizationBean")
@RequestScoped
public class OrganizationBean {


    @EJB
    private IUserManagerLocal userManager;


    private Map<String, Boolean> selectedLogins = new HashMap<>();
    private String loginToAdd;
    private String organizationAdmin;
    private String organizationName;
    private String organizationDescription;

    public OrganizationBean() {
    }

    /*
    public String editOrganization() throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        Workspace wks = adminState.getCurrentWorkspace();
        this.workspaceId = wks.getId();
        this.workspaceDescription = wks.getDescription();
        //this.workspaceAdmin = new User();
        this.workspaceAdmin = wks.getAdmin().getLogin();

        return "/admin/workspace/workspaceEditionForm.xhtml";
    }



    
    public String addUser() throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException, UserGroupNotFoundException, NotAllowedException, AccountNotFoundException, UserAlreadyExistsException, FolderAlreadyExistsException, CreationException {
        Workspace workspace = adminState.getCurrentWorkspace();
        //TODO switch to a more JSF style code
        HttpServletRequest request = (HttpServletRequest) (FacesContext.getCurrentInstance().getExternalContext().getRequest());
        
        if (adminState.getSelectedGroup() != null && !adminState.getSelectedGroup().equals("")) {
            userManager.addUserInGroup(new UserGroupKey(workspace.getId(), adminState.getSelectedGroup()), loginToAdd);
            return "/admin/workspace/manageUsersGroup.xhtml?group=" + adminState.getSelectedGroup();
        } else {
            userManager.addUserInWorkspace(workspace.getId(), loginToAdd);
            return "/admin/workspace/manageUsers.xhtml";
        }

    }

    public String updateWorkspace() throws AccountNotFoundException, AccessRightException, WorkspaceNotFoundException {

        //TODO switch to a more JSF style code
        HttpServletRequest request = (HttpServletRequest) (FacesContext.getCurrentInstance().getExternalContext().getRequest());
        HttpSession sessionHTTP = request.getSession();

        Workspace workspace = adminState.getCurrentWorkspace();

        Workspace clone = workspace.clone();

        clone.setFolderLocked(freezeFolders);
        clone.setDescription(workspaceDescription);


        Account newAdmin = null;
        if (!clone.getAdmin().getLogin().equals(workspaceAdmin)) {
            newAdmin = userManager.getAccount(workspaceAdmin);
            clone.setAdmin(newAdmin);
        }
        userManager.updateWorkspace(clone);

        workspace.setDescription(workspaceDescription);
        workspace.setFolderLocked(freezeFolders);
        if (newAdmin != null) {
            workspace.setAdmin(newAdmin);
            if(!userManager.isCallerInRole("admin")){
                Map<String, Workspace> administeredWorkspaces = (Map<String, Workspace>) sessionHTTP.getAttribute("administeredWorkspaces");
                administeredWorkspaces.remove(workspace.getId());
                Set<Workspace> regularWorkspaces = (Set<Workspace>) sessionHTTP.getAttribute("regularWorkspaces");
                regularWorkspaces.add(workspace);
            }
        }

        return "/admin/workspace/editWorkspace.xhtml";
    }
    */

    public String deleteOrganization() throws AccountNotFoundException, AccessRightException, OrganizationNotFoundException {
        //TODO switch to a more JSF style code
        HttpServletRequest request = (HttpServletRequest) (FacesContext.getCurrentInstance().getExternalContext().getRequest());
        HttpSession sessionHTTP = request.getSession();

        Account account = (Account) sessionHTTP.getAttribute("account");
        Organization organization = account.getOrganization();
        if(organization!=null) {
            userManager.deleteOrganization(organization.getName());
        }
        return "/admin/organization/organizationMenu.xhtml";

    }

    public String createOrganization() throws OrganizationAlreadyExistsException, CreationException, AccountNotFoundException, NotAllowedException {
        //TODO switch to a more JSF style code
        HttpServletRequest request = (HttpServletRequest) (FacesContext.getCurrentInstance().getExternalContext().getRequest());
        HttpSession sessionHTTP = request.getSession();

        Account account;
        if(userManager.isCallerInRole("admin")){
            account = userManager.getAccount(loginToAdd);
        }else{
            account = (Account) sessionHTTP.getAttribute("account");
        }

        Organization organization = userManager.createOrganization(organizationName, account, organizationDescription);

        return "/admin/organization/organizationMenu.xhtml";
    }


    /*
    public void remove() throws UserGroupNotFoundException, AccessRightException, UserNotFoundException, NotAllowedException, AccountNotFoundException, WorkspaceNotFoundException, FolderNotFoundException, ESServerException {
        if (!selectedLogins.isEmpty()) {
            userManager.removeUsers(adminState.getSelectedWorkspace(), getLogins());
        }

        selectedLogins.clear();
    }

*/

    private String[] getLogins() {
        List<String> logins = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : selectedLogins.entrySet()) {
            if (entry.getValue()) {
                logins.add(entry.getKey());
            }
        }
        return logins.toArray(new String[logins.size()]);
    }

    public String getOrganizationAdmin() {
        return organizationAdmin;
    }

    public void setOrganizationAdmin(String organizationAdmin) {
        this.organizationAdmin = organizationAdmin;
    }

    public String getOrganizationDescription() {
        return organizationDescription;
    }

    public void setOrganizationDescription(String organizationDescription) {
        this.organizationDescription = organizationDescription;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
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

    
}
