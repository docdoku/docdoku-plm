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
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IWorkflowManagerLocal;
import com.docdoku.core.workflow.WorkspaceWorkflow;
import com.docdoku.server.rest.dto.RoleMappingDTO;
import com.docdoku.server.rest.dto.WorkspaceWorkflowCreationDTO;
import com.docdoku.server.rest.dto.WorkspaceWorkflowDTO;
import io.swagger.annotations.*;
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
import java.util.*;

/**
 * @author Morgan Guimard
 */
@RequestScoped
@Api(hidden = true, value = "workspaceWorkflows", description = "Operations about workspace workflows")
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class WorkspaceWorkflowResource {

    @Inject
    private IWorkflowManagerLocal workflowService;

    private Mapper mapper;

    public WorkspaceWorkflowResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @ApiOperation(value = "Get workspace workflow list",
            response = WorkspaceWorkflowDTO.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of WorkspaceWorkflowDTOs. It can be an empty list."),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWorkspaceWorkflowList(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId)
            throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException,
            WorkspaceNotEnabledException {

        WorkspaceWorkflow[] workspaceWorkflowList = workflowService.getWorkspaceWorkflowList(workspaceId);
        List<WorkspaceWorkflowDTO> workspaceWorkflowListDTO = new ArrayList<>();

        for (WorkspaceWorkflow workspaceWorkflow : workspaceWorkflowList) {
            workspaceWorkflowListDTO.add(mapper.map(workspaceWorkflow, WorkspaceWorkflowDTO.class));
        }

        return Response.ok(new GenericEntity<List<WorkspaceWorkflowDTO>>((List<WorkspaceWorkflowDTO>) workspaceWorkflowListDTO) {
        }).build();
    }

    @GET
    @ApiOperation(value = "Get workspace workflow",
            response = WorkspaceWorkflowDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of WorkspaceWorkflowDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{workspaceWorkflowId}")
    @Produces(MediaType.APPLICATION_JSON)
    public WorkspaceWorkflowDTO getWorkspaceWorkflow(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Workspace workflow id") @PathParam("workspaceWorkflowId") String workspaceWorkflowId)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException,
            WorkflowNotFoundException, WorkspaceNotEnabledException {

        WorkspaceWorkflow workspaceWorkflow = workflowService.getWorkspaceWorkflow(workspaceId, workspaceWorkflowId);
        return mapper.map(workspaceWorkflow, WorkspaceWorkflowDTO.class);
    }


    @POST
    @ApiOperation(value = "Instantiate workspace workflow from workflow model",
            response = WorkspaceWorkflowDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of WorkspaceWorkflowDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public WorkspaceWorkflowDTO createWorkspaceWorkflow(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Workspace workflow to create") WorkspaceWorkflowCreationDTO workflowCreationDTO)
            throws RoleNotFoundException, WorkspaceNotFoundException, UserNotFoundException, AccessRightException,
            WorkflowModelNotFoundException, NotAllowedException, UserGroupNotFoundException,
            WorkspaceNotEnabledException {

        Map<String, Collection<String>> userRoleMapping = new HashMap<>();
        Map<String, Collection<String>> groupRoleMapping = new HashMap<>();
        RoleMappingDTO[] roleMappingDTOs = workflowCreationDTO.getRoleMapping();

        if (roleMappingDTOs != null) {
            for (RoleMappingDTO roleMappingDTO : roleMappingDTOs) {
                userRoleMapping.put(roleMappingDTO.getRoleName(), roleMappingDTO.getUserLogins());
                groupRoleMapping.put(roleMappingDTO.getRoleName(), roleMappingDTO.getGroupIds());
            }
        }

        WorkspaceWorkflow workspaceWorkflow = workflowService.instantiateWorkflow(workspaceId, workflowCreationDTO.getId(), workflowCreationDTO.getWorkflowModelId(), userRoleMapping, groupRoleMapping);
        return mapper.map(workspaceWorkflow, WorkspaceWorkflowDTO.class);
    }


    @DELETE
    @Path("{workspaceWorkflowId}")
    @ApiOperation(value = "Delete a workspace workflow ",
            response = Response.class)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successful deletion operation"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteWorkspaceWorkflow(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Workspace workflow id") @PathParam("workspaceWorkflowId") String workspaceWorkflowId)
            throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException, WorkspaceNotEnabledException {

        workflowService.deleteWorkspaceWorkflow(workspaceId, workspaceWorkflowId);
        return Response.noContent().build();
    }
}