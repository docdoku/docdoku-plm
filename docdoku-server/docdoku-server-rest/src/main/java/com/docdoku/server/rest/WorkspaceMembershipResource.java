/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
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

import com.docdoku.core.exceptions.EntityNotFoundException;
import com.docdoku.core.exceptions.UserNotActiveException;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.security.WorkspaceUserGroupMembership;
import com.docdoku.core.security.WorkspaceUserMembership;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.server.rest.dto.WorkspaceUserGroupMemberShipDTO;
import com.docdoku.server.rest.dto.WorkspaceUserMemberShipDTO;
import io.swagger.annotations.*;
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
import java.util.List;

/**
 * @author Morgan Guimard
 */
@RequestScoped
@Api(hidden = true, value = "workspaceMemberships", description = "Operations about workspace memberships")
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class WorkspaceMembershipResource {

    @Inject
    private IUserManagerLocal userManager;

    private Mapper mapper;

    public WorkspaceMembershipResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @ApiOperation(value = "Get workspace's user memberships",
            response = WorkspaceUserMemberShipDTO.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of WorkspaceUserMemberShipDTOs. It can be an empty list."),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("users")
    @Produces(MediaType.APPLICATION_JSON)
    public WorkspaceUserMemberShipDTO[] getWorkspaceUserMemberShips(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId)
            throws EntityNotFoundException, UserNotActiveException {

        WorkspaceUserMembership[] workspaceUserMemberships = userManager.getWorkspaceUserMemberships(workspaceId);
        WorkspaceUserMemberShipDTO[] workspaceUserMemberShipDTO = new WorkspaceUserMemberShipDTO[workspaceUserMemberships.length];
        for (int i = 0; i < workspaceUserMemberships.length; i++) {
            workspaceUserMemberShipDTO[i] = mapper.map(workspaceUserMemberships[i], WorkspaceUserMemberShipDTO.class);
        }
        return workspaceUserMemberShipDTO;
    }

    @GET
    @ApiOperation(value = "Get workspace's user membership for current user",
            response = WorkspaceUserMemberShipDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of WorkspaceUserMemberShipDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("users/me")
    @Produces(MediaType.APPLICATION_JSON)
    public WorkspaceUserMemberShipDTO getWorkspaceSpecificUserMemberShips(@ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId)
            throws EntityNotFoundException, UserNotActiveException {

        WorkspaceUserMembership workspaceUserMemberships = userManager.getWorkspaceSpecificUserMemberships(workspaceId);
        return mapper.map(workspaceUserMemberships, WorkspaceUserMemberShipDTO.class);
    }

    @GET
    @ApiOperation(value = "Get workspace's group membership for current user",
            response = WorkspaceUserGroupMemberShipDTO.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of WorkspaceUserGroupMemberShipDTOs. It can be an empty list."),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("usergroups")
    @Produces(MediaType.APPLICATION_JSON)
    public WorkspaceUserGroupMemberShipDTO[] getWorkspaceUserGroupMemberShips(
            @ApiParam(required = true, value = "Workspace id")  @PathParam("workspaceId") String workspaceId)
            throws EntityNotFoundException, UserNotActiveException {

        WorkspaceUserGroupMembership[] workspaceUserGroupMemberships = userManager.getWorkspaceUserGroupMemberships(workspaceId);
        WorkspaceUserGroupMemberShipDTO[] workspaceUserGroupMemberShipDTO = new WorkspaceUserGroupMemberShipDTO[workspaceUserGroupMemberships.length];
        for (int i = 0; i < workspaceUserGroupMemberships.length; i++) {
            workspaceUserGroupMemberShipDTO[i] = mapper.map(workspaceUserGroupMemberships[i], WorkspaceUserGroupMemberShipDTO.class);
        }
        return workspaceUserGroupMemberShipDTO;
    }

    @GET
    @ApiOperation(value = "Get workspace's group membership for current user",
            response = WorkspaceUserGroupMemberShipDTO.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of WorkspaceUserGroupMemberShipDTOs. It can be an empty list."),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("usergroups/me")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWorkspaceSpecificUserGroupMemberShips(
            @ApiParam(required = true, value = "Workspace id")  @PathParam("workspaceId") String workspaceId)
            throws EntityNotFoundException, UserNotActiveException {

        WorkspaceUserGroupMembership[] workspaceUserGroupMemberships = userManager.getWorkspaceSpecificUserGroupMemberships(workspaceId);
        List<WorkspaceUserGroupMemberShipDTO> workspaceUserGroupMemberShipDTO = new ArrayList<>();
        for (WorkspaceUserGroupMembership workspaceUserGroupMembership : workspaceUserGroupMemberships) {
            if (workspaceUserGroupMembership != null) {
                workspaceUserGroupMemberShipDTO.add(mapper.map(workspaceUserGroupMembership, WorkspaceUserGroupMemberShipDTO.class));
            }
        }

        return Response.ok(new GenericEntity<List<WorkspaceUserGroupMemberShipDTO>>((List<WorkspaceUserGroupMemberShipDTO>) workspaceUserGroupMemberShipDTO) {
        }).build();
    }

}
