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
package com.docdoku.server.rest;

import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.exceptions.EntityNotFoundException;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.exceptions.UserNotActiveException;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.core.services.IDocumentWorkflowManagerLocal;
import com.docdoku.core.services.IPartWorkflowManagerLocal;
import com.docdoku.core.workflow.ActivityKey;
import com.docdoku.core.workflow.TaskKey;
import com.docdoku.server.rest.dto.DocumentRevisionDTO;
import com.docdoku.server.rest.dto.TaskProcessDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

import javax.annotation.PostConstruct;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Morgan Guimard
 */

@RequestScoped
@Api(hidden = true, value = "tasks", description = "Operations about tasks")
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class TaskResource {

    @Inject
    private IDocumentWorkflowManagerLocal documentWorkflowService;

    @Inject
    private IDocumentManagerLocal documentService;

    @Inject
    private IPartWorkflowManagerLocal partWorkflowService;

    private Mapper mapper;

    public TaskResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @ApiOperation(value = "Get documents where user has assigned tasks", response = DocumentRevisionDTO.class, responseContainer = "List")
    @Path("{assignedUserLogin}/documents/")
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO[] getDocumentsWhereGivenUserHasAssignedTasks(
            @PathParam("workspaceId") String workspaceId,
            @PathParam("assignedUserLogin") String assignedUserLogin,
            @QueryParam("filter") String filter)
            throws EntityNotFoundException, UserNotActiveException {

        DocumentRevision[] docRs;

        if (filter == null) {
            docRs = documentService.getDocumentRevisionsWithAssignedTasksForGivenUser(workspaceId, assignedUserLogin);
        } else {
            if ("in_progress".equals(filter)) {
                docRs = documentService.getDocumentRevisionsWithOpenedTasksForGivenUser(workspaceId, assignedUserLogin);
            } else {
                docRs = documentService.getDocumentRevisionsWithAssignedTasksForGivenUser(workspaceId, assignedUserLogin);
            }
        }

        List<DocumentRevisionDTO> docRsDTOs = new ArrayList<>();

        for (DocumentRevision docR : docRs) {

            DocumentRevisionDTO docDTO = mapper.map(docR, DocumentRevisionDTO.class);
            docDTO.setPath(docR.getLocation().getCompletePath());
            docDTO = Tools.createLightDocumentRevisionDTO(docDTO);
            docDTO.setIterationSubscription(documentService.isUserIterationChangeEventSubscribedForGivenDocument(workspaceId, docR));
            docDTO.setStateSubscription(documentService.isUserStateChangeEventSubscribedForGivenDocument(workspaceId, docR));
            docRsDTOs.add(docDTO);

        }

        return docRsDTOs.toArray(new DocumentRevisionDTO[docRsDTOs.size()]);
    }


    @POST
    @ApiOperation(value = "Approve or reject task on document", response = Response.class)
    @Path("documents/process")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response processTaskForDocuments(@PathParam("workspaceId") String workspaceId,
                                            @QueryParam("activityWorkflowId") int activityWorkflowId,
                                            @QueryParam("activityStep") int activityStep,
                                            @QueryParam("index") int index,
                                            @QueryParam("action") String action,
                                            @ApiParam(required = true, value = "Task process data") TaskProcessDTO taskProcessDTO)
            throws EntityNotFoundException, NotAllowedException, UserNotActiveException {

        switch (action) {
            case "approve":
                documentWorkflowService.approveTaskOnDocument(workspaceId, new TaskKey(new ActivityKey(activityWorkflowId, activityStep), index), taskProcessDTO.getComment(), taskProcessDTO.getSignature());
                break;
            case "reject":
                documentWorkflowService.rejectTaskOnDocument(workspaceId, new TaskKey(new ActivityKey(activityWorkflowId, activityStep), index), taskProcessDTO.getComment(), taskProcessDTO.getSignature());
                break;
            default:
                return Response.status(Response.Status.BAD_REQUEST).build();
        }

        return Response.ok().build();
    }

    @POST
    @ApiOperation(value = "Approve or reject task on part", response = Response.class)
    @Path("parts/process")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response processTaskForParts(@PathParam("workspaceId") String workspaceId,
                                        @QueryParam("activityWorkflowId") int activityWorkflowId,
                                        @QueryParam("activityStep") int activityStep,
                                        @QueryParam("index") int index,
                                        @QueryParam("action") String action,
                                        @ApiParam(required = true, value = "Task process data") TaskProcessDTO taskProcessDTO)
            throws EntityNotFoundException, NotAllowedException, UserNotActiveException {

        switch (action) {
            case "approve":
                partWorkflowService.approveTaskOnPart(workspaceId, new TaskKey(new ActivityKey(activityWorkflowId, activityStep), index), taskProcessDTO.getComment(), taskProcessDTO.getSignature());
                break;
            case "reject":
                partWorkflowService.rejectTaskOnPart(workspaceId, new TaskKey(new ActivityKey(activityWorkflowId, activityStep), index), taskProcessDTO.getComment(), taskProcessDTO.getSignature());
                break;
            default:
                return Response.status(Response.Status.BAD_REQUEST).build();
        }

        return Response.ok().build();
    }
}