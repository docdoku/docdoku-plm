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

import com.docdoku.core.change.ModificationNotification;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.core.services.ITaskManagerLocal;
import com.docdoku.core.workflow.ActivityKey;
import com.docdoku.core.workflow.TaskKey;
import com.docdoku.core.workflow.TaskWrapper;
import com.docdoku.server.rest.dto.*;
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
import javax.ws.rs.core.GenericEntity;
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
    private IDocumentManagerLocal documentService;
    @Inject
    private IProductManagerLocal productService;

    @Inject
    private ITaskManagerLocal taskManager;

    private Mapper mapper;

    public TaskResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }


    @GET
    @ApiOperation(value = "Get assigned tasks for given user", response = TaskDTO.class, responseContainer = "List")
    @Path("{assignedUserLogin}/assigned")
    @Produces(MediaType.APPLICATION_JSON)
    public TaskDTO[] getAssignedTasksForGivenUser(
            @PathParam("workspaceId") String workspaceId,
            @PathParam("assignedUserLogin") String assignedUserLogin) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        TaskWrapper[] runningTasksForGivenUser = taskManager.getAssignedTasksForGivenUser(workspaceId, assignedUserLogin);
        List<TaskDTO> taskDTOs = new ArrayList<>();
        for(TaskWrapper taskWrapper:runningTasksForGivenUser){
            TaskDTO taskDTO = mapper.map(taskWrapper.getTask(), TaskDTO.class);
            taskDTO.setHolderType(taskWrapper.getHolderType());
            taskDTO.setWorkspaceId(workspaceId);
            taskDTO.setHolderReference(taskWrapper.getHolderReference());
            taskDTO.setHolderVersion(taskWrapper.getHolderVersion());
            taskDTOs.add(taskDTO);
        }
        return taskDTOs.toArray(new TaskDTO[taskDTOs.size()]);
    }

    @GET
    @ApiOperation(value = "Get task", response = TaskDTO.class)
    @Path("{taskId}")
    @Produces(MediaType.APPLICATION_JSON)
    public TaskDTO getTask(
            @PathParam("workspaceId") String workspaceId,
            @PathParam("taskId") String taskId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, TaskNotFoundException, AccessRightException, WorkspaceNotEnabledException {

        String[] split = taskId.split("-");

        int workflowId = Integer.valueOf(split[0]);
        int step = Integer.valueOf(split[1]);
        int task = Integer.valueOf(split[2]);

        TaskKey taskKey = new TaskKey(new ActivityKey(workflowId, step), task);
        TaskWrapper taskWrapper = taskManager.getTask(workspaceId, taskKey);

        TaskDTO taskDTO = mapper.map(taskWrapper.getTask(), TaskDTO.class);
        taskDTO.setHolderType(taskWrapper.getHolderType());
        taskDTO.setWorkspaceId(workspaceId);
        taskDTO.setHolderReference(taskWrapper.getHolderReference());
        taskDTO.setHolderVersion(taskWrapper.getHolderVersion());

        return taskDTO;
    }

    @GET
    @ApiOperation(value = "Get documents where user has assigned tasks", response = DocumentRevisionDTO.class, responseContainer = "List")
    @Path("{assignedUserLogin}/documents")
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO[] getDocumentsWhereGivenUserHasAssignedTasks(
            @PathParam("workspaceId") String workspaceId,
            @PathParam("assignedUserLogin") String assignedUserLogin,
            @QueryParam("filter") String filter)
            throws EntityNotFoundException, UserNotActiveException {

        DocumentRevision[] docRs;

        if ("in_progress".equals(filter)) {
            docRs = documentService.getDocumentRevisionsWithOpenedTasksForGivenUser(workspaceId, assignedUserLogin);
        } else {
            docRs = documentService.getDocumentRevisionsWithAssignedTasksForGivenUser(workspaceId, assignedUserLogin);
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

    @GET
    @ApiOperation(value = "Get parts where user has assigned tasks", response = PartRevisionDTO.class, responseContainer = "List")
    @Path("{assignedUserLogin}/parts")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPartsWhereGivenUserHasAssignedTasks(
            @PathParam("workspaceId") String workspaceId,
            @PathParam("assignedUserLogin") String assignedUserLogin,
            @QueryParam("filter") String filter)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {

        PartRevision[] withTaskPartRevisions;

        if ("in_progress".equals(filter)) {
            withTaskPartRevisions = productService.getPartRevisionsWithOpenedTasksForGivenUser(workspaceId, assignedUserLogin);
        } else {
            withTaskPartRevisions = productService.getPartRevisionsWithAssignedTasksForGivenUser(workspaceId, assignedUserLogin);
        }

        List<PartRevisionDTO> partRevisionDTOs = new ArrayList<>();

        for (PartRevision partRevision : withTaskPartRevisions) {
            PartRevisionDTO partRevisionDTO = Tools.mapPartRevisionToPartDTO(partRevision);

            PartIterationKey iterationKey = new PartIterationKey(partRevision.getKey(), partRevision.getLastIterationNumber());
            List<ModificationNotification> notifications = productService.getModificationNotifications(iterationKey);
            List<ModificationNotificationDTO> notificationDTOs =  Tools.mapModificationNotificationsToModificationNotificationDTO(notifications);
            partRevisionDTO.setNotifications(notificationDTOs);

            partRevisionDTOs.add(partRevisionDTO);
        }

        return Response.ok(new GenericEntity<List<PartRevisionDTO>>((List<PartRevisionDTO>) partRevisionDTOs) {
        }).build();
    }


    @PUT
    @ApiOperation(value = "Approve or reject task on document", response = Response.class)
    @Path("{taskId}/process")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response processTask(@PathParam("workspaceId") String workspaceId,
                                            @PathParam("taskId") String taskId,
                                            @ApiParam(required = true, value = "Task process data") TaskProcessDTO taskProcessDTO)
            throws EntityNotFoundException, NotAllowedException, UserNotActiveException, AccessRightException {

        String[] split = taskId.split("-");
        int workflowId = Integer.valueOf(split[0]);
        int step = Integer.valueOf(split[1]);
        int index = Integer.valueOf(split[2]);

        taskManager.processTask(workspaceId, new TaskKey(new ActivityKey(workflowId,step),index), taskProcessDTO.getAction().name(), taskProcessDTO.getComment(), taskProcessDTO.getSignature());
        return Response.ok().build();
    }

}