/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2014 DocDoku SARL
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
import com.docdoku.core.workflow.*;
import com.docdoku.server.rest.dto.ActivityModelDTO;
import com.docdoku.server.rest.dto.WorkflowModelDTO;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

import javax.annotation.PostConstruct;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Yassine Belouad
 */
@Stateless
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class WorkflowResource {
    @EJB
    private IWorkflowManagerLocal workflowService;

    private Mapper mapper;

    private static final Logger LOGGER = Logger.getLogger(WorkflowResource.class.getName());

    public WorkflowResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public WorkflowModelDTO[] getWorkflowsInWorkspace(@PathParam("workspaceId") String workspaceId) {
        try {

            WorkflowModel[] workflowModels = workflowService.getWorkflowModels(workspaceId);
            WorkflowModelDTO[] dtos = new WorkflowModelDTO[workflowModels.length];
            
            for(int i=0; i<workflowModels.length; i++){
                dtos[i] = mapper.map(workflowModels[i], WorkflowModelDTO.class);
            }
            
            return dtos;
            
        } catch (UserNotFoundException | UserNotActiveException e) {
            LOGGER.log(Level.WARNING, null, e);
            throw new RestApiException(e.toString(), e.getMessage(),Response.Status.FORBIDDEN);
        } catch (WorkspaceNotFoundException e) {
            LOGGER.log(Level.WARNING, null, e);
            throw new RestApiException(e.toString(), e.getMessage(),Response.Status.NOT_FOUND);
        }
    }

    @GET
    @Path("{workflowModelId}")
    @Produces(MediaType.APPLICATION_JSON)
    public WorkflowModelDTO getWorkflowInWorkspace(@PathParam("workspaceId") String workspaceId, @PathParam("workflowModelId") String workflowModelId) {
        try{
            WorkflowModel workflowModel = workflowService.getWorkflowModel(new WorkflowModelKey(workspaceId, workflowModelId));
            return mapper.map(workflowModel, WorkflowModelDTO.class);
        } catch (UserNotFoundException | UserNotActiveException e) {
            LOGGER.log(Level.WARNING, null, e);
            throw new RestApiException(e.toString(), e.getMessage(),Response.Status.FORBIDDEN);
        } catch (WorkspaceNotFoundException | WorkflowModelNotFoundException e) {
            LOGGER.log(Level.WARNING, null, e);
            throw new RestApiException(e.toString(), e.getMessage(),Response.Status.NOT_FOUND);
        }
    }

    @DELETE
    @Path("{workflowModelId}")
    public Response delWorkflowModel(@PathParam("workspaceId") String workspaceId, @PathParam("workflowModelId") String workflowModelId){
        try {
            workflowService.deleteWorkflowModel(new WorkflowModelKey(workspaceId, workflowModelId));
            return Response.status(Response.Status.OK).build();
        } catch (UserNotFoundException | AccessRightException e) {
            LOGGER.log(Level.WARNING, null, e);
            throw new RestApiException(e.toString(), e.getMessage(),Response.Status.FORBIDDEN);
        } catch (WorkspaceNotFoundException | WorkflowModelNotFoundException e) {
            LOGGER.log(Level.WARNING, null, e);
            throw new RestApiException(e.toString(), e.getMessage(),Response.Status.NOT_FOUND);
        }
    }

    @PUT
    @Path("{workflowModelId}")
    @Produces(MediaType.APPLICATION_JSON)
    public WorkflowModelDTO updateWorkflowModelInWorkspace(@PathParam("workspaceId") String workspaceId, @PathParam("workflowModelId") String workflowModelId, WorkflowModelDTO workflowModelDTOToPersist) {
        try {
            workflowService.deleteWorkflowModel(new WorkflowModelKey(workspaceId, workflowModelId));
            return this.createWorkflowModelInWorkspace(workspaceId, workflowModelDTOToPersist);
        } catch (UserNotFoundException | AccessRightException e) {
            LOGGER.log(Level.WARNING, null, e);
            throw new RestApiException(e.toString(), e.getMessage(),Response.Status.FORBIDDEN);
        } catch (WorkspaceNotFoundException | WorkflowModelNotFoundException e) {
            LOGGER.log(Level.WARNING, null, e);
            throw new RestApiException(e.toString(), e.getMessage(),Response.Status.NOT_FOUND);
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public WorkflowModelDTO createWorkflowModelInWorkspace(@PathParam("workspaceId") String workspaceId, WorkflowModelDTO workflowModelDTOToPersist) {
        try {
            Role[] roles = workflowService.getRoles(workspaceId);
            List<ActivityModelDTO> activityModelDTOsList = workflowModelDTOToPersist.getActivityModels();
            ActivityModel[] activityModels = extractActivityModelFromDTO(activityModelDTOsList,roles);

            WorkflowModel workflowModel = workflowService.createWorkflowModel(workspaceId, workflowModelDTOToPersist.getReference(), workflowModelDTOToPersist.getFinalLifeCycleState(), activityModels);
            return mapper.map(workflowModel, WorkflowModelDTO.class);

        } catch (UserNotActiveException | UserNotFoundException | AccessRightException e) {
            LOGGER.log(Level.WARNING, null, e);
            throw new RestApiException(e.toString(), e.getMessage(),Response.Status.FORBIDDEN);
        } catch (WorkspaceNotFoundException e) {
            LOGGER.log(Level.WARNING, null, e);
            throw new RestApiException(e.toString(), e.getMessage(),Response.Status.NOT_FOUND);
        } catch (WorkflowModelAlreadyExistsException e) {
            LOGGER.log(Level.WARNING, null, e);
            throw new RestApiException(e.toString(), e.getMessage(),Response.Status.CONFLICT);
        } catch (CreationException | NotAllowedException e) {
            LOGGER.log(Level.WARNING, null, e);
            throw new RestApiException(e.toString(), e.getMessage(),Response.Status.BAD_REQUEST);
        }
    }

    private ActivityModel[] extractActivityModelFromDTO(List<ActivityModelDTO> activityModelDTOsList, Role[] roles) throws NotAllowedException {
        Map<Integer,ActivityModel> activityModels = new HashMap<>();

        for(int i=0; i<activityModelDTOsList.size(); i++){
            ActivityModelDTO activityModelDTO = activityModelDTOsList.get(i);
            ActivityModel activityModel = mapper.map(activityModelDTO, ActivityModel.class);
            activityModels.put(activityModel.getStep(),activityModel);

            Integer relaunchStep = activityModelDTO.getRelaunchStep();
            if(relaunchStep != null && relaunchStep < i){
                ActivityModel relaunchActivity = activityModels.get(relaunchStep);
                activityModel.setRelaunchActivity(relaunchActivity);
            }

            assignRoleToTasks(activityModel,roles);
        }

        return activityModels.values().toArray(new ActivityModel[activityModels.size()]);
    }

    private void assignRoleToTasks(ActivityModel activityModel,Role[] roles) throws NotAllowedException {
        List<TaskModel> modelTask = activityModel.getTaskModels();
        if(modelTask==null || modelTask.isEmpty()){
            throw new NotAllowedException(Locale.getDefault(),"NotAllowedException3");
        }
        for(TaskModel taskModel : activityModel.getTaskModels()){
            Role modelRole = taskModel.getRole();
            if(modelRole==null){
                throw new NotAllowedException(Locale.getDefault(),"NotAllowedException13");
            }
            String roleName = modelRole.getName();
            for (Role role : roles) {
                if (role.getName().equals(roleName)) {
                    taskModel.setRole(role);
                    break;
                }
            }
        }
    }
    
}