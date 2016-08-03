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
import com.docdoku.core.common.*;
import com.docdoku.core.configuration.PSFilter;
import com.docdoku.core.configuration.ProductBaseline;
import com.docdoku.core.configuration.ProductInstanceMaster;
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.meta.InstanceAttributeTemplate;
import com.docdoku.core.meta.Tag;
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
import com.docdoku.server.rest.collections.VirtualInstanceCollection;
import com.docdoku.server.rest.dto.*;
import com.docdoku.server.rest.dto.baseline.ProductBaselineDTO;
import com.docdoku.server.rest.dto.product.ProductInstanceMasterDTO;
import com.docdoku.server.rest.util.InstanceAttributeFactory;
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
import java.util.*;

@RequestScoped
@Api(hidden = true, value = "part", description = "Operation about single parts")
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class PartResource {

    @Inject
    private IProductManagerLocal productService;

    @Inject
    private IProductInstanceManagerLocal productInstanceService;

    @Inject
    private IUserManagerLocal userManager;

    @Inject
    private IConverterManagerLocal converterService;

    private Mapper mapper;

    public PartResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @ApiOperation(value = "Get part revision", response = PartRevisionDTO.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPartRevision(@PathParam("workspaceId") String pWorkspaceId,
                               @PathParam("partNumber") String partNumber,
                               @PathParam("partVersion") String partVersion)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException {
        PartRevisionKey revisionKey = new PartRevisionKey(pWorkspaceId, partNumber, partVersion);
        PartRevision partRevision = productService.getPartRevision(revisionKey);
        PartRevisionDTO partRevisionDTO = Tools.mapPartRevisionToPartDTO(partRevision);

        PartIterationKey iterationKey = new PartIterationKey(revisionKey, partRevision.getLastIterationNumber());
        List<ModificationNotification> notifications = productService.getModificationNotifications(iterationKey);
        List<ModificationNotificationDTO> notificationDTOs = Tools.mapModificationNotificationsToModificationNotificationDTO(notifications);
        partRevisionDTO.setNotifications(notificationDTOs);

        return Response.ok(partRevisionDTO).build();
    }

    @GET
    @ApiOperation(value = "Get product instance where part is in use", response = ProductInstanceMasterDTO.class, responseContainer = "List")
    @Path("/used-by-product-instance-masters")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProductInstanceMasters(@PathParam("workspaceId") String pWorkspaceId,
                                              @PathParam("partNumber") String partNumber,
                                              @PathParam("partVersion") String partVersion)
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
        return Response.ok(new GenericEntity<List<ProductInstanceMasterDTO>>((List<ProductInstanceMasterDTO>) productInstanceMasterDTOs) {
        }).build();
    }

    @GET
    @ApiOperation(value = "Get part revisions where use as component", response = PartRevisionDTO.class, responseContainer = "List")
    @Path("/used-by-as-component")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsedByAsComponent(@PathParam("workspaceId") String pWorkspaceId,
                                         @PathParam("partNumber") String partNumber,
                                         @PathParam("partVersion") String partVersion)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException {

        List<PartIteration> partIterations = productService.getUsedByAsComponent(new PartRevisionKey(pWorkspaceId, partNumber, partVersion));

        Set<PartRevision> partRevisions = new HashSet<>();

        for (PartIteration partIteration : partIterations) {
            partRevisions.add(partIteration.getPartRevision());
        }
        List<PartRevisionDTO> partRevisionDTOs = getPartRevisionDTO(partRevisions);

        return Response.ok(new GenericEntity<List<PartRevisionDTO>>((List<PartRevisionDTO>) partRevisionDTOs) {
        }).build();
    }

    @GET
    @ApiOperation(value = "Get part revisions where use as substitute", response = PartRevisionDTO.class, responseContainer = "List")
    @Path("/used-by-as-substitute")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsedByAsSubstitute(@PathParam("workspaceId") String pWorkspaceId,
                                          @PathParam("partNumber") String partNumber,
                                          @PathParam("partVersion") String partVersion)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException {

        List<PartIteration> partIterations = productService.getUsedByAsSubstitute(new PartRevisionKey(pWorkspaceId, partNumber, partVersion));

        Set<PartRevision> partRevisions = new HashSet<>();

        for (PartIteration partIteration : partIterations) {
            partRevisions.add(partIteration.getPartRevision());
        }

        List<PartRevisionDTO> partRevisionDTOs = getPartRevisionDTO(partRevisions);
        return Response.ok(new GenericEntity<List<PartRevisionDTO>>((List<PartRevisionDTO>) partRevisionDTOs) {
        }).build();
    }

    @PUT
    @ApiOperation(value = "Update part iteration", response = PartRevisionDTO.class)
    @Path("/iterations/{partIteration}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updatePartIteration(@PathParam("workspaceId") String pWorkspaceId,
                                        @PathParam("partNumber") String partNumber,
                                        @PathParam("partVersion") String partVersion,
                                        @PathParam("partIteration") int partIteration,
                                        @ApiParam(required = true, value = "Part iteration to update") PartIterationDTO data)
            throws EntityNotFoundException, EntityAlreadyExistsException, AccessRightException, UserNotActiveException, NotAllowedException, CreationException, EntityConstraintException {

        PartRevisionKey revisionKey = new PartRevisionKey(pWorkspaceId, partNumber, partVersion);
        PartRevision partRevision = productService.getPartRevision(revisionKey);

        PartIterationKey pKey = new PartIterationKey(revisionKey, partIteration);

        List<InstanceAttributeDTO> instanceAttributes = data.getInstanceAttributes();
        List<InstanceAttribute> attributes = null;
        InstanceAttributeFactory instanceAttributeFactory = new InstanceAttributeFactory();
        if (instanceAttributes != null) {
            attributes = instanceAttributeFactory.createInstanceAttributes(instanceAttributes);
        }

        List<InstanceAttributeTemplateDTO> instanceAttributeTemplates = data.getInstanceAttributeTemplates();
        List<InstanceAttributeTemplate> attributeTemplates = null;
        String[] lovNames = null;
        if (instanceAttributeTemplates != null) {
            attributeTemplates = instanceAttributeFactory.createInstanceAttributeTemplateFromDTO(instanceAttributeTemplates);
            lovNames = new String[instanceAttributeTemplates.size()];
            for (int i = 0; i < instanceAttributeTemplates.size(); i++)
                lovNames[i] = instanceAttributeTemplates.get(i).getLovName();
        }


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
            for (DocumentRevisionDTO docRevisionForLink : linkedDocs) {
                String comment = docRevisionForLink.getCommentLink();
                if (comment == null) {
                    comment = "";
                }
                documentLinkComments[i++] = comment;
            }
        }

        PartIteration.Source sameSource = partRevision.getIteration(partIteration).getSource();

        PartRevision partRevisionUpdated = productService.updatePartIteration(pKey, data.getIterationNote(), sameSource, newComponents, attributes, attributeTemplates, links, documentLinkComments, lovNames);

        PartRevisionDTO partRevisionDTO = Tools.mapPartRevisionToPartDTO(partRevisionUpdated);
        return Response.ok(partRevisionDTO).build();
    }

    @GET
    @ApiOperation(value = "Get conversion status", response = ConversionDTO.class)
    @Path("/iterations/{partIteration}/conversion")
    @Produces(MediaType.APPLICATION_JSON)
    public ConversionDTO getConversionStatus(@PathParam("workspaceId") String pWorkspaceId,
                                             @PathParam("partNumber") String partNumber,
                                             @PathParam("partVersion") String partVersion,
                                             @PathParam("partIteration") int partIteration)
            throws UserNotActiveException, PartRevisionNotFoundException, WorkspaceNotFoundException, UserNotFoundException, PartIterationNotFoundException, AccessRightException {
        PartIterationKey partIPK = new PartIterationKey(pWorkspaceId, partNumber, partVersion, partIteration);
        Conversion conversion = productService.getConversion(partIPK);
        if (conversion != null) {
            return mapper.map(conversion, ConversionDTO.class);
        }
        return null;
    }

    @PUT
    @ApiOperation(value = "Retry conversion", response = Response.class)
    @Path("/iterations/{partIteration}/conversion")
    public Response retryConversion(@PathParam("workspaceId") String pWorkspaceId, @PathParam("partNumber") String partNumber, @PathParam("partVersion") String partVersion, @PathParam("partIteration") int iteration) throws UserNotActiveException, PartRevisionNotFoundException, WorkspaceNotFoundException, UserNotFoundException, PartIterationNotFoundException, AccessRightException, NotAllowedException {

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
    @ApiOperation(value = "Checkin part", response = PartRevisionDTO.class)
    @Path("/checkin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PartRevisionDTO checkIn(@PathParam("workspaceId") String workspaceId,
                            @PathParam("partNumber") String partNumber,
                            @PathParam("partVersion") String partVersion,
                            @ApiParam(name = "body", defaultValue = "") String body)
            throws EntityNotFoundException, ESServerException, AccessRightException, NotAllowedException, EntityConstraintException, UserNotActiveException {

        PartRevisionKey revisionKey = new PartRevisionKey(workspaceId, partNumber, partVersion);
        PartRevision partRevision = productService.checkInPart(revisionKey);
        return Tools.mapPartRevisionToPartDTO(partRevision);
    }

    @PUT
    @ApiOperation(value = "Checkout part", response = PartRevisionDTO.class)
    @Path("/checkout")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PartRevisionDTO checkOut(@PathParam("workspaceId") String workspaceId,
                             @PathParam("partNumber") String partNumber,
                             @PathParam("partVersion") String partVersion,
                             @ApiParam(name = "body") String body)
            throws EntityNotFoundException, EntityAlreadyExistsException, CreationException, AccessRightException, NotAllowedException, UserNotActiveException {

        PartRevisionKey revisionKey = new PartRevisionKey(workspaceId, partNumber, partVersion);
        PartRevision partRevision = productService.checkOutPart(revisionKey);
        return Tools.mapPartRevisionToPartDTO(partRevision);
    }

    @PUT
    @ApiOperation(value = "Undo checkout part", response = PartRevisionDTO.class)
    @Path("/undocheckout")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PartRevisionDTO undoCheckOut(@PathParam("workspaceId") String workspaceId,
                                 @PathParam("partNumber") String partNumber,
                                 @PathParam("partVersion") String partVersion,
                                 @ApiParam(name = "body") String body)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException {

        PartRevisionKey revisionKey = new PartRevisionKey(workspaceId, partNumber, partVersion);
        PartRevision partRevision = productService.undoCheckOutPart(revisionKey);
        return Tools.mapPartRevisionToPartDTO(partRevision);
    }

    @PUT
    @ApiOperation(value = "Update part ACL", response = Response.class)
    @Path("/acl")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateACL(@PathParam("workspaceId") String workspaceId,
                              @PathParam("partNumber") String partNumber,
                              @PathParam("partVersion") String partVersion,
                              @ApiParam(required = true, value = "ACL rules to set") ACLDTO acl)
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
    @ApiOperation(value = "Create new part version", response = Response.class)
    @Path("/newVersion")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createNewVersion(@PathParam("workspaceId") String workspaceId,
                                     @PathParam("partNumber") String partNumber,
                                     @PathParam("partVersion") String partVersion,
                                     @ApiParam(required = true, value = "New version of part to create") PartCreationDTO partCreationDTO)
            throws EntityNotFoundException, EntityAlreadyExistsException, CreationException, AccessRightException, NotAllowedException {

        RoleMappingDTO[] roleMappingDTOs = partCreationDTO.getRoleMapping();
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
                userEntries[i].setPrincipal(new User(new Workspace(workspaceId), new Account(entry.getKey())));
                userEntries[i++].setPermission(ACL.Permission.valueOf(entry.getValue().name()));
            }
            i = 0;
            for (Map.Entry<String, ACL.Permission> entry : acl.getGroupEntries().entrySet()) {
                userGroupEntries[i] = new ACLUserGroupEntry();
                userGroupEntries[i].setPrincipal(new UserGroup(new Workspace(workspaceId), entry.getKey()));
                userGroupEntries[i++].setPermission(ACL.Permission.valueOf(entry.getValue().name()));
            }
        }

        Map<String, Collection<String>> userRoleMapping = new HashMap<>();
        Map<String, Collection<String>> groupRoleMapping = new HashMap<>();

        if (roleMappingDTOs != null) {
            for (RoleMappingDTO roleMappingDTO : roleMappingDTOs) {
                userRoleMapping.put(roleMappingDTO.getRoleName(), roleMappingDTO.getUserLogins());
                groupRoleMapping.put(roleMappingDTO.getRoleName(), roleMappingDTO.getGroupIds());
            }
        }

        productService.createPartRevision(revisionKey, description, workflowModelId, userEntries, userGroupEntries, userRoleMapping, groupRoleMapping);

        return Response.ok().build();
    }

    @PUT
    @ApiOperation(value = "Release part", response = PartRevisionDTO.class)
    @Path("/release")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PartRevisionDTO releasePartRevision(@PathParam("workspaceId") String workspaceId,
                                        @PathParam("partNumber") String partNumber,
                                        @PathParam("partVersion") String partVersion,
                                        @ApiParam(name = "body", defaultValue = "") String body)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException {

        PartRevisionKey revisionKey = new PartRevisionKey(workspaceId, partNumber, partVersion);
        PartRevision partRevision = productService.releasePartRevision(revisionKey);
        return Tools.mapPartRevisionToPartDTO(partRevision);
    }

    @PUT
    @ApiOperation(value = "Set part as obsolete", response = PartRevisionDTO.class)
    @Path("/obsolete")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PartRevisionDTO markPartRevisionAsObsolete(@PathParam("workspaceId") String workspaceId,
                                               @PathParam("partNumber") String partNumber,
                                               @PathParam("partVersion") String partVersion,
                                               @ApiParam(name = "body", defaultValue = "") String body)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException {

        PartRevisionKey revisionKey = new PartRevisionKey(workspaceId, partNumber, partVersion);
        PartRevision partRevision = productService.markPartRevisionAsObsolete(revisionKey);
        return Tools.mapPartRevisionToPartDTO(partRevision);
    }

    @DELETE
    @ApiOperation(value = "Delete part", response = Response.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deletePartRevision(@PathParam("workspaceId") String workspaceId, @PathParam("partNumber") String partNumber, @PathParam("partVersion") String partVersion)
            throws EntityNotFoundException, UserNotActiveException, EntityConstraintException, ESServerException, AccessRightException {

        PartRevisionKey revisionKey = new PartRevisionKey(workspaceId, partNumber, partVersion);
        productService.deletePartRevision(revisionKey);
        return Response.ok().build();
    }

    @DELETE
    @ApiOperation(value = "Remove file from part iteration", response = Response.class)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/iterations/{partIteration}/files/{subType}/{fileName}")
    public Response removeFile(@PathParam("workspaceId") String workspaceId,
                               @PathParam("partNumber") String partNumber,
                               @PathParam("partVersion") String partVersion,
                               @PathParam("partIteration") int partIteration,
                               @PathParam("subType") String subType,
                               @PathParam("fileName") String fileName)
            throws EntityNotFoundException, UserNotActiveException {
        PartIterationKey partIKey = new PartIterationKey(workspaceId, partNumber, partVersion, partIteration);
        String fileFullName = workspaceId + "/parts/" + partNumber + "/" + partVersion + "/" + partIteration + "/" + subType + "/" + fileName;
        productService.removeFileInPartIteration(partIKey, subType, fileFullName);
        return Response.ok().build();
    }

    @PUT
    @ApiOperation(value = "Remove attached file from part iteration", response = Response.class)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/iterations/{partIteration}/files/{subType}/{fileName}")
    public FileDTO renameAttachedFile(@PathParam("workspaceId") String workspaceId,
                                      @PathParam("partNumber") String partNumber,
                                      @PathParam("partVersion") String partVersion,
                                      @PathParam("partIteration") int partIteration,
                                      @PathParam("subType") String subType,
                                      @PathParam("fileName") String fileName,
                                      @ApiParam(required = true, value = "File to rename") FileDTO fileDTO)
            throws UserNotActiveException, WorkspaceNotFoundException, CreationException, UserNotFoundException, FileNotFoundException, NotAllowedException, FileAlreadyExistsException, StorageException {
        String fileFullName = workspaceId + "/parts/" + partNumber + "/" + partVersion + "/" + partIteration + "/" + subType + "/" + fileName;
        BinaryResource binaryResource = productService.renameFileInPartIteration(subType, fileFullName, fileDTO.getShortName());
        return new FileDTO(true, binaryResource.getFullName(), binaryResource.getName());
    }

    @POST
    @ApiOperation(value = "Create a new shared part", response = SharedPartDTO.class)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/share")
    public Response createSharedPart(@PathParam("workspaceId") String workspaceId,
                                     @PathParam("partNumber") String partNumber,
                                     @PathParam("partVersion") String partVersion,
                                     @ApiParam(required = true, value = "Shared part to create") SharedPartDTO pSharedPartDTO)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException {

        String password = pSharedPartDTO.getPassword();
        Date expireDate = pSharedPartDTO.getExpireDate();

        SharedPart sharedPart = productService.createSharedPart(new PartRevisionKey(workspaceId, partNumber, partVersion), password, expireDate);
        SharedPartDTO sharedPartDTO = mapper.map(sharedPart, SharedPartDTO.class);
        return Response.ok().entity(sharedPartDTO).build();
    }

    @PUT
    @ApiOperation(value = "Publish part revision", response = Response.class)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/publish")
    public Response publishPartRevision(@PathParam("workspaceId") String workspaceId,
                                        @PathParam("partNumber") String partNumber,
                                        @PathParam("partVersion") String partVersion)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException {
        productService.setPublicSharedPart(new PartRevisionKey(workspaceId, partNumber, partVersion),true);
        return Response.ok().build();
    }

    @PUT
    @ApiOperation(value = "Unpublish part revision", response = Response.class)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/unpublish")
    public Response unPublishPartRevision(@PathParam("workspaceId") String workspaceId,
                                          @PathParam("partNumber") String partNumber,
                                          @PathParam("partVersion") String partVersion)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException {
        productService.setPublicSharedPart(new PartRevisionKey(workspaceId, partNumber, partVersion),false);
        return Response.ok().build();
    }

    @GET
    @ApiOperation(value = "Get part's aborted workflows", response = WorkflowDTO.class, responseContainer = "List")
    @Path("/aborted-workflows")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAbortedWorkflows(@PathParam("workspaceId") String workspaceId,
                                        @PathParam("partNumber") String partNumber,
                                        @PathParam("partVersion") String partVersion)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException {

        PartRevisionKey revisionKey = new PartRevisionKey(workspaceId, partNumber, partVersion);
        PartRevision partRevision = productService.getPartRevision(revisionKey);

        List<Workflow> abortedWorkflows = partRevision.getAbortedWorkflows();
        List<WorkflowDTO> abortedWorkflowsDTO = new ArrayList<>();

        for (Workflow abortedWorkflow : abortedWorkflows) {
            abortedWorkflowsDTO.add(mapper.map(abortedWorkflow, WorkflowDTO.class));
        }
        return Response.ok(new GenericEntity<List<WorkflowDTO>>((List<WorkflowDTO>) abortedWorkflowsDTO) {
        }).build();
    }

    @PUT
    @ApiOperation(value = "Save part's tags", response = PartRevisionDTO.class)
    @Path("/tags")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PartRevisionDTO savePartTags(@PathParam("workspaceId") String workspaceId,
                                        @PathParam("partNumber") String partNumber,
                                        @PathParam("partVersion") String partVersion,
                                        @ApiParam(required = true, value = "Tag list to add") TagListDTO tagListDTO)
            throws EntityNotFoundException, NotAllowedException, ESServerException, AccessRightException, UserNotActiveException {

        List<TagDTO> tagDTOs = tagListDTO.getTags();
        String[] tagLabels = new String[tagDTOs.size()];

        for (int i = 0; i < tagDTOs.size(); i++) {
            tagLabels[i] = tagDTOs.get(i).getLabel();
        }
        PartRevisionKey revisionKey = new PartRevisionKey(workspaceId, partNumber, partVersion);

        PartRevision partRevision = productService.saveTags(revisionKey, tagLabels);
        PartRevisionDTO partRevisionDTO = mapper.map(partRevision, PartRevisionDTO.class);

        return partRevisionDTO;
    }

    @POST
    @ApiOperation(value = "Add tags to part", response = Response.class)
    @Path("/tags")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addPartTag(@PathParam("workspaceId") String workspaceId,
                               @PathParam("partNumber") String partNumber,
                               @PathParam("partVersion") String partVersion,
                               @ApiParam(required = true, value = "Tag list to add") TagListDTO tagListDTO)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException, ESServerException {

        PartRevisionKey revisionKey = new PartRevisionKey(workspaceId, partNumber, partVersion);
        PartRevision partRevision = productService.getPartRevision(revisionKey);
        Set<Tag> tags = partRevision.getTags();
        Set<String> tagLabels = new HashSet<>();

        for (TagDTO tagDTO : tagListDTO.getTags()) {
            tagLabels.add(tagDTO.getLabel());
        }

        for (Tag tag : tags) {
            tagLabels.add(tag.getLabel());
        }

        productService.saveTags(revisionKey, tagLabels.toArray(new String[tagLabels.size()]));
        return Response.ok().build();
    }

    @DELETE
    @ApiOperation(value = "Delete tags from part", response = Response.class)
    @Path("/tags/{tagName}")
    public Response removePartTags(@PathParam("workspaceId") String workspaceId,
                                   @PathParam("partNumber") String partNumber,
                                   @PathParam("partVersion") String partVersion,
                                   @PathParam("tagName") String tagName)
            throws EntityNotFoundException, NotAllowedException, AccessRightException, UserNotActiveException, ESServerException {
        productService.removeTag(new PartRevisionKey(workspaceId, partNumber, partVersion), tagName);
        return Response.ok().build();
    }


    @GET
    @ApiOperation(value = "Get instances", response = VirtualInstanceCollection.class)
    @Path("/instances")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInstances(@PathParam("workspaceId") String workspaceId,
                                 @PathParam("partNumber") String partNumber,
                                 @PathParam("partVersion") String partVersion)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException {
        PartRevision partRevision = productService.getPartRevision(new PartRevisionKey(workspaceId, partNumber, partVersion));
        PSFilter filter = productService.getLatestCheckedInPSFilter(workspaceId);
        VirtualInstanceCollection virtualInstanceCollection = new VirtualInstanceCollection(partRevision, filter);
        return Response.ok().entity(virtualInstanceCollection).build();
    }

    @GET
    @ApiOperation(value = "Get baselines where part revision is involved", response = ProductBaselineDTO.class, responseContainer = "List")
    @Path("/baselines")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBaselinesWherePartRevisionHasIterations(@PathParam("workspaceId") String workspaceId,
                                 @PathParam("partNumber") String partNumber,
                                 @PathParam("partVersion") String partVersion)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException {

        List<ProductBaseline> baselines = productService.findBaselinesWherePartRevisionHasIterations(new PartRevisionKey(workspaceId, partNumber, partVersion));
        List<ProductBaselineDTO> productBaselineDTOs = new ArrayList<>();
        for(ProductBaseline baseline :baselines){
            productBaselineDTOs.add(mapper.map(baseline,ProductBaselineDTO.class));
        }
        return Response.ok(new GenericEntity<List<ProductBaselineDTO>>((List<ProductBaselineDTO>) productBaselineDTOs) {
        }).build();
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
                        CADInstance cadInstance = mapper.map(cadInstanceDTO, CADInstance.class);
                        cadInstance.setRotationMatrix(new RotationMatrix(cadInstanceDTO.getMatrix()));
                        if (cadInstance.getRotationType() == null) {
                            cadInstance.setRotationType(CADInstance.RotationType.ANGLE);
                        }
                        cadInstances.add(cadInstance);

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
                            for (double i = 0; i < substituteLinkDTO.getAmount(); i++) {
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
                partUsageLink.setId(partUsageLinkDTO.getId());
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
            return productService.createPartMaster(workspaceId, componentDTO.getNumber(), componentDTO.getName(), componentDTO.isStandardPart(), null, componentDTO.getDescription(), null, null, null, null, null);
        }
    }

    private DocumentRevisionKey[] createDocumentRevisionKey(List<DocumentRevisionDTO> dtos) {
        DocumentRevisionKey[] data = new DocumentRevisionKey[dtos.size()];
        int i = 0;
        for (DocumentRevisionDTO dto : dtos) {
            data[i++] = new DocumentRevisionKey(dto.getWorkspaceId(), dto.getDocumentMasterId(), dto.getVersion());
        }
        return data;
    }

    private List<PartRevisionDTO> getPartRevisionDTO(Set<PartRevision> partRevisions) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException {
        List<PartRevisionDTO> partRevisionDTOs = new ArrayList<>();

        for (PartRevision partRevision : partRevisions) {
            if (!productService.canAccess(partRevision.getKey())) {
                continue;
            }
            PartRevisionDTO partRevisionDTO = mapper.map(partRevision, PartRevisionDTO.class);
            partRevisionDTO.setNumber(partRevision.getPartNumber());
            partRevisionDTO.setPartKey(partRevision.getPartNumber() + "-" + partRevision.getVersion());
            partRevisionDTO.setName(partRevision.getPartMaster().getName());
            partRevisionDTO.setStandardPart(partRevision.getPartMaster().isStandardPart());
            partRevisionDTOs.add(partRevisionDTO);
        }
        return partRevisionDTOs;
    }

}