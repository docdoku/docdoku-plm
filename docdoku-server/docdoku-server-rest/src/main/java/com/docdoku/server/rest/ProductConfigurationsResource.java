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

import com.docdoku.core.configuration.ProductConfiguration;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.product.ConfigurationItemKey;
import com.docdoku.core.product.PartLink;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IProductBaselineManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.server.rest.dto.ACLDTO;
import com.docdoku.server.rest.dto.LightPartLinkDTO;
import com.docdoku.server.rest.dto.LightPartLinkListDTO;
import com.docdoku.server.rest.dto.baseline.ProductConfigurationDTO;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Morgan Guimard
 */
@RequestScoped
@Api(hidden = true, value = "productConfigurations", description = "Operations about product configurations")
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class ProductConfigurationsResource {

    @Inject
    private IProductBaselineManagerLocal productBaselineService;

    @Inject
    private IProductManagerLocal productService;

    private Mapper mapper;

    public ProductConfigurationsResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @ApiOperation(value = "Get all product configurations in workspace",
            response = ProductConfigurationDTO.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of ProductConfigurationDTOs. It can be an empty list."),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllConfigurations(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId)
            throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException,
            ConfigurationItemNotFoundException, WorkspaceNotEnabledException {
        List<ProductConfiguration> allProductConfigurations = productBaselineService.getAllProductConfigurations(workspaceId);
        return makeList(allProductConfigurations);
    }


    @GET
    @Path("{ciId}/configurations")
    @ApiOperation(value = "Get all product configurations for given product",
            response = ProductConfigurationDTO.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of ProductConfigurationDTOs. It can be an empty list."),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConfigurationsForProduct(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Configuration item id filter") @PathParam("ciId") String ciId)
            throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException,
            ConfigurationItemNotFoundException, WorkspaceNotEnabledException {

        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);
        List<ProductConfiguration> allProductConfigurations = productBaselineService.getAllProductConfigurationsByConfigurationItemId(ciKey);
        return makeList(allProductConfigurations);

    }


    @GET
    @ApiOperation(value = "Get product configuration",
            response = ProductConfigurationDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of ProductConfigurationDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{ciId}/configurations/{productConfigurationId}")
    @Produces(MediaType.APPLICATION_JSON)
    public ProductConfigurationDTO getConfiguration(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String ciId,
            @ApiParam(required = true, value = "Product configuration id") @PathParam("productConfigurationId") int productConfigurationId)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException,
            ProductConfigurationNotFoundException, EntityConstraintException, NotAllowedException,
            AccessRightException, ConfigurationItemNotFoundException, PartUsageLinkNotFoundException,
            PartMasterNotFoundException, WorkspaceNotEnabledException {

        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);
        ProductConfiguration productConfiguration = productBaselineService.getProductConfiguration(ciKey, productConfigurationId);
        ProductConfigurationDTO productConfigurationDTO = mapper.map(productConfiguration, ProductConfigurationDTO.class);
        productConfigurationDTO.setConfigurationItemId(productConfiguration.getConfigurationItem().getId());

        List<LightPartLinkListDTO> substitutesParts = new ArrayList<>();
        List<LightPartLinkListDTO> optionalParts = new ArrayList<>();

        for (String path : productConfiguration.getSubstituteLinks()) {
            LightPartLinkListDTO partDTOs = new LightPartLinkListDTO();
            for (PartLink partLink : productService.decodePath(ciKey, path)) {
                partDTOs.getPartLinks().add(new LightPartLinkDTO(partLink));
            }
            substitutesParts.add(partDTOs);
        }

        for (String path : productConfiguration.getOptionalUsageLinks()) {
            LightPartLinkListDTO partDTOs = new LightPartLinkListDTO();
            for (PartLink partLink : productService.decodePath(ciKey, path)) {
                partDTOs.getPartLinks().add(new LightPartLinkDTO(partLink));
            }
            optionalParts.add(partDTOs);
        }

        productConfigurationDTO.setSubstitutesParts(substitutesParts);
        productConfigurationDTO.setOptionalsParts(optionalParts);

        return productConfigurationDTO;
    }

    @POST
    @ApiOperation(value = "Create product configuration",
            response = ProductConfigurationDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of created ProductConfigurationDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ProductConfigurationDTO createConfiguration(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Product configuration to create") ProductConfigurationDTO pProductConfigurationDTO)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException,
            ConfigurationItemNotFoundException, CreationException, AccessRightException,
            WorkspaceNotEnabledException {

        String ciId = pProductConfigurationDTO.getConfigurationItemId();
        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);
        String description = pProductConfigurationDTO.getDescription();
        String name = pProductConfigurationDTO.getName();

        ACLDTO acldto = pProductConfigurationDTO.getAcl();
        Map<String, ACL.Permission> userEntries = new HashMap<>();
        Map<String, ACL.Permission> grpEntries = new HashMap<>();
        if (acldto != null) {
            userEntries = acldto.getUserEntries();
            grpEntries = acldto.getGroupEntries();
        }

        ProductConfiguration productConfiguration = productBaselineService.createProductConfiguration(ciKey, name, description, pProductConfigurationDTO.getSubstituteLinks(), pProductConfigurationDTO.getOptionalUsageLinks(), userEntries, grpEntries);
        ProductConfigurationDTO productConfigurationDTO = mapper.map(productConfiguration, ProductConfigurationDTO.class);
        productConfigurationDTO.setConfigurationItemId(productConfiguration.getConfigurationItem().getId());
        return productConfigurationDTO;
    }


    @PUT
    @ApiOperation(value = "Update product configuration ACL",
            response = Response.class)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successful ACL update"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{ciId}/configurations/{productConfigurationId}/acl")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateConfigurationACL(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String pCiId,
            @ApiParam(required = true, value = "Product configuration id") @PathParam("productConfigurationId") int productConfigurationId,
            @ApiParam(required = true, value = "ACL rules to set") ACLDTO acl)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException,
            ProductConfigurationNotFoundException, AccessRightException, WorkspaceNotEnabledException {

        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, pCiId);

        if (!acl.getGroupEntries().isEmpty() || !acl.getUserEntries().isEmpty()) {

            Map<String, String> userEntries = new HashMap<>();
            Map<String, String> groupEntries = new HashMap<>();

            for (Map.Entry<String, ACL.Permission> entry : acl.getUserEntries().entrySet()) {
                userEntries.put(entry.getKey(), entry.getValue().name());
            }

            for (Map.Entry<String, ACL.Permission> entry : acl.getGroupEntries().entrySet()) {
                groupEntries.put(entry.getKey(), entry.getValue().name());
            }

            productBaselineService.updateACLForConfiguration(ciKey, productConfigurationId, userEntries, groupEntries);
        } else {
            productBaselineService.removeACLFromConfiguration(ciKey, productConfigurationId);
        }

        return Response.noContent().build();
    }

    @DELETE
    @ApiOperation(value = "Delete product configuration",
            response = Response.class)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successful deletion of ProductConfigurationDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{ciId}/configurations/{productConfigurationId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteProductConfiguration(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String ciId,
            @ApiParam(required = true, value = "Product configuration id") @PathParam("productConfigurationId") int productConfigurationId)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException,
            ProductConfigurationNotFoundException, AccessRightException, WorkspaceNotEnabledException {

        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);
        productBaselineService.deleteProductConfiguration(ciKey, productConfigurationId);

        return Response.noContent().build();
    }


    private Response makeList(List<ProductConfiguration> allProductConfigurations) {
        List<ProductConfigurationDTO> configurationDTOs = new ArrayList<>();
        for (ProductConfiguration productConfiguration : allProductConfigurations) {
            ProductConfigurationDTO productConfigurationDTO = mapper.map(productConfiguration, ProductConfigurationDTO.class);
            productConfigurationDTO.setConfigurationItemId(productConfiguration.getConfigurationItem().getId());
            configurationDTOs.add(productConfigurationDTO);
        }

        return Response.ok(new GenericEntity<List<ProductConfigurationDTO>>((List<ProductConfigurationDTO>) configurationDTOs) {
        }).build();
    }

}