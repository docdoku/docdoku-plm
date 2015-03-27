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
import com.docdoku.core.common.User;
import com.docdoku.core.common.UserGroup;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.configuration.DocumentConfigSpec;
import com.docdoku.core.document.DocumentIterationKey;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.meta.*;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.ACLUserEntry;
import com.docdoku.core.security.ACLUserGroupEntry;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IDocumentConfigSpecManagerLocal;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.core.services.IDocumentWorkflowManagerLocal;
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
    @EJB
    private IDocumentWorkflowManagerLocal documentWorkflowService;
    @EJB
    private IDocumentConfigSpecManagerLocal documentConfigSpecService;

    private static final String BASELINE_LATEST = "latest";
    private static final String BASELINE_UNDEFINED = "undefined";
    private Mapper mapper;

    public DocumentResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO getDocumentRevision(@PathParam("workspaceId") String workspaceId,
                                                   @PathParam("documentId") String documentId,
                                                   @PathParam("documentVersion") String documentVersion,
                                                   @QueryParam("configSpec") String configSpecType)
            throws EntityNotFoundException, AccessRightException, NotAllowedException, UserNotActiveException {
        DocumentRevision docR;
        DocumentRevisionKey documentRevisionKey = new DocumentRevisionKey(workspaceId, documentId, documentVersion);
        if (configSpecType == null || BASELINE_UNDEFINED.equals(configSpecType) || BASELINE_LATEST.equals(configSpecType)) {
            docR = documentService.getDocumentRevision(documentRevisionKey);
        } else {
            DocumentConfigSpec configSpec = getConfigSpec(workspaceId, configSpecType);
            docR = documentConfigSpecService.getFilteredDocumentRevision(documentRevisionKey, configSpec);
        }

        DocumentRevisionDTO docRsDTO = mapper.map(docR, DocumentRevisionDTO.class);
        docRsDTO.setPath(docR.getLocation().getCompletePath());

        if (configSpecType == null || BASELINE_UNDEFINED.equals(configSpecType) || BASELINE_LATEST.equals(configSpecType)) {
            setDocumentRevisionDTOWorkflow(docR,docRsDTO);
            docRsDTO.setIterationSubscription(documentService.isUserIterationChangeEventSubscribedForGivenDocument(workspaceId, docR));
            docRsDTO.setStateSubscription(documentService.isUserStateChangeEventSubscribedForGivenDocument(workspaceId, docR));
        }else{
            docRsDTO.setWorkflow(null);
            docRsDTO.setTags(null);
        }
        return docRsDTO;
    }

    private void setDocumentRevisionDTOWorkflow(DocumentRevision documentRevision, DocumentRevisionDTO documentRevisionDTO)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException{
        Workflow currentWorkflow = documentWorkflowService.getCurrentWorkflow(documentRevision.getKey());
        if(currentWorkflow!=null){
            documentRevisionDTO.setWorkflow(mapper.map(currentWorkflow,WorkflowDTO.class));
            documentRevisionDTO.setLifeCycleState(currentWorkflow.getLifeCycleState());
        }
    }

    @PUT
    @Path("/checkin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO checkInDocument(@PathParam("workspaceId") String workspaceId, @PathParam("documentId") String documentId, @PathParam("documentVersion") String documentVersion)
            throws NotAllowedException, EntityNotFoundException, ESServerException, AccessRightException, UserNotActiveException{
        DocumentRevision docR = documentService.checkInDocument(new DocumentRevisionKey(workspaceId, documentId, documentVersion));
        DocumentRevisionDTO docRsDTO = mapper.map(docR, DocumentRevisionDTO.class);
        docRsDTO.setPath(docR.getLocation().getCompletePath());
        return docRsDTO;
    }

    @PUT
    @Path("/checkout")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO checkOutDocument(@PathParam("workspaceId") String workspaceId, @PathParam("documentId") String documentId, @PathParam("documentVersion") String documentVersion)
            throws EntityNotFoundException, NotAllowedException, CreationException, AccessRightException, UserNotActiveException, EntityAlreadyExistsException{
        DocumentRevision docR = documentService.checkOutDocument(new DocumentRevisionKey(workspaceId, documentId, documentVersion));
        DocumentRevisionDTO docRsDTO = mapper.map(docR, DocumentRevisionDTO.class);
        docRsDTO.setPath(docR.getLocation().getCompletePath());
        docRsDTO.setLifeCycleState(docR.getLifeCycleState());
        return docRsDTO;
    }

    @PUT
    @Path("/undocheckout")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO undoCheckOutDocument(@PathParam("workspaceId") String workspaceId, @PathParam("documentId") String documentId, @PathParam("documentVersion") String documentVersion)
            throws EntityNotFoundException, NotAllowedException, UserNotActiveException, AccessRightException {
        DocumentRevision docR = documentService.undoCheckOutDocument(new DocumentRevisionKey(workspaceId, documentId, documentVersion));
        DocumentRevisionDTO docRsDTO = mapper.map(docR, DocumentRevisionDTO.class);
        docRsDTO.setPath(docR.getLocation().getCompletePath());
        docRsDTO.setLifeCycleState(docR.getLifeCycleState());
        return docRsDTO;
    }

    @PUT
    @Path("/move")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO moveDocument(@PathParam("workspaceId") String workspaceId, @PathParam("documentId") String documentId, @PathParam("documentVersion") String documentVersion, DocumentCreationDTO docCreationDTO)
            throws EntityNotFoundException, NotAllowedException, AccessRightException, UserNotActiveException{
        String parentFolderPath = docCreationDTO.getPath();
        String newCompletePath = Tools.stripTrailingSlash(parentFolderPath);
        DocumentRevisionKey docRsKey = new DocumentRevisionKey(workspaceId, documentId, documentVersion);
        DocumentRevision movedDocumentRevision = documentService.moveDocumentRevision(newCompletePath, docRsKey);
        DocumentRevisionDTO docMsDTO = mapper.map(movedDocumentRevision, DocumentRevisionDTO.class);
        docMsDTO.setPath(movedDocumentRevision.getLocation().getCompletePath());
        docMsDTO.setLifeCycleState(movedDocumentRevision.getLifeCycleState());
        return docMsDTO;
    }

    @PUT
    @Path("/notification/iterationChange/subscribe")
    public Response subscribeToIterationChangeEvent(@PathParam("workspaceId") String workspaceId,@PathParam("documentId") String documentId, @PathParam("documentVersion") String documentVersion)
            throws EntityNotFoundException, AccessRightException, NotAllowedException, UserNotActiveException {
        documentService.subscribeToIterationChangeEvent(new DocumentRevisionKey(workspaceId, documentId, documentVersion));
        return Response.ok().build();
    }

    @PUT
    @Path("/notification/iterationChange/unsubscribe")
    public Response unSubscribeToIterationChangeEvent(@PathParam("workspaceId") String workspaceId, @PathParam("documentId") String documentId, @PathParam("documentVersion") String documentVersion)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException{
        documentService.unsubscribeToIterationChangeEvent(new DocumentRevisionKey(workspaceId, documentId, documentVersion));
        return Response.ok().build();
    }

    @PUT
    @Path("/notification/stateChange/subscribe")
    public Response subscribeToStateChangeEvent(@PathParam("workspaceId") String workspaceId, @PathParam("documentId") String documentId, @PathParam("documentVersion") String documentVersion)
            throws EntityNotFoundException, AccessRightException, NotAllowedException, UserNotActiveException {
        documentService.subscribeToStateChangeEvent(new DocumentRevisionKey(workspaceId, documentId, documentVersion));
        return Response.ok().build();
    }

    @PUT
    @Path("/notification/stateChange/unsubscribe")
    public Response unsubscribeToStateChangeEvent(@PathParam("workspaceId") String workspaceId, @PathParam("documentId") String documentId, @PathParam("documentVersion") String documentVersion)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException{
        documentService.unsubscribeToStateChangeEvent(new DocumentRevisionKey(workspaceId, documentId, documentVersion));
        return Response.ok().build();
    }

    @PUT
    @Path("/iterations/{docIteration}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentIterationDTO updateDocMs(@PathParam("workspaceId") String workspaceId, @PathParam("documentId") String documentId, @PathParam("documentVersion") String documentVersion, @PathParam("docIteration") String docIteration, DocumentIterationDTO data)
            throws EntityNotFoundException, NotAllowedException, AccessRightException, UserNotActiveException{
        String pRevisionNote = data.getRevisionNote();
        int pIteration = Integer.parseInt(docIteration);

        List<DocumentIterationDTO> linkedDocs = data.getLinkedDocuments();
        DocumentIterationKey[] links = null;
        String[] documentLinkComments = null;
        if (linkedDocs != null) {
            documentLinkComments = new String[linkedDocs.size()];
            links = createDocumentIterationKeys(linkedDocs);
            int i = 0;
            for (DocumentIterationDTO docItereationForLink : linkedDocs){
                String comment = docItereationForLink.getCommentLink();
                if (comment == null){
                    comment = "";
                }
                documentLinkComments[i++] = comment;
            }
        }

        List<InstanceAttributeDTO> instanceAttributes = data.getInstanceAttributes();
        List<InstanceAttribute> attributes = null;
        if (instanceAttributes != null) {
            attributes = createInstanceAttributes(instanceAttributes);
        }

        DocumentRevision docR = documentService.updateDocument(new DocumentIterationKey(workspaceId, documentId, documentVersion, pIteration), pRevisionNote, attributes, links, documentLinkComments);
        return mapper.map(docR.getLastIteration(), DocumentIterationDTO.class);
    }

    @PUT
    @Path("/newVersion")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO[] createNewVersion(@PathParam("workspaceId") String pWorkspaceId, @PathParam("documentId") String documentId, @PathParam("documentVersion") String documentVersion, DocumentCreationDTO docCreationDTO)
            throws EntityNotFoundException, EntityAlreadyExistsException, NotAllowedException, AccessRightException, CreationException, UserNotActiveException {
        String pTitle = docCreationDTO.getTitle();
        String pDescription = docCreationDTO.getDescription();
        String pWorkflowModelId = docCreationDTO.getWorkflowModelId();
        RoleMappingDTO[] rolesMappingDTO = docCreationDTO.getRoleMapping();
        ACLDTO acl = docCreationDTO.getAcl();

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
            dtos[i].setIterationSubscription(documentService.isUserIterationChangeEventSubscribedForGivenDocument(pWorkspaceId,docR[i]));
            dtos[i].setStateSubscription(documentService.isUserStateChangeEventSubscribedForGivenDocument(pWorkspaceId,docR[i]));
        }

        return dtos;
    }

    @PUT
    @Path("/tags")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO saveDocTags(@PathParam("workspaceId") String workspaceId, @PathParam("documentId") String documentId, @PathParam("documentVersion") String documentVersion, List<TagDTO> tagDtos)
            throws EntityNotFoundException, NotAllowedException, ESServerException, AccessRightException, UserNotActiveException{
        String[] tagsLabel = new String[tagDtos.size()];
        for (int i = 0; i < tagDtos.size(); i++) {
            tagsLabel[i] = tagDtos.get(i).getLabel();
        }

        DocumentRevision docRs = documentService.saveTags(new DocumentRevisionKey(workspaceId, documentId, documentVersion), tagsLabel);
        DocumentRevisionDTO docRsDto = mapper.map(docRs, DocumentRevisionDTO.class);
        docRsDto.setPath(docRs.getLocation().getCompletePath());
        docRsDto.setLifeCycleState(docRs.getLifeCycleState());

        return docRsDto;
    }

    @POST
    @Path("/tags")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addDocTag(@PathParam("workspaceId") String workspaceId, @PathParam("documentId") String documentId, @PathParam("documentVersion") String documentVersion, List<TagDTO> tagDtos)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException, ESServerException {

        DocumentRevisionKey docRPK=new DocumentRevisionKey(workspaceId, documentId, documentVersion);
        DocumentRevision docR = documentService.getDocumentRevision(docRPK);
        Set<Tag> tags = docR.getTags();
        Set<String> tagLabels = new HashSet<>();

        for(TagDTO tagDto:tagDtos){
            tagLabels.add(tagDto.getLabel());
        }

        for(Tag tag : tags){
            tagLabels.add(tag.getLabel());
        }

        documentService.saveTags(docRPK,tagLabels.toArray(new String[tagLabels.size()]));
        return Response.ok().build();
    }

    @DELETE
    @Path("/tags/{tagName}")
    public Response removeDocTags(@PathParam("workspaceId") String workspaceId, @PathParam("documentId") String documentId, @PathParam("documentVersion") String documentVersion, @PathParam("tagName") String tagName)
            throws EntityNotFoundException, NotAllowedException, AccessRightException, UserNotActiveException, ESServerException {
        documentService.removeTag(new DocumentRevisionKey(workspaceId, documentId, documentVersion), tagName);
        return Response.ok().build();
    }

    @DELETE
    public Response deleteDocument(@PathParam("workspaceId") String workspaceId, @PathParam("documentId") String documentId, @PathParam("documentVersion") String documentVersion)
            throws EntityNotFoundException, NotAllowedException, AccessRightException, UserNotActiveException, ESServerException, EntityConstraintException {
        documentService.deleteDocumentRevision(new DocumentRevisionKey(workspaceId, documentId, documentVersion));
        return Response.ok().build();
    }

     @PUT
     @Path("/iterations/{docIteration}/files/{fileName}")
     @Consumes(MediaType.APPLICATION_JSON)
     @Produces(MediaType.APPLICATION_JSON)
     public FileDTO renameAttachedFile(@PathParam("workspaceId") String workspaceId, @PathParam("documentId") String documentId, @PathParam("documentVersion") String documentVersion, @PathParam("docIteration") int docIteration, @PathParam("fileName") String fileName, FileDTO fileDTO)
             throws EntityNotFoundException, NotAllowedException, AccessRightException, UserNotActiveException, FileAlreadyExistsException, CreationException {
        String fileFullName = workspaceId + "/documents/" + documentId + "/" + documentVersion + "/" + docIteration + "/" + fileName;
        BinaryResource binaryResource = documentService.renameFileInDocument(fileFullName, fileDTO.getShortName());
        return new FileDTO(true,binaryResource.getFullName(),binaryResource.getName());
    }

    @DELETE
    @Path("/iterations/{docIteration}/files/{fileName}")
    public Response removeAttachedFile(@PathParam("workspaceId") String workspaceId, @PathParam("documentId") String documentId, @PathParam("documentVersion") String documentVersion, @PathParam("docIteration") int docIteration, @PathParam("fileName") String fileName)
            throws EntityNotFoundException, NotAllowedException, AccessRightException, UserNotActiveException {
        String fileFullName = workspaceId + "/documents/" + documentId + "/" + documentVersion + "/" + docIteration + "/" + fileName;
        documentService.removeFileFromDocument(fileFullName);
        return Response.ok().build();
    }

    @POST
    @Path("share")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createSharedDocument(@PathParam("workspaceId") String workspaceId, @PathParam("documentId") String documentId, @PathParam("documentVersion") String documentVersion, SharedDocumentDTO pSharedDocumentDTO)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException {

        String password = pSharedDocumentDTO.getPassword();
        Date expireDate = pSharedDocumentDTO.getExpireDate();

        SharedDocument sharedDocument = documentService.createSharedDocument(new DocumentRevisionKey(workspaceId, documentId, documentVersion),password,expireDate);
        SharedDocumentDTO sharedDocumentDTO = mapper.map(sharedDocument,SharedDocumentDTO.class);
        return Response.ok().entity(sharedDocumentDTO).build();
    }


    @PUT
    @Path("publish")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response publishPartRevision(@PathParam("workspaceId") String workspaceId, @PathParam("documentId") String documentId, @PathParam("documentVersion") String documentVersion)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException {
        DocumentRevision documentRevision = documentService.getDocumentRevision(new DocumentRevisionKey(workspaceId, documentId, documentVersion));
        documentRevision.setPublicShared(true);
        return Response.ok().build();
    }

    @PUT
    @Path("unpublish")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response unPublishPartRevision(@PathParam("workspaceId") String workspaceId, @PathParam("documentId") String documentId, @PathParam("documentVersion") String documentVersion)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException {
        DocumentRevision documentRevision = documentService.getDocumentRevision(new DocumentRevisionKey(workspaceId, documentId, documentVersion));
        documentRevision.setPublicShared(false);
        return Response.ok().build();
    }

    @PUT
    @Path("acl")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateACL(@PathParam("workspaceId") String pWorkspaceId, @PathParam("documentId") String documentId, @PathParam("documentVersion") String documentVersion, ACLDTO acl)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException, NotAllowedException {
        DocumentRevisionKey documentRevisionKey = new DocumentRevisionKey(pWorkspaceId, documentId, documentVersion);

        if (!acl.getGroupEntries().isEmpty() || !acl.getUserEntries().isEmpty()) {

            Map<String,String> userEntries = new HashMap<>();
            Map<String,String> groupEntries = new HashMap<>();

            for (Map.Entry<String, ACL.Permission> entry : acl.getUserEntries().entrySet()) {
                userEntries.put(entry.getKey(), entry.getValue().name());
            }

            for (Map.Entry<String, ACL.Permission> entry : acl.getGroupEntries().entrySet()) {
                groupEntries.put(entry.getKey(), entry.getValue().name());
            }

            documentService.updateDocumentACL(pWorkspaceId, documentRevisionKey, userEntries, groupEntries);
        }else{
            documentService.removeACLFromDocumentRevision(documentRevisionKey);
        }
        return Response.ok().build();
    }

    @GET
    @Path("aborted-workflows")
    @Produces(MediaType.APPLICATION_JSON)
    public List<WorkflowDTO> getAbortedWorkflows(@PathParam("workspaceId") String workspaceId, @PathParam("documentId") String documentId, @PathParam("documentVersion") String documentVersion)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException {
        Workflow[] abortedWorkflows = documentWorkflowService.getAbortedWorkflow(new DocumentRevisionKey(workspaceId, documentId, documentVersion));
        List<WorkflowDTO> abortedWorkflowsDTO = new ArrayList<>();

        for(Workflow abortedWorkflow:abortedWorkflows){
            abortedWorkflowsDTO.add(mapper.map(abortedWorkflow,WorkflowDTO.class));
        }

        Collections.sort(abortedWorkflowsDTO);

        return abortedWorkflowsDTO;
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
        switch (dto.getType()){
            case BOOLEAN :
                attr = new InstanceBooleanAttribute();
                break;
            case TEXT :
                attr = new InstanceTextAttribute();
                break;
            case NUMBER :
                attr = new InstanceNumberAttribute();
                break;
            case DATE :
                attr = new InstanceDateAttribute();
                break;
            case URL :
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
        return attr;
    }

    private DocumentIterationKey[] createDocumentIterationKeys(List<DocumentIterationDTO> dtos) {
        DocumentIterationKey[] data = new DocumentIterationKey[dtos.size()];
        int i = 0;
        for (DocumentIterationDTO dto : dtos) {
            data[i++] = new DocumentIterationKey(dto.getWorkspaceId(), dto.getDocumentMasterId(), dto.getDocumentRevisionVersion(), dto.getIteration());
        }

        return data;
    }

    /**
     * Get a configuration specification
     * @param workspaceId The current workspace
     * @param configSpecType The configuration specification type
     * @return A configuration specification
     * @throws com.docdoku.core.exceptions.UserNotFoundException If the user login-workspace doesn't exist
     * @throws com.docdoku.core.exceptions.UserNotActiveException If the user is disabled
     * @throws com.docdoku.core.exceptions.WorkspaceNotFoundException If the workspace doesn't exist
     * @throws com.docdoku.core.exceptions.BaselineNotFoundException If the baseline doesn't exist
     */
    private DocumentConfigSpec getConfigSpec(String workspaceId, String configSpecType) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, BaselineNotFoundException {
        DocumentConfigSpec cs;
        switch (configSpecType) {
            case BASELINE_LATEST:
            case BASELINE_UNDEFINED:
                cs = documentConfigSpecService.getLatestConfigSpec(workspaceId);
                break;
            default:
                cs = documentConfigSpecService.getConfigSpecForBaseline(Integer.parseInt(configSpecType));
                break;
        }
        return cs;
    }
}