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
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Morgan Guimard
 */
@Stateless
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class RoleResource {

    @EJB
    private IWorkflowManagerLocal roleService;

    private static final Logger LOGGER = Logger.getLogger(RoleResource.class.getName());
    private Mapper mapper;

    public RoleResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    } 

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public RoleDTO[] getRolesInWorkspace (@PathParam("workspaceId") String workspaceId)
            throws EntityNotFoundException, UserNotActiveException {

        Role[] roles = roleService.getRoles(workspaceId);
        RoleDTO[] rolesDTO = new RoleDTO[roles.length];

        for(int i = 0 ; i< roles.length ; i++){
            rolesDTO[i] = mapRoleToDTO(roles[i]);
        }

        return rolesDTO;
    }

    @GET
    @Path("inuse")
    @Produces(MediaType.APPLICATION_JSON)
    public RoleDTO[] getRolesInUseInWorkspace (@PathParam("workspaceId") String workspaceId)
            throws EntityNotFoundException, UserNotActiveException {

        Role[] roles = roleService.getRolesInUse(workspaceId);
        RoleDTO[] rolesDTO = new RoleDTO[roles.length];

        for(int i = 0 ; i< roles.length ; i++){
            rolesDTO[i] = mapRoleToDTO(roles[i]);
        }

        return rolesDTO;
    }


    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createRole(RoleDTO roleDTO)
            throws EntityNotFoundException, EntityAlreadyExistsException, UserNotActiveException, AccessRightException, CreationException {

        UserDTO userDTO = roleDTO.getDefaultAssignee();
        String userLogin = null;
        if(userDTO != null){
            userLogin = userDTO.getLogin();
        }

        Role roleCreated = roleService.createRole(roleDTO.getName(),roleDTO.getWorkspaceId(),userLogin);
        RoleDTO roleCreatedDTO = mapRoleToDTO(roleCreated);

        try{
            return Response.created(URI.create(URLEncoder.encode(roleCreatedDTO.getName(), "UTF-8"))).entity(roleCreatedDTO).build();
        } catch (UnsupportedEncodingException ex) {
            LOGGER.log(Level.WARNING,null,ex);
            return Response.ok().build();
        }
    }

    @PUT
    @Path("{roleName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateRole(@PathParam("roleName") String roleName, RoleDTO roleDTO)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException {

        UserDTO userDTO = roleDTO.getDefaultAssignee();
        String userLogin = null;
        if(userDTO != null){
            userLogin = userDTO.getLogin();
        }

        Role roleUpdated = roleService.updateRole(new RoleKey(roleDTO.getWorkspaceId(), roleName), userLogin);
        RoleDTO roleUpdatedDTO = mapRoleToDTO(roleUpdated);
        try{
            return Response.created(URI.create(URLEncoder.encode(roleUpdatedDTO.getName(), "UTF-8"))).entity(roleUpdatedDTO).build();
        } catch (UnsupportedEncodingException ex) {
            LOGGER.log(Level.WARNING,null,ex);
            return Response.ok().build();
        }
    }

    @DELETE
    @Path("{roleName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteRole(@PathParam("workspaceId") String workspaceId, @PathParam("roleName") String roleName)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, EntityConstraintException {

        RoleKey roleKey = new RoleKey(workspaceId, roleName);
        roleService.deleteRole(roleKey);
        return Response.ok().build();
    }


    private RoleDTO mapRoleToDTO(Role role){
        RoleDTO roleDTO = mapper.map(role,RoleDTO.class);
        roleDTO.setWorkspaceId(role.getWorkspace().getId());
        if(role.getDefaultAssignee() != null){
            roleDTO.setDefaultAssignee(mapper.map(role.getDefaultAssignee(), UserDTO.class));
        }
        roleDTO.setId(role.getName());

        return roleDTO;
    }

}