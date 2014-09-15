/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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

package com.docdoku.server.http;

import com.docdoku.core.common.UserGroup;
import com.docdoku.core.common.UserKey;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.exceptions.UserNotFoundException;
import com.docdoku.core.exceptions.WorkspaceNotFoundException;
import com.docdoku.core.services.IUserManagerLocal;
import org.apache.commons.lang.StringUtils;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class SearchServlet extends HttpServlet {

    @EJB
    private IUserManagerLocal userManager;

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
        } catch (IndexOutOfBoundsException ignored) {
            //we'll try to switch to default workspace
        }

        if (workspaceID == null) {
            Workspace[] workspaces = userManager.getWorkspacesWhereCallerIsActive();
            if (workspaces != null && workspaces.length > 0) {
                workspaceID = workspaces[0].getId();
            }

            if(workspaceID == null){
                pResponse.sendRedirect(pRequest.getContextPath() + "/faces/admin/workspace/workspacesMenu.xhtml");
            }else{
                pResponse.sendRedirect(pRequest.getContextPath() + "/search/" + workspaceID);
            }
        } else {
            boolean workspaceAdmin;
            try{
                UserGroup[] userGroups = userManager.getUserGroupsForUser(new UserKey(workspaceID, login));
                workspaceAdmin = login.equals(userManager.getWorkspace(workspaceID).getAdmin().getLogin());
                String[] groups = new String[userGroups.length];
                for(int i = 0 ; i< userGroups.length;i++){
                    groups[i] = "\""+userGroups[i].toString()+"\"";
                }
                pRequest.setAttribute("groups", StringUtils.join(groups, ","));
                pRequest.setAttribute("workspaceAdmin", workspaceAdmin);
                pRequest.setAttribute("workspaceID", workspaceID);
                pRequest.setAttribute("login", login);
                pRequest.getRequestDispatcher("/faces/search/index.xhtml").forward(pRequest, pResponse);
            }catch(UserNotFoundException | WorkspaceNotFoundException ex){
                Logger.getLogger(SearchServlet.class.getName()).log(Level.WARNING, String.valueOf(ex));
                pResponse.sendRedirect(pRequest.getContextPath() + "/faces/admin/workspace/workspacesMenu.xhtml");
            }catch (Exception ex) {
                throw new ServletException("error while fetching user data.", ex);
            }
        }
    }
}
