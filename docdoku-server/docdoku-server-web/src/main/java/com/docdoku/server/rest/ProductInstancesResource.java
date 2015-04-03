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
import com.docdoku.core.document.DocumentIterationKey;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.product.ConfigurationItemKey;
import com.docdoku.core.product.PartLink;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IProductInstanceManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.server.rest.dto.*;
import com.docdoku.server.rest.dto.baseline.BaselinedPartDTO;
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
            throws EntityNotFoundException, EntityAlreadyExistsException, AccessRightException, CreationException {

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
        Set<DocumentIterationDTO> linkedDocs = productInstanceCreationDTO.getLinkedDocuments();
        DocumentIterationKey[] links = null;
        String[] documentLinkComments = null;
        if (linkedDocs != null) {
            documentLinkComments = new String[linkedDocs.size()];
            links = createDocumentIterationKey(linkedDocs);
            int i = 0;
            for (DocumentIterationDTO docItereationForLink : linkedDocs){
                String comment = docItereationForLink.getCommentLink();
                if (comment == null){
                    comment = "";
                }
                documentLinkComments[i++] = comment;
            }
        }
        ProductInstanceMaster productInstanceMaster = productInstanceService.createProductInstance(workspaceId,new ConfigurationItemKey(workspaceId, productInstanceCreationDTO.getConfigurationItemId()), productInstanceCreationDTO.getSerialNumber(), productInstanceCreationDTO.getBaselineId(),userEntries,grpEntries,attributes, links, documentLinkComments);

        return mapper.map(productInstanceMaster, ProductInstanceMasterDTO.class);
    }

    @PUT
    @Path("{serialNumber}/iterations/{iteration}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ProductInstanceMasterDTO updateProductInstanceMaster(@PathParam("workspaceId") String workspaceId,@PathParam("iteration") int iteration, ProductInstanceIterationDTO productInstanceCreationDTO)
            throws EntityNotFoundException, EntityAlreadyExistsException, AccessRightException, CreationException {

        InstanceAttributeFactory factory = new InstanceAttributeFactory();

        List<InstanceAttributeDTO> instanceAttributes = productInstanceCreationDTO.getInstanceAttributes();
        List<InstanceAttribute> attributes = new ArrayList<>();
        if (instanceAttributes != null) {
            attributes = factory.createInstanceAttributes(instanceAttributes);
        }

        Set<DocumentIterationDTO> linkedDocs = productInstanceCreationDTO.getLinkedDocuments();
        DocumentIterationKey[] links = null;
        String[] documentLinkComments = null;
        if (linkedDocs != null) {
            documentLinkComments = new String[linkedDocs.size()];
            links = createDocumentIterationKey(linkedDocs);
            int i = 0;
            for (DocumentIterationDTO docItereationForLink : linkedDocs){
                String comment = docItereationForLink.getCommentLink();
                if (comment == null){
                    comment = "";
                }
                documentLinkComments[i++] = comment;
            }
        }
        ProductInstanceMaster productInstanceMaster = productInstanceService.updateProductInstance(workspaceId,iteration,productInstanceCreationDTO.getIterationNote(),new ConfigurationItemKey(workspaceId, productInstanceCreationDTO.getConfigurationItemId()), productInstanceCreationDTO.getSerialNumber(), productInstanceCreationDTO.getBasedOn().getId(),attributes, links, documentLinkComments);

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


        List<ProductInstanceIterationDTO> productInstanceIterations = dto.getProductInstanceIterations();

        for(ProductInstanceIterationDTO productInstanceIterationDTO : productInstanceIterations){

            List<PartMinimalListDTO> substitutesParts = new ArrayList<>();
            List<PartMinimalListDTO> optionalParts = new ArrayList<>();

            for(String path:productInstanceIterationDTO.getSubstituteLinks()){
                PartMinimalListDTO partMinimalListDTO = new PartMinimalListDTO();
                List<PartMinimalDTO> partDTOs = new ArrayList<>();
                for(PartLink partLink : productService.decodePath(ciKey, path)){
                    partDTOs.add(mapper.map(partLink.getComponent(), PartMinimalDTO.class));
                }
                partMinimalListDTO.setParts(partDTOs);
                substitutesParts.add(partMinimalListDTO);
            }
            for(String path:productInstanceIterationDTO.getOptionalUsageLinks()){
                PartMinimalListDTO partMinimalListDTO = new PartMinimalListDTO();
                List<PartMinimalDTO> partDTOs = new ArrayList<>();
                for(PartLink partLink : productService.decodePath(ciKey, path)){
                    partDTOs.add(mapper.map(partLink.getComponent(),PartMinimalDTO.class));
                }
                partMinimalListDTO.setParts(partDTOs);
                optionalParts.add(partMinimalListDTO);
            }

            productInstanceIterationDTO.setSubstitutesParts(substitutesParts);
            productInstanceIterationDTO.setOptionalsParts(optionalParts);
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

        ProductInstanceMaster productInstanceMasterUpdated = productInstanceService.removeFileFromProductInstanceIteration(workspaceId, iteration, fullName, productInstanceMaster);
        return Response.ok().build();
    }


    @PUT
    @Path("{serialNumber}/acl")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateProductInstanceACL(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String configurationItemId, @PathParam("serialNumber") String serialNumber,ACLDTO acl)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException, NotAllowedException {

        if (acl.getGroupEntries().size() > 0 || acl.getUserEntries().size() > 0) {

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
        return mapper.map(productInstanceIteration,ProductInstanceIterationDTO.class);
    }

    @GET
    @Path("{serialNumber}/iterations/{iteration}/baselined-parts")
    @Produces(MediaType.APPLICATION_JSON)
    public List<BaselinedPartDTO> getProductInstanceBaselinedPart(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String configurationItemId, @PathParam("serialNumber") String serialNumber, @PathParam("iteration") int iteration, @QueryParam("q") String q)
            throws EntityNotFoundException, UserNotActiveException {

        List<BaselinedPart> baselinedParts;
        if(q!=null){
            int maxResults = 8;
            baselinedParts = productInstanceService.getProductInstanceIterationPartWithReference(new ProductInstanceIterationKey(serialNumber, workspaceId, configurationItemId, iteration), q, maxResults);
        }else{
            baselinedParts = productInstanceService.getProductInstanceIterationBaselinedPart(new ProductInstanceIterationKey(serialNumber, workspaceId, configurationItemId, iteration));
        }

        return Tools.mapBaselinedPartsToBaselinedPartDTO(baselinedParts);
    }




    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{serialNumber}/iterations/{iteration}/files/{fileName}")
    public FileDTO renameAttachedFile(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String configurationItemId, @PathParam("serialNumber") String serialNumber, @PathParam("iteration") int iteration, @PathParam("fileName") String fileName, FileDTO fileDTO) throws UserNotActiveException, WorkspaceNotFoundException, CreationException, UserNotFoundException, FileNotFoundException, NotAllowedException, FileAlreadyExistsException, ProductInstanceMasterNotFoundException, AccessRightException {

        String fullName = workspaceId + "/product-instances/" + serialNumber +"/iterations/" + iteration + "/" + fileName;
        BinaryResource binaryResource = productInstanceService.renameFileInProductInstance(fullName, fileDTO.getShortName(), serialNumber, configurationItemId, iteration);
        return new FileDTO(true,binaryResource.getFullName(),binaryResource.getName());
    }


    @POST
    @Path("{serialNumber}/pathdata")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PathDataDTO createPathData(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String configurationItemId, @PathParam("serialNumber") String serialNumber, PathDataDTO pathDataDTO) throws UserNotFoundException, AccessRightException, UserNotActiveException, ProductInstanceMasterNotFoundException, WorkspaceNotFoundException {

        InstanceAttributeFactory factory = new InstanceAttributeFactory();

        List<InstanceAttributeDTO> instanceAttributes = pathDataDTO.getInstanceAttributes();
        List<InstanceAttribute> attributes = new ArrayList<>();
        if (instanceAttributes != null) {
            attributes = factory.createInstanceAttributes(instanceAttributes);
        }

        PathData pathData = productInstanceService.addPathData(workspaceId, configurationItemId, serialNumber, pathDataDTO.getPath(), attributes);

        return mapper.map(pathData, PathDataDTO.class);
    }



    private DocumentIterationKey[] createDocumentIterationKey(Set<DocumentIterationDTO> dtos) {
        DocumentIterationKey[] data = new DocumentIterationKey[dtos.size()];
        int i = 0;
        for (DocumentIterationDTO dto : dtos) {
            data[i++] = createObject(dto);
        }
        return data;
    }

    private DocumentIterationKey createObject(DocumentIterationDTO dto) {
        return new DocumentIterationKey(dto.getWorkspaceId(), dto.getDocumentMasterId(), dto.getDocumentRevisionVersion(), dto.getIteration());
    }
}