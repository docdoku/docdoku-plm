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
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLOVs(@PathParam("workspaceId") String workspaceId)
            throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
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
            response = ListOfValuesDTO.class,
            responseContainer = "List")
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createLOV(@PathParam("workspaceId") String workspaceId,
                              @ApiParam(required = true, value = "LOV to create") ListOfValuesDTO lovDTO)
            throws ListOfValuesAlreadyExistsException, CreationException, UnsupportedEncodingException, UserNotFoundException, AccessRightException, UserNotActiveException, WorkspaceNotFoundException {
        ListOfValues lov = mapper.map(lovDTO, ListOfValues.class);
        lovManager.createLov(workspaceId, lov.getName(), lov.getValues());
        return Response.created(URI.create(URLEncoder.encode(lov.getName(), "UTF-8"))).entity(lovDTO).build();
    }

    @GET
    @ApiOperation(value = "Get the ListOfValues from the given parameters",
            response = ListOfValuesDTO.class)
    @Path("/{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ListOfValuesDTO getLOV(@PathParam("workspaceId") String workspaceId,
                                  @PathParam("name") String name)
            throws ListOfValuesNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        ListOfValuesKey lovKey = new ListOfValuesKey(workspaceId, name);
        ListOfValues lov = lovManager.findLov(lovKey);
        return mapper.map(lov, ListOfValuesDTO.class);
    }

    @PUT
    @Path("/{name}")
    @ApiOperation(value = "Update the ListOfValues",
            response = ListOfValuesDTO.class)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ListOfValuesDTO updateLOV(@PathParam("workspaceId") String workspaceId,
                                     @PathParam("name") String name,
                                     @ApiParam(required = true, value = "LOV to update") ListOfValuesDTO lovDTO)
            throws ListOfValuesNotFoundException, ListOfValuesAlreadyExistsException, CreationException, UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, AccessRightException {
        ListOfValuesKey lovKey = new ListOfValuesKey(workspaceId, name);
        ListOfValues lov = mapper.map(lovDTO, ListOfValues.class);

        ListOfValues newLovUpdated = lovManager.updateLov(lovKey, lov.getName(), workspaceId, lov.getValues());
        return mapper.map(newLovUpdated, ListOfValuesDTO.class);
    }

    @DELETE
    @Path("/{name}")
    @ApiOperation(value = "Delete the ListOfValues",
            response = Response.class)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteLOV(@PathParam("workspaceId") String workspaceId,
                              @PathParam("name") String name)
            throws ListOfValuesNotFoundException, UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, AccessRightException, EntityConstraintException {
        ListOfValuesKey lovKey = new ListOfValuesKey(workspaceId, name);
        lovManager.deleteLov(lovKey);
        return Response.ok().build();
    }
}
