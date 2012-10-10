/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,  
 * but WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
 * GNU General Public License for more details.  
 *  
 * You should have received a copy of the GNU General Public License  
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.  
 */
package com.docdoku.server.rest;

import com.docdoku.core.product.ConfigurationItemKey;
import com.docdoku.core.product.Layer;
import com.docdoku.core.product.Marker;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.server.rest.dto.LayerDTO;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import com.docdoku.server.rest.dto.MarkerDTO;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

/**
 *
 * @author Florent Garin
 */
@Stateless
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class LayerResource {

    @EJB
    private IProductManagerLocal productService;

    private Mapper mapper;

    public LayerResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    public LayerDTO[] getLayersInProduct(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId){
        try {
            ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);
            List<Layer> layers = productService.getLayers(ciKey);
            LayerDTO[] layerDtos = new LayerDTO[layers.size()];
            for (int i = 0; i < layers.size(); i++) {                
                layerDtos[i] = new LayerDTO(layers.get(i).getId(), layers.get(i).getName());
            }
            return layerDtos;
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }        
    }

    
    @POST
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    public LayerDTO createLayer(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId, LayerDTO layer) {
        try {
            ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);
            Layer l = productService.createLayer(ciKey, layer.getName());
            return new LayerDTO(l.getId(), l.getName());
            
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @PUT
    @Path("{layerId}")
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    public LayerDTO updateLayer(@PathParam("layerId") int layerId, @PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId, LayerDTO layer) {
        try {
            ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);
            Layer l = productService.updateLayer(ciKey, layerId, layer.getName());
            return new LayerDTO(l.getId(), l.getName());
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    /*
    @Path("{layerId}/markers")
    public MarkerResource processMarkers(@PathParam("workspaceId") String workspaceId, @PathParam("layerId") int layerId) {
        try {
            Layer layer = productService.getLayer(layerId);
            return new MarkerResource(layer);
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }
    */

    @GET
    @Path("{layerId}/markers")
    @Produces("application/json;charset=UTF-8")
    public MarkerDTO[] getMarkersInLayer(@PathParam("workspaceId") String workspaceId, @PathParam("layerId") int layerId){
        try {
            Layer layer = productService.getLayer(layerId);
            Set<Marker> markers = layer.getMarkers();
            Marker[] markersArray = markers.toArray(new Marker[0]);
            MarkerDTO[] markersDTO = new MarkerDTO[markers.size()];
            for (int i = 0; i < markersArray.length; i++) {
                markersDTO[i] = new MarkerDTO(markersArray[i].getId(), markersArray[i].getTitle(),markersArray[i].getDescription(), markersArray[i].getX(), markersArray[i].getY(), markersArray[i].getZ());
            }
            return markersDTO;
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @POST
    @Path("{layerId}/markers")
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    public MarkerDTO createMarker(@PathParam("workspaceId") String workspaceId, @PathParam("layerId") int layerId, MarkerDTO markerDTO) {
        try {
            Marker marker = productService.createMarker(layerId, markerDTO.getTitle(), markerDTO.getDescription(), markerDTO.getX(), markerDTO.getY(), markerDTO.getZ());
            return new MarkerDTO(marker.getId(), marker.getTitle(), marker.getDescription(), marker.getX(), marker.getY(), marker.getZ());
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @DELETE
    @Path("{layerId}/markers/{markerId}")
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    public Response deleteMarker(@PathParam("workspaceId") String workspaceId, @PathParam("layerId") int layerId, @PathParam("markerId") int markerId) {
        try {
            productService.deleteMarker(layerId, markerId);
            return Response.status(Response.Status.OK).build();
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

}
