/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2014 DocDoku SARL
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
import com.docdoku.core.common.Organization;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.exceptions.AccountNotFoundException;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.server.jsf.actions.AccountBean;

import javax.ejb.EJB;
import javax.el.PropertyNotFoundException;
import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuthFilter implements Filter {
    private static final Logger LOGGER = Logger.getLogger(AuthFilter.class.getName());
    private static final String ENCODING = "UTF-8";

    private String[] excludedPaths;


    @Inject
    private AccountBean accountBean;

    @EJB
    private IUserManagerLocal userManager;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String remoteUser=httpRequest.getRemoteUser();

        if(isExcludedURL(httpRequest) && remoteUser==null) {
            chain.doFilter(request, response);
        }else if (remoteUser==null) {
            redirectLogin(httpRequest,response);
        } else {
            try {
                Account user = userManager.getAccount(remoteUser);
                boolean isAdmin = userManager.isCallerInRole(UserGroupMapping.ADMIN_ROLE_ID);
                accountBean.setLogin(user.getLogin());
                accountBean.setEmail(user.getEmail());
                accountBean.setLanguage(user.getLanguage());
                accountBean.setName(user.getName());
                Organization organization = user.getOrganization();
                if(organization!=null){
                    accountBean.setOrganizationName(user.getOrganization().getName());
                    accountBean.setOrganizationAdmin(organization.getOwner().getLogin());
                }

                accountBean.setSuperAdmin(isAdmin);

                Map<String, Workspace> administeredWorkspaces = new HashMap<>();
                for (Workspace wks : userManager.getAdministratedWorkspaces()) {
                    administeredWorkspaces.put(wks.getId(), wks);
                }
                accountBean.setAdministeredWorkspaces(administeredWorkspaces);

                if(!isAdmin){
                    Set<Workspace> regularWorkspaces = new HashSet<>();
                    Workspace[] workspaces = userManager.getWorkspacesWhereCallerIsActive();
                    regularWorkspaces.addAll(Arrays.asList(workspaces));
                    regularWorkspaces.removeAll(administeredWorkspaces.values());
                    accountBean.setRegularWorkspaces(regularWorkspaces);
                }
                chain.doFilter(request, response);
            } catch (AccountNotFoundException e) {
                LOGGER.log(Level.FINEST,null,e);
                redirectLogin(httpRequest, response);
            }

        }

    }

    private boolean isExcludedURL(HttpServletRequest httpRequest){
        String path = httpRequest.getPathInfo();
        String method=httpRequest.getMethod();
        if(path!=null && excludedPaths !=null && "GET".equals(method)){
            for(String excludedPath: excludedPaths){
                if(Pattern.matches(excludedPath, path))
                    return true;
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
                if(endLess)
                    excludedPaths[i] +=".*";
            }
        }
        else
            excludedPaths =null;
    }
}
