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

import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.product.PartRevisionKey;

import javax.ejb.EJB;
import javax.security.auth.login.LoginException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class PermalinkFilter implements Filter {
    private static final Logger LOGGER = Logger.getLogger(PermalinkFilter.class.getName());
    private static final String ENCODING = "UTF-8";
    private static final String ERROR_ENCODING = "The HttpRequest path information can not be decoding.";
    private static final String ERROR_ENTITY_TYPE = "Attempt of access to a unknown type of entity.";
    private static final String ERROR_403_DOCUMENT = "Attempt of access to a forbidden document.";
    private static final String ERROR_404_DOCUMENT = "Attempt of access to a not found document.";
    private static final String ERROR_403_PART = "Attempt of access to a forbidden part.";
    private static final String ERROR_404_PART = "Attempt of access to a not found part.";

    @EJB
    private GuestProxy guestProxy;


    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String remoteUser = httpRequest.getRemoteUser();

        if (remoteUser != null) {
            chain.doFilter(request, response);
        } else {
            try{
                filterPublicEntity(httpRequest);
                chain.doFilter(request, response);
            } catch (UnsupportedEncodingException e) {
                LOGGER.log(Level.WARNING,ERROR_ENCODING,e);
                redirectToLogin(request,response);
            } catch (LoginException e) {
                LOGGER.log(Level.WARNING,null,e);
                redirectToLogin(request,response);
            }
        }
    }

    private void redirectToLogin(ServletRequest request, ServletResponse response) throws IOException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        try {
            String qs=httpRequest.getQueryString();
            String originURL = httpRequest.getRequestURI() + (qs==null?"": "?" + qs);
            httpRequest.getRequestDispatcher(httpRequest.getContextPath()+"/faces/login.xhtml?originURL=" + URLEncoder.encode(originURL, ENCODING)).forward(request, response);
        } catch (ServletException | IOException e) {
            LOGGER.log(Level.SEVERE,"Cannot redirect to Login Page");
            LOGGER.log(Level.FINER,null,e);
            ((HttpServletResponse) response).sendRedirect("/");
        }
    }

    private void filterPublicEntity(HttpServletRequest httpRequest) throws UnsupportedEncodingException, LoginException {
        String requestURI = httpRequest.getRequestURI();
        String[] pathInfos = Pattern.compile("/").split(requestURI);
        int offset = "".equals(httpRequest.getContextPath()) ? 2 : 3;

        String entityType = URLDecoder.decode(pathInfos[offset - 1], ENCODING);
        if(entityType == null){
            throw new LoginException(ERROR_ENTITY_TYPE);
        }

        switch (entityType) {
            case "documents":
                filterPublicDocument(httpRequest,pathInfos,offset);
                break;
            case "parts":
                filterPublicPart(httpRequest,pathInfos,offset);
                break;
            default:
                throw new LoginException(ERROR_ENTITY_TYPE);
        }
    }

    private void filterPublicDocument(HttpServletRequest httpRequest, String[] pathInfos, int offset) throws UnsupportedEncodingException, LoginException{
        try {
            String workspaceId = URLDecoder.decode(pathInfos[offset], ENCODING);
            String documentMasterId = URLDecoder.decode(pathInfos[offset+1],ENCODING);
            String documentRevisionVersion = pathInfos[offset+2];

            DocumentRevisionKey docRK  =  new DocumentRevisionKey(workspaceId,documentMasterId,documentRevisionVersion);
            DocumentRevision publicDocumentRevision = guestProxy.getPublicDocumentRevision(docRK);
            httpRequest.setAttribute("publicDocumentRevision",publicDocumentRevision);
        } catch (AccessRightException | NotAllowedException | UserNotFoundException | UserNotActiveException e) {
            LOGGER.log(Level.FINE,ERROR_403_DOCUMENT,e);
            throw new LoginException(ERROR_403_DOCUMENT);
        } catch (WorkspaceNotFoundException | DocumentRevisionNotFoundException e) {
            LOGGER.log(Level.FINE, ERROR_404_DOCUMENT, e);
            throw new LoginException(ERROR_404_DOCUMENT);
        }
    }

    private void filterPublicPart(HttpServletRequest httpRequest, String[] pathInfos, int offset) throws UnsupportedEncodingException, LoginException{
        try {
            String workspaceId = URLDecoder.decode(pathInfos[offset], ENCODING);
            String partNumber = URLDecoder.decode(pathInfos[offset+1],ENCODING);
            String partVersion = pathInfos[offset+2];

            PartRevisionKey partK  = new PartRevisionKey(workspaceId,partNumber,partVersion);
            PartRevision publicPartRevision = guestProxy.getPublicPartRevision(partK);
            httpRequest.setAttribute("publicPartRevision",publicPartRevision);
        } catch (AccessRightException | UserNotFoundException | UserNotActiveException e) {
            LOGGER.log(Level.FINE,ERROR_403_PART,e);
            throw new LoginException(ERROR_403_PART);
        } catch (WorkspaceNotFoundException | PartRevisionNotFoundException e) {
            LOGGER.log(Level.FINE, ERROR_404_PART, e);
            throw new LoginException(ERROR_404_PART);
        }
    }

    @Override
    public void destroy() {
        // Nothing to do
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Nothing to do
    }

}
