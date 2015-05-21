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

import com.docdoku.core.change.ModificationNotification;
import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.common.User;
import com.docdoku.core.common.UserGroup;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.configuration.ProductInstanceMaster;
import com.docdoku.core.document.DocumentIterationKey;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.meta.*;
import com.docdoku.core.product.*;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.ACLUserEntry;
import com.docdoku.core.security.ACLUserGroupEntry;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IConverterManagerLocal;
import com.docdoku.core.services.IProductInstanceManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.core.sharing.SharedPart;
import com.docdoku.core.workflow.Workflow;
import com.docdoku.server.rest.dto.*;
import com.docdoku.server.rest.dto.product.ProductInstanceMasterDTO;
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

@Stateless
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class PartResource {

    @EJB
    private IProductManagerLocal productService;
    @EJB
    private IProductInstanceManagerLocal productInstanceService;
    @EJB
    private IUserManagerLocal userManager;
    @EJB
    private IConverterManagerLocal converterService;

    private Mapper mapper;

    public PartResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPartDTO(@PathParam("workspaceId") String pWorkspaceId, @PathParam("partNumber") String partNumber, @PathParam("partVersion") String partVersion)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException {
        PartRevisionKey revisionKey = new PartRevisionKey(pWorkspaceId, partNumber, partVersion);
        PartRevision partRevision = productService.getPartRevision(revisionKey);
        PartDTO partDTO = Tools.mapPartRevisionToPartDTO(partRevision);

        PartIterationKey iterationKey = new PartIterationKey(revisionKey, partRevision.getLastIterationNumber());
        List<ModificationNotification> notifications=productService.getModificationNotifications(iterationKey);
        List<ModificationNotificationDTO> notificationDTOs=Tools.mapModificationNotificationsToModificationNotificationDTO(notifications);
        partDTO.setNotifications(notificationDTOs);

        return Response.ok(partDTO).build();
    }

    @GET
    @Path("/used-by-product-instance-masters")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ProductInstanceMasterDTO> getProductInstanceMasterWherePartRevisionIsInUse(@PathParam("workspaceId") String pWorkspaceId, @PathParam("partNumber") String partNumber, @PathParam("partVersion") String partVersion)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException {

        PartRevisionKey revisionKey = new PartRevisionKey(pWorkspaceId, partNumber, partVersion);
        PartRevision partRevision = productService.getPartRevision(revisionKey);
        List<ProductInstanceMaster> productInstanceMasters = productInstanceService.getProductInstanceMasters(partRevision);
        List<ProductInstanceMasterDTO> productInstanceMasterDTOs = new ArrayList<>();

        for (ProductInstanceMaster productInstanceMaster : productInstanceMasters) {
            ProductInstanceMasterDTO productInstanceMasterDTO = mapper.map(productInstanceMaster, ProductInstanceMasterDTO.class);
            productInstanceMasterDTO.setProductInstanceIterations(null);
            productInstanceMasterDTO.setConfigurationItemId(productInstanceMaster.getInstanceOf().getId());
            productInstanceMasterDTOs.add(productInstanceMasterDTO);
        }

        return productInstanceMasterDTOs;
    }

    @GET
    @Path("/used-by-as-component")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PartDTO> getPartRevisionsWherePartRevisionIsUsedAsComponent(@PathParam("workspaceId") String pWorkspaceId, @PathParam("partNumber") String partNumber, @PathParam("partVersion") String partVersion)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException {

        List<PartIteration> partIterations = productService.getUsedByAsComponent(new PartRevisionKey(pWorkspaceId, partNumber, partVersion));

        Set<PartRevision> partRevisions = new HashSet<>();

        for(PartIteration partIteration:partIterations){
            partRevisions.add(partIteration.getPartRevision());
        }

        List<PartDTO> partDTOs = new ArrayList<>();

        for(PartRevision partRevision:partRevisions){
            PartDTO partDTO = mapper.map(partRevision, PartDTO.class);
            partDTO.setNumber(partRevision.getPartNumber());
            partDTO.setPartKey(partRevision.getPartNumber() + "-" + partRevision.getVersion());
            partDTO.setName(partRevision.getPartMaster().getName());
            partDTO.setStandardPart(partRevision.getPartMaster().isStandardPart());

            partDTOs.add(partDTO);
        }

        return partDTOs;
    }

    @GET
    @Path("/used-by-as-substitute")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PartDTO> getPartRevisionsWherePartRevisionIsUsedAsSubstitute(@PathParam("workspaceId") String pWorkspaceId, @PathParam("partNumber") String partNumber, @PathParam("partVersion") String partVersion)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException {

        List<PartIteration> partIterations = productService.getUsedByAsSubstitute(new PartRevisionKey(pWorkspaceId,partNumber,partVersion));

        Set<PartRevision> partRevisions = new HashSet<>();

        for(PartIteration partIteration:partIterations){
            partRevisions.add(partIteration.getPartRevision());
        }

        List<PartDTO> partDTOs = new ArrayList<>();

        for(PartRevision partRevision:partRevisions){
            PartDTO partDTO = mapper.map(partRevision, PartDTO.class);
            partDTO.setNumber(partRevision.getPartNumber());
            partDTO.setPartKey(partRevision.getPartNumber() + "-" + partRevision.getVersion());
            partDTO.setName(partRevision.getPartMaster().getName());
            partDTO.setStandardPart(partRevision.getPartMaster().isStandardPart());
            partDTOs.add(partDTO);
        }

        return partDTOs;
    }

    @PUT
    @Path("/iterations/{partIteration}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updatePartIteration(@PathParam("workspaceId") String pWorkspaceId, @PathParam("partNumber") String partNumber , @PathParam("partVersion") String partVersion, @PathParam("partIteration") int partIteration, PartIterationDTO data)
            throws EntityNotFoundException, EntityAlreadyExistsException, AccessRightException, UserNotActiveException, NotAllowedException, CreationException, EntityConstraintException {

        PartRevisionKey revisionKey = new PartRevisionKey(pWorkspaceId, partNumber, partVersion);
        PartRevision partRevision = productService.getPartRevision(revisionKey);

        PartIterationKey pKey = new PartIterationKey(revisionKey, partIteration);

        List<InstanceAttributeDTO> instanceAttributes = data.getInstanceAttributes();
        List<InstanceAttribute> attributes = null;
        if (instanceAttributes != null) {
            attributes = createInstanceAttributes(instanceAttributes);
        }

        List<InstanceAttributeTemplateDTO> instanceAttributeTemplates = data.getInstanceAttributeTemplates();
        List<InstanceAttributeTemplate> attributeTemplates = null;
        if (instanceAttributeTemplates != null) {
            attributeTemplates = createInstanceAttributeTemplateFromDto(instanceAttributeTemplates);
        }

        String[] lovNames=new String[instanceAttributeTemplates.size()];
        for (int i=0;i<instanceAttributeTemplates.size();i++)
            lovNames[i]=instanceAttributeTemplates.get(i).getLovName();

        List<PartUsageLinkDTO> components = data.getComponents();
        List<PartUsageLink> newComponents = null;
        if (components != null) {
            newComponents = createComponents(pWorkspaceId, components);
        }

        List<DocumentRevisionDTO> linkedDocs = data.getLinkedDocuments();
        DocumentRevisionKey[] links = null;
        String[] documentLinkComments = null;
        if (linkedDocs != null) {
            documentLinkComments = new String[linkedDocs.size()];
            links = createDocumentRevisionKey(linkedDocs);
            int i = 0;
            for (DocumentRevisionDTO docRevisionForLink : linkedDocs){
                String comment = docRevisionForLink.getCommentLink();
                if (comment == null){
                    comment = "";
                }
                documentLinkComments[i++] = comment;
            }
        }

        PartIteration.Source sameSource = partRevision.getIteration(partIteration).getSource();

        PartRevision partRevisionUpdated = productService.updatePartIteration(pKey, data.getIterationNote(), sameSource, newComponents, attributes, attributeTemplates, links, documentLinkComments, lovNames);

        PartDTO partDTO = Tools.mapPartRevisionToPartDTO(partRevisionUpdated);
        return Response.ok(partDTO).build();
    }

    @GET
    @Path("/iterations/{partIteration}/conversion")
    @Produces(MediaType.APPLICATION_JSON)
    public ConversionDTO getConversionStatus(@PathParam("workspaceId") String pWorkspaceId, @PathParam("partNumber") String partNumber, @PathParam("partVersion") String partVersion, @PathParam("partIteration") int partIteration) throws UserNotActiveException, PartRevisionNotFoundException, WorkspaceNotFoundException, UserNotFoundException, PartIterationNotFoundException, AccessRightException {
        PartIterationKey partIPK = new PartIterationKey(pWorkspaceId, partNumber, partVersion, partIteration);
        Conversion conversion = productService.getConversion(partIPK);
        if (conversion != null) {
            return mapper.map(conversion, ConversionDTO.class);
        }
        return null;
    }

    @PUT
    @Path("/iterations/{partIteration}/conversion")
    public Response retryConversion(@PathParam("workspaceId") String pWorkspaceId, @PathParam("partNumber") String partNumber, @PathParam("partVersion") String partVersion, @PathParam("partIteration") int iteration) throws UserNotActiveException, PartRevisionNotFoundException, WorkspaceNotFoundException, UserNotFoundException, PartIterationNotFoundException, AccessRightException {

        PartIterationKey partIPK = new PartIterationKey(pWorkspaceId, partNumber, partVersion, iteration);
        PartIteration partIteration = productService.getPartIteration(partIPK);
        BinaryResource nativeCADFile = partIteration.getNativeCADFile();
        if (nativeCADFile != null) {
            try {
                converterService.convertCADFileToOBJ(partIPK, nativeCADFile);
                return Response.ok().build();
            } catch (Exception e) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @PUT
    @Path("/checkin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkIn(@PathParam("workspaceId") String workspaceId, @PathParam("partNumber") String partNumber, @PathParam("partVersion") String partVersion)
            throws EntityNotFoundException, ESServerException, AccessRightException, NotAllowedException, EntityConstraintException, UserNotActiveException {

        PartRevisionKey revisionKey = new PartRevisionKey(workspaceId, partNumber, partVersion);
        productService.checkInPart(revisionKey);
        return Response.ok().build();
    }

    @PUT
    @Path("/checkout")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkOut(@PathParam("workspaceId") String workspaceId, @PathParam("partNumber") String partNumber, @PathParam("partVersion") String partVersion)
            throws EntityNotFoundException, EntityAlreadyExistsException, CreationException, AccessRightException, NotAllowedException {

        PartRevisionKey revisionKey = new PartRevisionKey(workspaceId, partNumber, partVersion);
        productService.checkOutPart(revisionKey);
        return Response.ok().build();
    }

    @PUT
    @Path("/undocheckout")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response undoCheckOut(@PathParam("workspaceId") String workspaceId, @PathParam("partNumber") String partNumber, @PathParam("partVersion") String partVersion)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException {

        PartRevisionKey revisionKey = new PartRevisionKey(workspaceId, partNumber, partVersion);
        productService.undoCheckOutPart(revisionKey);
        return Response.ok().build();
    }

    @PUT
    @Path("/acl")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateACL(@PathParam("workspaceId") String workspaceId, @PathParam("partNumber") String partNumber, @PathParam("partVersion") String partVersion, ACLDTO acl)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException {

        PartRevisionKey revisionKey = new PartRevisionKey(workspaceId, partNumber, partVersion);

        if (!acl.getGroupEntries().isEmpty() || !acl.getUserEntries().isEmpty()) {
            Map<String, String> userEntries = new HashMap<>();
            Map<String, String> groupEntries = new HashMap<>();

            for (Map.Entry<String, ACL.Permission> entry : acl.getUserEntries().entrySet()) {
                userEntries.put(entry.getKey(), entry.getValue().name());
            }

            for (Map.Entry<String, ACL.Permission> entry : acl.getGroupEntries().entrySet()) {
                groupEntries.put(entry.getKey(), entry.getValue().name());
            }

            productService.updatePartRevisionACL(workspaceId, revisionKey, userEntries, groupEntries);

        } else {
            productService.removeACLFromPartRevision(revisionKey);
        }
        return Response.ok().build();
    }

    @PUT
    @Path("/newVersion")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createNewVersion(@PathParam("workspaceId") String workspaceId, @PathParam("partNumber") String partNumber, @PathParam("partVersion") String partVersion, PartCreationDTO partCreationDTO)
            throws EntityNotFoundException, EntityAlreadyExistsException, CreationException, AccessRightException, NotAllowedException {

        RoleMappingDTO[] rolesMappingDTO = partCreationDTO.getRoleMapping();
        ACLDTO acl = partCreationDTO.getAcl();
        PartRevisionKey revisionKey = new PartRevisionKey(workspaceId, partNumber, partVersion);
        String description = partCreationDTO.getDescription();
        String workflowModelId = partCreationDTO.getWorkflowModelId();

        ACLUserEntry[] userEntries = null;
        ACLUserGroupEntry[] userGroupEntries = null;
        if (acl != null) {
            userEntries = new ACLUserEntry[acl.getUserEntries().size()];
            userGroupEntries = new ACLUserGroupEntry[acl.getGroupEntries().size()];
            int i = 0;
            for (Map.Entry<String, ACL.Permission> entry : acl.getUserEntries().entrySet()) {
                userEntries[i] = new ACLUserEntry();
                userEntries[i].setPrincipal(new User(new Workspace(workspaceId), entry.getKey()));
                userEntries[i++].setPermission(ACL.Permission.valueOf(entry.getValue().name()));
            }
            i = 0;
            for (Map.Entry<String, ACL.Permission> entry : acl.getGroupEntries().entrySet()) {
                userGroupEntries[i] = new ACLUserGroupEntry();
                userGroupEntries[i].setPrincipal(new UserGroup(new Workspace(workspaceId), entry.getKey()));
                userGroupEntries[i++].setPermission(ACL.Permission.valueOf(entry.getValue().name()));
            }
        }

        Map<String, String> roleMappings = new HashMap<>();

        if (rolesMappingDTO != null) {
            for (RoleMappingDTO roleMappingDTO : rolesMappingDTO) {
                roleMappings.put(roleMappingDTO.getRoleName(), roleMappingDTO.getUserLogin());
            }
        }

        productService.createPartRevision(revisionKey, description, workflowModelId, userEntries, userGroupEntries, roleMappings);

        return Response.ok().build();
    }

    @PUT
    @Path("/release")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response releasePartRevision(@PathParam("workspaceId") String workspaceId, @PathParam("partNumber") String partNumber, @PathParam("partVersion") String partVersion)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException {

        PartRevisionKey revisionKey = new PartRevisionKey(workspaceId, partNumber, partVersion);

        productService.releasePartRevision(revisionKey);
        return Response.ok().build();
    }

    @PUT
    @Path("/obsolete")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response markPartRevisionAsObsolete(@PathParam("workspaceId") String workspaceId, @PathParam("partNumber") String partNumber, @PathParam("partVersion") String partVersion)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException {

        PartRevisionKey revisionKey = new PartRevisionKey(workspaceId, partNumber, partVersion);

        productService.markPartRevisionAsObsolete(revisionKey);
        return Response.ok().build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deletePartRevision(@PathParam("workspaceId") String workspaceId, @PathParam("partNumber") String partNumber, @PathParam("partVersion") String partVersion)
            throws EntityNotFoundException, UserNotActiveException, EntityConstraintException, ESServerException {

        PartRevisionKey revisionKey = new PartRevisionKey(workspaceId, partNumber, partVersion);
        productService.deletePartRevision(revisionKey);
        return Response.ok().build();
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/iterations/{partIteration}/files/{subType}/{fileName}")
    public Response removeFile(@PathParam("workspaceId") String workspaceId, @PathParam("partNumber") String partNumber, @PathParam("partVersion") String partVersion, @PathParam("partIteration") int partIteration, @PathParam("subType") String subType, @PathParam("fileName") String fileName)
            throws EntityNotFoundException, UserNotActiveException {
        PartIterationKey partIKey = new PartIterationKey(workspaceId, partNumber, partVersion, partIteration);
        String fileFullName = workspaceId + "/parts/" + partNumber + "/" + partVersion + "/" + partIteration + "/" + subType + "/" + fileName;
        productService.removeFileInPartIteration(partIKey, subType, fileFullName);
        return Response.ok().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/iterations/{partIteration}/files/{subType}/{fileName}")
    public FileDTO renameAttachedFile(@PathParam("workspaceId") String workspaceId, @PathParam("partNumber") String partNumber, @PathParam("partVersion") String partVersion, @PathParam("partIteration") int partIteration, @PathParam("subType") String subType, @PathParam("fileName") String fileName, FileDTO fileDTO)
            throws UserNotActiveException, WorkspaceNotFoundException, CreationException, UserNotFoundException, FileNotFoundException, NotAllowedException, FileAlreadyExistsException, StorageException {
        String fileFullName = workspaceId + "/parts/" + partNumber + "/" + partVersion+ "/" + partIteration + "/" + subType + "/" + fileName;
        BinaryResource binaryResource = productService.renameFileInPartIteration(subType, fileFullName, fileDTO.getShortName());
        return new FileDTO(true,binaryResource.getFullName(),binaryResource.getName());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/share")
    public Response createSharedPart(@PathParam("workspaceId") String workspaceId, @PathParam("partNumber") String partNumber, @PathParam("partVersion") String partVersion, SharedPartDTO pSharedPartDTO)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException {

        String password = pSharedPartDTO.getPassword();
        Date expireDate = pSharedPartDTO.getExpireDate();

        SharedPart sharedPart = productService.createSharedPart(new PartRevisionKey(workspaceId, partNumber, partVersion), password, expireDate);
        SharedPartDTO sharedPartDTO = mapper.map(sharedPart, SharedPartDTO.class);
        return Response.ok().entity(sharedPartDTO).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/publish")
    public Response publishPartRevision(@PathParam("workspaceId") String workspaceId, @PathParam("partNumber") String partNumber, @PathParam("partVersion") String partVersion)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException {

        PartRevision partRevision = productService.getPartRevision(new PartRevisionKey(workspaceId, partNumber, partVersion));
        partRevision.setPublicShared(true);
        return Response.ok().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/unpublish")
    public Response unPublishPartRevision(@PathParam("workspaceId") String workspaceId, @PathParam("partNumber") String partNumber, @PathParam("partVersion") String partVersion)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException {

        PartRevision partRevision = productService.getPartRevision(new PartRevisionKey(workspaceId, partNumber, partVersion));
        partRevision.setPublicShared(false);
        return Response.ok().build();
    }

    @GET
    @Path("/aborted-workflows")
    @Produces(MediaType.APPLICATION_JSON)
    public List<WorkflowDTO> getAbortedWorkflows(@PathParam("workspaceId") String workspaceId, @PathParam("partNumber") String partNumber, @PathParam("partVersion") String partVersion)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException {

        PartRevisionKey revisionKey = new PartRevisionKey(workspaceId, partNumber, partVersion);
        PartRevision partRevision = productService.getPartRevision(revisionKey);

        List<Workflow> abortedWorkflows = partRevision.getAbortedWorkflows();
        List<WorkflowDTO> abortedWorkflowsDTO = new ArrayList<>();

        for (Workflow abortedWorkflow : abortedWorkflows) {
            abortedWorkflowsDTO.add(mapper.map(abortedWorkflow, WorkflowDTO.class));
        }

        return abortedWorkflowsDTO;
    }

    @PUT
    @Path("/tags")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PartDTO savePartTags(@PathParam("workspaceId") String workspaceId, @PathParam("partNumber") String partNumber, @PathParam("partVersion") String partVersion, List<TagDTO> tagDtos)
            throws EntityNotFoundException, NotAllowedException, ESServerException, AccessRightException, UserNotActiveException, TagException {


        String[] tagLabels = new String[tagDtos.size()];

        for (int i = 0; i < tagDtos.size(); i++) {
            tagLabels[i] = tagDtos.get(i).getLabel();
        }
        PartRevisionKey revisionKey = new PartRevisionKey(workspaceId, partNumber, partVersion);

        PartRevision partRevision =  productService.saveTags(revisionKey,tagLabels);
        PartDTO  partDTO = mapper.map(partRevision, PartDTO.class);

        return partDTO;
    }

    @POST
    @Path("/tags")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addPartTag(@PathParam("workspaceId") String workspaceId, @PathParam("partNumber") String partNumber, @PathParam("partVersion") String partVersion, List<TagDTO> tagDtos)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException, ESServerException, TagException {

        PartRevisionKey revisionKey = new PartRevisionKey(workspaceId, partNumber, partVersion);
        PartRevision partRevision = productService.getPartRevision(revisionKey);
        Set<Tag> tags = partRevision.getTags();
        Set<String> tagLabels = new HashSet<>();

        for(TagDTO tagDto:tagDtos){
            tagLabels.add(tagDto.getLabel());
        }

        for(Tag tag : tags){
            tagLabels.add(tag.getLabel());
        }

        productService.saveTags(revisionKey, tagLabels.toArray(new String[tagLabels.size()]));
        return Response.ok().build();
    }

    @DELETE
    @Path("/tags/{tagName}")
    public Response removePartTags(@PathParam("workspaceId") String workspaceId, @PathParam("partNumber") String partNumber, @PathParam("partVersion") String partVersion, @PathParam("tagName") String tagName)
            throws EntityNotFoundException, NotAllowedException, AccessRightException, UserNotActiveException, ESServerException {
        productService.removeTag(new PartRevisionKey(workspaceId, partNumber, partVersion), tagName);
        return Response.ok().build();
    }

    private List<InstanceAttribute> createInstanceAttributes(List<InstanceAttributeDTO> dtos) {
        if (dtos == null) {
            return new ArrayList<>();
        }
        List<InstanceAttribute> data = new ArrayList<>();
        for (InstanceAttributeDTO dto : dtos) {
            data.add(createInstanceAttribute(dto));
        }

        return data;
    }

    private InstanceAttribute createInstanceAttribute(InstanceAttributeDTO dto) {
        InstanceAttribute attr;
        switch (dto.getType()) {
            case BOOLEAN:
                attr = new InstanceBooleanAttribute();
                break;
            case TEXT:
                attr = new InstanceTextAttribute();
                break;
            case NUMBER:
                attr = new InstanceNumberAttribute();
                break;
            case DATE:
                attr = new InstanceDateAttribute();
                break;
            case URL:
                attr = new InstanceURLAttribute();
                break;
            case LOV :
                attr = new InstanceListOfValuesAttribute();
                List<NameValuePairDTO> itemsDTO = dto.getItems();
                List<NameValuePair> items = new ArrayList<>();
                if (itemsDTO!= null){
                    for (NameValuePairDTO itemDTO : itemsDTO){
                        items.add(mapper.map(itemDTO, NameValuePair.class));
                    }
                }
                ((InstanceListOfValuesAttribute) attr).setItems(items);
                break;
            default:
                throw new IllegalArgumentException("Instance attribute not supported");
        }

        attr.setName(dto.getName());
        attr.setValue(dto.getValue());
        attr.setLocked(dto.isLocked());
        return attr;
    }

    private List<InstanceAttributeTemplate> createInstanceAttributeTemplateFromDto(List<InstanceAttributeTemplateDTO> dtos) {
        List<InstanceAttributeTemplate> data = new ArrayList<>();
        for (InstanceAttributeTemplateDTO dto: dtos) {
            data.add(createInstanceAttributeTemplateObject(dto));
        }
        return data;
    }

    private InstanceAttributeTemplate createInstanceAttributeTemplateObject(InstanceAttributeTemplateDTO dto) {
        InstanceAttributeTemplate data;
        if(dto.getLovName()==null || dto.getLovName().isEmpty()) {
            DefaultAttributeTemplate defaultIA = new DefaultAttributeTemplate();
            defaultIA.setAttributeType(DefaultAttributeTemplate.AttributeType.valueOf(dto.getAttributeType().name()));
            data=defaultIA;
        }
        else {
            ListOfValuesAttributeTemplate lovA = new ListOfValuesAttributeTemplate();
            data=lovA;
        }

        data.setName(dto.getName());
        data.setMandatory(dto.isMandatory());
        data.setLocked(dto.isLocked());
        return data;
    }

    public List<PartUsageLink> createComponents(String workspaceId, List<PartUsageLinkDTO> pComponents)
            throws EntityNotFoundException, EntityAlreadyExistsException, AccessRightException, NotAllowedException, CreationException, UserNotActiveException {

        List<PartUsageLink> components = new ArrayList<>();
        for (PartUsageLinkDTO partUsageLinkDTO : pComponents) {

            PartMaster component = findOrCreatePartMaster(workspaceId, partUsageLinkDTO.getComponent());

            if (component != null) {
                PartUsageLink partUsageLink = new PartUsageLink();

                List<CADInstance> cadInstances = new ArrayList<>();
                List<PartSubstituteLink> partSubstituteLinks = new ArrayList<>();

                if (partUsageLinkDTO.getCadInstances() != null) {
                    for (CADInstanceDTO cadInstanceDTO : partUsageLinkDTO.getCadInstances()) {
                        cadInstances.add(mapper.map(cadInstanceDTO, CADInstance.class));
                    }
                } else if (partUsageLinkDTO.getUnit() == null || partUsageLinkDTO.getUnit().isEmpty()) {
                    for (double i = 0; i < partUsageLinkDTO.getAmount(); i++) {
                        cadInstances.add(new CADInstance(0, 0, 0, 0, 0, 0));
                    }
                } else {
                    cadInstances.add(new CADInstance(0, 0, 0, 0, 0, 0));
                }
                for (PartSubstituteLinkDTO substituteLinkDTO : partUsageLinkDTO.getSubstitutes()) {
                    PartMaster substitute = findOrCreatePartMaster(workspaceId, substituteLinkDTO.getSubstitute());
                    if (substitute != null) {
                        PartSubstituteLink partSubstituteLink = mapper.map(substituteLinkDTO, PartSubstituteLink.class);
                        List<CADInstance> subCADInstances = new ArrayList<>();
                        if (substituteLinkDTO.getCadInstances() != null) {
                            for (CADInstanceDTO cadInstanceDTO : substituteLinkDTO.getCadInstances()) {
                                subCADInstances.add(mapper.map(cadInstanceDTO, CADInstance.class));
                            }
                        } else if (substituteLinkDTO.getUnit() == null || substituteLinkDTO.getUnit().isEmpty()) {
                            for (double i = 0; i <substituteLinkDTO.getAmount() ; i++) {
                                subCADInstances.add(new CADInstance(0, 0, 0, 0, 0, 0));
                            }
                        } else {
                            subCADInstances.add(new CADInstance(0, 0, 0, 0, 0, 0));
                        }
                        partSubstituteLink.setCadInstances(subCADInstances);
                        partSubstituteLink.setSubstitute(substitute);
                        partSubstituteLinks.add(partSubstituteLink);
                    }
                }
                partUsageLink.setComponent(component);
                partUsageLink.setAmount(partUsageLinkDTO.getAmount());
                partUsageLink.setComment(partUsageLinkDTO.getComment());
                partUsageLink.setReferenceDescription(partUsageLinkDTO.getReferenceDescription());
                partUsageLink.setCadInstances(cadInstances);
                partUsageLink.setUnit(partUsageLinkDTO.getUnit());
                partUsageLink.setOptional(partUsageLinkDTO.isOptional());
                partUsageLink.setSubstitutes(partSubstituteLinks);
                components.add(partUsageLink);
            }

        }

        return components;

    }

    public PartMaster findOrCreatePartMaster(String workspaceId, ComponentDTO componentDTO)
            throws EntityNotFoundException, EntityAlreadyExistsException, NotAllowedException, UserNotActiveException, AccessRightException, CreationException {
        String componentNumber = componentDTO.getNumber();
        PartMasterKey partMasterKey = new PartMasterKey(workspaceId, componentNumber);
        if (productService.partMasterExists(partMasterKey)) {
            return new PartMaster(userManager.getWorkspace(workspaceId), componentNumber);
        } else {
            return productService.createPartMaster(workspaceId, componentDTO.getNumber(), componentDTO.getName(), componentDTO.isStandardPart(), null, componentDTO.getDescription(), null, null, null, null);
        }
    }

    private DocumentRevisionKey[] createDocumentRevisionKey(List<DocumentRevisionDTO> dtos) {
        DocumentRevisionKey[] data = new DocumentRevisionKey[dtos.size()];
        int i = 0;
        for (DocumentRevisionDTO dto : dtos) {
            data[i++] =new DocumentRevisionKey(dto.getWorkspaceId(), dto.getDocumentMasterId(), dto.getVersion());
        }
        return data;
    }

}