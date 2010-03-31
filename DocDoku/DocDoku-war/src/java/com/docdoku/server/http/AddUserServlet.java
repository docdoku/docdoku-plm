/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010 DocDoku SARL
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

import com.docdoku.core.entities.Account;
import com.docdoku.core.entities.User;
import com.docdoku.core.entities.Workspace;
import java.io.IOException;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;

import com.docdoku.core.*;
import com.docdoku.core.entities.keys.BasicElementKey;
import java.net.URLEncoder;

public class AddUserServlet extends HttpServlet {
    
    @EJB
    private ICommandLocal commandService;
    
    
    @Override
    protected void doPost(HttpServletRequest pRequest,
            HttpServletResponse pResponse)
            throws ServletException, IOException {
        try {
            if(pRequest.getCharacterEncoding()==null)
                pRequest.setCharacterEncoding("UTF-8");
            
            HttpSession sessionHTTP = pRequest.getSession();
            Workspace workspace = (Workspace) sessionHTTP.getAttribute("selectedWorkspace");
            User[] users = commandService.getUsers(workspace.getId());

            
            switch(workspace.getVaultType()){
                case DEMO:
                    if(users.length>1)
                        throw new NotAllowedException(pRequest.getLocale(),"NotAllowedException1");
                    break;
                    
                case SMALL:
                    if(users.length>9)
                        throw new NotAllowedException(pRequest.getLocale(),"NotAllowedException2");
                    break;
                    
                case LARGE:
                    if(users.length>19)
                        throw new NotAllowedException(pRequest.getLocale(),"NotAllowedException3");
                    break;
            }
            
            String login = pRequest.getParameter("login");
            String group = pRequest.getParameter("group");
            if(group!=null && !group.equals("")){
                commandService.addUserInGroup(new BasicElementKey(workspace.getId(),group),login);
                pResponse.sendRedirect(pRequest.getContextPath()+"/admin/workspace/manageUsersGroup?group="+URLEncoder.encode(group,"UTF-8"));
            }else{
                commandService.addUserInWorkspace(workspace.getId(),login);
                pResponse.sendRedirect(pRequest.getContextPath()+"/admin/workspace/manageUsers");
            }
        } catch (Exception pEx) {
            throw new ServletException("Error while adding a new user.", pEx);
        }
    }
}
