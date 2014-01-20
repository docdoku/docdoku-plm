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

import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.core.workflow.ActivityKey;
import com.docdoku.core.workflow.TaskKey;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class VoteServlet extends HttpServlet {

    @EJB
    private IDocumentManagerLocal documentService;
    @EJB
    private IProductManagerLocal productService;

    @Override
    protected void doGet(HttpServletRequest pRequest,
            HttpServletResponse pResponse)
            throws ServletException, IOException {
        if (pRequest.getCharacterEncoding() == null) {
            pRequest.setCharacterEncoding("UTF-8");
        }

        String action = pRequest.getParameter("action");
        String workspaceId = pRequest.getParameter("workspaceId");
        int activityWorkflowId = Integer.parseInt(pRequest.getParameter("activityWorkflowId"));
        int activityStep = Integer.parseInt(pRequest.getParameter("activityStep"));
        int index = Integer.parseInt(pRequest.getParameter("index"));
        String comment = pRequest.getParameter("comment");
        String entityType = pRequest.getParameter("entityType");

        try {

            if(entityType.equals("parts")){

                if (action.equals("Approve")) {
                    PartRevision partRevision = productService.approveTaskOnPart(workspaceId, new TaskKey(new ActivityKey(activityWorkflowId, activityStep), index), comment, null);
                    pRequest.setAttribute("entity", partRevision);
                    pRequest.getRequestDispatcher("/faces/taskApproved.xhtml").forward(pRequest, pResponse);
                } else if (action.equals("Reject")) {
                    PartRevision partRevision = productService.rejectTaskOnPart(workspaceId, new TaskKey(new ActivityKey(activityWorkflowId, activityStep), index), comment, null);
                    pRequest.setAttribute("entity", partRevision);
                    pRequest.getRequestDispatcher("/faces/taskRejected.xhtml").forward(pRequest, pResponse);
                }

            }else if(entityType.equals("documents")){

                if (action.equals("Approve")) {
                    DocumentRevision documentRevision = documentService.approveTaskOnDocument(workspaceId, new TaskKey(new ActivityKey(activityWorkflowId, activityStep), index), comment, null);
                    pRequest.setAttribute("entity", documentRevision);
                    pRequest.getRequestDispatcher("/faces/taskApproved.xhtml").forward(pRequest, pResponse);
                } else if (action.equals("Reject")) {
                    DocumentRevision documentRevision = documentService.rejectTaskOnDocument(workspaceId, new TaskKey(new ActivityKey(activityWorkflowId, activityStep), index), comment, null);
                    pRequest.setAttribute("entity", documentRevision);
                    pRequest.getRequestDispatcher("/faces/taskRejected.xhtml").forward(pRequest, pResponse);
                }

            }




        } catch (Exception pEx) {
            throw new ServletException("Error while voting.", pEx);
        }

    }
}
