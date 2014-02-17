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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Map;
import java.util.regex.Pattern;

public class PSServlet extends HttpServlet {

    @EJB
    private IUserManagerLocal userManager;

    @Override
    protected void doGet(HttpServletRequest pRequest,
            HttpServletResponse pResponse)
            throws ServletException, IOException {

        HttpSession sessionHTTP = pRequest.getSession();
        String login = pRequest.getRemoteUser();

        Map<String, Workspace> administeredWorkspaces = (Map<String, Workspace>) sessionHTTP.getAttribute("administeredWorkspaces");

        String[] pathInfos = Pattern.compile("/").split(pRequest.getRequestURI());

        int offset;
        if (pRequest.getContextPath().equals("")) {
            offset = 2;
        } else {
            offset = 3;
        }

        String workspaceID = null;
        String productID = null;
        
        try {
            workspaceID = URLDecoder.decode(pathInfos[offset], "UTF-8");
        } catch (IndexOutOfBoundsException ex) {
            
        }

        try {
            productID = URLDecoder.decode(pathInfos[offset+1], "UTF-8");
        } catch (IndexOutOfBoundsException ex) {
            
        }
        
        if (workspaceID == null ) {
            pResponse.sendRedirect(pRequest.getContextPath() + "/faces/admin/workspace/workspacesMenu.xhtml");
            
        } else if(productID == null){
            pResponse.sendRedirect(pRequest.getContextPath() + "/faces/admin/workspace/workspacesMenu.xhtml");
            
        }
        else {
            try{
                UserGroup[] userGroups = userManager.getUserGroupsForUser(new UserKey(workspaceID, login));
                String[] groups = new String[userGroups.length];
                for(int i = 0 ; i< userGroups.length;i++){
                    groups[i] = "\""+userGroups[i].toString()+"\"";
                }
                pRequest.setAttribute("groups",StringUtils.join(groups,","));
            } catch (UserNotFoundException e) {
            }
            pRequest.setAttribute("workspaceAdmin", administeredWorkspaces.containsKey(workspaceID));
            pRequest.setAttribute("urlRoot", getUrlRoot(pRequest));
            pRequest.setAttribute("workspaceID", workspaceID);
            pRequest.setAttribute("productID", productID);
            pRequest.setAttribute("login", login);
            pRequest.getRequestDispatcher("/faces/product-structure/index.xhtml").forward(pRequest, pResponse);
        }
    }

    private static String getUrlRoot(HttpServletRequest pRequest) {
        URL url = null;
        try {
            url = new URL(new URL(pRequest.getRequestURL().toString()),"/");
        } catch (MalformedURLException e) {
            return null;
        }
        return url.toString();
    }
}
