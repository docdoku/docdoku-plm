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
import com.docdoku.core.workflow.ActivityKey;
import com.docdoku.core.workflow.TaskKey;
import java.io.IOException;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;


public class VoteServlet extends HttpServlet {

    @EJB
    private IDocumentManagerLocal documentService;

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

        try {

            if (action.equals("Approve")) {
                documentService.approve(workspaceId, new TaskKey(new ActivityKey(activityWorkflowId, activityStep), index), comment);
                pRequest.getRequestDispatcher("/WEB-INF/taskApproved.jsp").forward(pRequest, pResponse);
            } else if (action.equals("Reject")) {
                documentService.reject(workspaceId, new TaskKey(new ActivityKey(activityWorkflowId, activityStep), index), comment);
                pRequest.getRequestDispatcher("/WEB-INF/taskRejected.jsp").forward(pRequest, pResponse);
            }


        } catch (Exception pEx) {
            throw new ServletException("Error while voting.", pEx);
        }

    }
}
