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

import com.docdoku.core.change.ChangeOrder;
import com.docdoku.core.change.ChangeRequest;
import com.docdoku.core.change.Milestone;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IChangeManagerLocal;
import com.docdoku.server.rest.dto.ACLDTO;
import com.docdoku.server.rest.dto.change.ChangeOrderDTO;
import com.docdoku.server.rest.dto.change.ChangeRequestDTO;
import com.docdoku.server.rest.dto.change.MilestoneDTO;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestScoped
@Api(hidden = true, value = "milestones", description = "Operations about milestones")
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class MilestonesResource {

    @Inject
    private IChangeManagerLocal changeManager;

    private Mapper mapper;

    public MilestonesResource() {

    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @ApiOperation(value = "Get milestones for given parameters",
            response = MilestoneDTO.class,
            responseContainer = "List")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMilestones(@PathParam("workspaceId") String workspaceId)
            throws EntityNotFoundException, UserNotActiveException {
        List<Milestone> milestones = changeManager.getMilestones(workspaceId);
        List<MilestoneDTO> milestoneDTOs = new ArrayList<>();
        for (Milestone milestone : milestones) {
            MilestoneDTO milestoneDTO = mapper.map(milestone, MilestoneDTO.class);
            milestoneDTO.setWritable(changeManager.isMilestoneWritable(milestone));
            milestoneDTO.setNumberOfRequests(changeManager.getNumberOfRequestByMilestone(milestone.getWorkspaceId(), milestone.getId()));
            milestoneDTO.setNumberOfOrders(changeManager.getNumberOfOrderByMilestone(milestone.getWorkspaceId(), milestone.getId()));
            milestoneDTOs.add(milestoneDTO);
        }
        return Response.ok(new GenericEntity<List<MilestoneDTO>>((List<MilestoneDTO>) milestoneDTOs) {
        }).build();
    }

    @POST
    @ApiOperation(value = "Create milestone",
            response = MilestoneDTO.class)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public MilestoneDTO createMilestone(@PathParam("workspaceId") String workspaceId,
                                        @ApiParam(required = true, value = "Milestone to create") MilestoneDTO milestoneDTO)
            throws EntityNotFoundException, AccessRightException, EntityAlreadyExistsException {
        Milestone milestone = changeManager.createMilestone(workspaceId, milestoneDTO.getTitle(), milestoneDTO.getDescription(), milestoneDTO.getDueDate());
        milestoneDTO = mapper.map(milestone, MilestoneDTO.class);
        milestoneDTO.setWritable(true);
        return milestoneDTO;
    }

    @GET
    @ApiOperation(value = "Get milestone",
            response = MilestoneDTO.class)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{milestoneId}")
    public MilestoneDTO getMilestone(@PathParam("workspaceId") String workspaceId,
                                     @PathParam("milestoneId") int milestoneId)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        Milestone milestone = changeManager.getMilestone(workspaceId, milestoneId);
        MilestoneDTO milestoneDTO = mapper.map(milestone, MilestoneDTO.class);
        milestoneDTO.setWritable(changeManager.isMilestoneWritable(milestone));
        milestoneDTO.setNumberOfRequests(changeManager.getNumberOfRequestByMilestone(milestone.getWorkspaceId(), milestone.getId()));
        milestoneDTO.setNumberOfOrders(changeManager.getNumberOfOrderByMilestone(milestone.getWorkspaceId(), milestone.getId()));
        return milestoneDTO;
    }

    @PUT
    @ApiOperation(value = "Update milestone",
            response = MilestoneDTO.class)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{milestoneId}")
    public MilestoneDTO updateMilestone(@PathParam("workspaceId") String workspaceId,
                                        @PathParam("milestoneId") int milestoneId,
                                        @ApiParam(required = true, value = "Milestone to update") MilestoneDTO pMilestoneDTO)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        Milestone milestone = changeManager.updateMilestone(milestoneId, workspaceId, pMilestoneDTO.getTitle(), pMilestoneDTO.getDescription(), pMilestoneDTO.getDueDate());
        MilestoneDTO milestoneDTO = mapper.map(milestone, MilestoneDTO.class);
        milestoneDTO.setWritable(changeManager.isMilestoneWritable(milestone));
        milestoneDTO.setNumberOfRequests(changeManager.getNumberOfRequestByMilestone(milestone.getWorkspaceId(), milestone.getId()));
        milestoneDTO.setNumberOfOrders(changeManager.getNumberOfOrderByMilestone(milestone.getWorkspaceId(), milestone.getId()));
        return milestoneDTO;
    }

    @DELETE
    @ApiOperation(value = "Delete milestone",
            response = Response.class)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{milestoneId}")
    public Response removeMilestone(@PathParam("workspaceId") String workspaceId,
                                    @PathParam("milestoneId") int milestoneId)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, EntityConstraintException {
        changeManager.deleteMilestone(workspaceId, milestoneId);
        return Response.ok().build();
    }

    @GET
    @ApiOperation(value = "Get requests for the given milestone",
            response = ChangeRequestDTO.class,
            responseContainer = "List")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{milestoneId}/requests")
    public Response getRequestsByMilestone(@PathParam("workspaceId") String workspaceId,
                                           @PathParam("milestoneId") int milestoneId)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        List<ChangeRequest> changeRequests = changeManager.getChangeRequestsByMilestone(workspaceId, milestoneId);
        List<ChangeRequestDTO> changeRequestDTOs = new ArrayList<>();
        for (ChangeRequest changeRequest : changeRequests) {
            changeRequestDTOs.add(mapper.map(changeRequest, ChangeRequestDTO.class));
        }
        return Response.ok(new GenericEntity<List<ChangeRequestDTO>>((List<ChangeRequestDTO>) changeRequestDTOs) {
        }).build();
    }

    @GET
    @ApiOperation(value = "Get orders for the given milestone",
            response = ChangeOrderDTO.class,
            responseContainer = "List")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{milestoneId}/orders")
    public Response getOrdersByMilestone(@PathParam("workspaceId") String workspaceId,
                                         @PathParam("milestoneId") int milestoneId)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        List<ChangeOrder> changeOrders = changeManager.getChangeOrdersByMilestone(workspaceId, milestoneId);
        List<ChangeOrderDTO> changeOrderDTOs = new ArrayList<>();
        for (ChangeOrder changeOrder : changeOrders) {
            changeOrderDTOs.add(mapper.map(changeOrder, ChangeOrderDTO.class));
        }
        return Response.ok(new GenericEntity<List<ChangeOrderDTO>>((List<ChangeOrderDTO>) changeOrderDTOs) {
        }).build();
    }

    @PUT
    @ApiOperation(value = "Update ACL of the milestone",
            response = Response.class)
    @Path("{milestoneId}/acl")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateMilestoneACL(@PathParam("workspaceId") String pWorkspaceId,
                              @PathParam("milestoneId") int milestoneId,
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

            changeManager.updateACLForMilestone(pWorkspaceId, milestoneId, userEntries, groupEntries);
        } else {
            changeManager.removeACLFromMilestone(pWorkspaceId, milestoneId);
        }
        return Response.ok().build();
    }
}