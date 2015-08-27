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

import com.docdoku.core.common.Workspace;
import com.docdoku.core.exceptions.AccountAlreadyExistsException;
import com.docdoku.core.exceptions.AccountNotFoundException;
import com.docdoku.core.exceptions.CreationException;
import com.docdoku.core.services.IAccountManagerLocal;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.*;

@Named("accountBean")
@RequestScoped
public class AccountBean {

    @EJB
    private IAccountManagerLocal accountManager;
    
    private String password;

    private String login;
    private String name;
    private String email;
    private String language;
    private String timeZone;
    private Locale locale;

    private static String[] availableTimeZones = TimeZone.getAvailableIDs();

    private boolean superAdmin;

    private Map<String, Workspace> administeredWorkspaces = new HashMap<>();
    private Set<Workspace> regularWorkspaces = new HashSet<>();

    private String organizationName;
    private String organizationAdmin;

    public AccountBean() {
    }

    public String register() throws AccountAlreadyExistsException, CreationException, ServletException {

        HttpServletRequest request = (HttpServletRequest) (FacesContext.getCurrentInstance().getExternalContext().getRequest());

        if(language == null || "".equals(language) || " ".equals(language)){
            language = FacesContext.getCurrentInstance().getViewRoot().getLocale().getLanguage();
        }

        accountManager.createAccount(login, name, email, language, password, timeZone);
        request.login(login, password);

        HttpSession session = request.getSession();
        session.setAttribute("remoteUser",login);
        return request.getContextPath()+"/register.xhtml";
    }

    public String updateAccount() throws AccountNotFoundException {
        if(language == null || "".equals(language) || " ".equals(language)){
            language = FacesContext.getCurrentInstance().getViewRoot().getLocale().getLanguage();
        }
        FacesContext.getCurrentInstance().getViewRoot().setLocale(new Locale(language));
        accountManager.updateAccount(name, email, language, password,timeZone);
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
        this.locale = new Locale(language);
    }
    public String getBrowserLanguage() {
        return FacesContext.getCurrentInstance().getViewRoot().getLocale().getLanguage();
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
        return new TreeMap<>(administeredWorkspaces);
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

    public String[] getAvailableTimeZones() {
        return availableTimeZones.clone();
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
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

    public Locale getLocale() {
        return locale;
    }

    public String addTimeZone(Date date){

        if(date == null){
            return "";
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if(timeZone == null || timeZone.isEmpty()){
            return simpleDateFormat.format(date);
        }

        simpleDateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
        return simpleDateFormat.format(date);
    }
}