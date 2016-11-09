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

import com.docdoku.core.common.User;
import com.docdoku.core.common.UserGroup;
import com.docdoku.core.common.UserGroupKey;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.notification.TagUserGroupSubscription;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.INotificationManagerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.server.rest.dto.TagSubscriptionDTO;
import com.docdoku.server.rest.dto.UserDTO;
import com.docdoku.server.rest.dto.UserGroupDTO;
import io.swagger.annotations.*;
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
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@RequestScoped
@Api(hidden = true, value = "groups", description = "Operations about user groups")
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class UserGroupResource {


    @Inject
    private IUserManagerLocal userManager;

    @Inject
    private INotificationManagerLocal notificationManager;

    private Mapper mapper;

    private static final Logger LOGGER = Logger.getLogger(UserGroupResource.class.getName());

    public UserGroupResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }


    @GET
    @ApiOperation(value = "Get groups",
            response = UserGroupDTO.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of UserGroupDTOs. It can be an empty list."),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public UserGroupDTO[] getGroups(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId)
            throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException, UserNotFoundException,
            UserNotActiveException, WorkspaceNotEnabledException {

        UserGroup[] userGroups = userManager.getUserGroups(workspaceId);
        UserGroupDTO[] userGroupDTOs = new UserGroupDTO[userGroups.length];
        for (int i = 0; i < userGroups.length; i++) {
            userGroupDTOs[i] = mapper.map(userGroups[i], UserGroupDTO.class);
        }
        return userGroupDTOs;
    }

    @GET
    @ApiOperation(value = "Get tag subscriptions of group",
            response = TagSubscriptionDTO.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of TagSubscriptionDTOs. It can be an empty list."),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{groupId}/tag-subscriptions")
    @Produces(MediaType.APPLICATION_JSON)
    public TagSubscriptionDTO[] getTagSubscriptionsForGroup(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Group id") @PathParam("groupId") String groupId)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, AccessRightException,
            UserGroupNotFoundException, WorkspaceNotEnabledException {

        List<TagUserGroupSubscription> subs = notificationManager.getTagUserGroupSubscriptionsByGroup(workspaceId, groupId);

        TagSubscriptionDTO[] subDTOs = new TagSubscriptionDTO[subs.size()];
        for (int i = 0; i < subs.size(); i++) {
            subDTOs[i] = mapper.map(subs.get(i), TagSubscriptionDTO.class);
        }
        return subDTOs;
    }

    @GET
    @ApiOperation(value = "Get users of group",
            response = UserDTO.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of UserDTOs. It can be an empty list."),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{groupId}/users")
    @Produces(MediaType.APPLICATION_JSON)
    public UserDTO[] getUsersInGroup(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Group id") @PathParam("groupId") String groupId)
            throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException,
            UserNotFoundException, UserNotActiveException, UserGroupNotFoundException,
            WorkspaceNotEnabledException {

        UserGroup userGroup = userManager.getUserGroup(new UserGroupKey(workspaceId, groupId));
        Set<User> users = userGroup.getUsers();
        User[] usersArray = users.toArray(new User[users.size()]);
        UserDTO[] userDTOs = new UserDTO[users.size()];
        for (int i = 0; i < usersArray.length; i++) {
            userDTOs[i] = mapper.map(usersArray[i], UserDTO.class);
        }
        return userDTOs;
    }


    @PUT
    @ApiOperation(value = "Update or create tag subscription of group",
            response = TagSubscriptionDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful retrieval of TagSubscriptionDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{groupId}/tag-subscriptions/{tagName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateSubscription(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Group id") @PathParam("groupId") String groupId,
            @ApiParam(required = true, value = "Tag name") @PathParam("tagName") String tagName,
            @ApiParam(required = true, value = "Tag subscription to update or create") TagSubscriptionDTO subDTO)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException {

        notificationManager.createOrUpdateTagUserGroupSubscription(workspaceId,
                groupId,
                tagName,
                subDTO.isOnIterationChange(),
                subDTO.isOnStateChange());
        subDTO.setTag(tagName);
        try {
            return Response.created(URI.create(URLEncoder.encode(tagName, "UTF-8"))).entity(subDTO).build();
        } catch (UnsupportedEncodingException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            return Response.ok().entity(subDTO).build();
        }
    }

    @DELETE
    @ApiOperation(value = "Delete tag subscription of group",
            response = Response.class)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successful deletion of TagSubscriptionDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{groupId}/tag-subscriptions/{tagName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteSubscription(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Group id id") @PathParam("groupId") String groupId,
            @ApiParam(required = true, value = "Tag name") @PathParam("tagName") String tagName)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException,
            AccessRightException, WorkspaceNotEnabledException {

        notificationManager.removeTagUserGroupSubscription(workspaceId, groupId, tagName);
        return Response.noContent().build();
    }

}

