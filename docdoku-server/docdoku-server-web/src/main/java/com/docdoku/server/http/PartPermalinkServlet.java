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
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.product.PartRevisionKey;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.server.filters.FilterUtils;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Morgan Guimard
 */

public class PartPermalinkServlet extends HttpServlet {
    
    @EJB
    private IProductManagerLocal productService;
    
    @Override
    protected void doGet(HttpServletRequest pRequest,
                         HttpServletResponse pResponse)
            throws ServletException, IOException {

        try {

            if(pRequest.getAttribute("publicPartRevision") != null){

                PartRevision partRevision = (PartRevision) pRequest.getAttribute("publicPartRevision");
                handleSuccess(pRequest,pResponse,partRevision);

            }else{

                PartRevisionKey partRevisionKey = FilterUtils.getPartRevisionKey(pRequest);
                PartRevision partRevision = productService.getPartRevision(partRevisionKey);
                handleSuccess(pRequest,pResponse,partRevision);
            }

        } catch (Exception pEx) {
            throw new ServletException("Error while fetching your part.", pEx);
        }
    }

    private void handleSuccess(HttpServletRequest pRequest, HttpServletResponse pResponse, PartRevision partRevision) throws ServletException, IOException {
        pRequest.setAttribute("partRevision", partRevision);
        PartIteration partIteration =  partRevision.getLastIteration();
        pRequest.setAttribute("attr",  new ArrayList<InstanceAttribute>(partIteration.getInstanceAttributes().values()));
        pRequest.getRequestDispatcher("/faces/partPermalink.xhtml").forward(pRequest, pResponse);
    }
}
