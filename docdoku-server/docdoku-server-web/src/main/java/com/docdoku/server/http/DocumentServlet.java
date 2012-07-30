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

import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.core.document.DocumentMasterKey;
import com.docdoku.core.document.DocumentMaster;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.regex.Pattern;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;

import com.docdoku.core.*;

public class DocumentServlet extends HttpServlet {
    
    @EJB
    private IDocumentManagerLocal documentService;
    
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
            String docMId = URLDecoder.decode(pathInfos[offset+1],"UTF-8");
            String docMVersion = pathInfos[offset+2];

            DocumentMaster docM = documentService.getDocumentMaster(new DocumentMasterKey(workspaceId, docMId, docMVersion));
            pRequest.setAttribute("docm", docM);

            pRequest.getRequestDispatcher("/WEB-INF/document.jsp").forward(pRequest, pResponse);

        } catch (Exception pEx) {
            throw new ServletException("Error while fetching your document.", pEx);
        }

    }
}
