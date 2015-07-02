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
import com.docdoku.server.rest.dto.change.ChangeMilestoneDTO;
import com.docdoku.server.rest.dto.change.ChangeOrderDTO;
import com.docdoku.server.rest.dto.change.ChangeRequestDTO;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Stateless
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class ChangeMilestonesResource {

    @EJB
    private IChangeManagerLocal changeManager;

    private Mapper mapper;

    public ChangeMilestonesResource() {

    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ChangeMilestoneDTO> getMilestones(@PathParam("workspaceId") String workspaceId)
            throws EntityNotFoundException, UserNotActiveException {
        List<Milestone> changeMilestones = changeManager.getChangeMilestones(workspaceId);
        List<ChangeMilestoneDTO> changeMilestoneDTOs = new ArrayList<>();
        for(Milestone milestone : changeMilestones){
            ChangeMilestoneDTO changeMilestoneDTO= mapper.map(milestone, ChangeMilestoneDTO.class);
            changeMilestoneDTO.setWritable(changeManager.isMilestoneWritable(milestone));
            changeMilestoneDTO.setNumberOfRequests(changeManager.getNumberOfRequestByMilestone(milestone.getWorkspaceId(),milestone.getId()));
            changeMilestoneDTO.setNumberOfOrders(changeManager.getNumberOfOrderByMilestone(milestone.getWorkspaceId(),milestone.getId()));
            changeMilestoneDTOs.add(changeMilestoneDTO);
        }
        return changeMilestoneDTOs;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeMilestoneDTO createMilestone(@PathParam("workspaceId") String workspaceId, ChangeMilestoneDTO changeMilestoneDTO)
            throws EntityNotFoundException, AccessRightException, EntityAlreadyExistsException {
        Milestone changeMilestone = changeManager.createChangeMilestone(workspaceId, changeMilestoneDTO.getTitle(), changeMilestoneDTO.getDescription(), changeMilestoneDTO.getDueDate());
        ChangeMilestoneDTO ret = mapper.map(changeMilestone, ChangeMilestoneDTO.class);
        ret.setWritable(true);
        return ret;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{milestoneId}")
    public ChangeMilestoneDTO getMilestone(@PathParam("workspaceId") String workspaceId, @PathParam("milestoneId") int milestoneId)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        Milestone changeMilestone = changeManager.getChangeMilestone(workspaceId, milestoneId);
        ChangeMilestoneDTO changeMilestoneDTO= mapper.map(changeMilestone, ChangeMilestoneDTO.class);
        changeMilestoneDTO.setWritable(changeManager.isMilestoneWritable(changeMilestone));
        changeMilestoneDTO.setNumberOfRequests(changeManager.getNumberOfRequestByMilestone(changeMilestone.getWorkspaceId(),changeMilestone.getId()));
        changeMilestoneDTO.setNumberOfOrders(changeManager.getNumberOfOrderByMilestone(changeMilestone.getWorkspaceId(),changeMilestone.getId()));
        return changeMilestoneDTO;
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{milestoneId}")
    public ChangeMilestoneDTO updateMilestone(@PathParam("workspaceId") String workspaceId, @PathParam("milestoneId") int milestoneId, ChangeMilestoneDTO pChangeMilestoneDTO)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        Milestone changeMilestone = changeManager.updateChangeMilestone(milestoneId, workspaceId, pChangeMilestoneDTO.getTitle(), pChangeMilestoneDTO.getDescription(), pChangeMilestoneDTO.getDueDate());
        ChangeMilestoneDTO changeMilestoneDTO= mapper.map(changeMilestone, ChangeMilestoneDTO.class);
        changeMilestoneDTO.setWritable(changeManager.isMilestoneWritable(changeMilestone));
        changeMilestoneDTO.setNumberOfRequests(changeManager.getNumberOfRequestByMilestone(changeMilestone.getWorkspaceId(),changeMilestone.getId()));
        changeMilestoneDTO.setNumberOfOrders(changeManager.getNumberOfOrderByMilestone(changeMilestone.getWorkspaceId(),changeMilestone.getId()));
        return changeMilestoneDTO;
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{milestoneId}")
    public Response removeMilestone(@PathParam("workspaceId") String workspaceId, @PathParam("milestoneId") int milestoneId)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, EntityConstraintException {
        changeManager.deleteChangeMilestone(workspaceId,milestoneId);
        return Response.ok().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{milestoneId}/requests")
    public List<ChangeRequestDTO> getRequestsByMilestone(@PathParam("workspaceId") String workspaceId, @PathParam("milestoneId") int milestoneId)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        List<ChangeRequest> changeRequests = changeManager.getChangeRequestsByMilestone(workspaceId, milestoneId);
        List<ChangeRequestDTO> changeRequestDTOs = new ArrayList<>();
        for(ChangeRequest changeRequest : changeRequests){
            changeRequestDTOs.add(mapper.map(changeRequest, ChangeRequestDTO.class));
        }
        return changeRequestDTOs;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{milestoneId}/orders")
    public List<ChangeOrderDTO> getOrdersByMilestone(@PathParam("workspaceId") String workspaceId, @PathParam("milestoneId") int milestoneId)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        List<ChangeOrder> changeOrders = changeManager.getChangeOrdersByMilestone(workspaceId, milestoneId);
        List<ChangeOrderDTO> changeOrderDTOs = new ArrayList<>();
        for(ChangeOrder changeOrder : changeOrders){
            changeOrderDTOs.add(mapper.map(changeOrder, ChangeOrderDTO.class));
        }
        return changeOrderDTOs;
    }

    @PUT
    @Path("{milestoneId}/acl")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateACL(@PathParam("workspaceId") String pWorkspaceId, @PathParam("milestoneId") int milestoneId, ACLDTO acl)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        if (!acl.getGroupEntries().isEmpty() || !acl.getUserEntries().isEmpty()) {

            Map<String,String> userEntries = new HashMap<>();
            Map<String,String> groupEntries = new HashMap<>();

            for (Map.Entry<String, ACL.Permission> entry : acl.getUserEntries().entrySet()) {
                userEntries.put(entry.getKey(), entry.getValue().name());
            }

            for (Map.Entry<String, ACL.Permission> entry : acl.getGroupEntries().entrySet()) {
                groupEntries.put(entry.getKey(), entry.getValue().name());
            }

            changeManager.updateACLForMilestone(pWorkspaceId, milestoneId, userEntries, groupEntries);
        }else{
            changeManager.removeACLFromMilestone(pWorkspaceId, milestoneId);
        }
        return Response.ok().build();
    }
}