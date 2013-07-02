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

import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.document.DocumentMasterKey;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.services.IDocumentManagerLocal;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * @author Morgan Guimard
 */

public class DocumentPermalinkServlet extends HttpServlet {

    @EJB
    private IDocumentManagerLocal documentService;

    @Override
    protected void doGet(HttpServletRequest pRequest, HttpServletResponse pResponse) throws ServletException, IOException {

        try {
            if(pRequest.getAttribute("publicDocumentMaster") != null){
                DocumentMaster documentMaster = (DocumentMaster) pRequest.getAttribute("publicDocumentMaster");
                handleSuccess(pRequest,pResponse,documentMaster);
            }else{
                HttpServletRequest httpRequest = (HttpServletRequest) pRequest;
                String requestURI = httpRequest.getRequestURI();
                String[] pathInfos = Pattern.compile("/").split(requestURI);
                int offset = httpRequest.getContextPath().equals("") ? 2 : 3;

                String workspaceId = URLDecoder.decode(pathInfos[offset], "UTF-8");
                String documentMasterId = URLDecoder.decode(pathInfos[offset+1],"UTF-8");
                String documentMasterVersion = pathInfos[offset+2];

                DocumentMasterKey documentMasterKey  =  new DocumentMasterKey(workspaceId,documentMasterId,documentMasterVersion);
                DocumentMaster documentMaster = documentService.getDocumentMaster(documentMasterKey);
                handleSuccess(pRequest,pResponse,documentMaster);
            }
        }  catch (Exception pEx) {
            pEx.printStackTrace();
            throw new ServletException("error while fetching your document.", pEx);
        }
    }

    private void handleSuccess(HttpServletRequest pRequest, HttpServletResponse pResponse, DocumentMaster documentMaster) throws ServletException, IOException {
        pRequest.setAttribute("documentMaster", documentMaster);
        DocumentIteration doc =  documentMaster.getLastIteration();
        pRequest.setAttribute("attr", new ArrayList<InstanceAttribute>(doc.getInstanceAttributes().values()));
        pRequest.getRequestDispatcher("/faces/documentPermalink.xhtml").forward(pRequest, pResponse);
    }

}
