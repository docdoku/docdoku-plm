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

import com.docdoku.core.common.*;
import com.docdoku.core.configuration.PathDataMaster;
import com.docdoku.core.configuration.ProductInstanceMaster;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentIterationKey;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.meta.Tag;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartLink;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.ACLUserEntry;
import com.docdoku.core.security.ACLUserGroupEntry;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.core.services.IDocumentWorkflowManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.core.sharing.SharedDocument;
import com.docdoku.core.workflow.Workflow;
import com.docdoku.server.rest.dto.*;
import com.docdoku.server.rest.dto.product.ProductInstanceMasterDTO;
import com.docdoku.server.rest.util.InstanceAttributeFactory;
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
import java.util.*;

@RequestScoped
@Api(hidden = true, value = "document", description = "Operations about document")
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class DocumentResource {

    @Inject
    private IDocumentManagerLocal documentService;

    @Inject
    private IProductManagerLocal productService;

    @Inject
    private IDocumentWorkflowManagerLocal documentWorkflowService;

    private Mapper mapper;

    public DocumentResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @ApiOperation(value = "Get document", response = DocumentRevisionDTO.class)
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO getDocumentRevision(@PathParam("workspaceId") String workspaceId,
                                                   @PathParam("documentId") String documentId,
                                                   @PathParam("documentVersion") String documentVersion)
            throws EntityNotFoundException, AccessRightException, NotAllowedException, UserNotActiveException {

        DocumentRevisionKey documentRevisionKey = new DocumentRevisionKey(workspaceId, documentId, documentVersion);
        DocumentRevision docR = documentService.getDocumentRevision(documentRevisionKey);

        DocumentRevisionDTO docRsDTO = mapper.map(docR, DocumentRevisionDTO.class);
        docRsDTO.setPath(docR.getLocation().getCompletePath());
        docRsDTO.setRoutePath(docR.getLocation().getRoutePath());

        setDocumentRevisionDTOWorkflow(docR, docRsDTO);
        docRsDTO.setIterationSubscription(documentService.isUserIterationChangeEventSubscribedForGivenDocument(workspaceId, docR));
        docRsDTO.setStateSubscription(documentService.isUserStateChangeEventSubscribedForGivenDocument(workspaceId, docR));

        return docRsDTO;
    }

    private void setDocumentRevisionDTOWorkflow(DocumentRevision documentRevision, DocumentRevisionDTO documentRevisionDTO)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        Workflow currentWorkflow = documentWorkflowService.getCurrentWorkflow(documentRevision.getKey());
        if (currentWorkflow != null) {
            documentRevisionDTO.setWorkflow(mapper.map(currentWorkflow, WorkflowDTO.class));
            documentRevisionDTO.setLifeCycleState(currentWorkflow.getLifeCycleState());
        }
    }

    @PUT
    @ApiOperation(value = "Checkin document", response = DocumentRevisionDTO.class)
    @Path("/checkin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO checkInDocument(@PathParam("workspaceId") String workspaceId,
                                               @PathParam("documentId") String documentId,
                                               @PathParam("documentVersion") String documentVersion,
                                               @ApiParam(name = "body") String body)
            throws NotAllowedException, EntityNotFoundException, ESServerException, AccessRightException, UserNotActiveException {
        DocumentRevision docR = documentService.checkInDocument(new DocumentRevisionKey(workspaceId, documentId, documentVersion));
        DocumentRevisionDTO docRsDTO = mapper.map(docR, DocumentRevisionDTO.class);
        docRsDTO.setPath(docR.getLocation().getCompletePath());
        return docRsDTO;
    }

    @PUT
    @ApiOperation(value = "Checkout document", response = DocumentRevisionDTO.class)
    @Path("/checkout")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO checkOutDocument(@PathParam("workspaceId") String workspaceId,
                                                @PathParam("documentId") String documentId,
                                                @PathParam("documentVersion") String documentVersion,
                                                @ApiParam(name = "body") String body)
            throws EntityNotFoundException, NotAllowedException, CreationException, AccessRightException, UserNotActiveException, EntityAlreadyExistsException {
        DocumentRevision docR = documentService.checkOutDocument(new DocumentRevisionKey(workspaceId, documentId, documentVersion));
        DocumentRevisionDTO docRsDTO = mapper.map(docR, DocumentRevisionDTO.class);
        docRsDTO.setPath(docR.getLocation().getCompletePath());
        docRsDTO.setLifeCycleState(docR.getLifeCycleState());
        return docRsDTO;
    }

    @PUT
    @ApiOperation(value = "Undo checkout document", response = DocumentRevisionDTO.class)
    @Path("/undocheckout")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO undoCheckOutDocument(@PathParam("workspaceId") String workspaceId,
                                                    @PathParam("documentId") String documentId,
                                                    @PathParam("documentVersion") String documentVersion,
                                                    @ApiParam(name = "body") String body)
            throws EntityNotFoundException, NotAllowedException, UserNotActiveException, AccessRightException {
        DocumentRevision docR = documentService.undoCheckOutDocument(new DocumentRevisionKey(workspaceId, documentId, documentVersion));
        DocumentRevisionDTO docRsDTO = mapper.map(docR, DocumentRevisionDTO.class);
        docRsDTO.setPath(docR.getLocation().getCompletePath());
        docRsDTO.setLifeCycleState(docR.getLifeCycleState());
        return docRsDTO;
    }

    @PUT
    @ApiOperation(value = "Move document to folder", response = DocumentRevisionDTO.class)
    @Path("/move")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO moveDocument(@PathParam("workspaceId") String workspaceId,
                                            @PathParam("documentId") String documentId,
                                            @PathParam("documentVersion") String documentVersion,
                                            @ApiParam(required = true, value = "Document revision to move") DocumentCreationDTO docCreationDTO)
            throws EntityNotFoundException, NotAllowedException, AccessRightException, UserNotActiveException {
        String parentFolderPath = docCreationDTO.getPath();
        String newCompletePath = Tools.stripTrailingSlash(parentFolderPath);
        DocumentRevisionKey docRsKey = new DocumentRevisionKey(workspaceId, documentId, documentVersion);
        DocumentRevision movedDocumentRevision = documentService.moveDocumentRevision(newCompletePath, docRsKey);
        DocumentRevisionDTO documentRevisionDTO = mapper.map(movedDocumentRevision, DocumentRevisionDTO.class);
        documentRevisionDTO.setPath(movedDocumentRevision.getLocation().getCompletePath());
        documentRevisionDTO.setLifeCycleState(movedDocumentRevision.getLifeCycleState());
        return documentRevisionDTO;
    }

    @PUT
    @ApiOperation(value = "Subscribe to notifications on change events", response = Response.class)
    @Path("/notification/iterationChange/subscribe")
    public Response subscribeToIterationChangeEvent(@PathParam("workspaceId") String workspaceId,
                                                    @PathParam("documentId") String documentId,
                                                    @PathParam("documentVersion") String documentVersion)
            throws EntityNotFoundException, AccessRightException, NotAllowedException, UserNotActiveException {
        documentService.subscribeToIterationChangeEvent(new DocumentRevisionKey(workspaceId, documentId, documentVersion));
        return Response.ok().build();
    }

    @PUT
    @ApiOperation(value = "Unsubscribe from notifications on change events", response = Response.class)
    @Path("/notification/iterationChange/unsubscribe")
    public Response unSubscribeToIterationChangeEvent(@PathParam("workspaceId") String workspaceId,
                                                      @PathParam("documentId") String documentId,
                                                      @PathParam("documentVersion") String documentVersion)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        documentService.unsubscribeToIterationChangeEvent(new DocumentRevisionKey(workspaceId, documentId, documentVersion));
        return Response.ok().build();
    }

    @PUT
    @ApiOperation(value = "Subscribe to notifications on state events", response = Response.class)
    @Path("/notification/stateChange/subscribe")
    public Response subscribeToStateChangeEvent(@PathParam("workspaceId") String workspaceId,
                                                @PathParam("documentId") String documentId,
                                                @PathParam("documentVersion") String documentVersion)
            throws EntityNotFoundException, AccessRightException, NotAllowedException, UserNotActiveException {
        documentService.subscribeToStateChangeEvent(new DocumentRevisionKey(workspaceId, documentId, documentVersion));
        return Response.ok().build();
    }

    @PUT
    @ApiOperation(value = "Unsubscribe to notifications on state events", response = Response.class)
    @Path("/notification/stateChange/unsubscribe")
    public Response unsubscribeToStateChangeEvent(@PathParam("workspaceId") String workspaceId,
                                                  @PathParam("documentId") String documentId,
                                                  @PathParam("documentVersion") String documentVersion)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        documentService.unsubscribeToStateChangeEvent(new DocumentRevisionKey(workspaceId, documentId, documentVersion));
        return Response.ok().build();
    }

    @PUT
    @ApiOperation(value = "Update document iteration", response = DocumentIterationDTO.class)
    @Path("/iterations/{docIteration}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentIterationDTO updateDocumentIteration(@PathParam("workspaceId") String workspaceId,
                                                        @PathParam("documentId") String documentId,
                                                        @PathParam("documentVersion") String documentVersion,
                                                        @PathParam("docIteration") String docIteration,
                                                        @ApiParam(required = true, value = "Document iteration to update") DocumentIterationDTO documentIterationDTO)
            throws EntityNotFoundException, NotAllowedException, AccessRightException, UserNotActiveException {
        String pRevisionNote = documentIterationDTO.getRevisionNote();
        int pIteration = Integer.parseInt(docIteration);

        List<DocumentRevisionDTO> linkedDocs = documentIterationDTO.getLinkedDocuments();
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

        List<InstanceAttributeDTO> instanceAttributes = documentIterationDTO.getInstanceAttributes();
        List<InstanceAttribute> attributes = null;
        if (instanceAttributes != null) {
            attributes = new InstanceAttributeFactory().createInstanceAttributes(instanceAttributes);
        }

        DocumentRevision docR = documentService.updateDocument(new DocumentIterationKey(workspaceId, documentId, documentVersion, pIteration), pRevisionNote, attributes, links, documentLinkComments);
        return mapper.map(docR.getLastIteration(), DocumentIterationDTO.class);
    }

    @PUT
    @ApiOperation(value = "Create a new version of the document", response = DocumentRevisionDTO.class, responseContainer = "List")
    @Path("/newVersion")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO[] createNewVersion(@PathParam("workspaceId") String pWorkspaceId,
                                                  @PathParam("documentId") String documentId,
                                                  @PathParam("documentVersion") String documentVersion,
                                                  @ApiParam(required = true, value = "New version of document to create") DocumentCreationDTO docCreationDTO)
            throws EntityNotFoundException, EntityAlreadyExistsException, NotAllowedException, AccessRightException, CreationException, UserNotActiveException {
        String pTitle = docCreationDTO.getTitle();
        String pDescription = docCreationDTO.getDescription();
        String pWorkflowModelId = docCreationDTO.getWorkflowModelId();
        RoleMappingDTO[] roleMappingDTOs = docCreationDTO.getRoleMapping();
        ACLDTO acl = docCreationDTO.getAcl();

        ACLUserEntry[] userEntries = null;
        ACLUserGroupEntry[] userGroupEntries = null;
        if (acl != null) {
            userEntries = new ACLUserEntry[acl.getUserEntries().size()];
            userGroupEntries = new ACLUserGroupEntry[acl.getGroupEntries().size()];
            int i = 0;
            for (Map.Entry<String, ACL.Permission> entry : acl.getUserEntries().entrySet()) {
                userEntries[i] = new ACLUserEntry();
                userEntries[i].setPrincipal(new User(new Workspace(pWorkspaceId), new Account(entry.getKey())));
                userEntries[i++].setPermission(ACL.Permission.valueOf(entry.getValue().name()));
            }
            i = 0;
            for (Map.Entry<String, ACL.Permission> entry : acl.getGroupEntries().entrySet()) {
                userGroupEntries[i] = new ACLUserGroupEntry();
                userGroupEntries[i].setPrincipal(new UserGroup(new Workspace(pWorkspaceId), entry.getKey()));
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

        DocumentRevision[] docR = documentService.createDocumentRevision(new DocumentRevisionKey(pWorkspaceId, documentId, documentVersion), pTitle, pDescription, pWorkflowModelId, userEntries, userGroupEntries, userRoleMapping, groupRoleMapping);
        DocumentRevisionDTO[] dtos = new DocumentRevisionDTO[docR.length];

        for (int i = 0; i < docR.length; i++) {
            dtos[i] = mapper.map(docR[i], DocumentRevisionDTO.class);
            dtos[i].setPath(docR[i].getLocation().getCompletePath());
            dtos[i].setLifeCycleState(docR[i].getLifeCycleState());
            dtos[i] = Tools.createLightDocumentRevisionDTO(dtos[i]);
            dtos[i].setIterationSubscription(documentService.isUserIterationChangeEventSubscribedForGivenDocument(pWorkspaceId, docR[i]));
            dtos[i].setStateSubscription(documentService.isUserStateChangeEventSubscribedForGivenDocument(pWorkspaceId, docR[i]));
        }

        return dtos;
    }

    @PUT
    @ApiOperation(value = "Release document", response = DocumentRevisionDTO.class)
    @Path("/release")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO releaseDocumentRevision(@PathParam("workspaceId") String workspaceId,
                                            @PathParam("documentId") String documentId,
                                            @PathParam("documentVersion") String documentVersion,
                                            @ApiParam(name = "body", defaultValue = "") String body)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException {

        DocumentRevisionKey revisionKey = new DocumentRevisionKey(workspaceId, documentId, documentVersion);
        DocumentRevision docR = documentService.releaseDocumentRevision(revisionKey);
        return mapper.map(docR, DocumentRevisionDTO.class);
    }

    @PUT
    @ApiOperation(value = "Set document as obsolete", response = DocumentRevisionDTO.class)
    @Path("/obsolete")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO markDocumentRevisionAsObsolete(@PathParam("workspaceId") String workspaceId,
                                                   @PathParam("documentId") String documentId,
                                                   @PathParam("documentVersion") String documentVersion,
                                                   @ApiParam(name = "body", defaultValue = "") String body)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException {

        DocumentRevisionKey revisionKey = new DocumentRevisionKey(workspaceId, documentId, documentVersion);
        DocumentRevision docR = documentService.markDocumentRevisionAsObsolete(revisionKey);
        return mapper.map(docR, DocumentRevisionDTO.class);
    }

    @PUT
    @ApiOperation(value = "Set the tags of the document", response = DocumentRevisionDTO.class)
    @Path("/tags")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO saveDocTags(@PathParam("workspaceId") String workspaceId,
                                           @PathParam("documentId") String documentId,
                                           @PathParam("documentVersion") String documentVersion,
                                           @ApiParam(required = true, value = "Tag list to save") TagListDTO tagListDTO)
            throws EntityNotFoundException, NotAllowedException, ESServerException, AccessRightException, UserNotActiveException {
        List<TagDTO> tagDTOs = tagListDTO.getTags();
        String[] tagsLabel = new String[tagDTOs.size()];
        for (int i = 0; i < tagDTOs.size(); i++) {
            tagsLabel[i] = tagDTOs.get(i).getLabel();
        }

        DocumentRevision documentRevision = documentService.saveTags(new DocumentRevisionKey(workspaceId, documentId, documentVersion), tagsLabel);
        DocumentRevisionDTO documentRevisionDTO = mapper.map(documentRevision, DocumentRevisionDTO.class);
        documentRevisionDTO.setPath(documentRevision.getLocation().getCompletePath());
        documentRevisionDTO.setLifeCycleState(documentRevision.getLifeCycleState());

        return documentRevisionDTO;
    }

    @POST
    @ApiOperation(value = "Add tags to document", response = Response.class)
    @Path("/tags")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addDocTag(@PathParam("workspaceId") String workspaceId,
                              @PathParam("documentId") String documentId,
                              @PathParam("documentVersion") String documentVersion,
                              @ApiParam(required = true, value = "Tag list to add") TagListDTO tagListDTO)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException, ESServerException {

        DocumentRevisionKey docRPK = new DocumentRevisionKey(workspaceId, documentId, documentVersion);
        DocumentRevision docR = documentService.getDocumentRevision(docRPK);
        Set<Tag> tags = docR.getTags();
        Set<String> tagLabels = new HashSet<>();

        for (TagDTO tagDTO : tagListDTO.getTags()) {
            tagLabels.add(tagDTO.getLabel());
        }

        for (Tag tag : tags) {
            tagLabels.add(tag.getLabel());
        }

        documentService.saveTags(docRPK, tagLabels.toArray(new String[tagLabels.size()]));
        return Response.ok().build();
    }

    @DELETE
    @ApiOperation(value = "Remove tags from document", response = Response.class)
    @Path("/tags/{tagName}")
    public Response removeDocTags(@PathParam("workspaceId") String workspaceId,
                                  @PathParam("documentId") String documentId,
                                  @PathParam("documentVersion") String documentVersion,
                                  @PathParam("tagName") String tagName)
            throws EntityNotFoundException, NotAllowedException, AccessRightException, UserNotActiveException, ESServerException {
        documentService.removeTag(new DocumentRevisionKey(workspaceId, documentId, documentVersion), tagName);
        return Response.ok().build();
    }

    @ApiOperation(value = "Delete the document", response = Response.class)
    @DELETE
    public Response deleteDocument(@PathParam("workspaceId") String workspaceId,
                                   @PathParam("documentId") String documentId,
                                   @PathParam("documentVersion") String documentVersion)
            throws EntityNotFoundException, NotAllowedException, AccessRightException, UserNotActiveException, ESServerException, EntityConstraintException {
        documentService.deleteDocumentRevision(new DocumentRevisionKey(workspaceId, documentId, documentVersion));
        return Response.ok().build();
    }

    @PUT
    @ApiOperation(value = "Rename attached files of document", response = FileDTO.class)
    @Path("/iterations/{docIteration}/files/{fileName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public FileDTO renameAttachedFile(@PathParam("workspaceId") String workspaceId,
                                      @PathParam("documentId") String documentId,
                                      @PathParam("documentVersion") String documentVersion,
                                      @PathParam("docIteration") int docIteration,
                                      @PathParam("fileName") String fileName,
                                      @ApiParam(required = true, value = "File to rename") FileDTO fileDTO)
            throws EntityNotFoundException, NotAllowedException, AccessRightException, UserNotActiveException, FileAlreadyExistsException, CreationException, StorageException {
        String fileFullName = workspaceId + "/documents/" + documentId + "/" + documentVersion + "/" + docIteration + "/" + fileName;
        BinaryResource binaryResource = documentService.renameFileInDocument(fileFullName, fileDTO.getShortName());
        return new FileDTO(true, binaryResource.getFullName(), binaryResource.getName());
    }

    @DELETE
    @ApiOperation(value = "Remove attached file from document", response = Response.class)
    @Path("/iterations/{docIteration}/files/{fileName}")
    public Response removeAttachedFile(@PathParam("workspaceId") String workspaceId,
                                       @PathParam("documentId") String documentId,
                                       @PathParam("documentVersion") String documentVersion,
                                       @PathParam("docIteration") int docIteration,
                                       @PathParam("fileName") String fileName)
            throws EntityNotFoundException, NotAllowedException, AccessRightException, UserNotActiveException {
        String fileFullName = workspaceId + "/documents/" + documentId + "/" + documentVersion + "/" + docIteration + "/" + fileName;
        documentService.removeFileFromDocument(fileFullName);
        return Response.ok().build();
    }

    @POST
    @ApiOperation(value = "Create a shared document", response = SharedDocumentDTO.class)
    @Path("share")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createSharedDocument(@PathParam("workspaceId") String workspaceId,
                                         @PathParam("documentId") String documentId,
                                         @PathParam("documentVersion") String documentVersion,
                                         @ApiParam(required = true, value = "Shared document to create") SharedDocumentDTO pSharedDocumentDTO)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException {

        String password = pSharedDocumentDTO.getPassword();
        Date expireDate = pSharedDocumentDTO.getExpireDate();

        SharedDocument sharedDocument = documentService.createSharedDocument(new DocumentRevisionKey(workspaceId, documentId, documentVersion), password, expireDate);
        SharedDocumentDTO sharedDocumentDTO = mapper.map(sharedDocument, SharedDocumentDTO.class);
        return Response.ok().entity(sharedDocumentDTO).build();
    }


    @PUT
    @ApiOperation(value = "Publish a document", response = Response.class)
    @Path("publish")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response publishDocumentRevision(@PathParam("workspaceId") String workspaceId,
                                        @PathParam("documentId") String documentId,
                                        @PathParam("documentVersion") String documentVersion)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException {
        documentService.setDocumentPublicShared(new DocumentRevisionKey(workspaceId, documentId, documentVersion), true);
        return Response.ok().build();
    }

    @PUT
    @ApiOperation(value = "Unpublish a document", response = Response.class)
    @Path("unpublish")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response unPublishDocumentRevision(@PathParam("workspaceId") String workspaceId,
                                          @PathParam("documentId") String documentId,
                                          @PathParam("documentVersion") String documentVersion)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException {
        documentService.setDocumentPublicShared(new DocumentRevisionKey(workspaceId, documentId, documentVersion), false);
        return Response.ok().build();
    }

    @PUT
    @ApiOperation(value = "Update document's ACL", response = Response.class)
    @Path("acl")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateACL(@PathParam("workspaceId") String pWorkspaceId,
                              @PathParam("documentId") String documentId,
                              @PathParam("documentVersion") String documentVersion,
                              @ApiParam(required = true, value = "ACL rules to set") ACLDTO acl)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException, NotAllowedException {
        DocumentRevisionKey documentRevisionKey = new DocumentRevisionKey(pWorkspaceId, documentId, documentVersion);

        if (!acl.getGroupEntries().isEmpty() || !acl.getUserEntries().isEmpty()) {

            Map<String, String> userEntries = new HashMap<>();
            Map<String, String> groupEntries = new HashMap<>();

            for (Map.Entry<String, ACL.Permission> entry : acl.getUserEntries().entrySet()) {
                userEntries.put(entry.getKey(), entry.getValue().name());
            }

            for (Map.Entry<String, ACL.Permission> entry : acl.getGroupEntries().entrySet()) {
                groupEntries.put(entry.getKey(), entry.getValue().name());
            }

            documentService.updateDocumentACL(pWorkspaceId, documentRevisionKey, userEntries, groupEntries);
        } else {
            documentService.removeACLFromDocumentRevision(documentRevisionKey);
        }
        return Response.ok().build();
    }

    @GET
    @ApiOperation(value = "Get document's aborted workflows", response = WorkflowDTO.class, responseContainer = "List")
    @Path("aborted-workflows")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAbortedWorkflows(@PathParam("workspaceId") String workspaceId,
                                        @PathParam("documentId") String documentId,
                                        @PathParam("documentVersion") String documentVersion)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException {
        Workflow[] abortedWorkflows = documentWorkflowService.getAbortedWorkflow(new DocumentRevisionKey(workspaceId, documentId, documentVersion));
        List<WorkflowDTO> abortedWorkflowsDTO = new ArrayList<>();

        for (Workflow abortedWorkflow : abortedWorkflows) {
            abortedWorkflowsDTO.add(mapper.map(abortedWorkflow, WorkflowDTO.class));
        }

        Collections.sort(abortedWorkflowsDTO);

        return Response.ok(new GenericEntity<List<WorkflowDTO>>((List<WorkflowDTO>) abortedWorkflowsDTO) {
        }).build();
    }

    @GET
    @ApiOperation(value = "Get inverse documents links", response = DocumentRevisionDTO.class, responseContainer = "List")
    @Path("{iteration}/inverse-document-link")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInverseDocumentLinks(@PathParam("workspaceId") String workspaceId,
                                            @PathParam("documentId") String documentId,
                                            @PathParam("documentVersion") String documentVersion,
                                            @PathParam("iteration") int iteration,
                                            @QueryParam("configSpec") String configSpecType)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, DocumentRevisionNotFoundException, DocumentIterationNotFoundException {
        DocumentRevisionKey docKey = new DocumentRevisionKey(workspaceId, documentId, documentVersion);
        List<DocumentIteration> documents = documentService.getInverseDocumentsLink(docKey);
        Set<DocumentRevisionDTO> dtos = new HashSet<>();
        for (DocumentIteration doc : documents) {
            dtos.add(new DocumentRevisionDTO(doc.getWorkspaceId(), doc.getDocumentMasterId(), doc.getTitle(), doc.getVersion()));
        }

        return Response.ok(new GenericEntity<List<DocumentRevisionDTO>>((List<DocumentRevisionDTO>) new ArrayList<>(dtos)) {
        }).build();
    }

    @GET
    @ApiOperation(value = "Get inverse parts links", response = PartRevisionDTO.class, responseContainer = "List")
    @Path("{iteration}/inverse-part-link")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInversePartsLinks(@PathParam("workspaceId") String workspaceId,
                                         @PathParam("documentId") String documentId,
                                         @PathParam("documentVersion") String documentVersion,
                                         @PathParam("iteration") int iteration,
                                         @QueryParam("configSpec") String configSpecType)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, PartIterationNotFoundException, DocumentRevisionNotFoundException {
        DocumentRevisionKey docKey = new DocumentRevisionKey(workspaceId, documentId, documentVersion);
        List<PartIteration> parts = productService.getInversePartsLink(docKey);
        Set<PartRevisionDTO> dtos = new HashSet<>();
        for (PartIteration part : parts) {
            dtos.add(new PartRevisionDTO(workspaceId, part.getNumber(), part.getPartName(), part.getVersion()));
        }

        return Response.ok(new GenericEntity<List<PartRevisionDTO>>((List<PartRevisionDTO>) new ArrayList<>(dtos)) {
        }).build();
    }

    @GET
    @ApiOperation(value = "Get inverse product instances links", response = ProductInstanceMasterDTO.class, responseContainer = "List")
    @Path("{iteration}/inverse-product-instances-link")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInverseProductInstancesLinks(@PathParam("workspaceId") String workspaceId,
                                                    @PathParam("documentId") String documentId,
                                                    @PathParam("documentVersion") String documentVersion,
                                                    @PathParam("iteration") int iteration,
                                                    @QueryParam("configSpec") String configSpecType)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, PartIterationNotFoundException, DocumentRevisionNotFoundException {
        DocumentRevisionKey docKey = new DocumentRevisionKey(workspaceId, documentId, documentVersion);
        Set<ProductInstanceMaster> productInstanceMasterList = productService.getInverseProductInstancesLink(docKey);
        Set<ProductInstanceMasterDTO> dtos = new HashSet<>();
        for (ProductInstanceMaster productInstanceMaster : productInstanceMasterList) {
            dtos.add(mapper.map(productInstanceMaster, ProductInstanceMasterDTO.class));
        }
        return Response.ok(new GenericEntity<List<ProductInstanceMasterDTO>>((List<ProductInstanceMasterDTO>) new ArrayList<>(dtos)) {
        }).build();
    }

    @GET
    @ApiOperation(value = "Get inverse path data links", response = PathDataMasterDTO.class, responseContainer = "List")
    @Path("{iteration}/inverse-path-data-link")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInversePathDataLinks(@PathParam("workspaceId") String workspaceId,
                                            @PathParam("documentId") String documentId,
                                            @PathParam("documentVersion") String documentVersion,
                                            @PathParam("iteration") int iteration,
                                            @QueryParam("configSpec") String configSpecType)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, PartIterationNotFoundException, DocumentRevisionNotFoundException, ConfigurationItemNotFoundException, PartUsageLinkNotFoundException {
        DocumentRevisionKey docKey = new DocumentRevisionKey(workspaceId, documentId, documentVersion);
        Set<PathDataMaster> pathDataMasters = productService.getInversePathDataLink(docKey);

        Set<PathDataMasterDTO> dtos = new HashSet<>();
        for (PathDataMaster pathDataMaster : pathDataMasters) {
            PathDataMasterDTO dto = mapper.map(pathDataMaster, PathDataMasterDTO.class);
            ProductInstanceMaster productInstanceMaster = productService.findProductByPathMaster(workspaceId, pathDataMaster);

            LightPartLinkListDTO partLinksList = new LightPartLinkListDTO();
            List<PartLink> path = productService.decodePath(productInstanceMaster.getInstanceOf().getKey(), pathDataMaster.getPath());
            for (PartLink partLink : path) {
                partLinksList.getPartLinks().add(new LightPartLinkDTO(partLink));
            }
            dto.setPartLinksList(partLinksList);

            dto.setSerialNumber(productInstanceMaster.getSerialNumber());
            dtos.add(dto);
        }

        return Response.ok(new GenericEntity<List<PathDataMasterDTO>>((List<PathDataMasterDTO>) new ArrayList<>(dtos)) {
        }).build();

    }

    private DocumentRevisionKey[] createDocumentRevisionKeys(List<DocumentRevisionDTO> dtos) {
        DocumentRevisionKey[] data = new DocumentRevisionKey[dtos.size()];
        int i = 0;
        for (DocumentRevisionDTO dto : dtos) {
            data[i++] = new DocumentRevisionKey(dto.getWorkspaceId(), dto.getDocumentMasterId(), dto.getVersion());
        }

        return data;
    }

}