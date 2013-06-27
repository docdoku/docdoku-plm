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
import javax.ws.rs.core.Response;
import java.util.List;

/**
 *
 * @author Yassine Belouad
 */
@Stateless
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class WorkflowResource {

    @EJB
    private IDocumentManagerLocal documentService;

    @EJB
    private IWorkflowManagerLocal workflowService;

    @EJB
    private IWorkflowManagerLocal roleService;

    private Mapper mapper;

    public WorkflowResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }
    
    @GET
    @Produces("application/json;charset=UTF-8")
    public WorkflowModelDTO[] getWorkflowsInWorkspace(@PathParam("workspaceId") String workspaceId) {
        try {

            WorkflowModel[] workflowModels = workflowService.getWorkflowModels(workspaceId);
            WorkflowModelDTO[] dtos = new WorkflowModelDTO[workflowModels.length];
            
            for(int i=0; i<workflowModels.length; i++){
                dtos[i] = mapper.map(workflowModels[i], WorkflowModelDTO.class);
            }
            
            return dtos;
            
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("{workflowModelId}")
    public WorkflowModelDTO getWorkflowInWorkspace(@PathParam("workspaceId") String workspaceId, @PathParam("workflowModelId") String workflowModelId) {
        try{
            WorkflowModel workflowModel = workflowService.getWorkflowModel(new WorkflowModelKey(workspaceId, workflowModelId));
            WorkflowModelDTO workflowModelDTO = mapper.map(workflowModel, WorkflowModelDTO.class);
            return workflowModelDTO;
        }  catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @DELETE
    @Path("{workflowModelId}")
    public Response delWorkflowModel(@PathParam("workspaceId") String workspaceId, @PathParam("workflowModelId") String workflowModelId){
        try {
            workflowService.deleteWorkflowModel(new WorkflowModelKey(workspaceId, workflowModelId));
            return Response.status(Response.Status.OK).build();
        }  catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @PUT
    @Produces("application/json;charset=UTF-8")
    @Path("{workflowModelId}")
    public WorkflowModelDTO updateWorkflowModelInWorkspace(@PathParam("workspaceId") String workspaceId, @PathParam("workflowModelId") String workflowModelId, WorkflowModelDTO workflowModelDTOToPersist) {
        try {

            workflowService.deleteWorkflowModel(new WorkflowModelKey(workspaceId, workflowModelId));

            return this.createWorkflowModelInWorkspace(workspaceId, workflowModelDTOToPersist);

        } catch (com.docdoku.core.services.ApplicationException ex) {
            ex.printStackTrace();
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @POST
    @Produces("application/json;charset=UTF-8")
    public WorkflowModelDTO createWorkflowModelInWorkspace(@PathParam("workspaceId") String workspaceId, WorkflowModelDTO workflowModelDTOToPersist) {
        try {

            Role[] roles = roleService.getRoles(workspaceId);

            List<ActivityModelDTO> activityModelDTOsList = workflowModelDTOToPersist.getActivityModels();

            ActivityModel[] activityModels = new ActivityModel[activityModelDTOsList.size()];
            for(int i=0; i<activityModelDTOsList.size(); i++){
                activityModels[i] = mapper.map(activityModelDTOsList.get(i), ActivityModel.class);

                if(activityModelDTOsList.get(i).getRelaunchStep() != null && activityModelDTOsList.get(i).getRelaunchStep() < i){
                    activityModels[i].setRelaunchActivity(activityModels[activityModelDTOsList.get(i).getRelaunchStep()]);
                }

                List<TaskModel> taskModelList = activityModels[i].getTaskModels();
                for(TaskModel taskModel : taskModelList){
                    String roleName = taskModel.getRole().getName();
                    for(int j=0; j<roles.length; j++){
                        if(roles[j].getName().equals(roleName)){
                            taskModel.setRole(roles[j]);
                            break;
                        }
                    }
                }
            }

            WorkflowModel workflowModel = workflowService.createWorkflowModel(workspaceId, workflowModelDTOToPersist.getReference(), workflowModelDTOToPersist.getFinalLifeCycleState(), activityModels);

            WorkflowModelDTO dto = mapper.map(workflowModel, WorkflowModelDTO.class);
            return dto;

        } catch (com.docdoku.core.services.ApplicationException ex) {
            ex.printStackTrace();
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }
    
}
