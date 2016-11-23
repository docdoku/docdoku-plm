package com.docdoku.server.rest;

import com.docdoku.core.exceptions.CreationException;
import com.docdoku.core.exceptions.EffectivityNotFoundException;
import com.docdoku.core.exceptions.UpdateException;
import com.docdoku.core.product.*;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IEffectivityManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.server.rest.dto.DateBasedEffectivityDTO;
import com.docdoku.server.rest.dto.EffectivityDTO;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RequestScoped
@Api(value = "effectivity", description = "Operations about effectivity")
@Path("effectivities")
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

    @GET
    @Path("/{effectivityId}")
    @ApiOperation(value = "Get an effectivity from its ID", response = EffectivityDTO.class)
    @Produces(MediaType.APPLICATION_JSON)
    public EffectivityDTO getEffectivity(
            @ApiParam(required = true, value = "Effectivity id") @PathParam("effectivityId") int effectivityId) throws EffectivityNotFoundException {
        Effectivity effectivity = effectivityManager.getEffectivity(effectivityId);
        EffectivityDTO effectivityDTO = mapper.map(effectivity, EffectivityDTO.class);

        TypeEffectivity typeEffectivity = null;
        if(effectivity.getClass().equals(SerialNumberBasedEffectivity.class)) {
            typeEffectivity = TypeEffectivity.SERIALNUMBERBASEDEFFECTIVITY;
        } else if(effectivity.getClass().equals(DateBasedEffectivity.class)) {
            typeEffectivity = TypeEffectivity.DATEBASEDEFFECTIVITY;
        } else if(effectivity.getClass().equals(LotBasedEffectivity.class)) {
            typeEffectivity = TypeEffectivity.LOTBASEDEFFECTIVITY;
        }
        effectivityDTO.setTypeEffectivity(typeEffectivity);

        return effectivityDTO;
    }

    @PUT
    @ApiOperation(value = "Update effectivity", response = EffectivityDTO.class)
    @Path("/{effectivityId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateEffectivity(
            @ApiParam(required = true, value = "Effectivity id") @PathParam("effectivityId") int effectivityId,
            @ApiParam(required = true, value = "Effectivity values to update") EffectivityDTO effectivityDTO) throws EffectivityNotFoundException, UpdateException {
        Effectivity effectivity = null;
        if(effectivityDTO.getClass().equals(SerialNumberBasedEffectivityDTO.class)) {
            effectivity = effectivityManager.updateSerialNumberBasedEffectivity(effectivityId,
                    effectivityDTO.getName(), effectivityDTO.getDescription(),
                    effectivityDTO.getStartNumber(), effectivityDTO.getEndNumber());
        } else if (effectivityDTO.getClass().equals(DateBasedEffectivityDTO.class)) {
            effectivity = effectivityManager.updateDateBasedEffectivity(effectivityId,
                    effectivityDTO.getName(), effectivityDTO.getDescription(),
                    effectivityDTO.getStartDate(), effectivityDTO.getEndDate());
        } else if (effectivityDTO.getClass().equals(LotBasedEffectivity.class)) {
            effectivity = effectivityManager.updateLotBasedEffectivity(effectivityId,
                    effectivityDTO.getName(), effectivityDTO.getDescription(),
                    effectivityDTO.getStartLotId(), effectivityDTO.getEndLotId());
        } else {
            effectivity = effectivityManager.updateEffectivity(effectivityId,
                    effectivityDTO.getName(), effectivityDTO.getDescription());
        }
        EffectivityDTO returnedEffectivityDTO = mapper.map(effectivity, EffectivityDTO.class);
        returnedEffectivityDTO.setTypeEffectivity(effectivityDTO.getTypeEffectivity());
        return Response.ok(returnedEffectivityDTO).build();
    }

}
