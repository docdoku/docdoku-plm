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
    @Path("/serial-number/{workspaceId}/{configurationItemId}/{partRevisionNumber}-{partRevisionVersion}")
    @ApiOperation(value = "Create Serial number based effectivity on a configuration item", response = SerialNumberBasedEffectivityDTO.class)
    @Produces(MediaType.APPLICATION_JSON)
    public SerialNumberBasedEffectivityDTO createSerialNumberBasedEffectivity(
            @ApiParam(required = true, value = "Effectivity to create") SerialNumberBasedEffectivityDTO effectivity,
            @ApiParam(required = true, value = "Id of the effectivity workspace") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Id of the effectivity configuration item") @PathParam("configurationItemId") String configurationItemId,
            @ApiParam(required = true, value = "Number of the effectivity part revision") @PathParam("partRevisionNumber") String partRevisionNumber,
            @ApiParam(required = true, value = "Version of the effectivity part revision") @PathParam("partRevisionVersion") String partRevisionVersion)
            throws UserNotActiveException, PartRevisionNotFoundException, WorkspaceNotFoundException, UserNotFoundException, AccessRightException,
            WorkspaceNotEnabledException, ConfigurationItemNotFoundException, EffectivityAlreadyExistsException, CreationException {

        PartRevision partRevision = productManager.getPartRevision(new PartRevisionKey(workspaceId, partRevisionNumber, partRevisionVersion));
        ConfigurationItem configurationItem = productManager.getConfigurationItem(new ConfigurationItemKey(workspaceId, configurationItemId));

        SerialNumberBasedEffectivity serialNumberBasedEffectivity = effectivityManager.createSerialNumberBasedEffectivity(
                partRevision, effectivity.getName(), effectivity.getDescription(), configurationItem, effectivity.getStartNumber(),
                effectivity.getEndNumber());
        return mapper.map(serialNumberBasedEffectivity, SerialNumberBasedEffectivityDTO.class);
    }

    @POST
    @Path("/date/{workspaceId}/{partRevisionNumber}-{partRevisionVersion}")
    @ApiOperation(value = "Create Date based effectivity on a configuration item", response = DateBasedEffectivityDTO.class)
    @Produces(MediaType.APPLICATION_JSON)
    public DateBasedEffectivityDTO createDateBasedEffectivity(
            @ApiParam(required = true, value = "Effectivity to create") DateBasedEffectivityDTO effectivity,
            @ApiParam(required = true, value = "Id of the effectivity workspace") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Number of the effectivity part revision") @PathParam("partRevisionNumber") String partRevisionNumber,
            @ApiParam(required = true, value = "Version of the effectivity part revision") @PathParam("partRevisionVersion") String partRevisionVersion)
            throws EffectivityAlreadyExistsException, CreationException, UserNotActiveException, PartRevisionNotFoundException, WorkspaceNotFoundException,
            UserNotFoundException, AccessRightException, WorkspaceNotEnabledException {

        PartRevision partRevision = productManager.getPartRevision(new PartRevisionKey(workspaceId, partRevisionNumber, partRevisionVersion));

        DateBasedEffectivity dateBasedEffectivity = effectivityManager.createDateBasedEffectivity(
                partRevision, effectivity.getName(), effectivity.getDescription(), effectivity.getStartDate(), effectivity.getEndDate());
        return mapper.map(dateBasedEffectivity, DateBasedEffectivityDTO.class);
    }

    @POST
    @Path("/lot/{workspaceId}/{configurationItemId}/{partRevisionNumber}-{partRevisionVersion}")
    @ApiOperation(value = "Create Lot based effectivity on a configuration item", response = LotBasedEffectivityDTO.class)
    @Produces(MediaType.APPLICATION_JSON)
    public LotBasedEffectivityDTO createLotBasedEffectivity(
            @ApiParam(required = true, value = "Lot Based Effectivity to create") LotBasedEffectivityDTO effectivity,
            @ApiParam(required = true, value = "Id of the effectivity workspace") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Id of the effectivity configuration item") @PathParam("configurationItemId") String configurationItemId,
            @ApiParam(required = true, value = "Number of the effectivity part revision") @PathParam("partRevisionNumber") String partRevisionNumber,
            @ApiParam(required = true, value = "Version of the effectivity part revision") @PathParam("partRevisionVersion") String partRevisionVersion)
            throws UserNotActiveException, PartRevisionNotFoundException, WorkspaceNotFoundException, UserNotFoundException, AccessRightException,
            WorkspaceNotEnabledException, ConfigurationItemNotFoundException, EffectivityAlreadyExistsException, CreationException {

        PartRevision partRevision = productManager.getPartRevision(new PartRevisionKey(workspaceId, partRevisionNumber, partRevisionVersion));
        ConfigurationItem configurationItem = productManager.getConfigurationItem(new ConfigurationItemKey(workspaceId, configurationItemId));

        LotBasedEffectivity lotBasedEffectivity = effectivityManager.createLotBasedEffectivity(
                partRevision, effectivity.getName(), effectivity.getDescription(), configurationItem, effectivity.getStartLotId(), effectivity.getEndLotId());
        return mapper.map(lotBasedEffectivity, LotBasedEffectivityDTO.class);
    }

    @GET
    @Path("/serial-number/{workspaceId}/{partRevisionNumber}-{partRevisionVersion}")
    @ApiOperation(value = "Get all serial number based effectivities", response = SerialNumberBasedEffectivityDTO.class, responseContainer = "List")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSerialNumberBasedEffectivities(
            @ApiParam(required = true, value = "Id of the effectivity workspace") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Number of the effectivity part revision") @PathParam("partRevisionNumber") String partRevisionNumber,
            @ApiParam(required = true, value = "Version of the effectivity part revision") @PathParam("partRevisionVersion") String partRevisionVersion)
            throws UserNotActiveException, PartRevisionNotFoundException, WorkspaceNotFoundException, UserNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        PartRevision partRevision = productManager.getPartRevision(new PartRevisionKey(workspaceId, partRevisionNumber, partRevisionVersion));

        List<SerialNumberBasedEffectivityDTO> effectivityDTOs = new ArrayList<>();
        for(SerialNumberBasedEffectivity effectivity : effectivityManager.getSerialNumberBasedEffectivities(partRevision)) {
            effectivityDTOs.add(mapper.map(effectivity, SerialNumberBasedEffectivityDTO.class));
        }
        return Response.ok(new GenericEntity<List<SerialNumberBasedEffectivityDTO>>(effectivityDTOs){}).build();
    }

    @GET
    @Path("/date/{workspaceId}/{partRevisionNumber}-{partRevisionVersion}")
    @ApiOperation(value = "Get all date based effectivities", response = DateBasedEffectivityDTO.class, responseContainer = "List")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDateBasedEffectivities(
            @ApiParam(required = true, value = "Id of the effectivity workspace") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Number of the effectivity part revision") @PathParam("partRevisionNumber") String partRevisionNumber,
            @ApiParam(required = true, value = "Version of the effectivity part revision") @PathParam("partRevisionVersion") String partRevisionVersion)
            throws UserNotActiveException, PartRevisionNotFoundException, WorkspaceNotFoundException, UserNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        PartRevision partRevision = productManager.getPartRevision(new PartRevisionKey(workspaceId, partRevisionNumber, partRevisionVersion));

        List<DateBasedEffectivityDTO> effectivityDTOs = new ArrayList<>();
        for(DateBasedEffectivity effectivity : effectivityManager.getDateBasedEffectivities(partRevision)) {
            effectivityDTOs.add(mapper.map(effectivity, DateBasedEffectivityDTO.class));
        }
        return Response.ok(new GenericEntity<List<DateBasedEffectivityDTO>>(effectivityDTOs){}).build();
    }

    @GET
    @Path("/lot/{workspaceId}/{partRevisionNumber}-{partRevisionVersion}")
    @ApiOperation(value = "Get all lot based effectivities", response = LotBasedEffectivityDTO.class, responseContainer = "List")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLotBasedEffectivities(
            @ApiParam(required = true, value = "Id of the effectivity workspace") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Number of the effectivity part revision") @PathParam("partRevisionNumber") String partRevisionNumber,
            @ApiParam(required = true, value = "Version of the effectivity part revision") @PathParam("partRevisionVersion") String partRevisionVersion)
            throws UserNotActiveException, PartRevisionNotFoundException, WorkspaceNotFoundException, UserNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        PartRevision partRevision = productManager.getPartRevision(new PartRevisionKey(workspaceId, partRevisionNumber, partRevisionVersion));

        List<LotBasedEffectivityDTO> effectivityDTOs = new ArrayList<>();
        for(LotBasedEffectivity effectivity : effectivityManager.getLotBasedEffectivities(partRevision)) {
            effectivityDTOs.add(mapper.map(effectivity, LotBasedEffectivityDTO.class));
        }
        return Response.ok(new GenericEntity<List<LotBasedEffectivityDTO>>(effectivityDTOs){}).build();
    }

    @GET
    @Path("/serial-number/{effectivityId}")
    @ApiOperation(value = "Get a serial number based effectivity from its ID", response = SerialNumberBasedEffectivityDTO.class)
    @Produces(MediaType.APPLICATION_JSON)
    public SerialNumberBasedEffectivityDTO getSerialNumberBasedEffectivity(
            @ApiParam(required = true, value = "Effectivity id") @PathParam("effectivityId") int effectivityId) throws EffectivityNotFoundException {
        SerialNumberBasedEffectivity effectivity = effectivityManager.getSerialNumberBasedEffectivity(effectivityId);
        return mapper.map(effectivity, SerialNumberBasedEffectivityDTO.class);
    }

    @GET
    @Path("/date/{effectivityId}")
    @ApiOperation(value = "Get a serial number based effectivity from its ID", response = DateBasedEffectivityDTO.class)
    @Produces(MediaType.APPLICATION_JSON)
    public DateBasedEffectivityDTO getDateBasedEffectivity(@ApiParam(required = true, value = "Effectivity id") @PathParam("effectivityId") int effectivityId) throws EffectivityNotFoundException {
        DateBasedEffectivity effectivity = effectivityManager.getDateBasedEffectivity(effectivityId);
        return mapper.map(effectivity, DateBasedEffectivityDTO.class);
    }

    @GET
    @Path("/lot/{effectivityId}")
    @ApiOperation(value = "Get a serial number based effectivity from its ID", response = LotBasedEffectivityDTO.class)
    @Produces(MediaType.APPLICATION_JSON)
    public LotBasedEffectivityDTO getLotBasedEffectivity(@ApiParam(required = true, value = "Effectivity id") @PathParam("effectivityId") int effectivityId) throws EffectivityNotFoundException {
        LotBasedEffectivity effectivity = effectivityManager.getLotBasedEffectivity(effectivityId);
        return mapper.map(effectivity, LotBasedEffectivityDTO.class);
    }

    @PUT
    @ApiOperation(value = "Update effectivity", response = EffectivityDTO.class)
    @Path("/{effectivityId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateEffectivity(
            @ApiParam(required = true, value = "Effectivity id") @PathParam("effectivityId") int effectivityId,
            @ApiParam(required = true, value = "Effectivity values to update") EffectivityDTO effectivityDTO) throws EffectivityNotFoundException {
        Effectivity effectivity = null;
        if(effectivityDTO.getClass().equals(SerialNumberBasedEffectivityDTO.class)) {
            effectivity = effectivityManager.updateSerialNumberBasedEffectivity(effectivityId,
                    effectivityDTO.getName(), effectivityDTO.getDescription(),
                    ((SerialNumberBasedEffectivityDTO)effectivityDTO).getStartNumber(), ((SerialNumberBasedEffectivityDTO)effectivityDTO).getEndNumber());
        } else if (effectivityDTO.getClass().equals(DateBasedEffectivityDTO.class)) {
            effectivity = effectivityManager.updateDateBasedEffectivity(effectivityId,
                    effectivityDTO.getName(), effectivityDTO.getDescription(),
                    ((DateBasedEffectivityDTO)effectivityDTO).getStartDate(), ((DateBasedEffectivityDTO)effectivityDTO).getEndDate());
        } else if (effectivityDTO.getClass().equals(LotBasedEffectivity.class)) {
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
    @Path("/{workspaceId}/{partRevisionNumber}-{partRevisionVersion}/{effectivityId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteEffectivity(
            @ApiParam(required = true, value = "Id of the effectivity workspace") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Number of the effectivity part revision") @PathParam("partRevisionNumber") String partRevisionNumber,
            @ApiParam(required = true, value = "Version of the effectivity part revision") @PathParam("partRevisionVersion") String partRevisionVersion,
            @ApiParam(required = true, value = "Effectivity id") @PathParam("effectivityId") int effectivityId)
            throws EffectivityNotFoundException, UserNotActiveException, PartRevisionNotFoundException, WorkspaceNotFoundException, UserNotFoundException,
            AccessRightException, WorkspaceNotEnabledException {
        PartRevision partRevision = productManager.getPartRevision(new PartRevisionKey(workspaceId, partRevisionNumber, partRevisionVersion));

        effectivityManager.deleteEffectivity(partRevision, effectivityId);
        return Response.ok().build();
    }

}
