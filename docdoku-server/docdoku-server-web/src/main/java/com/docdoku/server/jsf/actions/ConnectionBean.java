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
import com.docdoku.core.exceptions.AccountNotFoundException;
import com.docdoku.core.services.IUserManagerLocal;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Locale;

@Named("connectionBean")
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
        HttpServletRequest request = (HttpServletRequest) (FacesContext.getCurrentInstance().getExternalContext().getRequest());
        request.logout();
        request.getSession().invalidate();
        return "/login.xhtml";
    }

    public void logIn() throws ServletException, AccountNotFoundException, IOException {
        HttpServletRequest request = (HttpServletRequest) (FacesContext.getCurrentInstance().getExternalContext().getRequest());
        HttpSession session = request.getSession();

        //Logout in case of user is already logged in,
        //that could happen when using multiple tabs
        request.logout();
        request.login(login, password);

        String accountLogin=null;
        Locale accountLocale=Locale.getDefault();
        try {
            Account account = userManager.getAccount(login);
            if(account!=null) {
                accountLogin = account.getLogin();
                accountLocale = new Locale(account.getLanguage());
            }
        }catch(AccountNotFoundException ignored){}
        //case insensitive fix
        if(!login.equals(accountLogin)){
            request.logout();
            throw new AccountNotFoundException(accountLocale,login);
        }
        session.setAttribute("remoteUser",login);
        boolean isAdmin=userManager.isCallerInRole("admin");

        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();

        if(isAdmin){
            ec.redirect(request.getContextPath() + "/faces/admin/workspace/workspacesMenu.xhtml");
        }else{
            if(originURL!=null && originURL.length()>1)
                ec.redirect(originURL);
            else
                ec.redirect(request.getContextPath() + "/document-management/");
        }
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
