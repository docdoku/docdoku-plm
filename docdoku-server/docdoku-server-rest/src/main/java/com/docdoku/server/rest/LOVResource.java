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
import com.docdoku.core.meta.ListOfValues;
import com.docdoku.core.meta.ListOfValuesKey;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.ILOVManagerLocal;
import com.docdoku.server.rest.dto.ListOfValuesDTO;
import io.swagger.annotations.*;
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
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lebeaujulien on 03/03/15.
 */

@RequestScoped
@Api(hidden = true, value = "listOfValues", description = "Operations about ListOfValues")
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class LOVResource {

    @Inject
    private ILOVManagerLocal lovManager;

    private Mapper mapper;

    public LOVResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @ApiOperation(value = "Get a list of  ListOfValues for given parameters",
            response = ListOfValuesDTO.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of ListOfValuesDTOs. It can be an empty list."),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLOVs(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId)
            throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {

        List<ListOfValuesDTO> lovsDTO = new ArrayList<>();
        List<ListOfValues> lovs = lovManager.findLOVFromWorkspace(workspaceId);

        for (ListOfValues lov : lovs) {
            ListOfValuesDTO lovDTO = mapper.map(lov, ListOfValuesDTO.class);
            lovDTO.setDeletable(lovManager.isLOVDeletable(new ListOfValuesKey(lov.getWorkspaceId(), lov.getName())));
            lovsDTO.add(lovDTO);
        }
        return Response.ok(new GenericEntity<List<ListOfValuesDTO>>((List<ListOfValuesDTO>) lovsDTO) {
        }).build();
    }

    @POST
    @ApiOperation(value = "Create ListOfValues",
            response = ListOfValuesDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful retrieval of created ListOfValuesDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createLOV(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "LOV to create") ListOfValuesDTO lovDTO)
            throws ListOfValuesAlreadyExistsException, CreationException, UnsupportedEncodingException,
            UserNotFoundException, AccessRightException, UserNotActiveException, WorkspaceNotFoundException,
            WorkspaceNotEnabledException {

        ListOfValues lov = mapper.map(lovDTO, ListOfValues.class);
        lovManager.createLov(workspaceId, lov.getName(), lov.getValues());
        return Response.created(URI.create(URLEncoder.encode(lov.getName(), "UTF-8"))).entity(lovDTO).build();
    }

    @GET
    @ApiOperation(value = "Get the ListOfValues from the given parameters",
            response = ListOfValuesDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful retrieval of ListOfValuesDTOs. It can be an empty list."),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("/{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ListOfValuesDTO getLOV(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Name") @PathParam("name") String name)
            throws ListOfValuesNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {

        ListOfValuesKey lovKey = new ListOfValuesKey(workspaceId, name);
        ListOfValues lov = lovManager.findLov(lovKey);
        return mapper.map(lov, ListOfValuesDTO.class);
    }

    @PUT
    @Path("/{name}")
    @ApiOperation(value = "Update the ListOfValues",
            response = ListOfValuesDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful retrieval of updated ListOfValuesDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ListOfValuesDTO updateLOV(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Name") @PathParam("name") String name,
            @ApiParam(required = true, value = "LOV to update") ListOfValuesDTO lovDTO)
            throws ListOfValuesNotFoundException, ListOfValuesAlreadyExistsException, CreationException, UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, AccessRightException, WorkspaceNotEnabledException {

        ListOfValuesKey lovKey = new ListOfValuesKey(workspaceId, name);
        ListOfValues lov = mapper.map(lovDTO, ListOfValues.class);

        ListOfValues updatedLOV = lovManager.updateLov(lovKey, lov.getName(), workspaceId, lov.getValues());
        return mapper.map(updatedLOV, ListOfValuesDTO.class);
    }

    @DELETE
    @Path("/{name}")
    @ApiOperation(value = "Delete the ListOfValues",
            response = Response.class)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful deletion of ListOfValuesDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteLOV(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Name") @PathParam("name") String name)
            throws ListOfValuesNotFoundException, UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, AccessRightException, EntityConstraintException, WorkspaceNotEnabledException {
        ListOfValuesKey lovKey = new ListOfValuesKey(workspaceId, name);
        lovManager.deleteLov(lovKey);
        return Response.ok().build();
    }
}
