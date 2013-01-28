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

import com.docdoku.core.document.*;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.services.IDocumentManagerLocal;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;

import com.docdoku.core.meta.InstanceTextAttribute;

import com.docdoku.core.*;
import org.codehaus.jettison.mapped.MappedXMLStreamWriter;

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

            List<DocumentIteration> di =  docM.getDocumentIterations();

            Set<InstanceAttribute> attr=new HashSet<InstanceAttribute>();

            for (int i=0 ; i<di.size() ; i++)
                for (InstanceAttribute ia : di.get(i).getInstanceAttributes().values())
                    attr.add(ia);

            pRequest.setAttribute("attr", attr);

            pRequest.getRequestDispatcher("/WEB-INF/document.jsp").forward(pRequest, pResponse);

        } catch (Exception pEx) {
            throw new ServletException("Error while fetching your document.", pEx);
        }
    }
}
