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

import com.docdoku.core.exceptions.*;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IWorkflowManagerLocal;
import com.docdoku.core.workflow.ActivityModel;
import com.docdoku.core.workflow.Workflow;
import com.docdoku.core.workflow.WorkflowModel;
import com.docdoku.core.workflow.WorkflowModelKey;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Yassine Belouad
 */
@RequestScoped
@Api(hidden = true, value = "workflows", description = "Operations about workflow entities and workflow models")
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class WorkflowResource {

    @Inject
    private IWorkflowManagerLocal workflowService;

    private Mapper mapper;

    public WorkflowResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @ApiOperation(value = "Get workflow models", response = WorkflowModelDTO.class, responseContainer = "List")
    @Produces(MediaType.APPLICATION_JSON)
    public WorkflowModelDTO[] getWorkflowModelsInWorkspace(@PathParam("workspaceId") String workspaceId)
            throws EntityNotFoundException, UserNotActiveException {

        WorkflowModel[] workflowModels = workflowService.getWorkflowModels(workspaceId);
        WorkflowModelDTO[] dtos = new WorkflowModelDTO[workflowModels.length];

        for (int i = 0; i < workflowModels.length; i++) {
            dtos[i] = mapper.map(workflowModels[i], WorkflowModelDTO.class);
        }

        return dtos;
    }

    @GET
    @ApiOperation(value = "Get workflow model", response = WorkflowModelDTO.class)
    @Path("{workflowModelId}")
    @Produces(MediaType.APPLICATION_JSON)
    public WorkflowModelDTO getWorkflowModelInWorkspace(@PathParam("workspaceId") String workspaceId,
                                                   @PathParam("workflowModelId") String workflowModelId)
            throws EntityNotFoundException, UserNotActiveException {

        WorkflowModel workflowModel = workflowService.getWorkflowModel(new WorkflowModelKey(workspaceId, workflowModelId));
        return mapper.map(workflowModel, WorkflowModelDTO.class);
    }

    @DELETE
    @ApiOperation(value = "Delete workflow model", response = Response.class)
    @Path("{workflowModelId}")
    public Response delWorkflowModel(@PathParam("workspaceId") String workspaceId,
                                     @PathParam("workflowModelId") String workflowModelId)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException, EntityConstraintException {
        workflowService.deleteWorkflowModel(new WorkflowModelKey(workspaceId, workflowModelId));
        return Response.status(Response.Status.OK).build();
    }

    @PUT
    @ApiOperation(value = "Update workflow model", response = WorkflowModelDTO.class)
    @Path("{workflowModelId}")
    @Produces(MediaType.APPLICATION_JSON)
    public WorkflowModelDTO updateWorkflowModel(@PathParam("workspaceId") String workspaceId,
                                                @PathParam("workflowModelId") String workflowModelId,
                                                @ApiParam(required = true, value = "Workflow model to update") WorkflowModelDTO workflowModelDTOToPersist)
            throws EntityNotFoundException, AccessRightException, EntityAlreadyExistsException, CreationException, UserNotActiveException, NotAllowedException {

        WorkflowModelKey workflowModelKey = new WorkflowModelKey(workspaceId, workflowModelId);
        List<ActivityModelDTO> activityModelDTOsList = workflowModelDTOToPersist.getActivityModels();
        ActivityModel[] activityModels = extractActivityModelFromDTO(activityModelDTOsList);
        WorkflowModel workflowModel = workflowService.updateWorkflowModel(workflowModelKey, workflowModelDTOToPersist.getFinalLifeCycleState(), activityModels);
        return mapper.map(workflowModel, WorkflowModelDTO.class);
    }

    @PUT
    @ApiOperation(value = "Update workflow model ACL", response = Response.class)
    @Path("{workflowModelId}/acl")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateWorkflowModelACL(@PathParam("workspaceId") String pWorkspaceId,
                                           @PathParam("workflowModelId") String workflowModelId,
                                           @ApiParam(required = true, value = "ACL rules to set") ACLDTO acl)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        if (!acl.getGroupEntries().isEmpty() || !acl.getUserEntries().isEmpty()) {

            Map<String, String> userEntries = new HashMap<>();
            Map<String, String> groupEntries = new HashMap<>();

            for (Map.Entry<String, ACL.Permission> entry : acl.getUserEntries().entrySet()) {
                userEntries.put(entry.getKey(), entry.getValue().name());
            }

            for (Map.Entry<String, ACL.Permission> entry : acl.getGroupEntries().entrySet()) {
                groupEntries.put(entry.getKey(), entry.getValue().name());
            }

            workflowService.updateACLForWorkflow(pWorkspaceId, workflowModelId, userEntries, groupEntries);
        } else {
            workflowService.removeACLFromWorkflow(pWorkspaceId, workflowModelId);
        }
        return Response.ok().build();
    }

    @POST
    @ApiOperation(value = "Create workflow model", response = WorkflowModelDTO.class)
    @Produces(MediaType.APPLICATION_JSON)
    public WorkflowModelDTO createWorkflowModel(@PathParam("workspaceId") String workspaceId,
                                                @ApiParam(required = true, value = "Workflow model to create rules to set") WorkflowModelDTO workflowModelDTOToPersist)
            throws EntityNotFoundException, EntityAlreadyExistsException, UserNotActiveException, NotAllowedException, AccessRightException, CreationException {
        List<ActivityModelDTO> activityModelDTOsList = workflowModelDTOToPersist.getActivityModels();
        ActivityModel[] activityModels = extractActivityModelFromDTO(activityModelDTOsList);
        WorkflowModel workflowModel = workflowService.createWorkflowModel(workspaceId, workflowModelDTOToPersist.getReference(), workflowModelDTOToPersist.getFinalLifeCycleState(), activityModels);
        return mapper.map(workflowModel, WorkflowModelDTO.class);
    }

    @POST
    @Path("{workflowModelId}")
    @ApiOperation(value = "Instantiate workflow model", response = WorkflowModelDTO.class)
    @Produces(MediaType.APPLICATION_JSON)
    public WorkflowDTO createWorkflowModel(@PathParam("workspaceId") String workspaceId,
                                           @PathParam("workspaceId") String workflowModelId,
                                           @ApiParam(required = true, value = "Role list to use in the workflow") RoleMappingDTO[] roleMapping)
            throws RoleNotFoundException, NotAllowedException, WorkspaceNotFoundException, UserNotFoundException, AccessRightException, WorkflowModelNotFoundException {
              Map<String, String> roleMappings = new HashMap<>();

              for (RoleMappingDTO roleMappingDTO : roleMapping) {
                  roleMappings.put(roleMappingDTO.getRoleName(), roleMappingDTO.getUserLogin());
              }

              Workflow workflow = workflowService.instantiateWorkflow(workspaceId, workflowModelId, roleMappings);
              return mapper.map(workflow,WorkflowDTO.class);
    }

    private ActivityModel[] extractActivityModelFromDTO(List<ActivityModelDTO> activityModelDTOsList) throws NotAllowedException {
        Map<Integer, ActivityModel> activityModels = new HashMap<>();

        for (int i = 0; i < activityModelDTOsList.size(); i++) {
            ActivityModelDTO activityModelDTO = activityModelDTOsList.get(i);
            ActivityModel activityModel = mapper.map(activityModelDTO, ActivityModel.class);
            activityModels.put(activityModel.getStep(), activityModel);

            Integer relaunchStep = activityModelDTO.getRelaunchStep();
            if (relaunchStep != null && relaunchStep < i) {
                ActivityModel relaunchActivity = activityModels.get(relaunchStep);
                activityModel.setRelaunchActivity(relaunchActivity);
            }
        }

        return activityModels.values().toArray(new ActivityModel[activityModels.size()]);
    }

}