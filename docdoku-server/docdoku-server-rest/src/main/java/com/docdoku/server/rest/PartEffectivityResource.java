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

import com.docdoku.core.exceptions.*;
import com.docdoku.core.product.*;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IEffectivityManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.server.rest.dto.EffectivityDTO;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RequestScoped
@Api(hidden = true, value = "partEffectivities", description = "Operation about parts effectivities")
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class PartEffectivityResource {


    @Inject
    private IEffectivityManagerLocal effectivityManager;

    @Inject
    private IProductManagerLocal productManager;

    private Mapper mapper;

    public PartEffectivityResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @POST
    @ApiOperation(value = "Create an Effectivity for a PartRevision",
            response = EffectivityDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of created effectivity"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public EffectivityDTO createEffectivity(
            @ApiParam(required = true, value = "Effectivity to create") EffectivityDTO effectivity,
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Part revision number") @PathParam("partNumber") String partNumber,
            @ApiParam(required = true, value = "Part revision version") @PathParam("partVersion") String partVersion)
            throws UserNotActiveException, PartRevisionNotFoundException, WorkspaceNotFoundException, UserNotFoundException, AccessRightException, WorkspaceNotEnabledException, EffectivityAlreadyExistsException, CreationException, ConfigurationItemNotFoundException {

        TypeEffectivity typeEffectivity = effectivity.getTypeEffectivity();
        Effectivity createdEffectivity = null;
        EffectivityDTO returnedEffectivityDTO;

        ConfigurationItemKey configurationItemKey = effectivity.getConfigurationItemKey();
        String productId = configurationItemKey != null ? configurationItemKey.getId() : null;

        if (typeEffectivity.equals(TypeEffectivity.DATEBASEDEFFECTIVITY)) {
            createdEffectivity = effectivityManager.createDateBasedEffectivity(
                    workspaceId, partNumber, partVersion, effectivity.getName(), effectivity.getDescription(), productId, effectivity.getStartDate(), effectivity.getEndDate());
        } else if (typeEffectivity.equals(TypeEffectivity.SERIALNUMBERBASEDEFFECTIVITY)) {
            createdEffectivity = effectivityManager.createSerialNumberBasedEffectivity(
                    workspaceId, partNumber, partVersion, effectivity.getName(), effectivity.getDescription(), productId, effectivity.getStartNumber(),
                    effectivity.getEndNumber());
        } else if (typeEffectivity.equals(TypeEffectivity.LOTBASEDEFFECTIVITY)) {
            createdEffectivity = effectivityManager.createLotBasedEffectivity(
                    workspaceId, partNumber, partVersion, effectivity.getName(), effectivity.getDescription(), productId, effectivity.getStartLotId(),
                    effectivity.getEndLotId());
        }

        returnedEffectivityDTO = mapper.map(createdEffectivity, EffectivityDTO.class);
        returnedEffectivityDTO.setConfigurationItemKey(configurationItemKey);

        returnedEffectivityDTO.setTypeEffectivity(effectivity.getTypeEffectivity());
        return returnedEffectivityDTO;
    }

    @GET
    @ApiOperation(value = "Get effectivities of a PartRevision",
            response = EffectivityDTO.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of effectivities. It can be an empty list"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEffectivities(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Part revision number") @PathParam("partNumber") String partNumber,
            @ApiParam(required = true, value = "Part revision version") @PathParam("partVersion") String partVersion)
            throws UserNotActiveException, PartRevisionNotFoundException, WorkspaceNotFoundException, UserNotFoundException, AccessRightException, WorkspaceNotEnabledException {

        PartRevision partRevision = productManager.getPartRevision(new PartRevisionKey(workspaceId, partNumber, partVersion));

        Set<Effectivity> effectivitySet = partRevision.getEffectivities();
        List<EffectivityDTO> effectivityDTOs = new ArrayList<>();

        for (Effectivity effectivity : effectivitySet) {

            EffectivityDTO effectivityDTO = mapper.map(effectivity, EffectivityDTO.class);
            ConfigurationItem configurationItem = effectivity.getConfigurationItem();
            effectivityDTO.setConfigurationItemKey(configurationItem != null ? configurationItem.getKey() : null);

            if (effectivity.getClass().equals(SerialNumberBasedEffectivity.class)) {
                effectivityDTO.setTypeEffectivity(TypeEffectivity.SERIALNUMBERBASEDEFFECTIVITY);
            } else if (effectivity.getClass().equals(DateBasedEffectivity.class)) {
                effectivityDTO.setTypeEffectivity(TypeEffectivity.DATEBASEDEFFECTIVITY);
            } else if (effectivity.getClass().equals(LotBasedEffectivity.class)) {
                effectivityDTO.setTypeEffectivity(TypeEffectivity.LOTBASEDEFFECTIVITY);
            }

            effectivityDTOs.add(effectivityDTO);
        }

        return Response.ok(new GenericEntity<List<EffectivityDTO>>(effectivityDTOs) {
        }).build();
    }

    @DELETE
    @ApiOperation(value = "Delete effectivity from part revision",
            response = Response.class)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successful deletion of effectivity"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{effectivityId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteEffectivity(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Part revision number") @PathParam("partNumber") String partNumber,
            @ApiParam(required = true, value = "Part revision version") @PathParam("partVersion") String partVersion,
            @ApiParam(required = true, value = "Effectivity id") @PathParam("effectivityId") int effectivityId)
            throws EffectivityNotFoundException, UserNotActiveException, PartRevisionNotFoundException, WorkspaceNotFoundException, UserNotFoundException,
            AccessRightException, WorkspaceNotEnabledException {
        effectivityManager.deleteEffectivity(workspaceId, partNumber, partVersion, effectivityId);
        return Response.noContent().build();
    }

}
