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

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.configuration.*;
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.meta.InstanceAttributeTemplate;
import com.docdoku.core.product.*;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IImporterManagerLocal;
import com.docdoku.core.services.IPSFilterManagerLocal;
import com.docdoku.core.services.IProductInstanceManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.core.util.FileIO;
import com.docdoku.server.rest.dto.*;
import com.docdoku.server.rest.dto.baseline.ProductBaselineDTO;
import com.docdoku.server.rest.dto.product.ProductInstanceCreationDTO;
import com.docdoku.server.rest.dto.product.ProductInstanceIterationDTO;
import com.docdoku.server.rest.dto.product.ProductInstanceMasterDTO;
import com.docdoku.server.rest.file.util.BinaryResourceUpload;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Taylor LABEJOF
 */
@RequestScoped
@Api(hidden = true, value = "product-instances", description = "Operations about product-instances")
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class ProductInstancesResource {

    private static final Logger LOGGER = Logger.getLogger(ProductInstancesResource.class.getName());
    @Inject
    private IProductInstanceManagerLocal productInstanceService;
    @Inject
    private IProductManagerLocal productService;
    @Inject
    private IPSFilterManagerLocal psFilterService;
    @Inject
    private IImporterManagerLocal importerService;
    private Mapper mapper;

    public ProductInstancesResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @ApiOperation(value = "Get product-instance with given configuration item", response = ProductInstanceMasterDTO.class, responseContainer = "List")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProductInstances(@ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
                                        @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String ciId)
            throws EntityNotFoundException, UserNotActiveException {

        List<ProductInstanceMaster> productInstanceMasterList;
        if (ciId != null) {
            ConfigurationItemKey configurationItemKey = new ConfigurationItemKey(workspaceId, ciId);
            productInstanceMasterList = productInstanceService.getProductInstanceMasters(configurationItemKey);
        } else {
            productInstanceMasterList = productInstanceService.getProductInstanceMasters(workspaceId);
        }
        List<ProductInstanceMasterDTO> dtos = new ArrayList<>();
        for (ProductInstanceMaster productInstanceMaster : productInstanceMasterList) {
            ProductInstanceMasterDTO productInstanceMasterDTO = mapper.map(productInstanceMaster, ProductInstanceMasterDTO.class);
            productInstanceMasterDTO.setConfigurationItemId(productInstanceMaster.getInstanceOf().getId());
            dtos.add(productInstanceMasterDTO);
        }
        return Response.ok(new GenericEntity<List<ProductInstanceMasterDTO>>((List<ProductInstanceMasterDTO>) dtos) {
        }).build();
    }

    @POST
    @ApiOperation(value = "Create product-instance", response = ProductInstanceMasterDTO.class)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ProductInstanceMasterDTO createProductInstanceMaster(@ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
                                                                @ApiParam(required = true, value = "Product instance master to create") ProductInstanceCreationDTO productInstanceCreationDTO)
            throws EntityNotFoundException, EntityAlreadyExistsException, AccessRightException, CreationException, NotAllowedException, EntityConstraintException, UserNotActiveException {

        ACLDTO acldto = productInstanceCreationDTO.getAcl();
        Map<String, ACL.Permission> userEntries = new HashMap<>();
        Map<String, ACL.Permission> grpEntries = new HashMap<>();
        List<InstanceAttributeDTO> instanceAttributeDTOs = productInstanceCreationDTO.getInstanceAttributes();
        List<InstanceAttribute> attributes = new ArrayList<>();

        if (instanceAttributeDTOs != null) {
            for (InstanceAttributeDTO dto : instanceAttributeDTOs) {
                dto.setWorkspaceId(workspaceId);
                attributes.add(mapper.map(dto, InstanceAttribute.class));
            }
        }

        if (acldto != null) {
            userEntries = acldto.getUserEntries();
            grpEntries = acldto.getGroupEntries();
        }
        Set<DocumentRevisionDTO> linkedDocs = productInstanceCreationDTO.getLinkedDocuments();
        DocumentRevisionKey[] links = null;
        String[] documentLinkComments = null;
        if (linkedDocs != null) {
            documentLinkComments = new String[linkedDocs.size()];
            links = createDocumentRevisionKeys(linkedDocs);
            int i = 0;
            for (DocumentRevisionDTO docRevisionForLink : linkedDocs) {
                String comment = docRevisionForLink.getCommentLink();
                if (comment == null) {
                    comment = "";
                }
                documentLinkComments[i++] = comment;
            }
        }
        ProductInstanceMaster productInstanceMaster = productInstanceService.createProductInstance(workspaceId, new ConfigurationItemKey(workspaceId, productInstanceCreationDTO.getConfigurationItemId()), productInstanceCreationDTO.getSerialNumber(), productInstanceCreationDTO.getBaselineId(), userEntries, grpEntries, attributes, links, documentLinkComments);

        return mapper.map(productInstanceMaster, ProductInstanceMasterDTO.class);
    }

    @PUT
    @ApiOperation(value = "Update product-instance", response = ProductInstanceMasterDTO.class)
    @Path("{serialNumber}/iterations/{iteration}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ProductInstanceMasterDTO updateProductInstanceMaster(@ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
                                                                @ApiParam(required = true, value = "Serial number") @PathParam("serialNumber") String serialNumber,
                                                                @ApiParam(required = true, value = "Product instance iteration") @PathParam("iteration") int iteration,
                                                                @ApiParam(required = true, value = "Product instance master to create") ProductInstanceIterationDTO productInstanceCreationDTO)
            throws EntityNotFoundException, EntityAlreadyExistsException, AccessRightException, CreationException, UserNotActiveException {

        List<InstanceAttributeDTO> instanceAttributes = productInstanceCreationDTO.getInstanceAttributes();
        List<InstanceAttribute> attributes = new ArrayList<>();

        if (instanceAttributes != null) {
            for (InstanceAttributeDTO dto : instanceAttributes) {
                dto.setWorkspaceId(workspaceId);
                attributes.add(mapper.map(dto, InstanceAttribute.class));
            }
        }

        Set<DocumentRevisionDTO> linkedDocs = productInstanceCreationDTO.getLinkedDocuments();
        DocumentRevisionKey[] links = null;
        String[] documentLinkComments = null;
        if (linkedDocs != null) {
            documentLinkComments = new String[linkedDocs.size()];
            links = createDocumentRevisionKeys(linkedDocs);
            int i = 0;
            for (DocumentRevisionDTO docRevisionForLink : linkedDocs) {
                String comment = docRevisionForLink.getCommentLink();
                if (comment == null) {
                    comment = "";
                }
                documentLinkComments[i++] = comment;
            }
        }

        ProductInstanceMaster productInstanceMaster = productInstanceService.updateProductInstance(workspaceId, iteration, productInstanceCreationDTO.getIterationNote(), new ConfigurationItemKey(workspaceId, productInstanceCreationDTO.getConfigurationItemId()), productInstanceCreationDTO.getSerialNumber(), productInstanceCreationDTO.getBasedOn().getId(), attributes, links, documentLinkComments);

        return mapper.map(productInstanceMaster, ProductInstanceMasterDTO.class);
    }

    @GET
    @ApiOperation(value = "Get product-instance", response = ProductInstanceMasterDTO.class)
    @Path("{serialNumber}")
    @Produces(MediaType.APPLICATION_JSON)
    public ProductInstanceMasterDTO getProductInstance(@ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
                                                       @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String configurationItemId,
                                                       @ApiParam(required = true, value = "Serial number") @PathParam("serialNumber") String serialNumber)
            throws EntityNotFoundException, UserNotActiveException {

        ProductInstanceMaster productInstanceMaster = productInstanceService.getProductInstanceMaster(new ProductInstanceMasterKey(serialNumber, workspaceId, configurationItemId));
        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, configurationItemId);
        ProductInstanceMasterDTO dto = mapper.map(productInstanceMaster, ProductInstanceMasterDTO.class);


        List<ProductInstanceIterationDTO> productInstanceIterationsDTO = dto.getProductInstanceIterations();
        List<ProductInstanceIteration> productInstanceIterations = productInstanceMaster.getProductInstanceIterations();
        Iterator<ProductInstanceIteration> iterationIterator = productInstanceIterations.iterator();
        for (ProductInstanceIterationDTO productInstanceIterationDTO : productInstanceIterationsDTO) {

            List<LightPartLinkListDTO> substitutesParts = new ArrayList<>();
            List<LightPartLinkListDTO> optionalParts = new ArrayList<>();
            try {
                productInstanceIterationDTO.setPathToPathLinks(getPathToPathLinksForGivenProductInstance(iterationIterator.next()));
            } catch (AccessRightException e) {
                LOGGER.log(Level.FINEST, null, e);
            }
            for (String path : productInstanceIterationDTO.getSubstituteLinks()) {
                LightPartLinkListDTO lightPartLinkDTO = new LightPartLinkListDTO();
                for (PartLink partLink : productService.decodePath(ciKey, path)) {
                    lightPartLinkDTO.getPartLinks().add(new LightPartLinkDTO(partLink));
                }
                substitutesParts.add(lightPartLinkDTO);
            }
            for (String path : productInstanceIterationDTO.getOptionalUsageLinks()) {
                LightPartLinkListDTO lightPartLinkDTO = new LightPartLinkListDTO();
                for (PartLink partLink : productService.decodePath(ciKey, path)) {
                    lightPartLinkDTO.getPartLinks().add(new LightPartLinkDTO(partLink));
                }
                optionalParts.add(lightPartLinkDTO);
            }

            productInstanceIterationDTO.setSubstitutesParts(substitutesParts);
            productInstanceIterationDTO.setOptionalsParts(optionalParts);

            List<LightPartLinkListDTO> pathDataPaths = new ArrayList<>();
            for (PathDataMasterDTO pathDataMasterDTO : productInstanceIterationDTO.getPathDataMasterList()) {
                LightPartLinkListDTO lightPartLinkListDTO = new LightPartLinkListDTO();
                for (PartLink partLink : productService.decodePath(ciKey, pathDataMasterDTO.getPath())) {
                    lightPartLinkListDTO.getPartLinks().add(new LightPartLinkDTO(partLink));
                }
                pathDataPaths.add(lightPartLinkListDTO);
            }
            productInstanceIterationDTO.setPathDataPaths(pathDataPaths);
        }

        return dto;
    }

    @DELETE
    @ApiOperation(value = "Remove attached file from product-instance", response = Response.class)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{serialNumber}/iterations/{iteration}/files/{fileName}")
    public Response removeAttachedFile(@ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
                                       @ApiParam(required = true, value = "Product instance iteration") @PathParam("iteration") int iteration,
                                       @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String configurationItemId,
                                       @ApiParam(required = true, value = "Serial number") @PathParam("serialNumber") String serialNumber,
                                       @ApiParam(required = true, value = "File name") @PathParam("fileName") String fileName)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException {

        String fullName = workspaceId + "/product-instances/" + serialNumber + "/iterations/" + iteration + "/" + fileName;
        productInstanceService.removeFileFromProductInstanceIteration(workspaceId, iteration, fullName, new ProductInstanceMasterKey(serialNumber, workspaceId, configurationItemId));
        return Response.ok().build();
    }


    @PUT
    @ApiOperation(value = "Update product-instance's ACL", response = Response.class)
    @Path("{serialNumber}/acl")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateProductInstanceACL(@ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
                                             @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String configurationItemId,
                                             @ApiParam(required = true, value = "Serial number") @PathParam("serialNumber") String serialNumber,
                                             @ApiParam(required = true, value = "ACL to set") ACLDTO acl)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException, NotAllowedException {

        if (!acl.getGroupEntries().isEmpty() || !acl.getUserEntries().isEmpty()) {

            Map<String, String> userEntries = new HashMap<>();
            Map<String, String> groupEntries = new HashMap<>();

            for (Map.Entry<String, ACL.Permission> entry : acl.getUserEntries().entrySet()) {
                userEntries.put(entry.getKey(), entry.getValue().name());
            }

            for (Map.Entry<String, ACL.Permission> entry : acl.getGroupEntries().entrySet()) {
                groupEntries.put(entry.getKey(), entry.getValue().name());
            }

            productInstanceService.updateACLForProductInstanceMaster(workspaceId, configurationItemId, serialNumber, userEntries, groupEntries);
        } else {
            productInstanceService.removeACLFromProductInstanceMaster(workspaceId, configurationItemId, serialNumber);
        }

        return Response.ok().build();
    }

    @DELETE
    @ApiOperation(value = "Delete product-instance", response = Response.class)
    @Path("{serialNumber}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteProductInstanceMaster(@ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
                                                @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String configurationItemId,
                                                @ApiParam(required = true, value = "Serial number") @PathParam("serialNumber") String serialNumber)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException {

        productInstanceService.deleteProductInstance(workspaceId, configurationItemId, serialNumber);
        return Response.ok().build();
    }

    @GET
    @ApiOperation(value = "Get product-instance's iterations", response = ProductInstanceIterationDTO.class, responseContainer = "List")
    @Path("{serialNumber}/iterations")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProductInstanceIterations(@ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
                                                 @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String configurationItemId,
                                                 @ApiParam(required = true, value = "Serial number") @PathParam("serialNumber") String serialNumber)
            throws EntityNotFoundException, UserNotActiveException {

        List<ProductInstanceIteration> productInstanceIterationList = productInstanceService.getProductInstanceIterations(new ProductInstanceMasterKey(serialNumber, workspaceId, configurationItemId));
        List<ProductInstanceIterationDTO> dtos = new ArrayList<>();
        for (ProductInstanceIteration productInstanceIteration : productInstanceIterationList) {
            ProductInstanceIterationDTO productInstanceIterationDTO = mapper.map(productInstanceIteration, ProductInstanceIterationDTO.class);
            dtos.add(productInstanceIterationDTO);
        }
        return Response.ok(new GenericEntity<List<ProductInstanceIterationDTO>>((List<ProductInstanceIterationDTO>) dtos) {
        }).build();
    }


    @GET
    @ApiOperation(value = "Get product-instance's iteration", response = ProductInstanceIterationDTO.class)
    @Path("{serialNumber}/iterations/{iteration}")
    @Produces(MediaType.APPLICATION_JSON)
    public ProductInstanceIterationDTO getProductInstanceIteration(@ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
                                                                   @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String configurationItemId,
                                                                   @ApiParam(required = true, value = "Serial number") @PathParam("serialNumber") String serialNumber,
                                                                   @ApiParam(required = true, value = "Product instance iteration") @PathParam("iteration") int iteration)
            throws EntityNotFoundException, UserNotActiveException {

        ProductInstanceIteration productInstanceIteration = productInstanceService.getProductInstanceIteration(new ProductInstanceIterationKey(serialNumber, workspaceId, configurationItemId, iteration));
        return mapper.map(productInstanceIteration, ProductInstanceIterationDTO.class);
    }

    @PUT
    @ApiOperation(value = "Rebase product-instance", response = Response.class)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{serialNumber}/rebase")
    public Response rebaseProductInstance(@ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
                                          @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String configurationItemId,
                                          @ApiParam(required = true, value = "Serial number") @PathParam("serialNumber") String serialNumber,
                                          @ApiParam(required = true, value = "Product baseline to rebase with") ProductBaselineDTO baselineDTO)
            throws UserNotActiveException, WorkspaceNotFoundException, BaselineNotFoundException, UserNotFoundException, ProductInstanceMasterNotFoundException, AccessRightException, NotAllowedException, ConfigurationItemNotFoundException, PathToPathLinkAlreadyExistsException, PartMasterNotFoundException, CreationException, EntityConstraintException, WorkspaceNotEnabledException {

        productInstanceService.rebaseProductInstance(workspaceId, serialNumber, new ConfigurationItemKey(workspaceId, configurationItemId), baselineDTO.getId());
        return Response.ok().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{serialNumber}/iterations/{iteration}/files/{fileName}")
    public FileDTO renameAttachedFile(@ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
                                      @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String configurationItemId,
                                      @ApiParam(required = true, value = "Serial number") @PathParam("serialNumber") String serialNumber,
                                      @ApiParam(required = true, value = "Product instance iteration") @PathParam("iteration") int iteration,
                                      @ApiParam(required = true, value = "File name") @PathParam("fileName") String fileName,
                                      @ApiParam(required = true, value = "Renamed file") FileDTO fileDTO)
            throws UserNotActiveException, WorkspaceNotFoundException, CreationException, UserNotFoundException, FileNotFoundException, NotAllowedException, FileAlreadyExistsException, ProductInstanceMasterNotFoundException, AccessRightException, StorageException, WorkspaceNotEnabledException {
        String fullName = workspaceId + "/product-instances/" + serialNumber + "/iterations/" + iteration + "/" + fileName;
        BinaryResource binaryResource = productInstanceService.renameFileInProductInstance(fullName, fileDTO.getShortName(), serialNumber, configurationItemId, iteration);
        return new FileDTO(true, binaryResource.getFullName(), binaryResource.getName());
    }

    @GET
    @ApiOperation(value = "Get product-instance's path-data", response = PathDataMasterDTO.class)
    @Path("{serialNumber}/pathdata/{path}")
    @Produces(MediaType.APPLICATION_JSON)
    public PathDataMasterDTO getPathData(@ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
                                         @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String configurationItemId,
                                         @ApiParam(required = true, value = "Serial number") @PathParam("serialNumber") String serialNumber,
                                         @ApiParam(required = true, value = "Complete path in context") @PathParam("path") String pathAsString)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, AccessRightException, ProductInstanceMasterNotFoundException, ConfigurationItemNotFoundException, PartUsageLinkNotFoundException, BaselineNotFoundException, WorkspaceNotEnabledException {
        PathDataMaster pathDataMaster = productInstanceService.getPathDataByPath(workspaceId, configurationItemId, serialNumber, pathAsString);

        PathDataMasterDTO dto = pathDataMaster == null ? new PathDataMasterDTO(pathAsString) : mapper.map(pathDataMaster, PathDataMasterDTO.class);

        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, configurationItemId);

        LightPartLinkListDTO partLinksList = new LightPartLinkListDTO();
        List<PartLink> path = productService.decodePath(ciKey, pathAsString);
        for (PartLink partLink : path) {
            partLinksList.getPartLinks().add(new LightPartLinkDTO(partLink));
        }
        dto.setPartLinksList(partLinksList);

        List<InstanceAttributeDTO> attributesDTO = new ArrayList<>();
        List<InstanceAttributeTemplateDTO> attributeTemplatesDTO = new ArrayList<>();
        PartLink partLink = path.get(path.size() - 1);
        ProductStructureFilter filter = psFilterService.getPSFilter(ciKey, "pi-" + serialNumber, false);
        List<PartIteration> partIterations = filter.filter(partLink.getComponent());
        PartIteration partIteration = partIterations.get(0);

        if (partIteration != null) {
            for (InstanceAttribute instanceAttribute : partIteration.getInstanceAttributes()) {
                attributesDTO.add(mapper.map(instanceAttribute, InstanceAttributeDTO.class));
            }
            dto.setPartAttributes(attributesDTO);
            for (InstanceAttributeTemplate instanceAttributeTemplate : partIteration.getInstanceAttributeTemplates()) {
                attributeTemplatesDTO.add(mapper.map(instanceAttributeTemplate, InstanceAttributeTemplateDTO.class));
            }
            dto.setPartAttributeTemplates(attributeTemplatesDTO);
        }

        return dto;
    }


    @PUT
    @ApiOperation(value = "Rename product-instance's attached file", response = FileDTO.class)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{serialNumber}/pathdata/{pathDataId}/iterations/{iteration}/files/{fileName}")
    public FileDTO renameAttachedFileInPathData(@ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
                                                @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String configurationItemId,
                                                @ApiParam(required = true, value = "Serial number") @PathParam("serialNumber") String serialNumber,
                                                @ApiParam(required = true, value = "Path data master id") @PathParam("pathDataId") int pathDataId,
                                                @ApiParam(required = true, value = "Product instance iteration") @PathParam("iteration") int iteration,
                                                @ApiParam(required = true, value = "File name") @PathParam("fileName") String fileName,
                                                @ApiParam(required = true, value = "Renamed file") FileDTO fileDTO)
            throws UserNotActiveException, WorkspaceNotFoundException, CreationException, UserNotFoundException, FileNotFoundException, NotAllowedException, FileAlreadyExistsException, ProductInstanceMasterNotFoundException, AccessRightException, StorageException, WorkspaceNotEnabledException {

        String fullName = workspaceId + "/product-instances/" + serialNumber + "/pathdata/" + pathDataId + "/iterations/" + iteration + "/" + fileName;
        BinaryResource binaryResource = productInstanceService.renameFileInPathData(workspaceId, configurationItemId, serialNumber, pathDataId, iteration, fullName, fileDTO.getShortName());
        return new FileDTO(true, binaryResource.getFullName(), binaryResource.getName());
    }

    @DELETE
    @ApiOperation(value = "Delete product-instance's attached file", response = Response.class)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{serialNumber}/pathdata/{pathDataId}/iterations/{iteration}/files/{fileName}")
    public Response deleteAttachedFileInPathData(@ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
                                                 @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String configurationItemId,
                                                 @ApiParam(required = true, value = "Serial number") @PathParam("serialNumber") String serialNumber,
                                                 @ApiParam(required = true, value = "Path data master id") @PathParam("pathDataId") int pathDataId,
                                                 @ApiParam(required = true, value = "Product instance iteration") @PathParam("iteration") int iteration,
                                                 @ApiParam(required = true, value = "File name") @PathParam("fileName") String fileName
    ) throws UserNotActiveException, WorkspaceNotFoundException, CreationException, UserNotFoundException, FileNotFoundException, NotAllowedException, FileAlreadyExistsException, ProductInstanceMasterNotFoundException, AccessRightException, WorkspaceNotEnabledException {

        String fullName = workspaceId + "/product-instances/" + serialNumber + "/pathdata/" + pathDataId + "/iterations/" + iteration + "/" + fileName;
        ProductInstanceMaster productInstanceMaster = productInstanceService.getProductInstanceMaster(new ProductInstanceMasterKey(serialNumber, workspaceId, configurationItemId));
        productInstanceService.removeFileFromPathData(workspaceId, configurationItemId, serialNumber, pathDataId, iteration, fullName, productInstanceMaster);
        return Response.ok().build();
    }

    @DELETE
    @ApiOperation(value = "Delete product-instance's path-data", response = Response.class)
    @Path("{serialNumber}/pathdata/{pathDataId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deletePathData(@ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
                                   @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String configurationItemId,
                                   @ApiParam(required = true, value = "Serial number") @PathParam("serialNumber") String serialNumber,
                                   @ApiParam(required = true, value = "Path data master id") @PathParam("pathDataId") int pathDataId)
            throws UserNotActiveException, WorkspaceNotFoundException, UserNotFoundException, ProductInstanceMasterNotFoundException, AccessRightException, NotAllowedException, WorkspaceNotEnabledException {
        productInstanceService.deletePathData(workspaceId, configurationItemId, serialNumber, pathDataId);
        return Response.ok().build();
    }

    @POST
    @ApiOperation(value = "Add new path-data iteration", response = PathDataMasterDTO.class)
    @Path("{serialNumber}/pathdata/{pathDataId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PathDataMasterDTO addNewPathDataIteration(@ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
                                                     @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String configurationItemId,
                                                     @ApiParam(required = true, value = "Serial number") @PathParam("serialNumber") String serialNumber,
                                                     @ApiParam(required = true, value = "Path data master id") @PathParam("pathDataId") int pathDataId,
                                                     @ApiParam(required = true, value = "Path data iteration to create") PathDataIterationCreationDTO pathDataIterationCreationDTO) throws UserNotFoundException, AccessRightException, UserNotActiveException, ProductInstanceMasterNotFoundException, WorkspaceNotFoundException, NotAllowedException, PathDataAlreadyExistsException, FileAlreadyExistsException, CreationException, ConfigurationItemNotFoundException, PartUsageLinkNotFoundException, BaselineNotFoundException, PathDataMasterNotFoundException, DocumentRevisionNotFoundException, WorkspaceNotEnabledException {

        List<InstanceAttributeDTO> instanceAttributeDTOs = pathDataIterationCreationDTO.getInstanceAttributes();
        List<InstanceAttribute> attributes = new ArrayList<>();

        if (instanceAttributeDTOs != null) {
            for (InstanceAttributeDTO dto : instanceAttributeDTOs) {
                dto.setWorkspaceId(workspaceId);
                attributes.add(mapper.map(dto, InstanceAttribute.class));
            }
        }

        Set<DocumentRevisionDTO> linkedDocs = pathDataIterationCreationDTO.getLinkedDocuments();
        DocumentRevisionKey[] links = null;
        String[] documentLinkComments = null;
        if (linkedDocs != null) {
            documentLinkComments = new String[linkedDocs.size()];
            links = createDocumentRevisionKeys(linkedDocs);
            int i = 0;
            for (DocumentRevisionDTO docRevisionForLink : linkedDocs) {
                String comment = docRevisionForLink.getCommentLink();
                if (comment == null) {
                    comment = "";
                }
                documentLinkComments[i++] = comment;
            }
        }


        PathDataMaster pathDataMaster = productInstanceService.addNewPathDataIteration(workspaceId, configurationItemId, serialNumber, pathDataId, attributes, pathDataIterationCreationDTO.getIterationNote(), links, documentLinkComments);
        PathDataMasterDTO dto = mapper.map(pathDataMaster, PathDataMasterDTO.class);

        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, configurationItemId);

        LightPartLinkListDTO partLinksList = new LightPartLinkListDTO();
        List<PartLink> path = productService.decodePath(ciKey, pathDataIterationCreationDTO.getPath());
        for (PartLink partLink : path) {
            partLinksList.getPartLinks().add(new LightPartLinkDTO(partLink));
        }
        dto.setPartLinksList(partLinksList);

        List<InstanceAttributeDTO> attributesDTO = new ArrayList<>();
        PartLink partLink = path.get(path.size() - 1);
        ProductStructureFilter filter = psFilterService.getPSFilter(ciKey, "pi-" + serialNumber, false);
        List<PartIteration> partIterations = filter.filter(partLink.getComponent());
        PartIteration partIteration = partIterations.get(0);

        if (partIteration != null) {
            for (InstanceAttribute instanceAttribute : partIteration.getInstanceAttributes()) {
                attributesDTO.add(mapper.map(instanceAttribute, InstanceAttributeDTO.class));
            }
            dto.setPartAttributes(attributesDTO);
        }

        return dto;
    }

    @POST
    @ApiOperation(value = "Create new path-data", response = PathDataMasterDTO.class)
    @Path("{serialNumber}/pathdata/{path}/new")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PathDataMasterDTO createPathDataMaster(@ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
                                                  @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String configurationItemId,
                                                  @ApiParam(required = true, value = "Serial number") @PathParam("serialNumber") String serialNumber,
                                                  @ApiParam(required = true, value = "Complete path in context") @PathParam("path") String pathAsString,
                                                  @ApiParam(required = true, value = "Path data iteration create") PathDataIterationCreationDTO pathDataIterationCreationDTO)
            throws UserNotFoundException, AccessRightException, UserNotActiveException, ProductInstanceMasterNotFoundException, WorkspaceNotFoundException, NotAllowedException, PathDataAlreadyExistsException, FileAlreadyExistsException, CreationException, ConfigurationItemNotFoundException, PartUsageLinkNotFoundException, WorkspaceNotEnabledException {

        List<InstanceAttributeDTO> instanceAttributeDTOs = pathDataIterationCreationDTO.getInstanceAttributes();
        List<InstanceAttribute> attributes = new ArrayList<>();

        if (instanceAttributeDTOs != null) {
            for (InstanceAttributeDTO dto : instanceAttributeDTOs) {
                dto.setWorkspaceId(workspaceId);
                attributes.add(mapper.map(dto, InstanceAttribute.class));
            }
        }


        PathDataMaster pathDataMaster = productInstanceService.createPathDataMaster(workspaceId, configurationItemId, serialNumber, pathAsString, attributes, pathDataIterationCreationDTO.getIterationNote());

        PathDataMasterDTO dto = mapper.map(pathDataMaster, PathDataMasterDTO.class);

        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, configurationItemId);

        LightPartLinkListDTO partLinksList = new LightPartLinkListDTO();
        List<PartLink> path = productService.decodePath(ciKey, pathAsString);
        for (PartLink partLink : path) {
            partLinksList.getPartLinks().add(new LightPartLinkDTO(partLink));
        }
        dto.setPartLinksList(partLinksList);

        return dto;
    }

    @PUT
    @ApiOperation(value = "Update path-data", response = PathDataMasterDTO.class)
    @Path("{serialNumber}/pathdata/{pathDataId}/iterations/{iteration}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PathDataMasterDTO updatePathData(@ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
                                            @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String configurationItemId,
                                            @ApiParam(required = true, value = "Serial number") @PathParam("serialNumber") String serialNumber,
                                            @ApiParam(required = true, value = "Path data master id") @PathParam("pathDataId") int pathDataId,
                                            @ApiParam(required = true, value = "Product instance iteration") @PathParam("iteration") int iteration,
                                            @ApiParam(required = true, value = "Path data iteration to update") PathDataIterationCreationDTO pathDataIterationCreationDTO)
            throws UserNotFoundException, AccessRightException, UserNotActiveException, ProductInstanceMasterNotFoundException, WorkspaceNotFoundException, NotAllowedException, PathDataAlreadyExistsException, ConfigurationItemNotFoundException, PartUsageLinkNotFoundException, DocumentRevisionNotFoundException, WorkspaceNotEnabledException {

        List<InstanceAttributeDTO> instanceAttributeDTOs = pathDataIterationCreationDTO.getInstanceAttributes();
        List<InstanceAttribute> attributes = new ArrayList<>();

        if (instanceAttributeDTOs != null) {
            for (InstanceAttributeDTO dto : instanceAttributeDTOs) {
                dto.setWorkspaceId(workspaceId);
                attributes.add(mapper.map(dto, InstanceAttribute.class));
            }
        }

        Set<DocumentRevisionDTO> linkedDocs = pathDataIterationCreationDTO.getLinkedDocuments();
        DocumentRevisionKey[] links = null;
        String[] documentLinkComments = null;
        if (linkedDocs != null) {
            documentLinkComments = new String[linkedDocs.size()];
            links = createDocumentRevisionKeys(linkedDocs);
            int i = 0;
            for (DocumentRevisionDTO docRevisionForLink : linkedDocs) {
                String comment = docRevisionForLink.getCommentLink();
                if (comment == null) {
                    comment = "";
                }
                documentLinkComments[i++] = comment;
            }
        }

        PathDataMaster pathDataMaster = productInstanceService.updatePathData(workspaceId, configurationItemId, serialNumber, pathDataIterationCreationDTO.getId(), iteration, attributes, pathDataIterationCreationDTO.getIterationNote(), links, documentLinkComments);


        PathDataMasterDTO dto = mapper.map(pathDataMaster, PathDataMasterDTO.class);

        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, configurationItemId);

        LightPartLinkListDTO partLinksList = new LightPartLinkListDTO();
        List<PartLink> path = productService.decodePath(ciKey, dto.getPath());
        for (PartLink partLink : path) {
            partLinksList.getPartLinks().add(new LightPartLinkDTO(partLink));
        }
        dto.setPartLinksList(partLinksList);

        return dto;
    }

    @GET
    @ApiOperation(value = "Get path-to-path link types", response = LightPathToPathLinkDTO.class, responseContainer = "List")
    @Path("{serialNumber}/path-to-path-links-types")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPathToPathLinkTypes(@ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
                                           @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String configurationItemId,
                                           @ApiParam(required = true, value = "Serial number") @PathParam("serialNumber") String serialNumber)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, AccessRightException, ProductInstanceMasterNotFoundException, WorkspaceNotEnabledException {
        List<String> pathToPathLinkTypes = productInstanceService.getPathToPathLinkTypes(workspaceId, configurationItemId, serialNumber);
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
    @ApiOperation(value = "Get part from path-to-path link", response = LightPartMasterDTO.class)
    @Path("{serialNumber}/link-path-part/{pathPart}")
    @Produces(MediaType.APPLICATION_JSON)
    public LightPartMasterDTO getPartFromPathLink(@ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
                                                  @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String configurationItemId,
                                                  @ApiParam(required = true, value = "Serial number") @PathParam("serialNumber") String serialNumber,
                                                  @ApiParam(required = true, value = "Complete path to the part") @PathParam("pathPart") String partPath)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, AccessRightException, ProductInstanceMasterNotFoundException, PartUsageLinkNotFoundException, ConfigurationItemNotFoundException, WorkspaceNotEnabledException {

        PartMaster partMaster = productService.getPartMasterFromPath(workspaceId, configurationItemId, partPath);
        LightPartMasterDTO lightPartMasterDTO = new LightPartMasterDTO();
        lightPartMasterDTO.setPartName(partMaster.getName());
        lightPartMasterDTO.setPartNumber(partMaster.getNumber());
        return lightPartMasterDTO;

    }

    @GET
    @ApiOperation(value = "Get path-to-path links", response = LightPathToPathLinkDTO.class, responseContainer = "List")
    @Path("{serialNumber}/path-to-path-links")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPathToPathLinks(@ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
                                       @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String configurationItemId,
                                       @ApiParam(required = true, value = "Serial number") @PathParam("serialNumber") String serialNumber)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, AccessRightException, ProductInstanceMasterNotFoundException, WorkspaceNotEnabledException {
        List<PathToPathLink> pathToPathLinkTypes = productInstanceService.getPathToPathLinks(workspaceId, configurationItemId, serialNumber);
        List<LightPathToPathLinkDTO> pathToPathLinkDTOs = new ArrayList<>();
        for (PathToPathLink pathToPathLink : pathToPathLinkTypes) {
            pathToPathLinkDTOs.add(mapper.map(pathToPathLink, LightPathToPathLinkDTO.class));

        }
        return Response.ok(new GenericEntity<List<LightPathToPathLinkDTO>>((List<LightPathToPathLinkDTO>) pathToPathLinkDTOs) {
        }).build();
    }


    @GET
    @ApiOperation(value = "Get path-to-path link", response = LightPathToPathLinkDTO.class)
    @Path("{serialNumber}/path-to-path-links/{pathToPathLinkId}")
    @Produces(MediaType.APPLICATION_JSON)
    public LightPathToPathLinkDTO getPathToPathLink(@ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
                                                    @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String configurationItemId,
                                                    @ApiParam(required = true, value = "Serial number") @PathParam("serialNumber") String serialNumber,
                                                    @ApiParam(required = true, value = "Path to path link id") @PathParam("pathToPathLinkId") int pathToPathLinkId)
            throws UserNotActiveException, WorkspaceNotFoundException, UserNotFoundException, ProductInstanceMasterNotFoundException, AccessRightException, PathToPathLinkNotFoundException, WorkspaceNotEnabledException {
        PathToPathLink pathToPathLink = productInstanceService.getPathToPathLink(workspaceId, configurationItemId, serialNumber, pathToPathLinkId);
        return mapper.map(pathToPathLink, LightPathToPathLinkDTO.class);
    }

    @GET
    @ApiOperation(value = "Get path-to-path link for given source and target", response = LightPathToPathLinkDTO.class, responseContainer = "List")
    @Path("{serialNumber}/path-to-path-links/source/{sourcePath}/target/{targetPath}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPathToPathLinksForGivenSourceAndTarget(@ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
                                                              @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String configurationItemId,
                                                              @ApiParam(required = true, value = "Serial number") @PathParam("serialNumber") String serialNumber,
                                                              @ApiParam(required = true, value = "Complete source path") @PathParam("sourcePath") String sourcePathAsString,
                                                              @ApiParam(required = true, value = "Complete target path") @PathParam("targetPath") String targetPathAsString)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, AccessRightException, ProductInstanceMasterNotFoundException, ConfigurationItemNotFoundException, PartUsageLinkNotFoundException, WorkspaceNotEnabledException {
        List<PathToPathLink> pathToPathLinks = productInstanceService.getPathToPathLinkFromSourceAndTarget(workspaceId, configurationItemId, serialNumber, sourcePathAsString, targetPathAsString);
        List<PathToPathLinkDTO> dtos = new ArrayList<>();
        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, configurationItemId);

        for (PathToPathLink pathToPathLink : pathToPathLinks) {
            PathToPathLinkDTO pathToPathLinkDTO = mapper.map(pathToPathLink, PathToPathLinkDTO.class);
            List<LightPartLinkDTO> sourceLightPartLinkDTOs = new ArrayList<>();

            List<PartLink> sourcePath = productService.decodePath(ciKey, pathToPathLink.getSourcePath());
            List<PartLink> targetPath = productService.decodePath(ciKey, pathToPathLink.getTargetPath());

            for (PartLink partLink : sourcePath) {
                LightPartLinkDTO lightPartLinkDTO = new LightPartLinkDTO(partLink);
                sourceLightPartLinkDTOs.add(lightPartLinkDTO);
            }

            List<LightPartLinkDTO> targetLightPartLinkDTOs = new ArrayList<>();
            for (PartLink partLink : targetPath) {
                LightPartLinkDTO lightPartLinkDTO = new LightPartLinkDTO(partLink);
                targetLightPartLinkDTOs.add(lightPartLinkDTO);
            }

            pathToPathLinkDTO.setSourceComponents(sourceLightPartLinkDTOs);
            pathToPathLinkDTO.setTargetComponents(targetLightPartLinkDTOs);
            dtos.add(pathToPathLinkDTO);
        }

        return Response.ok(new GenericEntity<List<PathToPathLinkDTO>>((List<PathToPathLinkDTO>) dtos) {
        }).build();
    }

    @GET
    @ApiOperation(value = "Get root path-to-path links", response = LightPathToPathLinkDTO.class, responseContainer = "List")
    @Path("{serialNumber}/path-to-path-links-roots/{type}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRootPathToPathLinks(@ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
                                           @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String configurationItemId,
                                           @ApiParam(required = true, value = "Serial number") @PathParam("serialNumber") String serialNumber,
                                           @ApiParam(required = true, value = "Link type") @PathParam("type") String type)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, AccessRightException, ProductInstanceMasterNotFoundException, WorkspaceNotEnabledException {
        List<PathToPathLink> pathToPathLinks = productInstanceService.getRootPathToPathLinks(workspaceId, configurationItemId, serialNumber, type);
        List<LightPathToPathLinkDTO> dtos = new ArrayList<>();
        for (PathToPathLink pathToPathLink : pathToPathLinks) {
            dtos.add(mapper.map(pathToPathLink, LightPathToPathLinkDTO.class));
        }
        return Response.ok(new GenericEntity<List<LightPathToPathLinkDTO>>((List<LightPathToPathLinkDTO>) dtos) {
        }).build();
    }

    @POST
    @ApiOperation(value = "Import attribute into product-instance", response = Response.class)
    @Path("import")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response importAttributes(@Context HttpServletRequest request,
                                     @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
                                     @ApiParam(required = false, value = "Auto freeze after update flag") @QueryParam("autoFreezeAfterUpdate") boolean autoFreezeAfterUpdate,
                                     @ApiParam(required = false, value = "Permissive update flag") @QueryParam("permissiveUpdate") boolean permissiveUpdate,
                                     @ApiParam(required = false, value = "Revision note to set") @QueryParam("revisionNote") String revisionNote)
            throws Exception {

        Collection<Part> parts = request.getParts();

        if (parts.isEmpty() || parts.size() > 1) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        Part part = parts.iterator().next();
        String name = FileIO.getFileNameWithoutExtension(part.getSubmittedFileName());
        String extension = FileIO.getExtension(part.getSubmittedFileName());

        File importFile = Files.createTempFile("product-" + name, "-import.tmp" + (extension == null ? "" : "." + extension)).toFile();
        long length = BinaryResourceUpload.uploadBinary(new BufferedOutputStream(new FileOutputStream(importFile)), part);
        importerService.importIntoPathData(workspaceId, importFile, name + "." + extension, revisionNote, autoFreezeAfterUpdate, permissiveUpdate);

        importFile.deleteOnExit();

        return Response.noContent().build();

    }


    private DocumentRevisionKey[] createDocumentRevisionKeys(Set<DocumentRevisionDTO> dtos) {
        DocumentRevisionKey[] data = new DocumentRevisionKey[dtos.size()];
        int i = 0;
        for (DocumentRevisionDTO dto : dtos) {
            data[i++] = new DocumentRevisionKey(dto.getWorkspaceId(), dto.getDocumentMasterId(), dto.getVersion());
        }
        return data;
    }

    private List<PathToPathLinkDTO> getPathToPathLinksForGivenProductInstance(ProductInstanceIteration productInstanceIteration) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, AccessRightException, ProductInstanceMasterNotFoundException, PartUsageLinkNotFoundException, ConfigurationItemNotFoundException, WorkspaceNotEnabledException {
        List<PathToPathLink> pathToPathLinkTypes = productInstanceIteration.getPathToPathLinks();
        List<PathToPathLinkDTO> pathToPathLinkDTOs = new ArrayList<>();

        for (PathToPathLink pathToPathLink : pathToPathLinkTypes) {
            PathToPathLinkDTO pathToPathLinkDTO = mapper.map(pathToPathLink, PathToPathLinkDTO.class);
            List<PartLink> sourcePath = productService.decodePath(productInstanceIteration.getBasedOn().getConfigurationItem().getKey(), pathToPathLink.getSourcePath());
            List<PartLink> targetPath = productService.decodePath(productInstanceIteration.getBasedOn().getConfigurationItem().getKey(), pathToPathLink.getTargetPath());

            List<LightPartLinkDTO> sourceLightPartLinkDTOs = new ArrayList<>();
            for (PartLink partLink : sourcePath) {
                LightPartLinkDTO lightPartLinkDTO = new LightPartLinkDTO(partLink);
                sourceLightPartLinkDTOs.add(lightPartLinkDTO);
            }

            List<LightPartLinkDTO> targetLightPartLinkDTOs = new ArrayList<>();
            for (PartLink partLink : targetPath) {
                LightPartLinkDTO lightPartLinkDTO = new LightPartLinkDTO(partLink);
                targetLightPartLinkDTOs.add(lightPartLinkDTO);
            }

            pathToPathLinkDTO.setSourceComponents(sourceLightPartLinkDTOs);
            pathToPathLinkDTO.setTargetComponents(targetLightPartLinkDTOs);
            pathToPathLinkDTOs.add(pathToPathLinkDTO);
        }
        return pathToPathLinkDTOs;
    }

}
