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
package com.docdoku.server.rest;

import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.core.workflow.ActivityKey;
import com.docdoku.core.workflow.TaskKey;
import com.docdoku.server.rest.dto.TaskDTO;
import com.docdoku.server.rest.dto.TaskProcessDTO;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

/**
 * @author Morgan Guimard
 */

@Stateless
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class TaskResource {

    @EJB
    private DocumentsResource documentsResource;

    @EJB
    private IDocumentManagerLocal documentService;

    @EJB
    private IProductManagerLocal productService;

    public TaskResource() {
    }

    @Path("{assignedUserLogin}/documents/")
    public DocumentsResource getDocumentsResource() {
        return documentsResource;
    }

    @POST
    @Consumes("application/json;charset=UTF-8")
    @Path("documents/process")
    public Response processTaskForDocuments(@PathParam("workspaceId") String workspaceId, @QueryParam("activityWorkflowId") int activityWorkflowId, @QueryParam("activityStep") int activityStep, @QueryParam("index") int index, @QueryParam("action") String action, TaskProcessDTO taskProcessDTO) {

        try {

            if (action.equals("approve")) {
                documentService.approve(workspaceId, new TaskKey(new ActivityKey(activityWorkflowId, activityStep), index), taskProcessDTO.getComment(), taskProcessDTO.getSignature());
            } else if (action.equals("reject")) {
                documentService.reject(workspaceId, new TaskKey(new ActivityKey(activityWorkflowId, activityStep), index), taskProcessDTO.getComment(), taskProcessDTO.getSignature());
            } else {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            return Response.ok().build();

        } catch (Exception ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }

    }

    @POST
    @Consumes("application/json;charset=UTF-8")
    @Path("parts/process")
    public Response processTaskForParts(@PathParam("workspaceId") String workspaceId, @QueryParam("activityWorkflowId") int activityWorkflowId, @QueryParam("activityStep") int activityStep, @QueryParam("index") int index, @QueryParam("action") String action, TaskProcessDTO taskProcessDTO) {

        try {

            if (action.equals("approve")) {
                productService.approve(workspaceId, new TaskKey(new ActivityKey(activityWorkflowId, activityStep), index), taskProcessDTO.getComment(), taskProcessDTO.getSignature());
            } else if (action.equals("reject")) {
                productService.reject(workspaceId, new TaskKey(new ActivityKey(activityWorkflowId, activityStep), index), taskProcessDTO.getComment(), taskProcessDTO.getSignature());
            } else {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            return Response.ok().build();

        } catch (Exception ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }

    }

}
