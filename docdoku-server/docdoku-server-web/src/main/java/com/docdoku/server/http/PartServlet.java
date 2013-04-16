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

import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.product.*;
import com.docdoku.core.services.IProductManagerLocal;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class PartServlet extends HttpServlet {
    
    @EJB
    private IProductManagerLocal productService;
    
    @Override
    protected void doGet(HttpServletRequest pRequest,
                         HttpServletResponse pResponse)
            throws ServletException, IOException {

        try {
            String login = pRequest.getRemoteUser();
            String[] pathInfos = Pattern.compile("/").split(pRequest.getRequestURI());
            int offset;
            if(pRequest.getContextPath().equals(""))
                offset=2;
            else
                offset=3;
            
            String workspaceId = URLDecoder.decode(pathInfos[offset],"UTF-8");
            String partNumber = URLDecoder.decode(pathInfos[offset+1],"UTF-8");
            String partVersion = pathInfos[offset+2];

            PartRevision partRevision = productService.getPartRevision(new PartRevisionKey(workspaceId,partNumber,partVersion));

            pRequest.setAttribute("partRevision", partRevision);

            PartIteration partIteration =  partRevision.getLastIteration();
            pRequest.setAttribute("attr",  new ArrayList<InstanceAttribute>(partIteration.getInstanceAttributes().values()));

            pRequest.getRequestDispatcher("/WEB-INF/part.jsp").forward(pRequest, pResponse);

        } catch (Exception pEx) {
            throw new ServletException("Error while fetching your part.", pEx);
        }
    }
}
