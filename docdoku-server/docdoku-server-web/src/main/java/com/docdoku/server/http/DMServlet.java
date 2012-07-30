/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
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

import com.docdoku.core.common.Workspace;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;

public class DMServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest pRequest,
            HttpServletResponse pResponse)
            throws ServletException, IOException {

        String login = pRequest.getRemoteUser();
        String[] pathInfos = Pattern.compile("/").split(pRequest.getRequestURI());
        int offset;
        if (pRequest.getContextPath().equals("")) {
            offset = 2;
        } else {
            offset = 3;
        }

        String workspaceID = null;
        try {
            workspaceID = URLDecoder.decode(pathInfos[offset], "UTF-8");
        } catch (IndexOutOfBoundsException ex) {
            //we'll try to switch to default workspace
        }

        if (workspaceID == null) {
            HttpSession sessionHTTP = pRequest.getSession();
            Map<String, Workspace> administeredWorkspaces = (Map<String, Workspace>) sessionHTTP.getAttribute("administeredWorkspaces");
            Set<Workspace> regularWorkspaces = (Set<Workspace>) sessionHTTP.getAttribute("regularWorkspaces");

            if (administeredWorkspaces != null && !administeredWorkspaces.isEmpty()) {
                workspaceID = administeredWorkspaces.values().iterator().next().getId();
            } else if (regularWorkspaces != null && !regularWorkspaces.isEmpty()) {
                workspaceID = regularWorkspaces.iterator().next().getId();
            }

            if(workspaceID == null){
                pResponse.sendRedirect(pRequest.getContextPath() + "/admin/workspacesMenu.jsp");
            }else{
                pResponse.sendRedirect(pRequest.getContextPath() + "/document-management/" + workspaceID);
            }
        } else {
            pRequest.setAttribute("workspaceID", workspaceID);
            pRequest.setAttribute("login", login);
            pRequest.getRequestDispatcher("/WEB-INF/document-management/index.jsp").forward(pRequest, pResponse);
        }
    }
}
