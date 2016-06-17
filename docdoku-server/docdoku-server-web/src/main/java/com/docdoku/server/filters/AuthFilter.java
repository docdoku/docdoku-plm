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
import com.docdoku.server.http.UserRoleRequestWrapper;
import com.docdoku.server.jwt.RsaJsonWebKeyFactory;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class AuthFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(AuthFilter.class.getName());

    private String[] excludedPaths;
    private String apiPathMatcher;
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

        String apiPathParam = filterConfig.getInitParameter("apiPath");

        if(apiPathParam !=null) {
            apiPath = apiPathParam.replace("/*", "");
            apiPathMatcher = apiPathParam.replace("*", ".*");
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String remoteUser = httpRequest.getRemoteUser();

        // User has already an active session
        if(remoteUser != null && !remoteUser.isEmpty()){
            chain.doFilter(request, response);
            return;
        }

        // Tries to parse authentication from headers
        // Support JWT and basic auth
        String authHeaderVal = httpRequest.getHeader("Authorization");

        if(authHeaderVal != null && !authHeaderVal.isEmpty()){
            if(authHeaderVal.startsWith("Bearer")){
                authenticateJWT(request, response, chain);
            }
            else if(authHeaderVal.startsWith("Basic")){
                authenticateBasic(request,response,chain);
            }else {
                sendBadRequest(response);
            }
        } else {
            if(isRootCall(httpRequest) || isExcludedURL(httpRequest)) {
                chain.doFilter(request, response);
            }
            else if(isApiRequest(httpRequest)){
                sendUnauthorized(response);
            }
            // should we handle last case ? (not recognized URL)
        }
    }

    private boolean isRootCall(HttpServletRequest httpRequest){
        String path = httpRequest.getRequestURI();
        return path.equals(apiPath) || path.equals(apiPath+"/");
    }

    private void authenticateJWT(ServletRequest request, ServletResponse response,
                                 FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String authorization = httpRequest.getHeader("Authorization");
        String[] splitAuthorization = authorization.split(" ");
        if(splitAuthorization.length==2){
            String jwt = splitAuthorization[1];
            Account account = validateToken(jwt);
            if(account != null){
                // Not working
                // TODO : find a way to propagate role in security context
                chain.doFilter(new UserRoleRequestWrapper(account.getLogin(),accountManager.getRole(account.getLogin()),httpRequest),response);
            }else {
                sendUnauthorized(response);
            }
        }else{
            sendBadRequest(response);
        }
    }

    private Account validateToken(String jwt) {
        RsaJsonWebKey rsaJsonWebKey = RsaJsonWebKeyFactory.createKey();
        JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                .setRequireSubject()
                .setVerificationKey(rsaJsonWebKey.getKey())
                .build();

        Account account = null;

        try {
            JwtClaims jwtClaims = jwtConsumer.processToClaims(jwt);
            String userLogin= (String) jwtClaims.getClaimValue("sub");
            account = accountManager.getAccount(userLogin);
        } catch (InvalidJwtException e) {
            LOGGER.log(Level.SEVERE,null,e);
        } catch (AccountNotFoundException e) {
            LOGGER.log(Level.SEVERE, null, e);
        }

        return account;

    }

    private void sendUnauthorized(ServletResponse pResponse) throws ServletException, IOException {
        HttpServletResponse response = (HttpServletResponse) pResponse;
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }

    private void sendBadRequest(ServletResponse pResponse) throws ServletException, IOException {
        HttpServletResponse response = (HttpServletResponse) pResponse;
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad request");
    }

    private boolean isApiRequest(HttpServletRequest httpRequest) {
        String path = httpRequest.getRequestURI();
        return Pattern.matches(apiPathMatcher, path);
    }

    private void authenticateBasic(ServletRequest pRequest, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest httpRequest = (HttpServletRequest) pRequest;
        String authorization = httpRequest.getHeader("Authorization");
        String[] splitAuthorization = authorization.split(" ");
        if(splitAuthorization.length == 2){
            byte[] decoded = DatatypeConverter.parseBase64Binary(splitAuthorization[1]);
            String credentials = new String(decoded, "US-ASCII");
            String[] splitCredentials = credentials.split(":");
            String userLogin = splitCredentials[0];
            String userPassword = splitCredentials[1];
            // Recreate session will transmit JSESSIONID in headers
            httpRequest.getSession(true);
            httpRequest.login(userLogin, userPassword);
            chain.doFilter(pRequest,response);
        }
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
