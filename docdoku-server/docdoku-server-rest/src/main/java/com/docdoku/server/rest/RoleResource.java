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
import com.docdoku.core.workflow.Role;
import com.docdoku.core.workflow.RoleKey;
import com.docdoku.server.rest.dto.RoleDTO;
import com.docdoku.server.rest.dto.UserDTO;
import com.docdoku.server.rest.dto.UserGroupDTO;
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
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Morgan Guimard
 */
@RequestScoped
@Api(hidden=true, value = "roles", description = "Operations about roles")
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class RoleResource {

    private static final Logger LOGGER = Logger.getLogger(RoleResource.class.getName());

    @Inject
    private IWorkflowManagerLocal roleService;

    private Mapper mapper;

    public RoleResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @ApiOperation(hidden = false, value = "Get roles", response = RoleDTO.class, responseContainer = "List")
    @Produces(MediaType.APPLICATION_JSON)
    public RoleDTO[] getRolesInWorkspace(@PathParam("workspaceId") String workspaceId)
            throws EntityNotFoundException, UserNotActiveException {

        Role[] roles = roleService.getRoles(workspaceId);
        RoleDTO[] rolesDTO = new RoleDTO[roles.length];

        for (int i = 0; i < roles.length; i++) {
            rolesDTO[i] = mapRoleToDTO(roles[i]);
        }

        // TODO: return Response instead of RoleDTO[]
        return rolesDTO;
    }

    @GET
    @ApiOperation(value = "Get roles in use", response = RoleDTO.class, responseContainer = "List")
    @Path("inuse")
    @Produces(MediaType.APPLICATION_JSON)
    public RoleDTO[] getRolesInUseInWorkspace(@PathParam("workspaceId") String workspaceId)
            throws EntityNotFoundException, UserNotActiveException {

        Role[] roles = roleService.getRolesInUse(workspaceId);
        RoleDTO[] rolesDTO = new RoleDTO[roles.length];

        for (int i = 0; i < roles.length; i++) {
            rolesDTO[i] = mapRoleToDTO(roles[i]);
        }

        // TODO: return Response instead of RoleDTO[]
        return rolesDTO;
    }


    @POST
    @ApiOperation(value = "Create role", response = RoleDTO.class)
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createRole(
            @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Role to create") RoleDTO roleDTO)
            throws EntityNotFoundException, EntityAlreadyExistsException, UserNotActiveException, AccessRightException, CreationException {

        List<UserDTO> userDTOs = roleDTO.getDefaultAssignedUsers();
        List<UserGroupDTO> groupDTOs = roleDTO.getDefaultAssignedGroups();
        List<String> userLogins = new ArrayList<>();
        List<String> userGroupIds = new ArrayList<>();
        if (userDTOs != null) {
           for(UserDTO userDTO:userDTOs){
               userLogins.add(userDTO.getLogin());
           }
        }
        if (groupDTOs != null) {
            for(UserGroupDTO groupDTO:groupDTOs){
                userGroupIds.add(groupDTO.getId());
            }
        }

        Role roleCreated = roleService.createRole(roleDTO.getName(), roleDTO.getWorkspaceId(), userLogins, userGroupIds);
        RoleDTO roleCreatedDTO = mapRoleToDTO(roleCreated);

        try {
            return Response.created(URI.create(URLEncoder.encode(roleCreatedDTO.getName(), "UTF-8"))).entity(roleCreatedDTO).build();
        } catch (UnsupportedEncodingException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            return Response.ok().build();
        }
    }

    @PUT
    @ApiOperation(value = "Update role", response = RoleDTO.class)
    @Path("{roleName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateRole(@PathParam("roleName") String roleName,
                               @ApiParam(required = true, value = "Role to update") RoleDTO roleDTO)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException {

        List<UserDTO> userDTOs = roleDTO.getDefaultAssignedUsers();
        List<UserGroupDTO> groupDTOs = roleDTO.getDefaultAssignedGroups();
        List<String> userLogins = new ArrayList<>();
        List<String> userGroupIds = new ArrayList<>();
        if (userDTOs != null) {
            for(UserDTO userDTO:userDTOs){
                userLogins.add(userDTO.getLogin());
            }
        }
        if (groupDTOs != null) {
            for(UserGroupDTO groupDTO:groupDTOs){
                userGroupIds.add(groupDTO.getId());
            }
        }

        Role roleUpdated = roleService.updateRole(new RoleKey(roleDTO.getWorkspaceId(), roleName), userLogins, userGroupIds);
        RoleDTO roleUpdatedDTO = mapRoleToDTO(roleUpdated);
        try {
            return Response.created(URI.create(URLEncoder.encode(roleUpdatedDTO.getName(), "UTF-8"))).entity(roleUpdatedDTO).build();
        } catch (UnsupportedEncodingException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            return Response.ok().build();
        }
    }

    @DELETE
    @ApiOperation(value = "Delete role", response = Response.class)
    @Path("{roleName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteRole(@PathParam("workspaceId") String workspaceId,
                               @PathParam("roleName") String roleName)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, EntityConstraintException {

        RoleKey roleKey = new RoleKey(workspaceId, roleName);
        roleService.deleteRole(roleKey);
        return Response.ok().build();
    }


    private RoleDTO mapRoleToDTO(Role role) {
        RoleDTO roleDTO = mapper.map(role, RoleDTO.class);
        /*roleDTO.setWorkspaceId(role.getWorkspace().getId());
        if (role.getDefaultAssignee() != null) {
            roleDTO.setDefaultAssignee(mapper.map(role.getDefaultAssignee(), UserDTO.class));
        }
        roleDTO.setId(role.getName());*/

        return roleDTO;
    }

}