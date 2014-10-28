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

import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.server.rest.dto.UserDTO;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

import javax.annotation.PostConstruct;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class UserResource {

    @EJB
    private IDocumentManagerLocal documentService;
    @EJB
    private IUserManagerLocal userManager;

    private Mapper mapper;
    private static final Logger LOGGER = Logger.getLogger(UserResource.class.getName());

    public UserResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public UserDTO[] getUsersInWorkspace(@PathParam("workspaceId") String workspaceId) {
        try {

            User[] users = documentService.getUsers(workspaceId);
            UserDTO[] dtos = new UserDTO[users.length];

            for(int i=0; i<users.length; i++){
                dtos[i] = mapper.map(users[i], UserDTO.class);
            }

            return dtos;
        } catch (UserNotFoundException | UserNotActiveException | AccessRightException e) {
            LOGGER.log(Level.WARNING, null, e);
            throw new RestApiException(e.toString(), e.getMessage(), Response.Status.FORBIDDEN);
        } catch (WorkspaceNotFoundException e) {
            LOGGER.log(Level.WARNING, null, e);
            throw new RestApiException(e.toString(), e.getMessage(),Response.Status.NOT_FOUND);
        } catch (AccountNotFoundException e){
            LOGGER.log(Level.WARNING, null, e);
            throw new RestApiException(e.toString(), e.getMessage(),Response.Status.BAD_REQUEST);
        }
    }

    @GET
    @Path("me")
    @Produces(MediaType.APPLICATION_JSON)
    public UserDTO whoami(@PathParam("workspaceId") String workspaceId){
        try {
            User  user = userManager.whoAmI(workspaceId);
            return mapper.map(user, UserDTO.class);
        } catch (UserNotFoundException | UserNotActiveException e) {
            LOGGER.log(Level.WARNING, null, e);
            throw new RestApiException(e.toString(), e.getMessage(),Response.Status.FORBIDDEN);
        } catch (WorkspaceNotFoundException e) {
            LOGGER.log(Level.WARNING, null, e);
            throw new RestApiException(e.toString(), e.getMessage(),Response.Status.NOT_FOUND);
        }
    }

    @GET
    @Path("reachable")
    @Produces(MediaType.APPLICATION_JSON)
    public UserDTO[] getReachableUsersForCaller(@PathParam("workspaceId") String workspaceId) {
        try {
            User[] users = documentService.getReachableUsers(workspaceId);
            UserDTO[] dtos = new UserDTO[users.length];

            for(int i=0; i<users.length; i++){
                dtos[i] = mapper.map(users[i], UserDTO.class);
            }

            return dtos;
        } catch (AccountNotFoundException e) {
            LOGGER.log(Level.WARNING, null, e);
            throw new RestApiException(e.toString(), e.getMessage(),Response.Status.BAD_REQUEST);
        }
    }

    @GET
    @Path("admin")
    @Produces(MediaType.APPLICATION_JSON)
    public UserDTO getAdminInWorkspace(@PathParam("workspaceId") String workspaceId){
        try {
            Workspace workspace = userManager.getWorkspace(workspaceId);
            return mapper.map(workspace.getAdmin(),UserDTO.class);
        } catch (WorkspaceNotFoundException e) {
            LOGGER.log(Level.WARNING, null, e);
            throw new RestApiException(e.toString(), e.getMessage(),Response.Status.NOT_FOUND);
        }
    }
}

