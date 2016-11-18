package com.docdoku.server.rest;

import com.docdoku.core.exceptions.*;
import com.docdoku.core.product.*;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IEffectivityManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.server.rest.dto.EffectivityDTO;
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
import java.util.Set;

@RequestScoped
@Api(hidden = true, value = "parts effecitivities", description = "Operation about parts effectivities")
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class PartEffectivity {


    @Inject
    private IEffectivityManagerLocal effectivityManager;

    @Inject
    private IProductManagerLocal productManager;

    private Mapper mapper;

    public PartEffectivity() { }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @POST
    @ApiOperation(value = "Create an Effectivity for a PartRevision", response = EffectivityDTO.class)
    @Produces(MediaType.APPLICATION_JSON)
    public EffectivityDTO createEffectivity(
            @ApiParam(required = true, value = "Effectivity to create") EffectivityDTO effectivity,
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Part revision number") @PathParam("partNumber") String partNumber,
            @ApiParam(required = true, value = "Part revision version") @PathParam("partVersion") String partVersion)
            throws UserNotActiveException, PartRevisionNotFoundException, WorkspaceNotFoundException, UserNotFoundException, AccessRightException, WorkspaceNotEnabledException, EffectivityAlreadyExistsException, CreationException, ConfigurationItemNotFoundException {
        PartRevision partRevision = productManager.getPartRevision(new PartRevisionKey(workspaceId, partNumber, partVersion));
        TypeEffectivity typeEffectivity = effectivity.getTypeEffectivity();
        Effectivity createdEffectivity = null;
        EffectivityDTO returnedEffectivityDTO = null;

        if(typeEffectivity.equals(TypeEffectivity.DATEBASEDEFFECTIVITY)) {
            createdEffectivity = effectivityManager.createDateBasedEffectivity(
                    partRevision, effectivity.getName(), effectivity.getDescription(), effectivity.getStartDate(), effectivity.getEndDate());
        } else {
            ConfigurationItem configurationItem = productManager.getConfigurationItem(effectivity.getConfigurationItemKey());
            if (typeEffectivity.equals(TypeEffectivity.SERIALNUMBERBASEDEFFECTIVITY)) {
                createdEffectivity = effectivityManager.createSerialNumberBasedEffectivity(
                        partRevision, effectivity.getName(), effectivity.getDescription(), configurationItem, effectivity.getStartSerialNumber(),
                        effectivity.getEndSerialNumber());
            } else if (typeEffectivity.equals(TypeEffectivity.LOTBASEDEFFECTIVITY)) {
                createdEffectivity = effectivityManager.createLotBasedEffectivity(
                        partRevision, effectivity.getName(), effectivity.getDescription(), configurationItem, effectivity.getStartLotId(),
                        effectivity.getEndLotId());
            }
        }

        returnedEffectivityDTO = mapper.map(createdEffectivity, EffectivityDTO.class);
        returnedEffectivityDTO.setTypeEffectivity(effectivity.getTypeEffectivity());
        return returnedEffectivityDTO;
    }

    @GET
    @ApiOperation(value = "Get effectivities of a PartRevision", response = EffectivityDTO.class, responseContainer = "List")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEffectivities(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Part revision number") @PathParam("partNumber") String partNumber,
            @ApiParam(required = true, value = "Part revision version") @PathParam("partVersion") String partVersion)
            throws UserNotActiveException, PartRevisionNotFoundException, WorkspaceNotFoundException, UserNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        PartRevision partRevision = productManager.getPartRevision(new PartRevisionKey(workspaceId, partNumber, partVersion));
        Set<Effectivity> effectivitySet = partRevision.getEffectivities();
        List<EffectivityDTO> effectivityDTOs = new ArrayList<>();

        for(Effectivity effectivity : effectivitySet) {
            TypeEffectivity typeEffectivity = null;
            EffectivityDTO current = mapper.map(effectivity, EffectivityDTO.class);

            if(effectivity.getClass().equals(SerialNumberBasedEffectivity.class)) {
                typeEffectivity = TypeEffectivity.SERIALNUMBERBASEDEFFECTIVITY;
            } else if(effectivity.getClass().equals(DateBasedEffectivity.class)) {
                typeEffectivity = TypeEffectivity.DATEBASEDEFFECTIVITY;
            } else if(effectivity.getClass().equals(LotBasedEffectivity.class)) {
                typeEffectivity = TypeEffectivity.LOTBASEDEFFECTIVITY;
            }
            current.setTypeEffectivity(typeEffectivity);
            effectivityDTOs.add(current);
        }

        return Response.ok(new GenericEntity<List<EffectivityDTO>>(effectivityDTOs) {
        }).build();
    }

    @DELETE
    @ApiOperation(value = "Delete workspace", response = Response.class)
    @Path("{effectivityId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteEffectivity(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Part revision number") @PathParam("partNumber") String partNumber,
            @ApiParam(required = true, value = "Part revision version") @PathParam("partVersion") String partVersion,
            @ApiParam(required = true, value = "Effectivity id") @PathParam("effectivityId") int effectivityId)
            throws EffectivityNotFoundException, UserNotActiveException, PartRevisionNotFoundException, WorkspaceNotFoundException, UserNotFoundException,
            AccessRightException, WorkspaceNotEnabledException {
        PartRevision partRevision = productManager.getPartRevision(new PartRevisionKey(workspaceId, partNumber, partVersion));

        effectivityManager.deleteEffectivity(partRevision, effectivityId);
        return Response.ok().build();
    }

}
