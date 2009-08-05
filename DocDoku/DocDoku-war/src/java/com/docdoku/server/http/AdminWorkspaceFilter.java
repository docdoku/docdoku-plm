/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.server.http;

import java.io.*;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletResponse;

import com.docdoku.core.entities.Workspace;
import com.docdoku.core.entities.Account;


public class AdminWorkspaceFilter implements Filter {

 
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        if(httpRequest.getCharacterEncoding()==null)
                httpRequest.setCharacterEncoding("UTF-8");
        
        HttpSession sessionHTTP = httpRequest.getSession();
        
        Account account = (Account) sessionHTTP.getAttribute("account");
        Map<String, Workspace> administeredWorkspaces = (Map<String, Workspace>) sessionHTTP.getAttribute("administeredWorkspaces");
            
        String workspaceID = request.getParameter("selectedWorkspace");        
        Workspace workspace=workspaceID!=null?administeredWorkspaces.get(workspaceID):null;
        
        if (workspace != null) {
            sessionHTTP.setAttribute("selectedWorkspace", workspace);
        }
        if (sessionHTTP.getAttribute("selectedWorkspace") == null) {
            switch (administeredWorkspaces.size()) {
                case 1:
                    sessionHTTP.setAttribute("selectedWorkspace", administeredWorkspaces.values().iterator().next());
                    chain.doFilter(request, response);
                    break;
                default:
                    httpResponse.sendRedirect(httpRequest.getContextPath()+"/admin/workspacesMenu.jsp");
            }
        } else
            chain.doFilter(request, response);

    }


    public void destroy() {
    }

    public void init(FilterConfig filterConfig) {
    }

}
