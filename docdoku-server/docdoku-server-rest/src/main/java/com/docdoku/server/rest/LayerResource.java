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

import com.docdoku.core.exceptions.AccessRightException;
import com.docdoku.core.exceptions.EntityNotFoundException;
import com.docdoku.core.exceptions.UserNotActiveException;
import com.docdoku.core.product.ConfigurationItemKey;
import com.docdoku.core.product.Layer;
import com.docdoku.core.product.Marker;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.server.rest.dto.LayerDTO;
import com.docdoku.server.rest.dto.MarkerDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Set;

/**
 * @author Florent Garin
 */

@RequestScoped
@Api(hidden = true, value = "layers", description = "Operations about layers")
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class LayerResource {

    @Inject
    private IProductManagerLocal productService;

    public LayerResource() {
    }

    @GET
    @ApiOperation(value = "Get layers", response = LayerDTO.class, responseContainer = "List")
    @Produces(MediaType.APPLICATION_JSON)
    public LayerDTO[] getLayersInProduct(@ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
                                         @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String ciId)
            throws EntityNotFoundException, UserNotActiveException {

        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);
        List<Layer> layers = productService.getLayers(ciKey);
        LayerDTO[] layerDTOs = new LayerDTO[layers.size()];
        for (int i = 0; i < layers.size(); i++) {
            layerDTOs[i] = new LayerDTO(layers.get(i).getId(), layers.get(i).getName(), layers.get(i).getColor());
        }
        return layerDTOs;
    }


    @POST
    @ApiOperation(value = "Create layers", response = LayerDTO.class)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public LayerDTO createLayer(@ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
                                @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String ciId,
                                @ApiParam(required = true, value = "Layer to create") LayerDTO layer)
            throws EntityNotFoundException, AccessRightException {

        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);
        Layer l = productService.createLayer(ciKey, layer.getName(), layer.getColor());
        return new LayerDTO(l.getId(), l.getName(), l.getColor());
    }

    @PUT
    @ApiOperation(value = "Update layer", response = LayerDTO.class)
    @Path("{layerId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public LayerDTO updateLayer(@ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
                                @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String ciId,
                                @ApiParam(required = true, value = "Layer id") @PathParam("layerId") int layerId,
                                @ApiParam(required = true, value = "Layer to update") LayerDTO layer)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException {

        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);
        Layer l = productService.updateLayer(ciKey, layerId, layer.getName(), layer.getColor());
        return new LayerDTO(l.getId(), l.getName(), l.getColor());
    }

    @DELETE
    @ApiOperation(value = "Delete layer", response = Response.class)
    @Path("{layerId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteLayer(@ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
                                @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String ciId,
                                @ApiParam(required = true, value = "Layer id") @PathParam("layerId") int layerId)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException {

        productService.deleteLayer(workspaceId, layerId);
        return Response.ok().build();
    }

    @GET
    @ApiOperation(value = "Get markers", response = MarkerDTO.class, responseContainer = "List")
    @Path("{layerId}/markers")
    @Produces(MediaType.APPLICATION_JSON)
    public MarkerDTO[] getMarkersInLayer(@ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
                                         @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String ciId,
                                         @ApiParam(required = true, value = "Layer id") @PathParam("layerId") int layerId)
            throws EntityNotFoundException, UserNotActiveException {

        Layer layer = productService.getLayer(layerId);
        Set<Marker> markers = layer.getMarkers();
        Marker[] markersArray = markers.toArray(new Marker[markers.size()]);
        MarkerDTO[] markersDTO = new MarkerDTO[markers.size()];
        for (int i = 0; i < markersArray.length; i++) {
            markersDTO[i] = new MarkerDTO(markersArray[i].getId(), markersArray[i].getTitle(), markersArray[i].getDescription(), markersArray[i].getX(), markersArray[i].getY(), markersArray[i].getZ());
        }
        return markersDTO;
    }

    @POST
    @ApiOperation(value = "Create marker", response = MarkerDTO.class)
    @Path("{layerId}/markers")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public MarkerDTO createMarker(@ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
                                  @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String ciId,
                                  @ApiParam(required = true, value = "Layer id") @PathParam("layerId") int layerId,
                                  @ApiParam(required = true, value = "Marker to create") MarkerDTO markerDTO)
            throws EntityNotFoundException, AccessRightException {

        Marker marker = productService.createMarker(layerId, markerDTO.getTitle(), markerDTO.getDescription(), markerDTO.getX(), markerDTO.getY(), markerDTO.getZ());
        return new MarkerDTO(marker.getId(), marker.getTitle(), marker.getDescription(), marker.getX(), marker.getY(), marker.getZ());
    }

    @DELETE
    @ApiOperation(value = "Delete marker", response = Response.class)
    @Path("{layerId}/markers/{markerId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteMarker(@ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
                                 @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String ciId,
                                 @ApiParam(required = true, value = "Layer id") @PathParam("layerId") int layerId,
                                 @ApiParam(required = true, value = "Marker id")  @PathParam("markerId") int markerId)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException {

        productService.deleteMarker(layerId, markerId);
        return Response.status(Response.Status.OK).build();
    }
}