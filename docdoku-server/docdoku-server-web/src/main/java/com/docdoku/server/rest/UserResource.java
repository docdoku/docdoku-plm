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

import com.docdoku.core.common.User;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.core.workflow.*;
import com.docdoku.server.rest.dto.*;

import javax.annotation.PostConstruct;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.*;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

@Stateless
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class UserResource {

    @EJB
    private IDocumentManagerLocal documentService;
    private Mapper mapper;

    public UserResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    public UserDTO[] getUsersInWorkspace(@PathParam("workspaceId") String workspaceId) {
        try {

            User[] users = documentService.getUsers(workspaceId);
            UserDTO[] dtos = new UserDTO[users.length];

            for(int i=0; i<users.length; i++){
                dtos[i] = mapper.map(users[i], UserDTO.class);
            }

            return dtos;

        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @GET
    @Path("me")
    @Produces("application/json;charset=UTF-8")
    public UserDTO whoami(@PathParam("workspaceId") String workspaceId){
        try {
            User  user = documentService.whoAmI(workspaceId);
            return mapper.map(user, UserDTO.class);
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @GET
    @Path("reachable")
    @Produces("application/json;charset=UTF-8")
    public UserDTO[] getReachableUsersForCaller(@PathParam("workspaceId") String workspaceId) {
        try {

            User[] users = documentService.getReachableUsers();
            UserDTO[] dtos = new UserDTO[users.length];

            for(int i=0; i<users.length; i++){
                dtos[i] = mapper.map(users[i], UserDTO.class);
            }

            return dtos;

        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }
}

