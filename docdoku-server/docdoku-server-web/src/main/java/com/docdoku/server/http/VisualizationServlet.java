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

import com.docdoku.core.product.ConfigurationItem;
import com.docdoku.core.services.IProductManagerLocal;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;
import java.util.regex.Pattern;

public class VisualizationServlet extends HttpServlet {

    @EJB
    private IProductManagerLocal productService;
    
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
        String productID = null;
        float cameraX, cameraY, cameraZ;
        String pathToLoad;

        cameraX = Float.parseFloat(pRequest.getParameter("cameraX"));
        cameraY = Float.parseFloat(pRequest.getParameter("cameraY"));
        cameraZ = Float.parseFloat(pRequest.getParameter("cameraZ"));

        pathToLoad = pRequest.getParameter("pathToLoad");
        
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
            List<ConfigurationItem> products = null;
            try {
                products = productService.getConfigurationItems(workspaceID);
            } catch (Exception ex) {
                //Dropdown menu will not be able to be displayed
                //TODO log it
            }
            pRequest.setAttribute("products", products);
            pRequest.setAttribute("workspaceID", workspaceID);
            pRequest.setAttribute("productID", productID);
            pRequest.setAttribute("login", login);
            pRequest.setAttribute("cameraX", cameraX);
            pRequest.setAttribute("cameraY", cameraY);
            pRequest.setAttribute("cameraZ", cameraZ);
            pRequest.setAttribute("pathToLoad", pathToLoad);
            pRequest.getRequestDispatcher("/faces/product-structure/frame.xhtml").forward(pRequest, pResponse);
        }
    }
}
