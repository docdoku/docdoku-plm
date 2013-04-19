/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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

import com.docdoku.core.common.User;
import com.docdoku.core.common.UserGroup;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.document.DocumentIterationKey;
import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.document.DocumentMasterKey;
import com.docdoku.core.document.Tag;
import com.docdoku.core.meta.*;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.ACLUserEntry;
import com.docdoku.core.security.ACLUserGroupEntry;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.*;
import com.docdoku.core.sharing.SharedDocument;
import com.docdoku.server.rest.dto.*;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

import javax.annotation.PostConstruct;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.*;

@Stateless
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class DocumentResource {

    @EJB
    private IDocumentManagerLocal documentService;

    private Mapper mapper;

    public DocumentResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    public DocumentMasterDTO getDocumentMaster(@PathParam("workspaceId") String workspaceId, @PathParam("docKey") String docKey) {

        int lastDash = docKey.lastIndexOf('-');
        String id = docKey.substring(0, lastDash);
        String version = docKey.substring(lastDash + 1, docKey.length());

        try {
            DocumentMaster docM = documentService.getDocumentMaster(new DocumentMasterKey(workspaceId, id, version));
            DocumentMasterDTO docMsDTO = mapper.map(docM, DocumentMasterDTO.class);
            docMsDTO.setPath(docM.getLocation().getCompletePath());
            docMsDTO.setLifeCycleState(docM.getLifeCycleState());
            docMsDTO.setIterationSubscription(documentService.isUserIterationChangeEventSubscribedForGivenDocument(workspaceId,docM));
            docMsDTO.setStateSubscription(documentService.isUserStateChangeEventSubscribedForGivenDocument(workspaceId,docM));
            return docMsDTO;

        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @PUT
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    @Path("/checkin")
    public DocumentMasterDTO checkInDocument(@PathParam("workspaceId") String workspaceId, @PathParam("docKey") String docKey) {
        try {

            int lastDash = docKey.lastIndexOf('-');
            String docId = docKey.substring(0, lastDash);
            String docVersion = docKey.substring(lastDash + 1, docKey.length());

            DocumentMaster docM = documentService.checkInDocument(new DocumentMasterKey(workspaceId, docId, docVersion));

            DocumentMasterDTO docMsDTO = mapper.map(docM, DocumentMasterDTO.class);
            docMsDTO.setPath(docM.getLocation().getCompletePath());

            return docMsDTO;

        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @PUT
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    @Path("/checkout")
    public DocumentMasterDTO checkOutDocument(@PathParam("workspaceId") String workspaceId, @PathParam("docKey") String docKey) {
        try {

            int lastDash = docKey.lastIndexOf('-');
            String docId = docKey.substring(0, lastDash);
            String docVersion = docKey.substring(lastDash + 1, docKey.length());

            DocumentMaster docM = documentService.checkOutDocument(new DocumentMasterKey(workspaceId, docId, docVersion));

            DocumentMasterDTO docMsDTO = mapper.map(docM, DocumentMasterDTO.class);
            docMsDTO.setPath(docM.getLocation().getCompletePath());
            docMsDTO.setLifeCycleState(docM.getLifeCycleState());

            return docMsDTO;

        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @PUT
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    @Path("/undocheckout")
    public DocumentMasterDTO undoCheckOutDocument(@PathParam("workspaceId") String workspaceId, @PathParam("docKey") String docKey) {
        try {

            int lastDash = docKey.lastIndexOf('-');
            String docId = docKey.substring(0, lastDash);
            String docVersion = docKey.substring(lastDash + 1, docKey.length());

            DocumentMaster docM = documentService.undoCheckOutDocument(new DocumentMasterKey(workspaceId, docId, docVersion));

            DocumentMasterDTO docMsDTO = mapper.map(docM, DocumentMasterDTO.class);
            docMsDTO.setPath(docM.getLocation().getCompletePath());
            docMsDTO.setLifeCycleState(docM.getLifeCycleState());

            return docMsDTO;

        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @PUT
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    @Path("/move")
    public DocumentMasterDTO moveDocument(@PathParam("workspaceId") String workspaceId, @PathParam("docKey") String docKey, DocumentCreationDTO docCreationDTO) {
        try {

            int lastDash = docKey.lastIndexOf('-');
            String docId = docKey.substring(0, lastDash);
            String docVersion = docKey.substring(lastDash + 1, docKey.length());
            String parentFolderPath = docCreationDTO.getPath();
            String newCompletePath = Tools.stripTrailingSlash(parentFolderPath);

            DocumentMasterKey docMsKey = new DocumentMasterKey(workspaceId, docId, docVersion);
            DocumentMaster movedDocumentMaster = documentService.moveDocumentMaster(newCompletePath, docMsKey);

            DocumentMasterDTO docMsDTO = mapper.map(movedDocumentMaster, DocumentMasterDTO.class);
            docMsDTO.setPath(movedDocumentMaster.getLocation().getCompletePath());
            docMsDTO.setLifeCycleState(movedDocumentMaster.getLifeCycleState());

            return docMsDTO;

        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @PUT
    @Path("/notification/iterationChange/subscribe")
    public Response subscribeToIterationChangeEvent(@PathParam("workspaceId") String workspaceId, @PathParam("docKey") String docKey) {
        try {
            int lastDash = docKey.lastIndexOf('-');
            String docId = docKey.substring(0, lastDash);
            String docVersion = docKey.substring(lastDash + 1, docKey.length());

            documentService.subscribeToIterationChangeEvent(new DocumentMasterKey(workspaceId, docId, docVersion));

            return Response.ok().build();
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @PUT
    @Path("/notification/iterationChange/unsubscribe")
    public Response unsubscribeToIterationChangeEvent(@PathParam("workspaceId") String workspaceId, @PathParam("docKey") String docKey) {
        try {
            int lastDash = docKey.lastIndexOf('-');
            String docId = docKey.substring(0, lastDash);
            String docVersion = docKey.substring(lastDash + 1, docKey.length());

            documentService.unsubscribeToIterationChangeEvent(new DocumentMasterKey(workspaceId, docId, docVersion));

            return Response.ok().build();
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @PUT
    @Path("/notification/stateChange/subscribe")
    public Response subscribeToStateChangeEvent(@PathParam("workspaceId") String workspaceId, @PathParam("docKey") String docKey) {
        try {
            int lastDash = docKey.lastIndexOf('-');
            String docId = docKey.substring(0, lastDash);
            String docVersion = docKey.substring(lastDash + 1, docKey.length());

            documentService.subscribeToStateChangeEvent(new DocumentMasterKey(workspaceId, docId, docVersion));

            return Response.ok().build();
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @PUT
    @Path("/notification/stateChange/unsubscribe")
    public Response unsubscribeToStateChangeEvent(@PathParam("workspaceId") String workspaceId, @PathParam("docKey") String docKey) {
        try {
            int lastDash = docKey.lastIndexOf('-');
            String docId = docKey.substring(0, lastDash);
            String docVersion = docKey.substring(lastDash + 1, docKey.length());

            documentService.unsubscribeToStateChangeEvent(new DocumentMasterKey(workspaceId, docId, docVersion));

            return Response.ok().build();
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @PUT
    @Consumes("application/json;charset=UTF-8")
    @Path("/iterations/{docIteration}")
    public DocumentIterationDTO updateDocMs(@PathParam("workspaceId") String workspaceId, @PathParam("docKey") String docKey, @PathParam("docIteration") String docIteration, DocumentIterationDTO data) {
        try {

            int lastDash = docKey.lastIndexOf('-');
            String pID = docKey.substring(0, lastDash);
            String pVersion = docKey.substring(lastDash + 1, docKey.length());
            String pRevisionNote = data.getRevisionNote();
            int pIteration = Integer.parseInt(docIteration);

            List<DocumentIterationDTO> linkedDocs = data.getLinkedDocuments();
            DocumentIterationKey[] links = null;
            if (linkedDocs != null) {
                links = createDocumentIterationKey(linkedDocs);
            }

            List<InstanceAttributeDTO> instanceAttributes = data.getInstanceAttributes();
            InstanceAttribute[] attributes = null;
            if (instanceAttributes != null) {
                attributes = createInstanceAttribute(instanceAttributes);
            }

            DocumentMaster docM = documentService.updateDocument(new DocumentIterationKey(workspaceId, pID, pVersion, pIteration), pRevisionNote, attributes, links);
            DocumentIterationDTO docDTO = mapper.map(docM.getLastIteration(), DocumentIterationDTO.class);
            return docDTO;

        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }

    }

    @PUT
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    @Path("/newVersion")
    public DocumentMasterDTO[] createNewVersion(@PathParam("workspaceId") String workspaceId, @PathParam("docKey") String docKey, DocumentCreationDTO docCreationDTO) {

        int lastDash = docKey.lastIndexOf('-');
        String pID = docKey.substring(0, lastDash);
        String pVersion = docKey.substring(lastDash + 1, docKey.length());
        String pWorkspaceId = workspaceId;
        String pTitle = docCreationDTO.getTitle();
        String pDescription = docCreationDTO.getDescription();
        String pWorkflowModelId = docCreationDTO.getWorkflowModelId();
        RoleMappingDTO[] rolesMappingDTO = docCreationDTO.getRoleMapping();

        /*
         * Null value for test purpose only
         */
        ACLDTO acl = null;

        try {

            ACLUserEntry[] userEntries = null;
            ACLUserGroupEntry[] userGroupEntries = null;
            if (acl != null) {
                userEntries = new ACLUserEntry[acl.getUserEntries().size()];
                userGroupEntries = new ACLUserGroupEntry[acl.getGroupEntries().size()];
                int i = 0;
                for (Map.Entry<String, ACLDTO.Permission> entry : acl.getUserEntries().entrySet()) {
                    userEntries[i] = new ACLUserEntry();
                    userEntries[i].setPrincipal(new User(new Workspace(pWorkspaceId), entry.getKey()));
                    userEntries[i++].setPermission(ACL.Permission.valueOf(entry.getValue().name()));
                }
                i = 0;
                for (Map.Entry<String, ACLDTO.Permission> entry : acl.getGroupEntries().entrySet()) {
                    userGroupEntries[i] = new ACLUserGroupEntry();
                    userGroupEntries[i].setPrincipal(new UserGroup(new Workspace(pWorkspaceId), entry.getKey()));
                    userGroupEntries[i++].setPermission(ACL.Permission.valueOf(entry.getValue().name()));
                }
            }

            Map<String, String> roleMappings = new HashMap<>();

            if (rolesMappingDTO != null) {
                for(RoleMappingDTO roleMappingDTO : rolesMappingDTO) {
                    roleMappings.put(roleMappingDTO.getRoleName(), roleMappingDTO.getUserLogin());
                }
            }

            DocumentMaster[] docM = documentService.createVersion(new DocumentMasterKey(pWorkspaceId, pID, pVersion), pTitle, pDescription, pWorkflowModelId, userEntries, userGroupEntries,roleMappings);
            DocumentMasterDTO[] dtos = new DocumentMasterDTO[docM.length];

            for (int i = 0; i < docM.length; i++) {
                dtos[i] = mapper.map(docM[i], DocumentMasterDTO.class);
                dtos[i].setPath(docM[i].getLocation().getCompletePath());
                dtos[i].setLifeCycleState(docM[i].getLifeCycleState());
                dtos[i] = Tools.createLightDocumentMasterDTO(dtos[i]);
                dtos[i].setIterationSubscription(documentService.isUserIterationChangeEventSubscribedForGivenDocument(workspaceId,docM[i]));
                dtos[i].setStateSubscription(documentService.isUserStateChangeEventSubscribedForGivenDocument(workspaceId,docM[i]));
            }

            return dtos;

        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @PUT
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    @Path("/tags")
    public DocumentMasterDTO saveDocTags(@PathParam("workspaceId") String workspaceId, @PathParam("docKey") String docKey, TagDTO[] tagDtos) {

        int lastDash = docKey.lastIndexOf('-');
        String id = docKey.substring(0, lastDash);
        String version = docKey.substring(lastDash + 1, docKey.length());

        String[] tagsLabel = new String[tagDtos.length];
        for (int i = 0; i < tagDtos.length; i++) {
            tagsLabel[i] = tagDtos[i].getLabel();
        }

        try {
            DocumentMaster docMs = documentService.saveTags(new DocumentMasterKey(workspaceId, id, version), tagsLabel);
            DocumentMasterDTO docMsDto = mapper.map(docMs, DocumentMasterDTO.class);
            docMsDto.setPath(docMs.getLocation().getCompletePath());
            docMsDto.setLifeCycleState(docMs.getLifeCycleState());

            return docMsDto;
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @POST
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    @Path("/tags")
    public Response addDocTag(@PathParam("workspaceId") String workspaceId, @PathParam("docKey") String docKey, TagDTO[] tagDtos) {

        int lastDash = docKey.lastIndexOf('-');
        String id = docKey.substring(0, lastDash);
        String version = docKey.substring(lastDash + 1, docKey.length());

        try {
            DocumentMasterKey docMPK=new DocumentMasterKey(workspaceId, id, version);
            DocumentMaster docM = documentService.getDocumentMaster(docMPK);
            Set<Tag> tags = docM.getTags();
            Set<String> tagLabels = new HashSet<String>();

            for(TagDTO tagDto:tagDtos)
                tagLabels.add(tagDto.getLabel());

            for(Tag tag : tags){
                tagLabels.add(tag.getLabel());
            }

            documentService.saveTags(docMPK,tagLabels.toArray(new String[tagLabels.size()]));
            return Response.ok().build();

        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }


    }

    @DELETE
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    @Path("/tags")
    public Response removeDocTags(@PathParam("workspaceId") String workspaceId, @PathParam("docKey") String docKey, String[] tagLabels) {

        int lastDash = docKey.lastIndexOf('-');
        String id = docKey.substring(0, lastDash);
        String version = docKey.substring(lastDash + 1, docKey.length());

        try {

            DocumentMaster docM = documentService.getDocumentMaster(new DocumentMasterKey(workspaceId, id, version));

            for(String tagLabel : tagLabels){
                Tag tagToRemove = new Tag(new Workspace(workspaceId), tagLabel);
                docM.getTags().remove(tagToRemove);
            }

            return Response.ok().build();

        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }

    }

    @DELETE
    @Produces("application/json;charset=UTF-8")
    public Response deleteDocument(@PathParam("workspaceId") String workspaceId, @PathParam("docKey") String docKey) {

        int lastDash = docKey.lastIndexOf('-');
        String id = docKey.substring(0, lastDash);
        String version = docKey.substring(lastDash + 1, docKey.length());

        try {
            documentService.deleteDocumentMaster(new DocumentMasterKey(workspaceId, id, version));
            return Response.status(Response.Status.OK).build();
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @DELETE
    @Consumes("application/json;charset=UTF-8")
    @Path("/iterations/{docIteration}/files/{fileName}")
    public Response removeAttachedFile(@PathParam("workspaceId") String workspaceId, @PathParam("docKey") String docKey, @PathParam("docIteration") int docIteration, @PathParam("fileName") String fileName) {
        try {
            int lastDash = docKey.lastIndexOf('-');
            String id = docKey.substring(0, lastDash);
            String version = docKey.substring(lastDash + 1, docKey.length());

            String fileFullName = workspaceId + "/documents/" + id + "/" + version + "/" + docIteration + "/" + fileName;

            documentService.removeFileFromDocument(fileFullName);
            return Response.ok().build();

        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @POST
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    @Path("share")
    public Response createSharedDocument(@PathParam("workspaceId") String workspaceId, SharedDocumentDTO pSharedDocumentDTO) {

        String id = pSharedDocumentDTO.getDocumentMasterId();
        String version = pSharedDocumentDTO.getDocumentMasterVersion();
        String password = pSharedDocumentDTO.getPassword();
        Date expireDate = pSharedDocumentDTO.getExpireDate();

        try {
            SharedDocument sharedDocument = documentService.createSharedDocument(new DocumentMasterKey(workspaceId, id, version),password,expireDate);
            SharedDocumentDTO sharedDocumentDTO = mapper.map(sharedDocument,SharedDocumentDTO.class);
            return Response.ok().entity(sharedDocumentDTO).build();
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }

    }


    @PUT
    @Consumes("application/json;charset=UTF-8")
    @Path("publish")
    public Response publishPartRevision(@PathParam("workspaceId") String workspaceId, @PathParam("docKey") String docKey) {
        try {
            int lastDash = docKey.lastIndexOf('-');
            String id = docKey.substring(0, lastDash);
            String version = docKey.substring(lastDash + 1, docKey.length());
            DocumentMaster documentMaster = documentService.getDocumentMaster(new DocumentMasterKey(workspaceId, id, version));
            documentMaster.setPublicShared(true);
            return Response.ok().build();
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }

    }

    @PUT
    @Consumes("application/json;charset=UTF-8")
    @Path("unpublish")
    public Response unPublishPartRevision(@PathParam("workspaceId") String workspaceId, @PathParam("docKey") String docKey) {
        try {
            int lastDash = docKey.lastIndexOf('-');
            String id = docKey.substring(0, lastDash);
            String version = docKey.substring(lastDash + 1, docKey.length());
            DocumentMaster documentMaster = documentService.getDocumentMaster(new DocumentMasterKey(workspaceId, id, version));
            documentMaster.setPublicShared(false);
            return Response.ok().build();
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }

    }

    private InstanceAttribute[] createInstanceAttribute(InstanceAttributeDTO[] dtos) {
        if (dtos == null) {
            return null;
        }
        InstanceAttribute[] data = new InstanceAttribute[dtos.length];

        for (int i = 0; i < dtos.length; i++) {
            data[i] = createObject(dtos[i]);
        }

        return data;
    }

    private InstanceAttribute[] createInstanceAttribute(List<InstanceAttributeDTO> dtos) {
        if (dtos == null) {
            return null;
        }
        InstanceAttribute[] data = new InstanceAttribute[dtos.size()];
        int i = 0;
        for (InstanceAttributeDTO dto : dtos) {
            data[i++] = createObject(dto);
        }

        return data;
    }

    private InstanceAttribute createObject(InstanceAttributeDTO dto) {
        if (dto.getType().equals(InstanceAttributeDTO.Type.BOOLEAN)) {
            InstanceBooleanAttribute attr = new InstanceBooleanAttribute();
            attr.setName(dto.getName());
            attr.setBooleanValue(Boolean.parseBoolean(dto.getValue()));
            return attr;
        } else if (dto.getType().equals(InstanceAttributeDTO.Type.TEXT)) {
            InstanceTextAttribute attr = new InstanceTextAttribute();
            attr.setName(dto.getName());
            attr.setTextValue((String) dto.getValue());
            return attr;
        } else if (dto.getType().equals(InstanceAttributeDTO.Type.NUMBER)) {
            InstanceNumberAttribute attr = new InstanceNumberAttribute();
            attr.setName(dto.getName());
            try{
                attr.setNumberValue(Float.parseFloat(dto.getValue()));
            }catch(NumberFormatException ex){
                attr.setNumberValue(0);
            }
            return attr;
        } else if (dto.getType().equals(InstanceAttributeDTO.Type.DATE)) {
            InstanceDateAttribute attr = new InstanceDateAttribute();
            attr.setName(dto.getName());
            try{
                attr.setDateValue(new Date(Long.parseLong(dto.getValue())));
            }catch(NumberFormatException ex){
                attr.setDateValue(null);
            }
            return attr;
        } else if (dto.getType().equals(InstanceAttributeDTO.Type.URL)) {
            InstanceURLAttribute attr = new InstanceURLAttribute();
            attr.setName(dto.getName());
            attr.setUrlValue(dto.getValue());
            return attr;
        } else {
            throw new IllegalArgumentException("Instance attribute not supported");
        }
    }

    private DocumentIterationKey[] createDocumentIterationKey(DocumentIterationDTO[] dtos) {
        DocumentIterationKey[] data = new DocumentIterationKey[dtos.length];

        for (int i = 0; i < dtos.length; i++) {
            data[i] = createObject(dtos[i]);
        }

        return data;
    }

    private DocumentIterationKey[] createDocumentIterationKey(List<DocumentIterationDTO> dtos) {
        DocumentIterationKey[] data = new DocumentIterationKey[dtos.size()];
        int i = 0;
        for (DocumentIterationDTO dto : dtos) {
            data[i++] = createObject(dto);
        }

        return data;
    }

    private DocumentIterationKey createObject(DocumentIterationDTO dto) {
        return new DocumentIterationKey(dto.getWorkspaceId(), dto.getDocumentMasterId(), dto.getDocumentMasterVersion(), dto.getIteration());
    }
}
