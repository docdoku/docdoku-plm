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
import com.docdoku.core.common.Workspace;
import com.docdoku.core.services.AccountNotFoundException;
import com.docdoku.core.services.IUserManagerLocal;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.*;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@ManagedBean(name = "connectionBean")
@RequestScoped
public class ConnectionBean {
    
    @EJB
    private IUserManagerLocal userManager;

    private String login;
    private String password;

    private String originURL;
    
    public ConnectionBean() {
    }

    public String logOut() throws ServletException {

        //TODO switch to a more JSF style code
        HttpServletRequest request = (HttpServletRequest) (FacesContext.getCurrentInstance().getExternalContext().getRequest());
        HttpSession session = (HttpSession) request.getSession();
        session.removeAttribute("account");
        session.removeAttribute("administeredWorkspaces");
        session.removeAttribute("regularWorkspaces");
        request.logout();
        return "/admin/logout.xhtml";
    }

    public void logIn() throws ServletException, AccountNotFoundException, IOException {
        //TODO switch to a more JSF style code
        HttpServletRequest request = (HttpServletRequest) (FacesContext.getCurrentInstance().getExternalContext().getRequest());     
        HttpSession session = (HttpSession) request.getSession();
        request.login(login, password);

        Account account = userManager.getAccount(login);

        //case insensitive fix
        if(!login.equals(account.getLogin())){
            request.logout();
            throw new AccountNotFoundException(new Locale(account.getLanguage()),login);
        }

        session.setAttribute("account", account);

        Map<String, Workspace> administeredWorkspaces = new HashMap<String, Workspace>();
        for (Workspace wks : userManager.getAdministratedWorkspaces()) {
            administeredWorkspaces.put(wks.getId(), wks);
        }
        session.setAttribute("administeredWorkspaces", administeredWorkspaces);

        Set<Workspace> regularWorkspaces = new HashSet<Workspace>();
        regularWorkspaces.addAll(Arrays.asList(userManager.getWorkspaces()));
        regularWorkspaces.removeAll(administeredWorkspaces.values());
        session.setAttribute("regularWorkspaces", regularWorkspaces);

        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();
        if(originURL!=null && originURL.length()>1)
            ec.redirect(originURL);
        else
            ec.redirect(request.getContextPath() + "/document-management/");
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOriginURL() {
        return originURL;
    }

    public void setOriginURL(String originURL) {
        this.originURL = originURL;
    }
    
    
}
