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
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IWorkflowManagerLocal;
import com.docdoku.core.workflow.Workflow;
import com.docdoku.server.rest.dto.WorkflowDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

import javax.annotation.PostConstruct;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Morgan Guimard
 */
@RequestScoped
@Api(hidden = true, value = "workflows", description = "Operations about workflow instances")
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
    @ApiOperation(value = "Get instantiated workflow", response = WorkflowDTO.class)
    @Path("{workflowId}")
    @Produces(MediaType.APPLICATION_JSON)
    public WorkflowDTO getWorkflowInstance(@PathParam("workspaceId") String workspaceId, @PathParam("workflowId") int workflowId)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        Workflow workflow = workflowService.getWorkflow(workspaceId, workflowId);
        return mapper.map(workflow, WorkflowDTO.class);
    }

    @GET
    @ApiOperation(value = "Get workflow's aborted workflows", response = WorkflowDTO.class, responseContainer = "List")
    @Path("{workflowId}/aborted")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWorkflowAbortedWorkflows(@PathParam("workspaceId") String workspaceId, @PathParam("workflowId") int workflowId)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, WorkflowNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        Workflow[] abortedWorkflows =  workflowService.getWorkflowAbortedWorkflows(workspaceId, workflowId);

        List<WorkflowDTO> abortedWorkflowsDTO = new ArrayList<>();

        for (Workflow abortedWorkflow : abortedWorkflows) {
            abortedWorkflowsDTO.add(mapper.map(abortedWorkflow, WorkflowDTO.class));
        }

        Collections.sort(abortedWorkflowsDTO);

        return Response.ok(new GenericEntity<List<WorkflowDTO>>((List<WorkflowDTO>) abortedWorkflowsDTO) {
        }).build();

    }


}