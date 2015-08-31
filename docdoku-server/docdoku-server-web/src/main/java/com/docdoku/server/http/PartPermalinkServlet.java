/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2015 DocDoku SARL
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

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.product.PartRevisionKey;
import com.docdoku.core.services.IProductManagerLocal;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * @author Morgan Guimard
 */

public class PartPermalinkServlet extends HttpServlet {

    @EJB
    private IProductManagerLocal productService;

    @Override
    protected void doGet(HttpServletRequest pRequest, HttpServletResponse pResponse) throws ServletException, IOException {

        try {
            if(pRequest.getAttribute("publicPartRevision") != null){
                PartRevision partRevision = (PartRevision) pRequest.getAttribute("publicPartRevision");
                handleSuccess(pRequest,pResponse,partRevision);
            }else{

                String requestURI = pRequest.getRequestURI();
                String[] pathInfos = Pattern.compile("/").split(requestURI);
                int offset = pRequest.getContextPath().isEmpty() ? 2 : 3;

                String workspaceId = URLDecoder.decode(pathInfos[offset], "UTF-8");
                String partNumber = URLDecoder.decode(pathInfos[offset+1],"UTF-8");
                String partVersion = pathInfos[offset+2];

                PartRevisionKey partRevisionKey  = new PartRevisionKey(workspaceId,partNumber,partVersion);
                PartRevision partRevision = productService.getPartRevision(partRevisionKey);
                handleSuccess(pRequest,pResponse,partRevision);

            }
        } catch (Exception pEx) {
            throw new ServletException("Error while fetching your part.", pEx);
        }
    }

    private void handleSuccess(HttpServletRequest pRequest, HttpServletResponse pResponse, PartRevision partRevision) throws ServletException, IOException, NotAllowedException {

        PartIteration partIteration =  partRevision.getLastIteration();

        if(partIteration == null){
            throw new NotAllowedException(Locale.getDefault(), "NotAllowedException41");
        }

        String nativeCadFileURI ="";
        if(partRevision.getLastIteration().getNativeCADFile() != null){
            BinaryResource binaryResource = partRevision.getLastIteration().getNativeCADFile();
            nativeCadFileURI =  "/api/files/" + binaryResource.getFullName();
        }

        String geometryFileURI = "";
        if(!partRevision.getLastIteration().getGeometries().isEmpty()){
            geometryFileURI = "/api/files/"+partRevision.getLastIteration().getSortedGeometries().get(0).getFullName();
        }

        pRequest.setAttribute("partRevision", partRevision);
        pRequest.setAttribute("attr",  new ArrayList<>(partIteration.getInstanceAttributes()));
        pRequest.setAttribute("nativeCadFileURI",nativeCadFileURI);
        pRequest.setAttribute("geometryFileURI",geometryFileURI);
        pRequest.getRequestDispatcher(pRequest.getContextPath()+"/faces/partPermalink.xhtml").forward(pRequest, pResponse);
    }

}
