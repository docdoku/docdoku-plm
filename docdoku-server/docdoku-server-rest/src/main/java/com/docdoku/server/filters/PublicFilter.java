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

import com.docdoku.core.services.IAccountManagerLocal;
import com.docdoku.core.services.IContextManagerLocal;
import com.docdoku.core.services.IOrganizationManagerLocal;
import com.docdoku.core.services.IUserManagerLocal;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.regex.Pattern;

public class PublicFilter implements Filter {

    private String[] publicPaths;

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
        String parameter=filterConfig.getInitParameter("publicPaths");
        if(parameter !=null) {
            publicPaths = parameter.split(",");
            for(int i=0;i< publicPaths.length;i++) {
                boolean endLess=false;
                if(publicPaths[i].endsWith("/**")) {
                    publicPaths[i]= publicPaths[i].substring(0, publicPaths[i].length()-2);
                    endLess=true;
                }
                publicPaths[i] = publicPaths[i].replace("*", "[^/]+?");
                if(endLess) {
                    publicPaths[i] += ".*";
                }
            }
        } else {
            publicPaths = null;
        }

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        if(!FilterUtils.isAuthenticated(request)){
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            if(isPublicPath(httpRequest)) {
                FilterUtils.authenticate(request);
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isPublicPath(HttpServletRequest httpRequest){
        String path = httpRequest.getRequestURI();
        if(path!=null && publicPaths !=null){
            for(String excludedPath: publicPaths){
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
