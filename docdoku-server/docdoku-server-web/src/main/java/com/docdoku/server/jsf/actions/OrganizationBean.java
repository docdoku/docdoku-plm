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
import com.docdoku.core.exceptions.*;
import com.docdoku.core.services.IUserManagerLocal;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
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



    public String deleteOrganization() throws AccountNotFoundException, AccessRightException, OrganizationNotFoundException {
        String remoteUser = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
        Account account = userManager.getAccount(remoteUser);

        Organization organization = account.getOrganization();
        if(organization!=null) {
            userManager.deleteOrganization(organization.getName());
        }
        return "/admin/organization/organizationMenu.xhtml";

    }

    public String createOrganization() throws OrganizationAlreadyExistsException, CreationException, AccountNotFoundException, NotAllowedException {
        String remoteUser = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();

        Account account;
        if(userManager.isCallerInRole("admin")){
            account = userManager.getAccount(loginToAdd);
        }else{
            account = userManager.getAccount(remoteUser);
        }

        Organization organization = userManager.createOrganization(organizationName, account, organizationDescription);

        return "/admin/organization/organizationMenu.xhtml";
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
