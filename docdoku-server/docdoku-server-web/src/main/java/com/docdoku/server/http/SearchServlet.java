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
import com.docdoku.core.services.IUserManagerLocal;
import org.apache.commons.lang.StringUtils;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Set;
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
        } catch (IndexOutOfBoundsException ex) {
            //we'll try to switch to default workspace
        }

        HttpSession sessionHTTP = pRequest.getSession();
        Map<String, Workspace> administeredWorkspaces = (Map<String, Workspace>) sessionHTTP.getAttribute("administeredWorkspaces");

        if (workspaceID == null) {
            Set<Workspace> regularWorkspaces = (Set<Workspace>) sessionHTTP.getAttribute("regularWorkspaces");
            if (administeredWorkspaces != null && !administeredWorkspaces.isEmpty()) {
                workspaceID = administeredWorkspaces.values().iterator().next().getId();
            } else if (regularWorkspaces != null && !regularWorkspaces.isEmpty()) {
                workspaceID = regularWorkspaces.iterator().next().getId();
            }

            if(workspaceID == null){
                pResponse.sendRedirect(pRequest.getContextPath() + "/faces/admin/workspace/workspacesMenu.xhtml");
            }else{
                pResponse.sendRedirect(pRequest.getContextPath() + "/search/" + workspaceID);
            }
        } else {
            try{
                UserGroup[] userGroups = userManager.getUserGroupsForUser(new UserKey(workspaceID, login));
                String[] groups = new String[userGroups.length];
                for(int i = 0 ; i< userGroups.length;i++){
                    groups[i] = "\""+userGroups[i].toString()+"\"";
                }
                pRequest.setAttribute("groups", StringUtils.join(groups, ","));
            } catch (UserNotFoundException e) {
            }

            pRequest.setAttribute("workspaceAdmin", administeredWorkspaces.containsKey(workspaceID));
            pRequest.setAttribute("workspaceID", workspaceID);
            pRequest.setAttribute("login", login);
            pRequest.getRequestDispatcher("/faces/search/index.xhtml").forward(pRequest, pResponse);
        }
    }
}
