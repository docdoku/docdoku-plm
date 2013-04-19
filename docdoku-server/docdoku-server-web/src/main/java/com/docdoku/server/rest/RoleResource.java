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
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

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

    private Mapper mapper;

    public RoleResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    } 

    @GET
    @Produces("application/json;charset=UTF-8")
    public RoleDTO[] getRolesInWorkspace (@PathParam("workspaceId") String workspaceId){

        try {
            Role[] roles = roleService.getRoles(workspaceId);
            RoleDTO[] rolesDTO = new RoleDTO[roles.length];

            for(int i = 0 ; i< roles.length ; i++){
                rolesDTO[i] = mapRoleToDTO(roles[i]);
            }

            return rolesDTO;

        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }

    }

    @GET
    @Path("inuse")
    @Produces("application/json;charset=UTF-8")
    public RoleDTO[] getRolesInUseInWorkspace (@PathParam("workspaceId") String workspaceId){

        try {
            Role[] roles = roleService.getRolesInUse(workspaceId);
            RoleDTO[] rolesDTO = new RoleDTO[roles.length];

            for(int i = 0 ; i< roles.length ; i++){
                rolesDTO[i] = mapRoleToDTO(roles[i]);
            }

            return rolesDTO;

        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }

    }


    @POST
    @Produces("application/json;charset=UTF-8")
    @Consumes("application/json;charset=UTF-8")
    public Response createRole(RoleDTO roleDTO) throws UnsupportedEncodingException {
        try {

            UserDTO userDTO = roleDTO.getDefaultUserMapped();
            String userLogin = null;
            if(userDTO != null){
                userLogin = userDTO.getLogin();
            }

            Role roleCreated = roleService.createRole(roleDTO.getName(),roleDTO.getWorkspaceId(),userLogin);
            RoleDTO roleCreatedDTO = mapRoleToDTO(roleCreated);
            return Response.created(URI.create(URLEncoder.encode(roleCreatedDTO.getName(), "UTF-8"))).entity(roleCreatedDTO).build();
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @PUT
    @Produces("application/json;charset=UTF-8")
    @Consumes("application/json;charset=UTF-8")
    @Path("{roleName}")
    public Response updateRole(@PathParam("roleName") String roleName, RoleDTO roleDTO) throws UnsupportedEncodingException {
        try {

            UserDTO userDTO = roleDTO.getDefaultUserMapped();
            String userLogin = null;
            if(userDTO != null){
                userLogin = userDTO.getLogin();
            }

            Role roleUpdated = roleService.updateRole(new RoleKey(roleDTO.getWorkspaceId(), roleName), userLogin);
            RoleDTO roleUpdatedDTO = mapRoleToDTO(roleUpdated);
            return Response.status(Response.Status.OK).entity(roleUpdatedDTO).build();
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @DELETE
    @Path("{roleName}")
    @Produces("application/json;charset=UTF-8")
    public Response deleteRole(@PathParam("workspaceId") String workspaceId, @PathParam("roleName") String roleName) {
        try {
            RoleKey roleKey = new RoleKey(workspaceId, roleName);
            roleService.deleteRole(roleKey);
            return Response.ok().build();
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }


    private RoleDTO mapRoleToDTO(Role role){

        RoleDTO roleDTO = mapper.map(role,RoleDTO.class);
        roleDTO.setWorkspaceId(role.getWorkspace().getId());
        if(role.getDefaultUserMapped() != null){
            roleDTO.setDefaultUserMapped(mapper.map(role.getDefaultUserMapped(), UserDTO.class));
        }
        roleDTO.setId(role.getName());

        return roleDTO;
    }

}
