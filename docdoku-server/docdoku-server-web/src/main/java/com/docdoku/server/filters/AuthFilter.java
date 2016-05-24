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
import com.docdoku.core.services.IContextManagerLocal;
import com.docdoku.core.services.IOrganizationManagerLocal;
import com.docdoku.core.services.IUserManagerLocal;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class AuthFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(AuthFilter.class.getName());

    private String[] excludedPaths;
    private String apiPath;

    @Inject
    private IUserManagerLocal userManager;

    @Inject
    private IContextManagerLocal contextManager;

    @Inject
    private IAccountManagerLocal accountManager;

    @EJB
    private IOrganizationManagerLocal organizationManager;

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

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        String authHeaderVal = httpRequest.getHeader("Authorization");

        if(authHeaderVal.startsWith("Bearer")){
            authenticateJWT(request);
            chain.doFilter(request, response);
        }
        else if(authHeaderVal.startsWith("Basic")){
            authenticateBasic(request);
            chain.doFilter(request, response);
        }
        else if(isExcludedURL(httpRequest)) {
            chain.doFilter(request, response);
        }
        else if(isApiRequest(httpRequest)){
            sendUnauthorized(response);
        }

    }

    private void authenticateJWT(ServletRequest pRequest) {
        HttpServletRequest httpRequest = (HttpServletRequest) pRequest;
        String authorization = httpRequest.getHeader("Authorization");
        String[] splitAuthorization = authorization.split(" ");
        if(splitAuthorization.length==2){
            String jwt = splitAuthorization[1];
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

    private String authenticateBasic(ServletRequest pRequest) {

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
        if(path!=null && excludedPaths !=null){
            for(String excludedPath: excludedPaths){
                if(Pattern.matches(excludedPath, path)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void destroy() {
        // Nothing to do
    }

}
