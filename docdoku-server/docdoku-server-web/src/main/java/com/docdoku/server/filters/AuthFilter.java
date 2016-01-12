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

package com.docdoku.server.filters;

import com.docdoku.core.common.Account;
import com.docdoku.core.exceptions.AccountNotFoundException;
import com.docdoku.core.services.IAccountManagerLocal;
import com.docdoku.core.services.IOrganizationManagerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.server.jsf.actions.AccountBean;

import javax.ejb.EJB;
import javax.el.PropertyNotFoundException;
import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class AuthFilter implements Filter {
    private static final Logger LOGGER = Logger.getLogger(AuthFilter.class.getName());
    private static final String ENCODING = "UTF-8";

    private String[] excludedPaths;
    private String apiPath;


    @Inject
    private AccountBean accountBean;

    @EJB
    private IUserManagerLocal userManager;

    @EJB
    private IAccountManagerLocal accountManager;

    @EJB
    private IOrganizationManagerLocal organizationManager;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String remoteUser=httpRequest.getRemoteUser();

        if(remoteUser == null){
            remoteUser = authenticateUserWithHeaders(request);
        }

        if(isExcludedURL(httpRequest) && remoteUser==null) {
            chain.doFilter(request, response);
        }
        else if(isApiRequest(httpRequest) && remoteUser==null){
            sendUnauthorized(response);
        }
        else if (remoteUser==null) {
            redirectLogin(httpRequest,response);
        } else {
            try {
                FilterUtils.hookAccountBeanData(remoteUser, userManager, accountManager, organizationManager, accountBean);
                chain.doFilter(request, response);
            } catch (AccountNotFoundException e) {
                LOGGER.log(Level.FINEST,null,e);
                redirectLogin(httpRequest, response);
            }

        }

    }

    private void sendUnauthorized(ServletResponse pResponse) throws ServletException, IOException {
        HttpServletResponse response = (HttpServletResponse) pResponse;
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    private boolean isApiRequest(HttpServletRequest httpRequest) {
        String path = httpRequest.getRequestURI();
        return Pattern.matches(apiPath, path);
    }

    private String authenticateUserWithHeaders(ServletRequest pRequest) {

        HttpServletRequest httpRequest = (HttpServletRequest) pRequest;
        String authorization = httpRequest.getHeader("Authorization");

        if(authorization != null && !authorization.isEmpty()){
            String[] splitAuthorization = authorization.split(" ");
            if(splitAuthorization.length == 2){
                byte[] decoded = DatatypeConverter.parseBase64Binary(splitAuthorization[1]);
                try {
                    String credentials = new String(decoded, "US-ASCII");
                    String[] splitCredentials = credentials.split(":");
                    String userLogin = splitCredentials[0];
                    String userPassword = splitCredentials[1];
                    httpRequest.login(userLogin, userPassword);
                    Account account = accountManager.getAccount(userLogin);
                    if(account!=null) {
                        return account.getLogin();
                    }
                } catch (UnsupportedEncodingException | ServletException | AccountNotFoundException e) {
                    LOGGER.log(Level.FINEST, null, e);
                }
            }
        }
        return null;
    }

    private boolean isExcludedURL(HttpServletRequest httpRequest){
        String path = httpRequest.getRequestURI();
        String method=httpRequest.getMethod();
        if(path!=null && excludedPaths !=null && "GET".equals(method)){
            for(String excludedPath: excludedPaths){
                if(Pattern.matches(excludedPath, path)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void redirectLogin(HttpServletRequest httpRequest, ServletResponse response) throws IOException, ServletException {
        String qs=httpRequest.getQueryString();
        String originURL = httpRequest.getRequestURI() + (qs==null?"": "?" + qs);
        HttpSession session = httpRequest.getSession();
        session.setAttribute("hasFail", false);
        session.setAttribute("hasLogout", false);
        try {
            httpRequest.getRequestDispatcher(httpRequest.getContextPath() + "/faces/login.xhtml?originURL=" + URLEncoder.encode(originURL, ENCODING))
                       .forward(httpRequest, response);
        }catch (PropertyNotFoundException e){
            LOGGER.log(Level.SEVERE,"Cannot redirect to Login Page");
            LOGGER.log(Level.FINER,null,e);
        }
    }

    @Override
    public void destroy() {
        // Nothing to do
    }

    @Override
    public void init(FilterConfig filterConfig) {
        String parameter=filterConfig.getInitParameter("excludedPaths");
        if(parameter !=null) {
            excludedPaths = parameter.split(",");
            for(int i=0;i<excludedPaths.length;i++) {
                boolean endLess=false;
                if(excludedPaths[i].endsWith("/**")) {
                    excludedPaths[i]=excludedPaths[i].substring(0,excludedPaths[i].length()-2);
                    endLess=true;
                }
                excludedPaths[i] = excludedPaths[i].replace("*", "[^/]+?");
                if(endLess) {
                    excludedPaths[i] += ".*";
                }
            }
        } else {
            excludedPaths = null;
        }

        String apiPathParam=filterConfig.getInitParameter("apiPath");
        if(apiPathParam !=null) {
            apiPath = apiPathParam.replace("*", ".*");
        }
    }
}
