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

import com.docdoku.core.entities.Workspace;
import java.io.IOException;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;

import com.docdoku.core.*;
import com.docdoku.core.entities.Account;
import java.util.Map;
import java.util.Set;

public class UpdateWorkspaceServlet extends HttpServlet {

    @EJB
    private ICommandLocal commandService;

    protected void doPost(HttpServletRequest pRequest,
            HttpServletResponse pResponse)
            throws ServletException, IOException {
        try {
            if (pRequest.getCharacterEncoding() == null) {
                pRequest.setCharacterEncoding("UTF-8");
            }

            HttpSession sessionHTTP = pRequest.getSession();
            Workspace workspace = (Workspace) sessionHTTP.getAttribute("selectedWorkspace");
            Workspace clone = workspace.clone();
            
            clone.setFolderLocked(Boolean.parseBoolean(pRequest.getParameter("folderLocked")));
            clone.setDescription(pRequest.getParameter("description"));

            String newAdminLogin = pRequest.getParameter("admin");
            Account newAdmin = null;
            if (!clone.getAdmin().getLogin().equals(newAdminLogin)) {
                newAdmin = commandService.getAccount(newAdminLogin);
                clone.setAdmin(newAdmin);
            }
            commandService.updateWorkspace(clone);
            
            workspace.setDescription(pRequest.getParameter("description"));
            workspace.setFolderLocked(Boolean.parseBoolean(pRequest.getParameter("folderLocked")));
            if (newAdmin!=null) {
                workspace.setAdmin(newAdmin);
                
                Map<String, Workspace> administeredWorkspaces=(Map<String, Workspace>) sessionHTTP.getAttribute("administeredWorkspaces");
                administeredWorkspaces.remove(workspace.getId());
                Set<Workspace> regularWorkspaces = (Set<Workspace>) sessionHTTP.getAttribute("regularWorkspaces");
                regularWorkspaces.add(workspace);
                sessionHTTP.removeAttribute("selectedWorkspace");
            }
            pRequest.getRequestDispatcher("/WEB-INF/admin/editWorkspace.jsp").forward(pRequest, pResponse);
        } catch (Exception pEx) {
            throw new ServletException("Error while editing a workspace.", pEx);
        }
    }
}
