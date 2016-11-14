package com.docdoku.server.rest;

import com.docdoku.core.exceptions.*;
import com.docdoku.core.product.*;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IEffectivityManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.server.rest.dto.DateBasedEffectivityDTO;
import com.docdoku.server.rest.dto.EffectivityDTO;
import com.docdoku.server.rest.dto.LotBasedEffectivityDTO;
import com.docdoku.server.rest.dto.SerialNumberBasedEffectivityDTO;
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
import java.util.ArrayList;
import java.util.List;

@RequestScoped
@Api(value = "effectivity", description = "Operations about effectivity")
@Path("effectivity")
@DeclareRoles({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
@RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
public class EffectivityResource {

    @Inject
    private IEffectivityManagerLocal effectivityManager;

    @Inject
    private IProductManagerLocal productManager;

    private Mapper mapper;

    public EffectivityResource() { }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @POST
    @Path("/{workspaceId}/serial-number/{confirgurationItemId}")
    @ApiOperation(value = "Create Serial number based effectivity on a configuration item", response = SerialNumberBasedEffectivityDTO.class)
    @Produces(MediaType.APPLICATION_JSON)
    public SerialNumberBasedEffectivityDTO createSerialNumberBasedEffectivity(
            @ApiParam(required = true, value = "Serial Number Based Effectivity to create") SerialNumberBasedEffectivityDTO srEffectivity,
            @ApiParam(required = true, value = "Id of the effectivity workspace") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Id of the effectivity configuration item") @PathParam("confirgurationItemId") String confirgurationItemId)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, ConfigurationItemNotFoundException, WorkspaceNotEnabledException,
            EffectivityAlreadyExistsException, CreationException {
        ConfigurationItemKey configurationItemKey = new ConfigurationItemKey(workspaceId, confirgurationItemId);
        ConfigurationItem configurationItem = productManager.getConfigurationItem(configurationItemKey);
        SerialNumberBasedEffectivity serialNumberBasedEffectivity = effectivityManager.createSerialNumberBasedEffectivity(srEffectivity.getName(),
                srEffectivity.getDescription(), configurationItem, srEffectivity.getStartNumber(), srEffectivity.getEndNumber());
        return mapper.map(serialNumberBasedEffectivity, SerialNumberBasedEffectivityDTO.class);
    }

    @POST
    @Path("/{workspaceId}/date/{confirgurationItemId}")
    @ApiOperation(value = "Create Date based effectivity on a configuration item", response = DateBasedEffectivityDTO.class)
    @Produces(MediaType.APPLICATION_JSON)
    public DateBasedEffectivityDTO createDateBasedEffectivity(
            @ApiParam(required = true, value = "Serial Number Based Effectivity to create") DateBasedEffectivityDTO dateEffectivity,
            @ApiParam(required = true, value = "Id of the effectivity workspace") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Id of the effectivity configuration item") @PathParam("confirgurationItemId") String confirgurationItemId)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, ConfigurationItemNotFoundException, WorkspaceNotEnabledException,
            EffectivityAlreadyExistsException, CreationException {
        ConfigurationItemKey configurationItemKey = new ConfigurationItemKey(workspaceId, confirgurationItemId);
        ConfigurationItem configurationItem = productManager.getConfigurationItem(configurationItemKey);
        DateBasedEffectivity dateBasedEffectivity = effectivityManager.createDateBasedEffectivity(dateEffectivity.getName(),
                dateEffectivity.getDescription(), configurationItem, dateEffectivity.getStartDate(), dateEffectivity.getEndDate());
        return mapper.map(dateBasedEffectivity, DateBasedEffectivityDTO.class);
    }

    @POST
    @Path("/{workspaceId}/lot/{confirgurationItemId}")
    @ApiOperation(value = "Create Lot based effectivity on a configuration item", response = LotBasedEffectivityDTO.class)
    @Produces(MediaType.APPLICATION_JSON)
    public LotBasedEffectivityDTO createLotBasedEffectivity(
            @ApiParam(required = true, value = "Lot Based Effectivity to create") LotBasedEffectivityDTO lotEffectivity,
            @ApiParam(required = true, value = "Id of the effectivity workspace") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Id of the effectivity configuration item") @PathParam("confirgurationItemId") String confirgurationItemId)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, ConfigurationItemNotFoundException, WorkspaceNotEnabledException,
            EffectivityAlreadyExistsException, CreationException {
        ConfigurationItemKey configurationItemKey = new ConfigurationItemKey(workspaceId, confirgurationItemId);
        ConfigurationItem configurationItem = productManager.getConfigurationItem(configurationItemKey);
        LotBasedEffectivity lotBasedEffectivity = effectivityManager.createLotBasedEffectivity(lotEffectivity.getName(),
                lotEffectivity.getDescription(), configurationItem, lotEffectivity.getStartLotId(), lotEffectivity.getEndLotId());
        return mapper.map(lotBasedEffectivity, LotBasedEffectivityDTO.class);
    }

    @GET
    @ApiOperation(value = "Get all effectivities", response = EffectivityDTO.class, responseContainer = "List")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEffectivities() {
        List<EffectivityDTO> effectivityDTOs = new ArrayList<>();
        for(Effectivity effectivity : effectivityManager.getEffectivities()) {
            effectivityDTOs.add(mapper.map(effectivity, EffectivityDTO.class));
        }
        return Response.ok(new GenericEntity<List<EffectivityDTO>>(effectivityDTOs){}).build();
    }

    @GET
    @Path("/{effectivityId}")
    @ApiOperation(value = "Get an effectivity from its ID", response = EffectivityDTO.class)
    @Produces(MediaType.APPLICATION_JSON)
    public EffectivityDTO getEffectivity(@ApiParam(required = true, value = "Effectivity id") @PathParam("effectivityId") int effectivityId) throws EffectivityNotFoundException {
        Effectivity effectivity = effectivityManager.getEffectivity(effectivityId);
        return mapper.map(effectivity, EffectivityDTO.class);
    }

    @PUT
    @ApiOperation(value = "Update effectivity", response = EffectivityDTO.class)
    @Path("/{effectivityId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateEffectivity(
            @ApiParam(required = true, value = "Effectivity id") @PathParam("effectivityId") int effectivityId,
            @ApiParam(required = true, value = "Effectivity values to update") EffectivityDTO effectivityDTO) throws EffectivityNotFoundException {
        Effectivity effectivity = null;
        if(effectivity.getClass().equals(SerialNumberBasedEffectivityDTO.class)) {
            effectivity = effectivityManager.updateSerialNumberBasedEffectivity(effectivityId,
                    effectivityDTO.getName(), effectivityDTO.getDescription(),
                    ((SerialNumberBasedEffectivityDTO)effectivityDTO).getStartNumber(), ((SerialNumberBasedEffectivityDTO)effectivityDTO).getEndNumber());
        } else if (effectivity.getClass().equals(DateBasedEffectivityDTO.class)) {
            effectivity = effectivityManager.updateDateBasedEffectivity(effectivityId,
                    effectivityDTO.getName(), effectivityDTO.getDescription(),
                    ((DateBasedEffectivityDTO)effectivityDTO).getStartDate(), ((DateBasedEffectivityDTO)effectivityDTO).getEndDate());
        } else if (effectivity.getClass().equals(LotBasedEffectivity.class)) {
            effectivity = effectivityManager.updateLotBasedEffectivity(effectivityId,
                    effectivityDTO.getName(), effectivityDTO.getDescription(),
                    ((LotBasedEffectivityDTO)effectivityDTO).getStartLotId(), ((LotBasedEffectivityDTO)effectivityDTO).getEndLotId());
        } else {
            effectivity = effectivityManager.updateEffectivity(effectivityId,
                    effectivityDTO.getName(), effectivityDTO.getDescription());
        }
        return Response.ok(mapper.map(effectivity, EffectivityDTO.class)).build();
    }

    @DELETE
    @ApiOperation(value = "Delete workspace", response = Response.class)
    @Path("/{effectivityId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteEffectivity(@ApiParam(required = true, value = "Effectivity id") @PathParam("effectivityId") int effectivityId) throws EffectivityNotFoundException {
        effectivityManager.deleteEffectivity(effectivityId);
        return Response.ok().build();
    }

}
