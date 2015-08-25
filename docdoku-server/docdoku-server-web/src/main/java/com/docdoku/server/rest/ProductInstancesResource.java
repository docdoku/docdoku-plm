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
import com.docdoku.core.services.IProductInstanceManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.server.rest.dto.*;
import com.docdoku.server.rest.dto.baseline.BaselineDTO;
import com.docdoku.server.rest.dto.product.ProductInstanceCreationDTO;
import com.docdoku.server.rest.dto.product.ProductInstanceIterationDTO;
import com.docdoku.server.rest.dto.product.ProductInstanceMasterDTO;
import com.docdoku.server.rest.util.InstanceAttributeFactory;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

import javax.annotation.PostConstruct;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Taylor LABEJOF
 */
@Stateless
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class ProductInstancesResource {

    @EJB
    private IProductInstanceManagerLocal productInstanceService;

    @EJB
    private IProductManagerLocal productService;

    private Mapper mapper;

    private static final Logger LOGGER = Logger.getLogger(ProductInstancesResource.class.getName());

    public ProductInstancesResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ProductInstanceMasterDTO> getProductInstances(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId)
            throws EntityNotFoundException, UserNotActiveException {

        List<ProductInstanceMaster> productInstanceMasterList;
        if(ciId != null) {
            ConfigurationItemKey configurationItemKey = new ConfigurationItemKey(workspaceId, ciId);
            productInstanceMasterList = productInstanceService.getProductInstanceMasters(configurationItemKey);
        }else{
            productInstanceMasterList = productInstanceService.getProductInstanceMasters(workspaceId);
        }
        List<ProductInstanceMasterDTO> productInstanceMasterDTOList = new ArrayList<>();
        for(ProductInstanceMaster productInstanceMaster : productInstanceMasterList){
            ProductInstanceMasterDTO productInstanceMasterDTO = mapper.map(productInstanceMaster,ProductInstanceMasterDTO.class);
            productInstanceMasterDTO.setConfigurationItemId(productInstanceMaster.getInstanceOf().getId());
            productInstanceMasterDTOList.add(productInstanceMasterDTO);
        }
        return productInstanceMasterDTOList;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ProductInstanceMasterDTO createProductInstanceMaster(@PathParam("workspaceId") String workspaceId, ProductInstanceCreationDTO productInstanceCreationDTO)
            throws EntityNotFoundException, EntityAlreadyExistsException, AccessRightException, CreationException, NotAllowedException, EntityConstraintException, UserNotActiveException {

        InstanceAttributeFactory factory = new InstanceAttributeFactory();
        ACLDTO acldto = productInstanceCreationDTO.getAcl();
        Map<String,ACL.Permission> userEntries=new HashMap<>();
        Map<String,ACL.Permission> grpEntries=new HashMap<>();
        List<InstanceAttributeDTO> instanceAttributes = productInstanceCreationDTO.getInstanceAttributes();
        List<InstanceAttribute> attributes = new ArrayList<>();
        if (instanceAttributes != null) {
            attributes = factory.createInstanceAttributes(instanceAttributes);
        }
        if(acldto != null){
            userEntries = acldto.getUserEntries();
            grpEntries= acldto.getGroupEntries();
        }
        Set<DocumentRevisionDTO> linkedDocs = productInstanceCreationDTO.getLinkedDocuments();
        DocumentRevisionKey[] links = null;
        String[] documentLinkComments = null;
        if (linkedDocs != null) {
            documentLinkComments = new String[linkedDocs.size()];
            links = createDocumentRevisionKeys(linkedDocs);
            int i = 0;
            for (DocumentRevisionDTO docRevisionForLink : linkedDocs){
                String comment = docRevisionForLink.getCommentLink();
                if (comment == null){
                    comment = "";
                }
                documentLinkComments[i++] = comment;
            }
        }
        ProductInstanceMaster productInstanceMaster = productInstanceService.createProductInstance(workspaceId, new ConfigurationItemKey(workspaceId, productInstanceCreationDTO.getConfigurationItemId()), productInstanceCreationDTO.getSerialNumber(), productInstanceCreationDTO.getBaselineId(), userEntries, grpEntries, attributes, links, documentLinkComments);

        return mapper.map(productInstanceMaster, ProductInstanceMasterDTO.class);
    }

    @PUT
    @Path("{serialNumber}/iterations/{iteration}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ProductInstanceMasterDTO updateProductInstanceMaster(@PathParam("workspaceId") String workspaceId,@PathParam("iteration") int iteration, ProductInstanceIterationDTO productInstanceCreationDTO)
            throws EntityNotFoundException, EntityAlreadyExistsException, AccessRightException, CreationException, UserNotActiveException {

        InstanceAttributeFactory factory = new InstanceAttributeFactory();

        List<InstanceAttributeDTO> instanceAttributes = productInstanceCreationDTO.getInstanceAttributes();
        List<InstanceAttribute> attributes = new ArrayList<>();
        if (instanceAttributes != null) {
            attributes = factory.createInstanceAttributes(instanceAttributes);
        }

        Set<DocumentRevisionDTO> linkedDocs = productInstanceCreationDTO.getLinkedDocuments();
        DocumentRevisionKey[] links = null;
        String[] documentLinkComments = null;
        if (linkedDocs != null) {
            documentLinkComments = new String[linkedDocs.size()];
            links = createDocumentRevisionKeys(linkedDocs);
            int i = 0;
            for (DocumentRevisionDTO docRevisionForLink : linkedDocs){
                String comment = docRevisionForLink.getCommentLink();
                if (comment == null){
                    comment = "";
                }
                documentLinkComments[i++] = comment;
            }
        }

        ProductInstanceMaster productInstanceMaster = productInstanceService.updateProductInstance(workspaceId,iteration,productInstanceCreationDTO.getIterationNote(),new ConfigurationItemKey(workspaceId, productInstanceCreationDTO.getConfigurationItemId()), productInstanceCreationDTO.getSerialNumber(), productInstanceCreationDTO.getBasedOn().getId(), attributes, links, documentLinkComments);

        return mapper.map(productInstanceMaster, ProductInstanceMasterDTO.class);
    }

    @GET
    @Path("{serialNumber}")
    @Produces(MediaType.APPLICATION_JSON)
    public ProductInstanceMasterDTO getProductInstance(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String configurationItemId, @PathParam("serialNumber") String serialNumber)
            throws EntityNotFoundException, UserNotActiveException {

        ProductInstanceMaster productInstanceMaster = productInstanceService.getProductInstanceMaster(new ProductInstanceMasterKey(serialNumber,workspaceId,configurationItemId));
        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId,configurationItemId);
        ProductInstanceMasterDTO dto = mapper.map(productInstanceMaster, ProductInstanceMasterDTO.class);


        List<ProductInstanceIterationDTO> productInstanceIterationsDTO = dto.getProductInstanceIterations();
        List<ProductInstanceIteration> productInstanceIterations = productInstanceMaster.getProductInstanceIterations();
        Iterator<ProductInstanceIteration> iterationIterator = productInstanceIterations.iterator();
        for(ProductInstanceIterationDTO productInstanceIterationDTO : productInstanceIterationsDTO){

            List<LightPartLinkListDTO> substitutesParts = new ArrayList<>();
            List<LightPartLinkListDTO> optionalParts = new ArrayList<>();
            try {
                productInstanceIterationDTO.setPathToPathLinks(getPathToPathLinksForGivenProductInstance(iterationIterator.next()));
            } catch (AccessRightException e) {
                LOGGER.log(Level.FINEST, null, e);
            }
            for(String path:productInstanceIterationDTO.getSubstituteLinks()){
                LightPartLinkListDTO lightPartLinkDTO = new LightPartLinkListDTO();
                for(PartLink partLink : productService.decodePath(ciKey, path)){
                    lightPartLinkDTO.getPartLinks().add(new LightPartLinkDTO(partLink));
                }
                substitutesParts.add(lightPartLinkDTO);
            }
            for(String path:productInstanceIterationDTO.getOptionalUsageLinks()){
                LightPartLinkListDTO lightPartLinkDTO = new LightPartLinkListDTO();
                for(PartLink partLink : productService.decodePath(ciKey, path)){
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
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{serialNumber}/iterations/{iteration}/files/{fileName}")
    public Response removeAttachedFile(@PathParam("workspaceId") String workspaceId,@PathParam("iteration") int iteration, @PathParam("ciId") String configurationItemId, @PathParam("serialNumber") String serialNumber, @PathParam("fileName") String fileName)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException {

        String fullName = workspaceId + "/product-instances/" + serialNumber +"/iterations/" + iteration + "/" + fileName;
        ProductInstanceMaster productInstanceMaster = productInstanceService.getProductInstanceMaster(new ProductInstanceMasterKey(serialNumber,workspaceId,configurationItemId));

        productInstanceService.removeFileFromProductInstanceIteration(workspaceId, iteration, fullName, productInstanceMaster);
        return Response.ok().build();
    }


    @PUT
    @Path("{serialNumber}/acl")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateProductInstanceACL(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String configurationItemId, @PathParam("serialNumber") String serialNumber,ACLDTO acl)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException, NotAllowedException {

        if (!acl.getGroupEntries().isEmpty() || !acl.getUserEntries().isEmpty()) {

            Map<String,String> userEntries = new HashMap<>();
            Map<String,String> groupEntries = new HashMap<>();

            for (Map.Entry<String, ACL.Permission> entry : acl.getUserEntries().entrySet()) {
                userEntries.put(entry.getKey(), entry.getValue().name());
            }

            for (Map.Entry<String, ACL.Permission> entry : acl.getGroupEntries().entrySet()) {
                groupEntries.put(entry.getKey(), entry.getValue().name());
            }

            productInstanceService.updateACLForProductInstanceMaster(workspaceId, configurationItemId,serialNumber, userEntries, groupEntries);
        }else{
            productInstanceService.removeACLFromProductInstanceMaster(workspaceId, configurationItemId,serialNumber);
        }

        return Response.ok().build();
    }

    @DELETE
    @Path("{serialNumber}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteProductInstanceMaster(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String configurationItemId, @PathParam("serialNumber") String serialNumber)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException {

        productInstanceService.deleteProductInstance(workspaceId, configurationItemId, serialNumber);
        return Response.ok().build();
    }

    @GET
    @Path("{serialNumber}/iterations")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ProductInstanceIterationDTO> getProductInstanceIterations(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String configurationItemId, @PathParam("serialNumber") String serialNumber)
            throws EntityNotFoundException, UserNotActiveException {

        List<ProductInstanceIteration> productInstanceIterationList = productInstanceService.getProductInstanceIterations(new ProductInstanceMasterKey(serialNumber, workspaceId, configurationItemId));
        List<ProductInstanceIterationDTO> productInstanceIterationDTOList = new ArrayList<>();
        for (ProductInstanceIteration productInstanceIteration : productInstanceIterationList) {
            ProductInstanceIterationDTO productInstanceIterationDTO = mapper.map(productInstanceIteration, ProductInstanceIterationDTO.class);
            productInstanceIterationDTOList.add(productInstanceIterationDTO);
        }
        return productInstanceIterationDTOList;
    }


    @GET
    @Path("{serialNumber}/iterations/{iteration}")
    @Produces(MediaType.APPLICATION_JSON)
    public ProductInstanceIterationDTO getProductInstanceIteration(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String configurationItemId, @PathParam("serialNumber") String serialNumber, @PathParam("iteration") int iteration)
            throws EntityNotFoundException, UserNotActiveException {

        ProductInstanceIteration productInstanceIteration = productInstanceService.getProductInstanceIteration(new ProductInstanceIterationKey(serialNumber, workspaceId, configurationItemId, iteration));
        return mapper.map(productInstanceIteration, ProductInstanceIterationDTO.class);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{serialNumber}/rebase")
    public Response rebaseProductInstance(@PathParam("workspaceId") String workspaceId,
                                          @PathParam("ciId") String configurationItemId,
                                          @PathParam("serialNumber") String serialNumber, BaselineDTO baselineDTO) throws UserNotActiveException, WorkspaceNotFoundException, BaselineNotFoundException, UserNotFoundException, ProductInstanceMasterNotFoundException, AccessRightException, NotAllowedException, ConfigurationItemNotFoundException, PathToPathLinkAlreadyExistsException, PartMasterNotFoundException, CreationException, EntityConstraintException {

        productInstanceService.rebaseProductInstance(workspaceId, serialNumber, new ConfigurationItemKey(workspaceId, configurationItemId), baselineDTO.getId());
        return Response.ok().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{serialNumber}/iterations/{iteration}/files/{fileName}")
    public FileDTO renameAttachedFile(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String configurationItemId, @PathParam("serialNumber") String serialNumber, @PathParam("iteration") int iteration, @PathParam("fileName") String fileName, FileDTO fileDTO) throws UserNotActiveException, WorkspaceNotFoundException, CreationException, UserNotFoundException, FileNotFoundException, NotAllowedException, FileAlreadyExistsException, ProductInstanceMasterNotFoundException, AccessRightException, StorageException {
        String fullName = workspaceId + "/product-instances/" + serialNumber +"/iterations/" + iteration + "/" + fileName;
        BinaryResource binaryResource = productInstanceService.renameFileInProductInstance(fullName, fileDTO.getShortName(), serialNumber, configurationItemId, iteration);
        return new FileDTO(true,binaryResource.getFullName(),binaryResource.getName());
    }

    @GET
    @Path("{serialNumber}/pathdata/{path}")
    @Produces(MediaType.APPLICATION_JSON)
    public PathDataMasterDTO getPathData(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String configurationItemId, @PathParam("serialNumber") String serialNumber, @PathParam("path") String pathAsString) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, AccessRightException, ProductInstanceMasterNotFoundException, ConfigurationItemNotFoundException, PartUsageLinkNotFoundException, BaselineNotFoundException {
        PathDataMaster pathDataMaster = productInstanceService.getPathDataByPath(workspaceId, configurationItemId, serialNumber, pathAsString);

        PathDataMasterDTO dto = pathDataMaster == null ? new PathDataMasterDTO(pathAsString) : mapper.map(pathDataMaster, PathDataMasterDTO.class);

        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId,configurationItemId);

        LightPartLinkListDTO partLinksList = new LightPartLinkListDTO();
        List<PartLink> path = productService.decodePath(ciKey, pathAsString);
        for(PartLink partLink : path){
            partLinksList.getPartLinks().add(new LightPartLinkDTO(partLink));
        }
        dto.setPartLinksList(partLinksList);

        List<InstanceAttributeDTO> attributesDTO = new ArrayList<>();
        List<InstanceAttributeTemplateDTO> attributeTemplatesDTO = new ArrayList<>();
        PartLink partLink = path.get(path.size() - 1);
        PSFilter filter = productService.getPSFilter(ciKey,"pi-"+serialNumber, false);
        List<PartIteration> partIterations = filter.filter(partLink.getComponent());
        PartIteration partIteration = partIterations.get(0);

        if(partIteration != null){
            for(InstanceAttribute instanceAttribute : partIteration.getInstanceAttributes()){
                attributesDTO.add(mapper.map(instanceAttribute,InstanceAttributeDTO.class));
            }
            dto.setPartAttributes(attributesDTO);
            for(InstanceAttributeTemplate instanceAttributeTemplate : partIteration.getInstanceAttributeTemplates()){
                attributeTemplatesDTO.add(mapper.map(instanceAttributeTemplate,InstanceAttributeTemplateDTO.class));
            }
            dto.setPartAttributeTemplates(attributeTemplatesDTO);
        }

        return dto;
    }


    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{serialNumber}/pathdata/{pathDataId}/iterations/{iteration}/files/{fileName}")
    public FileDTO renameAttachedFileInPathData(@PathParam("workspaceId") String workspaceId,
                                                @PathParam("ciId") String configurationItemId,
                                                @PathParam("serialNumber") String serialNumber,
                                                @PathParam("pathDataId") int pathDataId,
                                                @PathParam("iteration") int iteration,
                                                @PathParam("fileName") String fileName,
                                                FileDTO fileDTO) throws UserNotActiveException, WorkspaceNotFoundException, CreationException, UserNotFoundException, FileNotFoundException, NotAllowedException, FileAlreadyExistsException, ProductInstanceMasterNotFoundException, AccessRightException, StorageException {

        String fullName = workspaceId + "/product-instances/" + serialNumber +"/pathdata/" + pathDataId + "/iterations/" + iteration + "/" + fileName;
        BinaryResource binaryResource = productInstanceService.renameFileInPathData(workspaceId, configurationItemId, serialNumber, pathDataId, iteration, fullName, fileDTO.getShortName());
        return new FileDTO(true,binaryResource.getFullName(),binaryResource.getName());
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{serialNumber}/pathdata/{pathDataId}/iterations/{iteration}/files/{fileName}")
    public Response deleteAttachedFileInPathData(@PathParam("workspaceId") String workspaceId,
                                                @PathParam("ciId") String configurationItemId,
                                                @PathParam("serialNumber") String serialNumber,
                                                @PathParam("pathDataId") int pathDataId,
                                                @PathParam("iteration") int iteration,
                                                @PathParam("fileName") String fileName
                                               ) throws UserNotActiveException, WorkspaceNotFoundException, CreationException, UserNotFoundException, FileNotFoundException, NotAllowedException, FileAlreadyExistsException, ProductInstanceMasterNotFoundException, AccessRightException {

        String fullName = workspaceId + "/product-instances/" + serialNumber +"/pathdata/" + pathDataId + "/iterations/" + iteration + "/" + fileName;
        ProductInstanceMaster productInstanceMaster = productInstanceService.getProductInstanceMaster(new ProductInstanceMasterKey(serialNumber, workspaceId, configurationItemId));
        productInstanceService.removeFileFromPathData(workspaceId, configurationItemId, serialNumber, pathDataId, iteration, fullName, productInstanceMaster);
        return Response.ok().build();
    }

    @DELETE
    @Path("{serialNumber}/pathdata/{pathDataId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deletePathData(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String configurationItemId, @PathParam("serialNumber") String serialNumber, @PathParam("pathDataId") int pathDataId) throws UserNotActiveException, WorkspaceNotFoundException, UserNotFoundException, ProductInstanceMasterNotFoundException, AccessRightException, NotAllowedException {
        productInstanceService.deletePathData(workspaceId,configurationItemId,serialNumber,pathDataId);
        return Response.ok().build();
    }

    @POST
    @Path("{serialNumber}/pathdata/{pathDataId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PathDataMasterDTO addNewPathDataIteration(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String configurationItemId, @PathParam("serialNumber") String serialNumber, @PathParam("pathDataId") int pathDataId, PathDataIterationCreationDTO pathDataIterationCreationDTO) throws UserNotFoundException, AccessRightException, UserNotActiveException, ProductInstanceMasterNotFoundException, WorkspaceNotFoundException, NotAllowedException, PathDataAlreadyExistsException, FileAlreadyExistsException, CreationException, ConfigurationItemNotFoundException, PartUsageLinkNotFoundException, BaselineNotFoundException {

        InstanceAttributeFactory factory = new InstanceAttributeFactory();

        List<InstanceAttributeDTO> instanceAttributes = pathDataIterationCreationDTO.getInstanceAttributes();
        List<InstanceAttribute> attributes = new ArrayList<>();
        if (instanceAttributes != null) {
            attributes = factory.createInstanceAttributes(instanceAttributes);
        }
        Set<DocumentRevisionDTO> linkedDocs = pathDataIterationCreationDTO.getLinkedDocuments();
        DocumentRevisionKey[] links = null;
        String[] documentLinkComments = null;
        if (linkedDocs != null) {
            documentLinkComments = new String[linkedDocs.size()];
            links = createDocumentRevisionKeys(linkedDocs);
            int i = 0;
            for (DocumentRevisionDTO docRevisionForLink : linkedDocs){
                String comment = docRevisionForLink.getCommentLink();
                if (comment == null){
                    comment = "";
                }
                documentLinkComments[i++] = comment;
            }
        }


        PathDataMaster pathDataMaster = productInstanceService.addNewPathDataIteration(workspaceId, configurationItemId, serialNumber, pathDataId, pathDataIterationCreationDTO.getPath(), attributes, pathDataIterationCreationDTO.getIterationNote(), links, documentLinkComments);
        PathDataMasterDTO dto = mapper.map(pathDataMaster, PathDataMasterDTO.class);

        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId,configurationItemId);

        LightPartLinkListDTO partLinksList = new LightPartLinkListDTO();
        List<PartLink> path = productService.decodePath(ciKey, pathDataIterationCreationDTO.getPath());
        for(PartLink partLink : path){
            partLinksList.getPartLinks().add(new LightPartLinkDTO(partLink));
        }
        dto.setPartLinksList(partLinksList);

        List<InstanceAttributeDTO> attributesDTO = new ArrayList<>();
        PartLink partLink = path.get(path.size() - 1);
        PSFilter filter = productService.getPSFilter(ciKey,"pi-"+serialNumber, false);
        List<PartIteration> partIterations = filter.filter(partLink.getComponent());
        PartIteration partIteration = partIterations.get(0);

        if(partIteration != null){
            for(InstanceAttribute instanceAttribute : partIteration.getInstanceAttributes()){
                attributesDTO.add(mapper.map(instanceAttribute,InstanceAttributeDTO.class));
            }
            dto.setPartAttributes(attributesDTO);
        }

        return dto;
    }

    @POST
    @Path("{serialNumber}/pathdata/{path}/new")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PathDataMasterDTO createPathDataMaster(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String configurationItemId, @PathParam("serialNumber") String serialNumber,@PathParam("path") String pathAsString, PathDataIterationCreationDTO pathDataIterationCreationDTO) throws UserNotFoundException, AccessRightException, UserNotActiveException, ProductInstanceMasterNotFoundException, WorkspaceNotFoundException, NotAllowedException, PathDataAlreadyExistsException, FileAlreadyExistsException, CreationException, ConfigurationItemNotFoundException, PartUsageLinkNotFoundException {

        InstanceAttributeFactory factory = new InstanceAttributeFactory();

        List<InstanceAttributeDTO> instanceAttributes = pathDataIterationCreationDTO.getInstanceAttributes();
        List<InstanceAttribute> attributes = new ArrayList<>();
        if (instanceAttributes != null) {
            attributes = factory.createInstanceAttributes(instanceAttributes);
        }

        PathDataMaster pathDataMaster = productInstanceService.createPathDataMaster(workspaceId, configurationItemId, serialNumber, pathAsString, attributes, pathDataIterationCreationDTO.getIterationNote());

        PathDataMasterDTO dto = mapper.map(pathDataMaster, PathDataMasterDTO.class);

        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId,configurationItemId);

        LightPartLinkListDTO partLinksList = new LightPartLinkListDTO();
        List<PartLink> path = productService.decodePath(ciKey, pathAsString);
        for(PartLink partLink : path){
            partLinksList.getPartLinks().add(new LightPartLinkDTO(partLink));
        }
        dto.setPartLinksList(partLinksList);

        return dto;
    }

    @PUT
    @Path("{serialNumber}/pathdata/{pathDataId}/iterations/{iteration}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PathDataMasterDTO updatePathData(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String configurationItemId, @PathParam("iteration") int iteration,@PathParam("serialNumber") String serialNumber,@PathParam("pathDataId") int pathDataId, PathDataIterationCreationDTO pathDataIterationCreationDTO) throws UserNotFoundException, AccessRightException, UserNotActiveException, ProductInstanceMasterNotFoundException, WorkspaceNotFoundException, NotAllowedException, PathDataAlreadyExistsException, ConfigurationItemNotFoundException, PartUsageLinkNotFoundException {

        InstanceAttributeFactory factory = new InstanceAttributeFactory();

        List<InstanceAttributeDTO> instanceAttributes = pathDataIterationCreationDTO.getInstanceAttributes();
        List<InstanceAttribute> attributes = new ArrayList<>();
        if (instanceAttributes != null) {
            attributes = factory.createInstanceAttributes(instanceAttributes);
        }

        Set<DocumentRevisionDTO> linkedDocs = pathDataIterationCreationDTO.getLinkedDocuments();
        DocumentRevisionKey[] links = null;
        String[] documentLinkComments = null;
        if (linkedDocs != null) {
            documentLinkComments = new String[linkedDocs.size()];
            links = createDocumentRevisionKeys(linkedDocs);
            int i = 0;
            for (DocumentRevisionDTO docRevisionForLink : linkedDocs){
                String comment = docRevisionForLink.getCommentLink();
                if (comment == null){
                    comment = "";
                }
                documentLinkComments[i++] = comment;
            }
        }

        PathDataMaster pathDataMaster = productInstanceService.updatePathData(workspaceId, configurationItemId, serialNumber, pathDataIterationCreationDTO.getId(),iteration, attributes, pathDataIterationCreationDTO.getIterationNote(), links, documentLinkComments);


        PathDataMasterDTO dto = mapper.map(pathDataMaster, PathDataMasterDTO.class);

        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId,configurationItemId);

        LightPartLinkListDTO partLinksList = new LightPartLinkListDTO();
        List<PartLink> path = productService.decodePath(ciKey, dto.getPath());
        for(PartLink partLink : path){
            partLinksList.getPartLinks().add(new LightPartLinkDTO(partLink));
        }
        dto.setPartLinksList(partLinksList);

        return dto;
    }

    @GET
    @Path("{serialNumber}/path-to-path-links-types")
    @Produces(MediaType.APPLICATION_JSON)
    public List<LightPathToPathLinkDTO> getPathToPathLinkTypes(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String configurationItemId, @PathParam("serialNumber") String serialNumber) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, AccessRightException, ProductInstanceMasterNotFoundException {
        List<String> pathToPathLinkTypes = productInstanceService.getPathToPathLinkTypes(workspaceId, configurationItemId, serialNumber);
        List<LightPathToPathLinkDTO> pathToPathLinkDTOs = new ArrayList<>();
        for(String type : pathToPathLinkTypes){
            LightPathToPathLinkDTO pathToPathLinkDTO = new LightPathToPathLinkDTO();
            pathToPathLinkDTO.setType(type);
            pathToPathLinkDTOs.add(pathToPathLinkDTO);
        }
        return pathToPathLinkDTOs;
    }
    @GET
    @Path("{serialNumber}/link-path-part/{pathPart}")
    @Produces(MediaType.APPLICATION_JSON)
    public LightPartMasterDTO getPartFromPathLink(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String configurationItemId, @PathParam("serialNumber") String serialNumber,@PathParam("pathPart") String partPath) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, AccessRightException, ProductInstanceMasterNotFoundException, PartUsageLinkNotFoundException, ConfigurationItemNotFoundException {

        PartMaster partMaster = productService.getPartMasterFromPath(workspaceId, configurationItemId, partPath);
        LightPartMasterDTO lightPartMasterDTO = new LightPartMasterDTO();
        lightPartMasterDTO.setPartName(partMaster.getName());
        lightPartMasterDTO.setPartNumber(partMaster.getNumber());
       return lightPartMasterDTO;

    }
    @GET
    @Path("{serialNumber}/path-to-path-links")
    @Produces(MediaType.APPLICATION_JSON)
    public List<LightPathToPathLinkDTO> getPathToPathLinks(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String configurationItemId, @PathParam("serialNumber") String serialNumber) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, AccessRightException, ProductInstanceMasterNotFoundException {
        List<PathToPathLink> pathToPathLinkTypes = productInstanceService.getPathToPathLinks(workspaceId, configurationItemId, serialNumber);
        List<LightPathToPathLinkDTO> pathToPathLinkDTOs = new ArrayList<>();
        for(PathToPathLink pathToPathLink : pathToPathLinkTypes){
            pathToPathLinkDTOs.add(mapper.map(pathToPathLink, LightPathToPathLinkDTO.class));

        }
        return pathToPathLinkDTOs;
    }


    @GET
    @Path("{serialNumber}/path-to-path-links/{pathToPathLinkId}")
    @Produces(MediaType.APPLICATION_JSON)
    public LightPathToPathLinkDTO getPathToPathLink(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String configurationItemId, @PathParam("serialNumber") String serialNumber, @PathParam("pathToPathLinkId") int pathToPathLinkId) throws UserNotActiveException, WorkspaceNotFoundException, UserNotFoundException, ProductInstanceMasterNotFoundException, AccessRightException, PathToPathLinkNotFoundException {
        PathToPathLink pathToPathLink = productInstanceService.getPathToPathLink(workspaceId, configurationItemId, serialNumber, pathToPathLinkId);
        return mapper.map(pathToPathLink,LightPathToPathLinkDTO.class);
    }

    @GET
    @Path("{serialNumber}/path-to-path-links/source/{sourcePath}/target/{targetPath}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PathToPathLinkDTO> getPathToPathLinksForGivenSourceAndTarget(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String configurationItemId, @PathParam("serialNumber") String serialNumber, @PathParam("sourcePath") String sourcePathAsString, @PathParam("targetPath") String targetPathAsString) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, AccessRightException, ProductInstanceMasterNotFoundException, ConfigurationItemNotFoundException, PartUsageLinkNotFoundException {
        List<PathToPathLink> pathToPathLinks = productInstanceService.getPathToPathLinkFromSourceAndTarget(workspaceId, configurationItemId, serialNumber, sourcePathAsString, targetPathAsString);
        List<PathToPathLinkDTO> dtos = new ArrayList<>();
        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId,configurationItemId);

        for(PathToPathLink pathToPathLink : pathToPathLinks) {
            PathToPathLinkDTO pathToPathLinkDTO = mapper.map(pathToPathLink, PathToPathLinkDTO.class);
            List<LightPartLinkDTO> sourceLightPartLinkDTOs = new ArrayList<>();

            List<PartLink> sourcePath = productService.decodePath(ciKey, pathToPathLink.getSourcePath());
            List<PartLink> targetPath = productService.decodePath(ciKey, pathToPathLink.getTargetPath());

            for(PartLink partLink : sourcePath){
                LightPartLinkDTO lightPartLinkDTO = new LightPartLinkDTO(partLink);
                sourceLightPartLinkDTOs.add(lightPartLinkDTO);
            }

            List<LightPartLinkDTO> targetLightPartLinkDTOs = new ArrayList<>();
            for(PartLink partLink : targetPath){
                LightPartLinkDTO lightPartLinkDTO = new LightPartLinkDTO(partLink);
                targetLightPartLinkDTOs.add(lightPartLinkDTO);
            }

            pathToPathLinkDTO.setSourceComponents(sourceLightPartLinkDTOs);
            pathToPathLinkDTO.setTargetComponents(targetLightPartLinkDTOs);
            dtos.add(pathToPathLinkDTO);
        }

        return dtos;
    }

    @GET
    @Path("{serialNumber}/path-to-path-links-roots/{type}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<LightPathToPathLinkDTO> getRootPathToPathLinks(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String configurationItemId, @PathParam("serialNumber") String serialNumber, @PathParam("type") String type) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, AccessRightException, ProductInstanceMasterNotFoundException {
        List<PathToPathLink> pathToPathLinks = productInstanceService.getRootPathToPathLinks(workspaceId, configurationItemId, serialNumber, type);
        List<LightPathToPathLinkDTO> dtos = new ArrayList<>();
        for(PathToPathLink pathToPathLink : pathToPathLinks) {
            dtos.add(mapper.map(pathToPathLink, LightPathToPathLinkDTO.class));
        }
        return dtos;
    }

    private DocumentRevisionKey[] createDocumentRevisionKeys(Set<DocumentRevisionDTO> dtos) {
        DocumentRevisionKey[] data = new DocumentRevisionKey[dtos.size()];
        int i = 0;
        for (DocumentRevisionDTO dto : dtos) {
            data[i++] = new DocumentRevisionKey(dto.getWorkspaceId(), dto.getDocumentMasterId(), dto.getVersion());
        }
        return data;
    }

    private List<PathToPathLinkDTO> getPathToPathLinksForGivenProductInstance(ProductInstanceIteration productInstanceIteration) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, AccessRightException, ProductInstanceMasterNotFoundException, PartUsageLinkNotFoundException, ConfigurationItemNotFoundException {
        List<PathToPathLink> pathToPathLinkTypes = productInstanceIteration.getPathToPathLinks();
        List<PathToPathLinkDTO> pathToPathLinkDTOs = new ArrayList<>();

        for (PathToPathLink pathToPathLink : pathToPathLinkTypes) {
            PathToPathLinkDTO pathToPathLinkDTO = mapper.map(pathToPathLink, PathToPathLinkDTO.class);
            List<PartLink> sourcePath = productService.decodePath(productInstanceIteration.getBasedOn().getConfigurationItem().getKey(), pathToPathLink.getSourcePath());
            List<PartLink> targetPath = productService.decodePath(productInstanceIteration.getBasedOn().getConfigurationItem().getKey(), pathToPathLink.getTargetPath());

            List<LightPartLinkDTO> sourceLightPartLinkDTOs = new ArrayList<>();
            for(PartLink partLink : sourcePath){
                LightPartLinkDTO lightPartLinkDTO = new LightPartLinkDTO(partLink);
                sourceLightPartLinkDTOs.add(lightPartLinkDTO);
            }

            List<LightPartLinkDTO> targetLightPartLinkDTOs = new ArrayList<>();
            for(PartLink partLink : targetPath){
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