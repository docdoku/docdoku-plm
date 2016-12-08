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
import com.docdoku.core.security.*;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.core.services.IDocumentWorkflowManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.core.sharing.SharedDocument;
import com.docdoku.core.workflow.Workflow;
import com.docdoku.server.rest.dto.*;
import com.docdoku.server.rest.dto.product.ProductInstanceMasterDTO;
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
    @ApiOperation(value = "Get document",
            response = DocumentRevisionDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of DocumentRevisionDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO getDocumentRevision(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Document master id") @PathParam("documentId") String documentId,
            @ApiParam(required = true, value = "Document version") @PathParam("documentVersion") String documentVersion)
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


    @PUT
    @ApiOperation(value = "Checkin document",
            response = DocumentRevisionDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of checked in DocumentRevisionDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("/checkin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO checkInDocument(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Document master id") @PathParam("documentId") String documentId,
            @ApiParam(required = true, value = "Document version") @PathParam("documentVersion") String documentVersion)
            throws NotAllowedException, EntityNotFoundException, ESServerException, AccessRightException, UserNotActiveException {
        DocumentRevision docR = documentService.checkInDocument(new DocumentRevisionKey(workspaceId, documentId, documentVersion));
        DocumentRevisionDTO docRsDTO = mapper.map(docR, DocumentRevisionDTO.class);
        docRsDTO.setPath(docR.getLocation().getCompletePath());
        return docRsDTO;
    }

    @PUT
    @ApiOperation(value = "Checkout document",
            response = DocumentRevisionDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of checked out DocumentRevisionDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("/checkout")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO checkOutDocument(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Document master id") @PathParam("documentId") String documentId,
            @ApiParam(required = true, value = "Document version") @PathParam("documentVersion") String documentVersion)
            throws EntityNotFoundException, NotAllowedException, CreationException, AccessRightException,
            UserNotActiveException, EntityAlreadyExistsException {

        DocumentRevision docR = documentService.checkOutDocument(new DocumentRevisionKey(workspaceId, documentId, documentVersion));
        DocumentRevisionDTO docRsDTO = mapper.map(docR, DocumentRevisionDTO.class);
        docRsDTO.setPath(docR.getLocation().getCompletePath());
        docRsDTO.setLifeCycleState(docR.getLifeCycleState());
        return docRsDTO;
    }

    @PUT
    @ApiOperation(value = "Undo checkout document",
            response = DocumentRevisionDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of undo checked out DocumentRevisionDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("/undocheckout")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO undoCheckOutDocument(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Document master id") @PathParam("documentId") String documentId,
            @ApiParam(required = true, value = "Document version") @PathParam("documentVersion") String documentVersion)
            throws EntityNotFoundException, NotAllowedException, UserNotActiveException, AccessRightException {
        DocumentRevision docR = documentService.undoCheckOutDocument(new DocumentRevisionKey(workspaceId, documentId, documentVersion));
        DocumentRevisionDTO docRsDTO = mapper.map(docR, DocumentRevisionDTO.class);
        docRsDTO.setPath(docR.getLocation().getCompletePath());
        docRsDTO.setLifeCycleState(docR.getLifeCycleState());
        return docRsDTO;
    }

    @PUT
    @ApiOperation(value = "Move document to folder",
            response = DocumentRevisionDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of updated DocumentRevisionDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("/move")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO moveDocument(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Document master id") @PathParam("documentId") String documentId,
            @ApiParam(required = true, value = "Document version") @PathParam("documentVersion") String documentVersion,
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
    @ApiOperation(value = "Subscribe to notifications on change events",
            response = Response.class)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successful change event subscription"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("/notification/iterationChange/subscribe")
    public Response subscribeToIterationChangeEvent(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Document master id") @PathParam("documentId") String documentId,
            @ApiParam(required = true, value = "Document version") @PathParam("documentVersion") String documentVersion)
            throws EntityNotFoundException, AccessRightException, NotAllowedException, UserNotActiveException {
        documentService.subscribeToIterationChangeEvent(new DocumentRevisionKey(workspaceId, documentId, documentVersion));
        return Response.noContent().build();
    }

    @PUT
    @ApiOperation(value = "Unsubscribe from notifications on change events",
            response = Response.class)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successful change event un-subscription"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("/notification/iterationChange/unsubscribe")
    public Response unSubscribeToIterationChangeEvent(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Document master id") @PathParam("documentId") String documentId,
            @ApiParam(required = true, value = "Document version") @PathParam("documentVersion") String documentVersion)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        documentService.unsubscribeToIterationChangeEvent(new DocumentRevisionKey(workspaceId, documentId, documentVersion));
        return Response.noContent().build();
    }

    @PUT
    @ApiOperation(value = "Subscribe to notifications on state events",
            response = Response.class)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successful state event subscription"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("/notification/stateChange/subscribe")
    public Response subscribeToStateChangeEvent(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Document master id") @PathParam("documentId") String documentId,
            @ApiParam(required = true, value = "Document version") @PathParam("documentVersion") String documentVersion)
            throws EntityNotFoundException, AccessRightException, NotAllowedException, UserNotActiveException {
        documentService.subscribeToStateChangeEvent(new DocumentRevisionKey(workspaceId, documentId, documentVersion));
        return Response.noContent().build();
    }

    @PUT
    @ApiOperation(value = "Unsubscribe to notifications on state events",
            response = Response.class)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successful state event un-subscription"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("/notification/stateChange/unsubscribe")
    public Response unsubscribeToStateChangeEvent(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Document master id") @PathParam("documentId") String documentId,
            @ApiParam(required = true, value = "Document version") @PathParam("documentVersion") String documentVersion)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        documentService.unsubscribeToStateChangeEvent(new DocumentRevisionKey(workspaceId, documentId, documentVersion));
        return Response.noContent().build();
    }

    @PUT
    @ApiOperation(value = "Update document iteration",
            response = DocumentIterationDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of updated DocumentIterationDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("/iterations/{docIteration}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentIterationDTO updateDocumentIteration(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Document master id") @PathParam("documentId") String documentId,
            @ApiParam(required = true, value = "Document version") @PathParam("documentVersion") String documentVersion,
            @ApiParam(required = true, value = "Document iteration") @PathParam("docIteration") String docIteration,
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

        List<InstanceAttributeDTO> instanceAttributeDTOs = documentIterationDTO.getInstanceAttributes();
        List<InstanceAttribute> attributes = new ArrayList<>();

        if (instanceAttributeDTOs != null) {
            for (InstanceAttributeDTO dto : instanceAttributeDTOs) {
                dto.setWorkspaceId(workspaceId);
                attributes.add(mapper.map(dto, InstanceAttribute.class));
            }
        }

        DocumentRevision docR = documentService.updateDocument(
                new DocumentIterationKey(workspaceId, documentId, documentVersion, pIteration),
                pRevisionNote, attributes, links, documentLinkComments);

        return mapper.map(docR.getLastIteration(), DocumentIterationDTO.class);
    }

    @PUT
    @ApiOperation(value = "Create a new version of the document",
            response = DocumentRevisionDTO.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of created DocumentRevisionDTO version, and its previous version"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("/newVersion")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO[] createNewDocumentVersion(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Document master id") @PathParam("documentId") String documentId,
            @ApiParam(required = true, value = "Document version") @PathParam("documentVersion") String documentVersion,
            @ApiParam(required = true, value = "New version of document to create") DocumentCreationDTO docCreationDTO)
            throws EntityNotFoundException, EntityAlreadyExistsException, NotAllowedException, AccessRightException,
            CreationException, UserNotActiveException {
        String pTitle = docCreationDTO.getTitle();
        String pDescription = docCreationDTO.getDescription();
        String pWorkflowModelId = docCreationDTO.getWorkflowModelId();
        RoleMappingDTO[] roleMappingDTOs = docCreationDTO.getRoleMapping();

        ACLDTO acl = docCreationDTO.getAcl();

        Map<String, String> userEntries = acl != null ? acl.getUserEntriesMap() : null;
        Map<String, String> userGroupEntries = acl != null ? acl.getUserGroupEntriesMap() : null;

        Map<String, Collection<String>> userRoleMapping = new HashMap<>();
        Map<String, Collection<String>> groupRoleMapping = new HashMap<>();

        if (roleMappingDTOs != null) {
            for (RoleMappingDTO roleMappingDTO : roleMappingDTOs) {
                userRoleMapping.put(roleMappingDTO.getRoleName(), roleMappingDTO.getUserLogins());
                groupRoleMapping.put(roleMappingDTO.getRoleName(), roleMappingDTO.getGroupIds());
            }
        }

        DocumentRevision[] docR = documentService.createDocumentRevision(
                new DocumentRevisionKey(workspaceId, documentId, documentVersion),
                pTitle, pDescription, pWorkflowModelId, userEntries, userGroupEntries, userRoleMapping, groupRoleMapping);
        DocumentRevisionDTO[] dtos = new DocumentRevisionDTO[docR.length];

        for (int i = 0; i < docR.length; i++) {
            dtos[i] = mapper.map(docR[i], DocumentRevisionDTO.class);
            dtos[i].setPath(docR[i].getLocation().getCompletePath());
            dtos[i].setLifeCycleState(docR[i].getLifeCycleState());
            dtos[i] = Tools.createLightDocumentRevisionDTO(dtos[i]);
            dtos[i].setIterationSubscription(documentService.isUserIterationChangeEventSubscribedForGivenDocument(workspaceId, docR[i]));
            dtos[i].setStateSubscription(documentService.isUserStateChangeEventSubscribedForGivenDocument(workspaceId, docR[i]));
        }

        return dtos;
    }

    @PUT
    @ApiOperation(value = "Release document",
            response = DocumentRevisionDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of released DocumentRevisionDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("/release")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO releaseDocumentRevision(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Document master id") @PathParam("documentId") String documentId,
            @ApiParam(required = true, value = "Document version") @PathParam("documentVersion") String documentVersion)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException {

        DocumentRevisionKey revisionKey = new DocumentRevisionKey(workspaceId, documentId, documentVersion);
        DocumentRevision docR = documentService.releaseDocumentRevision(revisionKey);
        return mapper.map(docR, DocumentRevisionDTO.class);
    }

    @PUT
    @ApiOperation(value = "Set document as obsolete",
            response = DocumentRevisionDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of obsolete DocumentRevisionDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("/obsolete")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO markDocumentRevisionAsObsolete(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Document master id") @PathParam("documentId") String documentId,
            @ApiParam(required = true, value = "Document version") @PathParam("documentVersion") String documentVersion)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException {

        DocumentRevisionKey revisionKey = new DocumentRevisionKey(workspaceId, documentId, documentVersion);
        DocumentRevision docR = documentService.markDocumentRevisionAsObsolete(revisionKey);
        return mapper.map(docR, DocumentRevisionDTO.class);
    }

    @PUT
    @ApiOperation(value = "Set the tags of the document",
            response = DocumentRevisionDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of updated DocumentRevisionDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("/tags")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO saveDocTags(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Document master id") @PathParam("documentId") String documentId,
            @ApiParam(required = true, value = "Document version") @PathParam("documentVersion") String documentVersion,
            @ApiParam(required = true, value = "Tag list to save") TagListDTO tagListDTO)
            throws EntityNotFoundException, NotAllowedException, ESServerException,
            AccessRightException, UserNotActiveException {

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
    @ApiOperation(value = "Add tags to document",
            response = DocumentRevisionDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of updated DocumentRevisionDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("/tags")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO addDocTag(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Document master id") @PathParam("documentId") String documentId,
            @ApiParam(required = true, value = "Document version") @PathParam("documentVersion") String documentVersion,
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

        DocumentRevision documentRevision = documentService.saveTags(docRPK, tagLabels.toArray(new String[tagLabels.size()]));
        DocumentRevisionDTO documentRevisionDTO = mapper.map(documentRevision, DocumentRevisionDTO.class);
        documentRevisionDTO.setPath(documentRevision.getLocation().getCompletePath());
        documentRevisionDTO.setLifeCycleState(documentRevision.getLifeCycleState());

        return documentRevisionDTO;
    }

    @DELETE
    @ApiOperation(value = "Remove tags from document",
            response = DocumentRevisionDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of updated DocumentRevisionDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("/tags/{tagName}")
    public DocumentRevisionDTO removeDocTags(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Document master id") @PathParam("documentId") String documentId,
            @ApiParam(required = true, value = "Document version") @PathParam("documentVersion") String documentVersion,
            @ApiParam(required = true, value = "Tag name") @PathParam("tagName") String tagName)
            throws EntityNotFoundException, NotAllowedException, AccessRightException, UserNotActiveException, ESServerException {
        DocumentRevision documentRevision = documentService.removeTag(new DocumentRevisionKey(workspaceId, documentId, documentVersion), tagName);
        DocumentRevisionDTO documentRevisionDTO = mapper.map(documentRevision, DocumentRevisionDTO.class);
        documentRevisionDTO.setPath(documentRevision.getLocation().getCompletePath());
        documentRevisionDTO.setLifeCycleState(documentRevision.getLifeCycleState());

        return documentRevisionDTO;
    }

    @ApiOperation(value = "Delete the document",
            response = Response.class)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successful deletion of DocumentRevisionDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @DELETE
    public Response deleteDocument(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Document master id") @PathParam("documentId") String documentId,
            @ApiParam(required = true, value = "Document version") @PathParam("documentVersion") String documentVersion)
            throws EntityNotFoundException, NotAllowedException, AccessRightException, UserNotActiveException,
            ESServerException, EntityConstraintException {

        documentService.deleteDocumentRevision(new DocumentRevisionKey(workspaceId, documentId, documentVersion));
        return Response.noContent().build();
    }

    @PUT
    @ApiOperation(value = "Rename attached files of document",
            response = FileDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of updated FileDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("/iterations/{docIteration}/files/{fileName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public FileDTO renameAttachedFileInDocument(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Document master id") @PathParam("documentId") String documentId,
            @ApiParam(required = true, value = "Document version") @PathParam("documentVersion") String documentVersion,
            @ApiParam(required = true, value = "Document iteration") @PathParam("docIteration") int docIteration,
            @ApiParam(required = true, value = "File name") @PathParam("fileName") String fileName,
            @ApiParam(required = true, value = "File to rename") FileDTO fileDTO)
            throws EntityNotFoundException, NotAllowedException, AccessRightException, UserNotActiveException,
            FileAlreadyExistsException, CreationException, StorageException {

        String fileFullName = workspaceId + "/documents/" + documentId + "/" + documentVersion + "/" + docIteration + "/" + fileName;
        BinaryResource binaryResource = documentService.renameFileInDocument(fileFullName, fileDTO.getShortName());
        return new FileDTO(true, binaryResource.getFullName(), binaryResource.getName());
    }

    @DELETE
    @ApiOperation(value = "Remove attached file from document",
            response = Response.class)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successful deletion of file"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("/iterations/{docIteration}/files/{fileName}")
    public Response removeAttachedFileFromDocument(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Document master id") @PathParam("documentId") String documentId,
            @ApiParam(required = true, value = "Document version") @PathParam("documentVersion") String documentVersion,
            @ApiParam(required = true, value = "Document iteration") @PathParam("docIteration") int docIteration,
            @ApiParam(required = true, value = "File name") @PathParam("fileName") String fileName)
            throws EntityNotFoundException, NotAllowedException, AccessRightException, UserNotActiveException {
        String fileFullName = workspaceId + "/documents/" + documentId + "/" + documentVersion + "/" + docIteration + "/" + fileName;
        documentService.removeFileFromDocument(fileFullName);
        return Response.noContent().build();
    }

    @POST
    @ApiOperation(value = "Create a shared document",
            response = SharedDocumentDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of created SharedDocumentDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("share")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public SharedDocumentDTO createSharedDocument(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Document master id") @PathParam("documentId") String documentId,
            @ApiParam(required = true, value = "Document version") @PathParam("documentVersion") String documentVersion,
            @ApiParam(required = true, value = "Shared document to create") SharedDocumentDTO pSharedDocumentDTO)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException {

        String password = pSharedDocumentDTO.getPassword();
        Date expireDate = pSharedDocumentDTO.getExpireDate();

        SharedDocument sharedDocument = documentService.createSharedDocument(
                new DocumentRevisionKey(workspaceId, documentId, documentVersion),
                password, expireDate);

        return mapper.map(sharedDocument, SharedDocumentDTO.class);
    }


    @PUT
    @ApiOperation(value = "Publish a document",
            response = Response.class)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successful publication"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("publish")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response publishDocumentRevision(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Document master id") @PathParam("documentId") String documentId,
            @ApiParam(required = true, value = "Document version") @PathParam("documentVersion") String documentVersion)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException {
        documentService.setDocumentPublicShared(new DocumentRevisionKey(workspaceId, documentId, documentVersion), true);
        return Response.noContent().build();
    }

    @PUT
    @ApiOperation(value = "Unpublish a document",
            response = Response.class)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successful un-publication"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("unpublish")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response unPublishDocumentRevision(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Document master id") @PathParam("documentId") String documentId,
            @ApiParam(required = true, value = "Document version") @PathParam("documentVersion") String documentVersion)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException {
        documentService.setDocumentPublicShared(new DocumentRevisionKey(workspaceId, documentId, documentVersion), false);
        return Response.noContent().build();
    }

    @PUT
    @ApiOperation(value = "Update document's ACL",
            response = Response.class)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successful ACL removal"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("acl")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateDocumentRevisionACL(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Document master id") @PathParam("documentId") String documentId,
            @ApiParam(required = true, value = "Document version") @PathParam("documentVersion") String documentVersion,
            @ApiParam(required = true, value = "ACL rules to set") ACLDTO acl)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException, NotAllowedException {
        DocumentRevisionKey documentRevisionKey = new DocumentRevisionKey(workspaceId, documentId, documentVersion);

        if (acl.hasEntries()) {
            documentService.updateDocumentACL(workspaceId, documentRevisionKey, acl.getUserEntriesMap(), acl.getUserGroupEntriesMap());
        } else {
            documentService.removeACLFromDocumentRevision(documentRevisionKey);
        }
        return Response.noContent().build();
    }

    @GET
    @ApiOperation(value = "Get document's aborted workflows",
            response = WorkflowDTO.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of aborted WorkflowDTOs. It can be an empty list."),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("aborted-workflows")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAbortedWorkflowsInDocument(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Document master id") @PathParam("documentId") String documentId,
            @ApiParam(required = true, value = "Document version") @PathParam("documentVersion") String documentVersion)
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
    @ApiOperation(value = "Get inverse documents links",
            response = DocumentRevisionDTO.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of DocumentRevisionDTO pointing to this document. It can be an empty list."),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{iteration}/inverse-document-link")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInverseDocumentLinks(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Document master id") @PathParam("documentId") String documentId,
            @ApiParam(required = true, value = "Document version") @PathParam("documentVersion") String documentVersion,
            @ApiParam(required = true, value = "Document iteration") @PathParam("iteration") int iteration)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException,
            DocumentRevisionNotFoundException, DocumentIterationNotFoundException, WorkspaceNotEnabledException {

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
    @ApiOperation(value = "Get inverse parts links",
            response = PartRevisionDTO.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of PartRevisionDTO pointing to this document. It can be an empty list."),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{iteration}/inverse-part-link")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInversePartsLinks(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Document master id") @PathParam("documentId") String documentId,
            @ApiParam(required = true, value = "Document version") @PathParam("documentVersion") String documentVersion,
            @ApiParam(required = true, value = "Document iteration") @PathParam("iteration") int iteration)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException,
            PartRevisionNotFoundException, PartIterationNotFoundException,
            DocumentRevisionNotFoundException, WorkspaceNotEnabledException {

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
    @ApiOperation(value = "Get inverse product instances links",
            response = ProductInstanceMasterDTO.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of ProductInstanceMasterDTO pointing to this document. It can be an empty list."),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{iteration}/inverse-product-instances-link")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInverseProductInstancesLinks(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Document master id") @PathParam("documentId") String documentId,
            @ApiParam(required = true, value = "Document version") @PathParam("documentVersion") String documentVersion,
            @ApiParam(required = true, value = "Document iteration") @PathParam("iteration") int iteration)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException,
            PartRevisionNotFoundException, PartIterationNotFoundException,
            DocumentRevisionNotFoundException, WorkspaceNotEnabledException {

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
    @ApiOperation(value = "Get inverse path data links",
            response = PathDataMasterDTO.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of PathDataMasterDTO pointing to this document. It can be an empty list."),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{iteration}/inverse-path-data-link")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInversePathDataLinks(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Document master id") @PathParam("documentId") String documentId,
            @ApiParam(required = true, value = "Document version") @PathParam("documentVersion") String documentVersion,
            @ApiParam(required = true, value = "Document iteration") @PathParam("iteration") int iteration)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException,
            PartRevisionNotFoundException, PartIterationNotFoundException, DocumentRevisionNotFoundException,
            ConfigurationItemNotFoundException, PartUsageLinkNotFoundException, WorkspaceNotEnabledException {

        DocumentRevisionKey docKey = new DocumentRevisionKey(workspaceId, documentId, documentVersion);
        Set<PathDataMaster> pathDataMasters = productService.getInversePathDataLink(docKey);

        Set<PathDataMasterDTO> dtos = new HashSet<>();
        for (PathDataMaster pathDataMaster : pathDataMasters) {
            PathDataMasterDTO dto = mapper.map(pathDataMaster, PathDataMasterDTO.class);
            ProductInstanceMaster productInstanceMaster = productService.findProductByPathMaster(workspaceId, pathDataMaster);

            LightPartLinkListDTO partLinksList = new LightPartLinkListDTO();
            List<PartLink> path = productService.decodePath(productInstanceMaster.getInstanceOf().getKey(), pathDataMaster.getPath());
            for (PartLink partLink : path) {
                partLinksList.getPartLinks().add(new LightPartLinkDTO(partLink.getComponent().getNumber(), partLink.getComponent().getName(), partLink.getReferenceDescription(), partLink.getFullId()));
            }
            dto.setPartLinksList(partLinksList);

            dto.setSerialNumber(productInstanceMaster.getSerialNumber());
            dtos.add(dto);
        }

        return Response.ok(new GenericEntity<List<PathDataMasterDTO>>((List<PathDataMasterDTO>) new ArrayList<>(dtos)) {
        }).build();

    }

    private void setDocumentRevisionDTOWorkflow(DocumentRevision documentRevision, DocumentRevisionDTO documentRevisionDTO)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        Workflow currentWorkflow = documentWorkflowService.getCurrentWorkflow(documentRevision.getKey());
        if (currentWorkflow != null) {
            documentRevisionDTO.setWorkflow(mapper.map(currentWorkflow, WorkflowDTO.class));
            documentRevisionDTO.setLifeCycleState(currentWorkflow.getLifeCycleState());
        }
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