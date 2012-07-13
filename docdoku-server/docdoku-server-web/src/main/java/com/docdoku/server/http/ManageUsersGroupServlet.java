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

import com.docdoku.core.services.UserGroupNotFoundException;
import com.docdoku.core.services.WorkspaceNotFoundException;
import com.docdoku.core.services.UserNotFoundException;
import com.docdoku.core.services.UserNotActiveException;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.core.common.Workspace;
import java.io.IOException;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;

import com.docdoku.core.common.UserGroup;
import com.docdoku.core.common.UserGroupKey;


public class ManageUsersGroupServlet extends HttpServlet {
    
    @EJB
    private IUserManagerLocal userManager;
    
    
    @Override
    protected void doPost(HttpServletRequest pRequest,
            HttpServletResponse pResponse)
            throws ServletException, IOException {
        try {
            if(pRequest.getCharacterEncoding()==null)
                pRequest.setCharacterEncoding("UTF-8");
            
            HttpSession sessionHTTP = pRequest.getSession();
            Workspace workspace = (Workspace) sessionHTTP.getAttribute("selectedWorkspace"); 
            String[] logins = pRequest.getParameterValues("users");
            String groupId = pRequest.getParameter("group");

            if(logins!=null){
                String action = pRequest.getParameter("action");
                if(action.equals("remove")){
                    userManager.removeUserFromGroup(new UserGroupKey(workspace.getId(),groupId),logins);
                }
            }
            
            displayView(pRequest, pResponse, workspace.getId(), groupId);
            
        } catch (Exception pEx) {
            throw new ServletException("Error while updating a user.", pEx);
        }
    }


    @Override
    protected void doGet(HttpServletRequest pRequest,
            HttpServletResponse pResponse)
            throws ServletException, IOException {
        try {
            if(pRequest.getCharacterEncoding()==null)
                pRequest.setCharacterEncoding("UTF-8");
            
            HttpSession sessionHTTP = pRequest.getSession();
            Workspace workspace = (Workspace) sessionHTTP.getAttribute("selectedWorkspace");
            String group = pRequest.getParameter("group");

            displayView(pRequest, pResponse, workspace.getId(), group);
            
        } catch (Exception pEx) {
            throw new ServletException("Error while retrieving users.", pEx);
        }
    }
    
    private void displayView(HttpServletRequest pRequest, HttpServletResponse pResponse, String workspaceId, String groupId) throws UserNotFoundException, UserNotActiveException, IOException, ServletException, UserGroupNotFoundException, WorkspaceNotFoundException{
        UserGroup group = userManager.getUserGroup(new UserGroupKey(workspaceId,groupId));

        pRequest.setAttribute("group",group);
        pRequest.getRequestDispatcher("/WEB-INF/admin/manageUsersGroup.jsp").forward(pRequest, pResponse);
    }
}
