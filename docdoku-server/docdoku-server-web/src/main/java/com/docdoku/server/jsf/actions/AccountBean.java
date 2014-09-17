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

import com.docdoku.core.common.Workspace;
import com.docdoku.core.exceptions.AccountNotFoundException;
import com.docdoku.core.services.IUserManagerLocal;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Named("accountBean")
@RequestScoped
public class AccountBean {

    @EJB
    private IUserManagerLocal userManager;
    
    private String password;

    private String login;
    private String name;
    private String email;
    private String language;

    private boolean superAdmin;

    private Map<String, Workspace> administeredWorkspaces = new HashMap<>();
    private Set<Workspace> regularWorkspaces = new HashSet<>();

    private String organizationName;
    private String organizationAdmin;

    public AccountBean() {
    }

    public String updateAccount() throws AccountNotFoundException {
        language = FacesContext.getCurrentInstance().getViewRoot().getLocale().getLanguage();
        userManager.updateAccount(name, email, language, password);
        HttpServletRequest request = (HttpServletRequest) (FacesContext.getCurrentInstance().getExternalContext().getRequest());
        return request.getContextPath()+"/";
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getLogin() {
        return login;
    }
    public void setLogin(String login) {
        this.login = login;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getLanguage() {
        return language;
    }
    public void setLanguage(String language) {
        this.language = language;
    }

    public String getOrganizationName() {
        return organizationName;
    }
    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public boolean isSuperAdmin() {
        return superAdmin;
    }
    public void setSuperAdmin(boolean superAdmin) {
        this.superAdmin = superAdmin;
    }

    public Map<String, Workspace> getAdministeredWorkspaces() {
        return administeredWorkspaces;
    }
    public void setAdministeredWorkspaces(Map<String, Workspace> administeredWorkspaces) {
        this.administeredWorkspaces = administeredWorkspaces;
    }

    public Set<Workspace> getRegularWorkspaces() {
        return regularWorkspaces;
    }
    public void setRegularWorkspaces(Set<Workspace> regularWorkspaces) {
        this.regularWorkspaces = regularWorkspaces;
    }

    public String getOrganizationAdmin() {
        return organizationAdmin;
    }
    public void setOrganizationAdmin(String organizationAdmin) {
        this.organizationAdmin = organizationAdmin;
    }

    public Set<Workspace> getWorkspaces(){
        Set<Workspace> workspaces = new TreeSet<>();
        for(Workspace wk : administeredWorkspaces.values()){
            workspaces.add(wk);
        }
        for(Workspace wk : regularWorkspaces){
            workspaces.add(wk);
        }
        return workspaces;
    }
}