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

import com.docdoku.core.common.Account;
import com.docdoku.core.common.Organization;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.services.IAccountManagerLocal;
import com.docdoku.core.services.IOrganizationManagerLocal;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Named("organizationBean")
@RequestScoped
public class OrganizationBean {

    @EJB
    private IAccountManagerLocal accountManager;
    @EJB
    private IOrganizationManagerLocal organizationManager;

    private Map<String, Boolean> selectedLogins = new HashMap<>();

    private String organizationName;
    private String organizationDescription;

    private String loginToAdd;

    public OrganizationBean() {
    }



    public List<Account> getAccountsToManage() {
        String remoteUser = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
        Organization organization = organizationManager.getOrganizationOfAccount(remoteUser);
        if(organization!=null){
            return organization.getMembers();
        }else{
            return null;
        }
    }



    public String editOrganization() {
        String remoteUser = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
        Organization organization = organizationManager.getOrganizationOfAccount(remoteUser);
        if(organization!=null) {
            organizationName=organization.getName();
            organizationDescription=organization.getDescription();
        }
        return "/admin/organization/organizationEditionForm.xhtml";
    }

    public String addAccount() throws NotAllowedException, AccountNotFoundException, AccessRightException, OrganizationNotFoundException {
        String remoteUser = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
        Organization organization = organizationManager.getOrganizationOfAccount(remoteUser);
        if (organization == null) {
            Account account = accountManager.getAccount(remoteUser);
            throw new NotAllowedException(new Locale(account.getLanguage()), "NotAllowedException62");
        }

        organizationManager.addAccountInOrganization(organization.getName(), loginToAdd);
        return "/admin/organization/manageAccounts.xhtml";
    }


    public void removeAccounts() throws AccountNotFoundException, AccessRightException, OrganizationNotFoundException, NotAllowedException {
        if (!selectedLogins.isEmpty()) {
            String remoteUser = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
            Organization organization = organizationManager.getOrganizationOfAccount(remoteUser);
            if (organization == null) {
                Account account = accountManager.getAccount(remoteUser);
                throw new NotAllowedException(new Locale(account.getLanguage()), "NotAllowedException62");
            }
            organizationManager.removeAccountsFromOrganization(organization.getName(), getLogins());
        }

        selectedLogins.clear();
    }


    public void moveMemberUp() throws AccountNotFoundException, NotAllowedException, OrganizationNotFoundException, AccessRightException {
        String[] logins=getLogins();
        if (logins.length==1) {
            String remoteUser = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
            Organization organization = organizationManager.getOrganizationOfAccount(remoteUser);
            if (organization == null) {
                Account account = accountManager.getAccount(remoteUser);
                throw new NotAllowedException(new Locale(account.getLanguage()), "NotAllowedException62");
            }
            List<Account> members=organization.getMembers();
            Account member = accountManager.getAccount(logins[0]);
            int i =members.indexOf(member);
            if(i>0) {
                Collections.swap(members, i - 1, i);
                organizationManager.updateOrganization(organization);
            }
        }
    }

    public void moveMemberDown() throws AccountNotFoundException, NotAllowedException, OrganizationNotFoundException, AccessRightException {
        String[] logins=getLogins();
        if (logins.length==1) {
            String remoteUser = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
            Organization organization = organizationManager.getOrganizationOfAccount(remoteUser);
            if (organization == null) {
                Account account = accountManager.getAccount(remoteUser);
                throw new NotAllowedException(new Locale(account.getLanguage()), "NotAllowedException62");
            }
            List<Account> members=organization.getMembers();
            Account member = accountManager.getAccount(logins[0]);
            int i =members.indexOf(member);
            if(i>-1 && i<members.size()-1) {
                Collections.swap(members, i + 1, i);
                organizationManager.updateOrganization(organization);
            }
        }
    }

    public String updateOrganization()
            throws AccountNotFoundException, AccessRightException, OrganizationNotFoundException, NotAllowedException {

        String remoteUser = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
        Organization organization = organizationManager.getOrganizationOfAccount(remoteUser);

        if (organization == null) {
            Account account = accountManager.getAccount(remoteUser);
            throw new NotAllowedException(new Locale(account.getLanguage()), "NotAllowedException62");
        }

        organization.setDescription(organizationDescription);
        organizationManager.updateOrganization(organization);
        return "/admin/organization/organizationMenu.xhtml";
    }

    public String deleteOrganization() throws AccountNotFoundException, AccessRightException, OrganizationNotFoundException {
        String remoteUser = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
        Organization organization = organizationManager.getOrganizationOfAccount(remoteUser);
        if(organization!=null) {
            organizationManager.deleteOrganization(organization.getName());
        }
        return "/admin/organization/organizationMenu.xhtml?faces-redirect=true";
    }

    public String createOrganization() throws OrganizationAlreadyExistsException, CreationException, AccountNotFoundException, NotAllowedException {
        organizationManager.createOrganization(organizationName, organizationDescription);
        return "/admin/organization/organizationMenu.xhtml?faces-redirect=true";
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
