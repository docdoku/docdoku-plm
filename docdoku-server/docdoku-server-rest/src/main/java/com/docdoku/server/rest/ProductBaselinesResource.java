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

import com.docdoku.core.configuration.BaselinedPart;
import com.docdoku.core.configuration.ProductBaseline;
import com.docdoku.core.configuration.ProductBaselineType;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.product.ConfigurationItemKey;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.product.PartLink;
import com.docdoku.core.product.PathToPathLink;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IProductBaselineManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.server.rest.dto.LightPartLinkDTO;
import com.docdoku.server.rest.dto.LightPartLinkListDTO;
import com.docdoku.server.rest.dto.LightPathToPathLinkDTO;
import com.docdoku.server.rest.dto.PathToPathLinkDTO;
import com.docdoku.server.rest.dto.baseline.BaselinedPartDTO;
import com.docdoku.server.rest.dto.baseline.ProductBaselineDTO;
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

/**
 * @author Taylor LABEJOF
 */
@RequestScoped
@Api(hidden = true, value = "productBaseline", description = "Operations about product-baseline")
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class ProductBaselinesResource {

    @Inject
    private IProductBaselineManagerLocal productBaselineService;

    @Inject
    private IProductManagerLocal productService;

    private Mapper mapper;

    public ProductBaselinesResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @ApiOperation(value = "Get product-baselines with given workspace",
            response = ProductBaselineDTO.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of ProductBaselineDTOs. It can be an empty list."),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllProductBaselines(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId)
            throws UserNotActiveException, EntityNotFoundException, AccessRightException {

        List<ProductBaseline> productBaselines = productBaselineService.getAllBaselines(workspaceId);
        return makeList(productBaselines, workspaceId);
    }


    @GET
    @Path("{ciId}/baselines")
    @ApiOperation(value = "Get product-baseline with given configuration item",
            response = ProductBaselineDTO.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of ProductBaselineDTOs. It can be an empty list."),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProductBaselinesForProduct(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String ciId)
            throws UserNotActiveException, EntityNotFoundException, AccessRightException {

        ConfigurationItemKey configurationItemKey = new ConfigurationItemKey(workspaceId, ciId);
        List<ProductBaseline> productBaselines = productBaselineService.getBaselines(configurationItemKey);
        return makeList(productBaselines,workspaceId);
    }

    @POST
    @ApiOperation(value = "Create product-baseline",
            response = ProductBaselineDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of ProductBaselineDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ProductBaselineDTO createProductBaseline(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Product baseline to create") ProductBaselineDTO productBaselineDTO)
            throws UserNotActiveException, EntityNotFoundException, NotAllowedException, AccessRightException,
            PartRevisionNotReleasedException, EntityConstraintException, CreationException,
            PathToPathLinkAlreadyExistsException {

        String ciId = productBaselineDTO.getConfigurationItemId();
        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);
        String description = productBaselineDTO.getDescription();
        String name = productBaselineDTO.getName();
        ProductBaselineType type = productBaselineDTO.getType();

        List<BaselinedPartDTO> baselinedPartsDTO = productBaselineDTO.getBaselinedParts();
        List<PartIterationKey> partIterationKeys = new ArrayList<>();
        for (BaselinedPartDTO part : baselinedPartsDTO) {
            partIterationKeys.add(new PartIterationKey(workspaceId, part.getNumber(), part.getVersion(), part.getIteration()));
        }

        ProductBaseline baseline = productBaselineService.createBaseline(ciKey, name, type, description, partIterationKeys, productBaselineDTO.getSubstituteLinks(), productBaselineDTO.getOptionalUsageLinks());
        ProductBaselineDTO dto = mapper.map(baseline, ProductBaselineDTO.class);
        dto.setConfigurationItemLatestRevision(baseline.getConfigurationItem().getDesignItem().getLastRevision().getVersion());
        dto.setHasObsoletePartRevisions(!productBaselineService.getObsoletePartRevisionsInBaseline(workspaceId, baseline.getId()).isEmpty());

        return dto;
    }

    @DELETE
    @ApiOperation(value = "Delete product-baseline",
            response = Response.class)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successful deletion of ProductBaselineDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{ciId}/baselines/{baselineId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteProductBaseline(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String ciId,
            @ApiParam(required = true, value = "Baseline id") @PathParam("baselineId") int baselineId)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException,
            EntityConstraintException {

        productBaselineService.deleteBaseline(workspaceId, baselineId);
        return Response.noContent().build();
    }

    @GET
    @ApiOperation(value = "Get product-baseline",
            response = ProductBaselineDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of ProductBaselineDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{ciId}/baselines/{baselineId}")
    @Produces(MediaType.APPLICATION_JSON)
    public ProductBaselineDTO getProductBaseline(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String ciId,
            @ApiParam(required = true, value = "Baseline id") @PathParam("baselineId") int baselineId)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {

        ProductBaseline productBaseline = productBaselineService.getBaseline(baselineId);
        ProductBaselineDTO productBaselineDTO = mapper.map(productBaseline, ProductBaselineDTO.class);
        productBaselineDTO.setPathToPathLinks(getPathToPathLinksForGivenBaseline(productBaseline));
        productBaselineDTO.setConfigurationItemId(productBaseline.getConfigurationItem().getId());
        productBaselineDTO.setConfigurationItemLatestRevision(productBaseline.getConfigurationItem().getDesignItem().getLastRevision().getVersion());

        ConfigurationItemKey ciKey = productBaseline.getConfigurationItem().getKey();

        List<LightPartLinkListDTO> substitutesParts = new ArrayList<>();
        List<LightPartLinkListDTO> optionalParts = new ArrayList<>();

        for (String path : productBaseline.getSubstituteLinks()) {
            LightPartLinkListDTO lightPartLinkListDTO = new LightPartLinkListDTO();
            for (PartLink partLink : productService.decodePath(ciKey, path)) {
                lightPartLinkListDTO.getPartLinks().add(new LightPartLinkDTO(partLink.getComponent().getNumber(), partLink.getComponent().getName(),partLink.getReferenceDescription(),partLink.getFullId()));
            }
            substitutesParts.add(lightPartLinkListDTO);
        }
        for (String path : productBaseline.getOptionalUsageLinks()) {
            LightPartLinkListDTO lightPartLinkListDTO = new LightPartLinkListDTO();
            for (PartLink partLink : productService.decodePath(ciKey, path)) {
                lightPartLinkListDTO.getPartLinks().add(new LightPartLinkDTO(partLink.getComponent().getNumber(), partLink.getComponent().getName(),partLink.getReferenceDescription(),partLink.getFullId()));
            }
            optionalParts.add(lightPartLinkListDTO);
        }

        productBaselineDTO.setSubstitutesParts(substitutesParts);
        productBaselineDTO.setOptionalsParts(optionalParts);

        return productBaselineDTO;
    }

    @GET
    @ApiOperation(value = "Get product-baseline's part",
            response = BaselinedPartDTO.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of BaselinedPartDTOs. It can be an empty list."),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{ciId}/baselines/{baselineId}/parts")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProductBaselineParts(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String ciId,
            @ApiParam(required = true, value = "Baseline id") @PathParam("baselineId") int baselineId,
            @ApiParam(required = true, value = "Query") @QueryParam("q") String q)
            throws EntityNotFoundException, UserNotActiveException {

        int maxResults = 8;
        List<BaselinedPart> baselinedPartList = productBaselineService.getBaselinedPartWithReference(baselineId, q, maxResults);

        List<BaselinedPartDTO> baselinedPartDTOList = new ArrayList<>();
        for (BaselinedPart baselinedPart : baselinedPartList) {
            baselinedPartDTOList.add(Tools.mapBaselinedPartToBaselinedPartDTO(baselinedPart));
        }
        return Response.ok(new GenericEntity<List<BaselinedPartDTO>>((List<BaselinedPartDTO>) baselinedPartDTOList) {
        }).build();
    }

    @GET
    @ApiOperation(value = "Get product-baseline's path-to-path links",
            response = LightPathToPathLinkDTO.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of LightPathToPathLinkDTOs. It can be an empty list."),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{ciId}/baselines/{baselineId}/path-to-path-links-types")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPathToPathLinkTypesInBaseline(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String ciId,
            @ApiParam(required = true, value = "Baseline id") @PathParam("baselineId") int baselineId)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException,
            BaselineNotFoundException, WorkspaceNotEnabledException {

        List<String> pathToPathLinkTypes = productBaselineService.getPathToPathLinkTypes(workspaceId, ciId, baselineId);
        List<LightPathToPathLinkDTO> pathToPathLinkDTOs = new ArrayList<>();
        for (String type : pathToPathLinkTypes) {
            LightPathToPathLinkDTO pathToPathLinkDTO = new LightPathToPathLinkDTO();
            pathToPathLinkDTO.setType(type);
            pathToPathLinkDTOs.add(pathToPathLinkDTO);
        }
        return Response.ok(new GenericEntity<List<LightPathToPathLinkDTO>>((List<LightPathToPathLinkDTO>) pathToPathLinkDTOs) {
        }).build();
    }

    @GET
    @ApiOperation(value = "Get product-baseline's path-to-path links for given source and target",
            response = LightPathToPathLinkDTO.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of LightPathToPathLinkDTOs. It can be an empty list."),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{ciId}/baselines/{baselineId}/path-to-path-links/source/{sourcePath}/target/{targetPath}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPathToPathLinkInProductBaseline(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String configurationItemId,
            @ApiParam(required = true, value = "Baseline id") @PathParam("baselineId") int baselineId,
            @ApiParam(required = true, value = "Source path") @PathParam("sourcePath") String sourcePathAsString,
            @ApiParam(required = true, value = "Target path") @PathParam("targetPath") String targetPathAsString)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, AccessRightException,
            ProductInstanceMasterNotFoundException, BaselineNotFoundException, ConfigurationItemNotFoundException,
            PartUsageLinkNotFoundException, WorkspaceNotEnabledException {

        List<PathToPathLink> pathToPathLinks = productBaselineService.getPathToPathLinkFromSourceAndTarget(workspaceId, configurationItemId, baselineId, sourcePathAsString, targetPathAsString);
        List<PathToPathLinkDTO> dtos = new ArrayList<>();
        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, configurationItemId);

        for (PathToPathLink pathToPathLink : pathToPathLinks) {
            PathToPathLinkDTO pathToPathLinkDTO = mapper.map(pathToPathLink, PathToPathLinkDTO.class);
            List<LightPartLinkDTO> sourceLightPartLinkDTOs = new ArrayList<>();

            List<PartLink> sourcePath = productService.decodePath(ciKey, pathToPathLink.getSourcePath());
            List<PartLink> targetPath = productService.decodePath(ciKey, pathToPathLink.getTargetPath());

            for (PartLink partLink : sourcePath) {
                LightPartLinkDTO lightPartLinkDTO = new LightPartLinkDTO(partLink.getComponent().getNumber(), partLink.getComponent().getName(),partLink.getReferenceDescription(),partLink.getFullId());
                sourceLightPartLinkDTOs.add(lightPartLinkDTO);
            }

            List<LightPartLinkDTO> targetLightPartLinkDTOs = new ArrayList<>();
            for (PartLink partLink : targetPath) {
                LightPartLinkDTO lightPartLinkDTO = new LightPartLinkDTO(partLink.getComponent().getNumber(), partLink.getComponent().getName(),partLink.getReferenceDescription(),partLink.getFullId());
                targetLightPartLinkDTOs.add(lightPartLinkDTO);
            }

            pathToPathLinkDTO.setSourceComponents(sourceLightPartLinkDTOs);
            pathToPathLinkDTO.setTargetComponents(targetLightPartLinkDTOs);
            dtos.add(pathToPathLinkDTO);
        }

        return Response.ok(new GenericEntity<List<PathToPathLinkDTO>>((List<PathToPathLinkDTO>) dtos) {
        }).build();

    }

    private List<PathToPathLinkDTO> getPathToPathLinksForGivenBaseline(ProductBaseline productBaseline) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, ConfigurationItemNotFoundException, PartUsageLinkNotFoundException, ProductInstanceMasterNotFoundException, BaselineNotFoundException, AccessRightException, WorkspaceNotEnabledException {

        List<PathToPathLink> pathToPathLinkTypes = productBaseline.getPathToPathLinks();
        List<PathToPathLinkDTO> pathToPathLinkDTOs = new ArrayList<>();

        for (PathToPathLink pathToPathLink : pathToPathLinkTypes) {
            PathToPathLinkDTO pathToPathLinkDTO = mapper.map(pathToPathLink, PathToPathLinkDTO.class);
            List<PartLink> sourcePath = productService.decodePath(productBaseline.getConfigurationItem().getKey(), pathToPathLink.getSourcePath());
            List<PartLink> targetPath = productService.decodePath(productBaseline.getConfigurationItem().getKey(), pathToPathLink.getTargetPath());

            List<LightPartLinkDTO> sourceLightPartLinkDTOs = new ArrayList<>();
            for (PartLink partLink : sourcePath) {
                LightPartLinkDTO lightPartLinkDTO = new LightPartLinkDTO(partLink.getComponent().getNumber(), partLink.getComponent().getName(),partLink.getReferenceDescription(),partLink.getFullId());
                sourceLightPartLinkDTOs.add(lightPartLinkDTO);
            }

            List<LightPartLinkDTO> targetLightPartLinkDTOs = new ArrayList<>();
            for (PartLink partLink : targetPath) {
                LightPartLinkDTO lightPartLinkDTO = new LightPartLinkDTO(partLink.getComponent().getNumber(), partLink.getComponent().getName(),partLink.getReferenceDescription(),partLink.getFullId());
                targetLightPartLinkDTOs.add(lightPartLinkDTO);
            }

            pathToPathLinkDTO.setSourceComponents(sourceLightPartLinkDTOs);
            pathToPathLinkDTO.setTargetComponents(targetLightPartLinkDTOs);
            pathToPathLinkDTOs.add(pathToPathLinkDTO);

        }
        return pathToPathLinkDTOs;
    }

    private Response makeList(List<ProductBaseline> productBaselines, String workspaceId) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, BaselineNotFoundException, WorkspaceNotEnabledException {
        List<ProductBaselineDTO> baselinesDTO = new ArrayList<>();
        for (ProductBaseline productBaseline : productBaselines) {
            ProductBaselineDTO productBaselineDTO = mapper.map(productBaseline, ProductBaselineDTO.class);
            productBaselineDTO.setConfigurationItemId(productBaseline.getConfigurationItem().getId());
            productBaselineDTO.setConfigurationItemLatestRevision(productBaseline.getConfigurationItem().getDesignItem().getLastRevision().getVersion());
            productBaselineDTO.setHasObsoletePartRevisions(!productBaselineService.getObsoletePartRevisionsInBaseline(workspaceId, productBaseline.getId()).isEmpty());
            baselinesDTO.add(productBaselineDTO);
        }
        return Response.ok(new GenericEntity<List<ProductBaselineDTO>>((List<ProductBaselineDTO>) baselinesDTO) {
        }).build();
    }

}