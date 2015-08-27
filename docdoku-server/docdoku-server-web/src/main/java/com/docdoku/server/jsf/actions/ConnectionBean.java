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
import com.docdoku.core.exceptions.AccountNotFoundException;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IAccountManagerLocal;
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
import java.net.URL;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named("connectionBean")
@RequestScoped
public class ConnectionBean {
    private static final Logger LOGGER = Logger.getLogger(ConnectionBean.class.getName());
    
    @EJB
    private IUserManagerLocal userManager;

    @EJB
    private IAccountManagerLocal accountManager;

    private String login;
    private String password;

    private String originURL;
    
    public ConnectionBean() {
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        HttpServletRequest request = (HttpServletRequest) (ec.getRequest());
        HttpSession session = request.getSession();
        if(session.getAttribute("hasFail") == null) {
            session.setAttribute("hasFail", false);
        }
        if(session.getAttribute("hasLogout") == null) {
            session.setAttribute("hasLogout", false);
        }
    }

    public void logOut() throws ServletException, IOException {
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        HttpServletRequest request = (HttpServletRequest) (ec.getRequest());
        request.logout();
        request.getSession().invalidate();
        HttpSession newSession = request.getSession(true);
        newSession.setAttribute("hasFail", false);
        newSession.setAttribute("hasLogout", true);
        ec.redirect(request.getContextPath() + "/faces/login.xhtml");
    }

    public void logIn() throws ServletException, AccountNotFoundException, IOException {
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        HttpServletRequest request = (HttpServletRequest) ec.getRequest();
        HttpSession session = request.getSession();

        //Logout in case of user is already logged in,
        //that could happen when using multiple tabs
        request.logout();
        if(tryLogin(request)) {
            checkAccount(request);
            session.setAttribute("remoteUser",login);
            boolean isAdmin=userManager.isCallerInRole(UserGroupMapping.ADMIN_ROLE_ID);

            if(isAdmin){
                URL url=new URL(request.getRequestURL().toString());
                URL redirectURL=new URL(url.getProtocol(),url.getHost(), url.getPort(),request.getContextPath() + "/faces/admin/workspace/workspacesMenu.xhtml");
                ec.redirect(redirectURL.toString());
            }else{
                redirectionPostLogin(request,ec);
            }
        }else{
            session.setAttribute("hasFail", true);
            session.setAttribute("hasLogout", false);
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

    private boolean tryLogin(HttpServletRequest request){
        try {
            request.login(login, password);
            return true;
        }catch(ServletException e){
            String message = "The user '"+login+"' failed to login";
            LOGGER.log(Level.WARNING, message);
            LOGGER.log(Level.FINEST,message,e);
            return false;
        }
    }

    private void checkAccount(HttpServletRequest request) throws AccountNotFoundException, ServletException {
        String accountLogin=null;
        Locale accountLocale=Locale.getDefault();
        try {
            Account account = accountManager.getAccount(login);
            if(account!=null) {
                accountLogin = account.getLogin();
                accountLocale = new Locale(account.getLanguage());
            }
        }catch(AccountNotFoundException e){
            LOGGER.log(Level.FINEST,null,e);
        }
        //case insensitive fix
        if(!login.equals(accountLogin)){
            request.logout();
            throw new AccountNotFoundException(accountLocale,login);
        }
    }

    private void redirectionPostLogin(HttpServletRequest request,ExternalContext ec) throws IOException {
        URL url = new URL(request.getRequestURL().toString());

        if (originURL != null && originURL.length() > 1) {
            URL redirectURL = new URL(url.getProtocol(), url.getHost(), url.getPort(), originURL);
            ec.redirect(redirectURL.toString());

        } else {
            URL redirectURL = new URL(url.getProtocol(), url.getHost(), url.getPort(), request.getContextPath() + "/faces/admin/workspace/workspacesMenu.xhtml");
            ec.redirect(redirectURL.toString());
        }
    }
}
