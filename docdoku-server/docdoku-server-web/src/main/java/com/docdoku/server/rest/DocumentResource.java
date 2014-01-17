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
import com.docdoku.core.document.*;
import com.docdoku.core.exceptions.ApplicationException;
import com.docdoku.core.meta.*;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.ACLUserEntry;
import com.docdoku.core.security.ACLUserGroupEntry;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.core.sharing.SharedDocument;
import com.docdoku.core.workflow.Workflow;
import com.docdoku.server.rest.dto.*;
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
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO getDocumentRevision(@PathParam("workspaceId") String workspaceId, @PathParam("documentId") String documentId, @PathParam("documentVersion") String documentVersion) {
        try {
            DocumentRevision docR = documentService.getDocumentRevision(new DocumentRevisionKey(workspaceId, documentId, documentVersion));
            DocumentRevisionDTO docRsDTO = mapper.map(docR, DocumentRevisionDTO.class);
            docRsDTO.setPath(docR.getLocation().getCompletePath());
            docRsDTO.setLifeCycleState(docR.getLifeCycleState());
            docRsDTO.setIterationSubscription(documentService.isUserIterationChangeEventSubscribedForGivenDocument(workspaceId, docR));
            docRsDTO.setStateSubscription(documentService.isUserStateChangeEventSubscribedForGivenDocument(workspaceId,docR));
            ACL acl = docR.getACL();
            if(acl != null){
                docRsDTO.setAcl(Tools.mapACLtoACLDTO(acl));
            }
            return docRsDTO;

        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @PUT
    @Path("/checkin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO checkInDocument(@PathParam("workspaceId") String workspaceId, @PathParam("documentId") String documentId, @PathParam("documentVersion") String documentVersion) {
        try {
            DocumentRevision docR = documentService.checkInDocument(new DocumentRevisionKey(workspaceId, documentId, documentVersion));
            DocumentRevisionDTO docRsDTO = mapper.map(docR, DocumentRevisionDTO.class);
            docRsDTO.setPath(docR.getLocation().getCompletePath());
            return docRsDTO;
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @PUT
    @Path("/checkout")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO checkOutDocument(@PathParam("workspaceId") String workspaceId, @PathParam("documentId") String documentId, @PathParam("documentVersion") String documentVersion) {
        try {
            DocumentRevision docR = documentService.checkOutDocument(new DocumentRevisionKey(workspaceId, documentId, documentVersion));
            DocumentRevisionDTO docRsDTO = mapper.map(docR, DocumentRevisionDTO.class);
            docRsDTO.setPath(docR.getLocation().getCompletePath());
            docRsDTO.setLifeCycleState(docR.getLifeCycleState());
            return docRsDTO;
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @PUT
    @Path("/undocheckout")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO undoCheckOutDocument(@PathParam("workspaceId") String workspaceId, @PathParam("documentId") String documentId, @PathParam("documentVersion") String documentVersion) {
        try {
            DocumentRevision docR = documentService.undoCheckOutDocument(new DocumentRevisionKey(workspaceId, documentId, documentVersion));
            DocumentRevisionDTO docRsDTO = mapper.map(docR, DocumentRevisionDTO.class);
            docRsDTO.setPath(docR.getLocation().getCompletePath());
            docRsDTO.setLifeCycleState(docR.getLifeCycleState());
            return docRsDTO;
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @PUT
    @Path("/move")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO moveDocument(@PathParam("workspaceId") String workspaceId, @PathParam("documentId") String documentId, @PathParam("documentVersion") String documentVersion, DocumentCreationDTO docCreationDTO) {
        try {
            String parentFolderPath = docCreationDTO.getPath();
            String newCompletePath = Tools.stripTrailingSlash(parentFolderPath);
            DocumentRevisionKey docRsKey = new DocumentRevisionKey(workspaceId, documentId, documentVersion);
            DocumentRevision movedDocumentRevision = documentService.moveDocumentRevision(newCompletePath, docRsKey);
            DocumentRevisionDTO docMsDTO = mapper.map(movedDocumentRevision, DocumentRevisionDTO.class);
            docMsDTO.setPath(movedDocumentRevision.getLocation().getCompletePath());
            docMsDTO.setLifeCycleState(movedDocumentRevision.getLifeCycleState());
            return docMsDTO;
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @PUT
    @Path("/notification/iterationChange/subscribe")
    public Response subscribeToIterationChangeEvent(@PathParam("workspaceId") String workspaceId,@PathParam("documentId") String documentId, @PathParam("documentVersion") String documentVersion) {
        try {
            documentService.subscribeToIterationChangeEvent(new DocumentRevisionKey(workspaceId, documentId, documentVersion));
            return Response.ok().build();
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @PUT
    @Path("/notification/iterationChange/unsubscribe")
    public Response unSubscribeToIterationChangeEvent(@PathParam("workspaceId") String workspaceId, @PathParam("documentId") String documentId, @PathParam("documentVersion") String documentVersion) {
        try {
            documentService.unsubscribeToIterationChangeEvent(new DocumentRevisionKey(workspaceId, documentId, documentVersion));
            return Response.ok().build();
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @PUT
    @Path("/notification/stateChange/subscribe")
    public Response subscribeToStateChangeEvent(@PathParam("workspaceId") String workspaceId, @PathParam("documentId") String documentId, @PathParam("documentVersion") String documentVersion) {
        try {
            documentService.subscribeToStateChangeEvent(new DocumentRevisionKey(workspaceId, documentId, documentVersion));
            return Response.ok().build();
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @PUT
    @Path("/notification/stateChange/unsubscribe")
    public Response unsubscribeToStateChangeEvent(@PathParam("workspaceId") String workspaceId, @PathParam("documentId") String documentId, @PathParam("documentVersion") String documentVersion) {
        try {
            documentService.unsubscribeToStateChangeEvent(new DocumentRevisionKey(workspaceId, documentId, documentVersion));
            return Response.ok().build();
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @PUT
    @Path("/iterations/{docIteration}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentIterationDTO updateDocMs(@PathParam("workspaceId") String workspaceId, @PathParam("documentId") String documentId, @PathParam("documentVersion") String documentVersion, @PathParam("docIteration") String docIteration, DocumentIterationDTO data) {
        try {
            String pRevisionNote = data.getRevisionNote();
            int pIteration = Integer.parseInt(docIteration);

            List<DocumentIterationDTO> linkedDocs = data.getLinkedDocuments();
            DocumentIterationKey[] links = null;
            if (linkedDocs != null) {
                links = createDocumentIterationKeys(linkedDocs);
            }

            List<InstanceAttributeDTO> instanceAttributes = data.getInstanceAttributes();
            InstanceAttribute[] attributes = null;
            if (instanceAttributes != null) {
                attributes = createInstanceAttributes(instanceAttributes);
            }

            DocumentRevision docR = documentService.updateDocument(new DocumentIterationKey(workspaceId, documentId, documentVersion, pIteration), pRevisionNote, attributes, links);
            DocumentIterationDTO docDTO = mapper.map(docR.getLastIteration(), DocumentIterationDTO.class);
            return docDTO;

        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }

    }

    @PUT
    @Path("/newVersion")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO[] createNewVersion(@PathParam("workspaceId") String workspaceId, @PathParam("documentId") String documentId, @PathParam("documentVersion") String documentVersion, DocumentCreationDTO docCreationDTO) {

        String pWorkspaceId = workspaceId;
        String pTitle = docCreationDTO.getTitle();
        String pDescription = docCreationDTO.getDescription();
        String pWorkflowModelId = docCreationDTO.getWorkflowModelId();
        RoleMappingDTO[] rolesMappingDTO = docCreationDTO.getRoleMapping();
        ACLDTO acl = docCreationDTO.getAcl();

        try {

            ACLUserEntry[] userEntries = null;
            ACLUserGroupEntry[] userGroupEntries = null;
            if (acl != null) {
                userEntries = new ACLUserEntry[acl.getUserEntries().size()];
                userGroupEntries = new ACLUserGroupEntry[acl.getGroupEntries().size()];
                int i = 0;
                for (Map.Entry<String, ACL.Permission> entry : acl.getUserEntries().entrySet()) {
                    userEntries[i] = new ACLUserEntry();
                    userEntries[i].setPrincipal(new User(new Workspace(pWorkspaceId), entry.getKey()));
                    userEntries[i++].setPermission(ACL.Permission.valueOf(entry.getValue().name()));
                }
                i = 0;
                for (Map.Entry<String, ACL.Permission> entry : acl.getGroupEntries().entrySet()) {
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

            DocumentRevision[] docR = documentService.createDocumentRevision(new DocumentRevisionKey(pWorkspaceId, documentId, documentVersion), pTitle, pDescription, pWorkflowModelId, userEntries, userGroupEntries, roleMappings);
            DocumentRevisionDTO[] dtos = new DocumentRevisionDTO[docR.length];

            for (int i = 0; i < docR.length; i++) {
                dtos[i] = mapper.map(docR[i], DocumentRevisionDTO.class);
                dtos[i].setPath(docR[i].getLocation().getCompletePath());
                dtos[i].setLifeCycleState(docR[i].getLifeCycleState());
                dtos[i] = Tools.createLightDocumentRevisionDTO(dtos[i]);
                dtos[i].setIterationSubscription(documentService.isUserIterationChangeEventSubscribedForGivenDocument(workspaceId,docR[i]));
                dtos[i].setStateSubscription(documentService.isUserStateChangeEventSubscribedForGivenDocument(workspaceId,docR[i]));
            }

            return dtos;

        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @PUT
    @Path("/tags")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO saveDocTags(@PathParam("workspaceId") String workspaceId, @PathParam("documentId") String documentId, @PathParam("documentVersion") String documentVersion, List<TagDTO> tagDtos) {
        String[] tagsLabel = new String[tagDtos.size()];
        for (int i = 0; i < tagDtos.size(); i++) {
            tagsLabel[i] = tagDtos.get(i).getLabel();
        }

        try {
            DocumentRevision docRs = documentService.saveTags(new DocumentRevisionKey(workspaceId, documentId, documentVersion), tagsLabel);
            DocumentRevisionDTO docRsDto = mapper.map(docRs, DocumentRevisionDTO.class);
            docRsDto.setPath(docRs.getLocation().getCompletePath());
            docRsDto.setLifeCycleState(docRs.getLifeCycleState());

            return docRsDto;
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @POST
    @Path("/tags")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addDocTag(@PathParam("workspaceId") String workspaceId, @PathParam("documentId") String documentId, @PathParam("documentVersion") String documentVersion, List<TagDTO> tagDtos) {
        try {
            DocumentRevisionKey docRPK=new DocumentRevisionKey(workspaceId, documentId, documentVersion);
            DocumentRevision docR = documentService.getDocumentRevision(docRPK);
            Set<Tag> tags = docR.getTags();
            Set<String> tagLabels = new HashSet<String>();

            for(TagDTO tagDto:tagDtos){
                tagLabels.add(tagDto.getLabel());
            }

            for(Tag tag : tags){
                tagLabels.add(tag.getLabel());
            }

            documentService.saveTags(docRPK,tagLabels.toArray(new String[tagLabels.size()]));
            return Response.ok().build();

        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @DELETE
    @Path("/tags/{tagName}")
    public Response removeDocTags(@PathParam("workspaceId") String workspaceId, @PathParam("documentId") String documentId, @PathParam("documentVersion") String documentVersion, @PathParam("tagName") String tagName) {
        try {
            documentService.removeTag(new DocumentMasterKey(workspaceId, documentId, documentVersion),tagName);
            return Response.ok().build();
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }

    }

    @DELETE
    public Response deleteDocument(@PathParam("workspaceId") String workspaceId, @PathParam("documentId") String documentId, @PathParam("documentVersion") String documentVersion) {
        try {
            documentService.deleteDocumentRevision(new DocumentRevisionKey(workspaceId, documentId, documentVersion));
            return Response.ok().build();
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @DELETE
    @Path("/iterations/{docIteration}/files/{fileName}")
    public Response removeAttachedFile(@PathParam("workspaceId") String workspaceId, @PathParam("documentId") String documentId, @PathParam("documentVersion") String documentVersion, @PathParam("docIteration") int docIteration, @PathParam("fileName") String fileName) {
        try {
            String fileFullName = workspaceId + "/documents/" + documentId + "/" + documentVersion + "/" + docIteration + "/" + fileName;
            documentService.removeFileFromDocument(fileFullName);
            return Response.ok().build();
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @POST
    @Path("share")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createSharedDocument(@PathParam("workspaceId") String workspaceId, @PathParam("documentId") String documentId, @PathParam("documentVersion") String documentVersion, SharedDocumentDTO pSharedDocumentDTO) {

        String password = pSharedDocumentDTO.getPassword();
        Date expireDate = pSharedDocumentDTO.getExpireDate();

        try {
            SharedDocument sharedDocument = documentService.createSharedDocument(new DocumentRevisionKey(workspaceId, documentId, documentVersion),password,expireDate);
            SharedDocumentDTO sharedDocumentDTO = mapper.map(sharedDocument,SharedDocumentDTO.class);
            return Response.ok().entity(sharedDocumentDTO).build();
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }

    }


    @PUT
    @Path("publish")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response publishPartRevision(@PathParam("workspaceId") String workspaceId, @PathParam("documentId") String documentId, @PathParam("documentVersion") String documentVersion) {
        try {
            DocumentMaster documentMaster = documentService.getDocumentMaster(new DocumentMasterKey(workspaceId, documentId, documentVersion));
            documentMaster.setPublicShared(true);
            return Response.ok().build();
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @PUT
    @Path("unpublish")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response unPublishPartRevision(@PathParam("workspaceId") String workspaceId, @PathParam("documentId") String documentId, @PathParam("documentVersion") String documentVersion) {
        try {
            DocumentMaster documentMaster = documentService.getDocumentMaster(new DocumentMasterKey(workspaceId, documentId, documentVersion));
            documentMaster.setPublicShared(false);
            return Response.ok().build();
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }

    }

    @PUT
    @Path("acl")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateACL(@PathParam("workspaceId") String pWorkspaceId, @PathParam("documentId") String documentId, @PathParam("documentVersion") String documentVersion, ACLDTO acl) {
        try {
            DocumentMasterKey documentMasterKey = new DocumentMasterKey(pWorkspaceId, documentId, documentVersion);
            if (acl.getGroupEntries().size() > 0 && acl.getUserEntries().size() > 0) {

                Map<String,String> userEntries = new HashMap<String,String>();
                Map<String,String> groupEntries = new HashMap<String,String>();

                for (Map.Entry<String, ACL.Permission> entry : acl.getUserEntries().entrySet()) {
                    userEntries.put(entry.getKey(), entry.getValue().name());
                }

                for (Map.Entry<String, ACL.Permission> entry : acl.getGroupEntries().entrySet()) {
                    groupEntries.put(entry.getKey(), entry.getValue().name());
                }

                documentService.updateDocumentACL(pWorkspaceId, documentMasterKey, userEntries, groupEntries);
            }else{
                documentService.removeACLFromDocumentMaster(documentMasterKey);
            }
            return Response.ok().build();
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @GET
    @Path("aborted-workflows")
    @Produces(MediaType.APPLICATION_JSON)
    public List<WorkflowDTO> getAbortedWorkflows(@PathParam("workspaceId") String workspaceId, @PathParam("documentId") String documentId, @PathParam("documentVersion") String documentVersion) {

        try {
            DocumentMaster docM = documentService.getDocumentMaster(new DocumentMasterKey(workspaceId, documentId, documentVersion));
            List<Workflow> abortedWorkflows = docM.getAbortedWorkflows();
            List<WorkflowDTO> abortedWorkflowsDTO = new ArrayList<WorkflowDTO>();

            for(Workflow abortedWorkflow:abortedWorkflows){
                abortedWorkflowsDTO.add(mapper.map(abortedWorkflow,WorkflowDTO.class));
            }

            return abortedWorkflowsDTO;

        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    private InstanceAttribute[] createInstanceAttributes(List<InstanceAttributeDTO> dtos) {
        if (dtos == null) {
            return null;
        }
        InstanceAttribute[] data = new InstanceAttribute[dtos.size()];
        int i = 0;
        for (InstanceAttributeDTO dto : dtos) {
            data[i++] = createInstanceAttribute(dto);
        }

        return data;
    }

    private InstanceAttribute createInstanceAttribute(InstanceAttributeDTO dto) {
        if (dto.getType().equals(InstanceAttributeDTO.Type.BOOLEAN)) {
            InstanceBooleanAttribute attr = new InstanceBooleanAttribute();
            attr.setName(dto.getName());
            attr.setBooleanValue(Boolean.parseBoolean(dto.getValue()));
            return attr;
        } else if (dto.getType().equals(InstanceAttributeDTO.Type.TEXT)) {
            InstanceTextAttribute attr = new InstanceTextAttribute();
            attr.setName(dto.getName());
            attr.setTextValue(dto.getValue());
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

    private DocumentIterationKey[] createDocumentIterationKeys(List<DocumentIterationDTO> dtos) {
        DocumentIterationKey[] data = new DocumentIterationKey[dtos.size()];
        int i = 0;
        for (DocumentIterationDTO dto : dtos) {
            data[i++] = new DocumentIterationKey(dto.getWorkspaceId(), dto.getDocumentMasterId(), dto.getDocumentMasterVersion(), dto.getIteration());
        }

        return data;
    }

}
