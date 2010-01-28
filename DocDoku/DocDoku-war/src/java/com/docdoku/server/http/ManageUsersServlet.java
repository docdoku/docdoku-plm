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

import com.docdoku.core.entities.User;
import com.docdoku.core.entities.Workspace;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;

import com.docdoku.core.*;
import com.docdoku.core.entities.UserGroup;
import com.docdoku.core.entities.WorkspaceUserGroupMembership;
import com.docdoku.core.entities.WorkspaceUserMembership;
import java.util.LinkedList;
import java.util.List;


public class ManageUsersServlet extends HttpServlet {
    
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
            String[] logins = pRequest.getParameterValues("users");
            String[] groupIds = pRequest.getParameterValues("groups");

            if(logins!=null){
                String action = pRequest.getParameter("action");
                if(action.equals("remove")){
                    commandService.removeUsers(workspace.getId(),logins);
                }else if(action.equals("disable")){
                    commandService.passivateUsers(workspace.getId(),logins);
                }else if(action.equals("enable")){
                    commandService.activateUsers(workspace.getId(),logins);
                }else if(action.equals("read")){
                    commandService.grantUserAccess(workspace.getId(),logins,true);
                }else if(action.equals("full")){
                    commandService.grantUserAccess(workspace.getId(),logins,false);
                }
            }
            if(groupIds!=null){
                String action = pRequest.getParameter("action");
                if(action.equals("remove")){
                    commandService.removeUserGroups(workspace.getId(),groupIds);
                }else if(action.equals("disable")){
                    commandService.passivateUserGroups(workspace.getId(),groupIds);
                }else if(action.equals("enable")){
                    commandService.activateUserGroups(workspace.getId(),groupIds);
                }else if(action.equals("read")){
                    commandService.grantGroupAccess(workspace.getId(),groupIds,true);
                }else if(action.equals("full")){
                    commandService.grantGroupAccess(workspace.getId(),groupIds,false);
                }
            }
            displayView(pRequest, pResponse, workspace.getId());
            
        } catch (Exception pEx) {
            throw new ServletException("Error while updating a user.", pEx);
        }
    }


    @Override
    protected void doGet(HttpServletRequest pRequest,
            HttpServletResponse pResponse)
            throws ServletException, IOException {
        try {
 
            HttpSession sessionHTTP = pRequest.getSession();
            Workspace workspace = (Workspace) sessionHTTP.getAttribute("selectedWorkspace");            
            displayView(pRequest, pResponse, workspace.getId());
            
        } catch (Exception pEx) {
            throw new ServletException("Error while retrieving users.", pEx);
        }
    }
    
    private void displayView(HttpServletRequest pRequest, HttpServletResponse pResponse, String workspaceId) throws UserNotFoundException, UserNotActiveException, IOException, ServletException, WorkspaceNotFoundException{
        User[] users = commandService.getUsers(workspaceId);
        UserGroup[] groups = commandService.getUserGroups(workspaceId);

        Map<String,List<UserGroup>> usersGroups = new HashMap<String,List<UserGroup>>();
        for(UserGroup group:groups){
            for(User user:group.getUsers()){
                List<UserGroup> lstGroups = usersGroups.get(user.getLogin());
                if(lstGroups==null){
                    lstGroups=new LinkedList<UserGroup>();
                    usersGroups.put(user.getLogin(),lstGroups);
                }
                lstGroups.add(group);
            }
        }


        WorkspaceUserMembership[] userMemberships = commandService.getWorkspaceUserMemberships(workspaceId);
        Map<String,WorkspaceUserMembership> userMembersMap=new HashMap<String,WorkspaceUserMembership>();
        for(WorkspaceUserMembership membership:userMemberships){
            userMembersMap.put(membership.getMemberLogin(), membership);
        }

        WorkspaceUserGroupMembership[] groupMemberships = commandService.getWorkspaceUserGroupMemberships(workspaceId);
        Map<String,WorkspaceUserGroupMembership> groupMembersMap=new HashMap<String,WorkspaceUserGroupMembership>();
        for(WorkspaceUserGroupMembership membership:groupMemberships){
            groupMembersMap.put(membership.getMemberId(), membership);
        }
        pRequest.setAttribute("users",users);
        pRequest.setAttribute("groups",groups);
        pRequest.setAttribute("usersGroups",usersGroups);
        
        pRequest.setAttribute("userMembers",userMembersMap);
        pRequest.setAttribute("groupMembers",groupMembersMap);
        pRequest.getRequestDispatcher("/WEB-INF/admin/manageUsers.jsp").forward(pRequest, pResponse);
    }
}
