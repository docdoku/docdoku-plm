/* DocDoku, Professional Open Source
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

import com.docdoku.core.common.User;
import com.docdoku.core.common.UserGroup;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.configuration.DocumentConfigSpec;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.meta.TagKey;
import com.docdoku.core.query.DocumentSearchQuery;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.ACLUserEntry;
import com.docdoku.core.security.ACLUserGroupEntry;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IDocumentConfigSpecManagerLocal;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.core.services.IDocumentWorkflowManagerLocal;
import com.docdoku.server.rest.dto.*;
import com.docdoku.server.rest.util.SearchQueryParser;
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
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class DocumentsResource {

    @EJB
    private IDocumentManagerLocal documentService;
    @EJB
    private IDocumentConfigSpecManagerLocal documentConfigSpecService;
    @EJB
    private IDocumentWorkflowManagerLocal documentWorkflowService;

    @EJB
    private DocumentBaselinesResource baselinesResource;
    @EJB
    private DocumentResource document;

    private static final Logger LOGGER = Logger.getLogger(DocumentsResource.class.getName());
    private static final String BASELINE_LATEST = "latest";
    private static final String BASELINE_UNDEFINED = "undefined";
    private Mapper mapper;

    public DocumentsResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @Path("{documentId: [^/].*}-{documentVersion:[A-Z]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentResource getDocument() {
        return document;
    }

    // Todo Split it
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO[] getDocuments(@PathParam("workspaceId") String workspaceId, @PathParam("folderId") String folderId, @PathParam("tagId") String tagId, @PathParam("query") String query, @PathParam("assignedUserLogin") String assignedUserLogin, @PathParam("checkoutUser") String checkoutUser, @QueryParam("filter") String filter, @QueryParam("start") int start, @QueryParam("configSpec") String configSpecType)
            throws EntityNotFoundException, UserNotActiveException, ESServerException {
        if(checkoutUser != null){
            return getDocumentsCheckedOutByUser(workspaceId);
        }
        if(query != null){
            return getDocumentsWithSearchQuery(workspaceId, query, configSpecType);
        }
        if(tagId != null){
            return getDocumentsWithGivenTagIdAndWorkspaceId(workspaceId, tagId, configSpecType);
        }
        if(assignedUserLogin !=null){
            return getDocumentsWhereGivenUserHasAssignedTasks(workspaceId, assignedUserLogin, filter);
        }
        if(folderId != null){
            return getDocumentsWithGivenFolderIdAndWorkspaceId(workspaceId,folderId, configSpecType);
        }
        return getDocumentsInWorkspace(workspaceId, start, configSpecType);
    }

    @GET
    @Path("count")
    @Produces(MediaType.APPLICATION_JSON)
    public CountDTO getDocumentsInWorkspaceCount(@PathParam("workspaceId") String workspaceId)
            throws EntityNotFoundException, UserNotActiveException {
        return new CountDTO(documentService.getDocumentsInWorkspaceCount(Tools.stripTrailingSlash(workspaceId)));
    }

    private DocumentRevisionDTO[] getDocumentsInWorkspace(String workspaceId, int start, String configSpecType)
            throws EntityNotFoundException, UserNotActiveException {
        int maxResult = 20;

        DocumentRevision[] docRs;
        if(configSpecType==null || BASELINE_UNDEFINED.equals(configSpecType) || BASELINE_LATEST.equals(configSpecType)) {
            docRs = documentService.getAllDocumentsInWorkspace(workspaceId, start, maxResult);
        }else{
            DocumentConfigSpec configSpec = getConfigSpec(workspaceId,configSpecType);
            docRs = documentConfigSpecService.getFilteredDocuments(workspaceId,configSpec,start,maxResult);
        }
        DocumentRevisionDTO[] docRsDTOs = new DocumentRevisionDTO[docRs.length];

        for (int i = 0; i < docRs.length; i++) {
            docRsDTOs[i] = mapper.map(docRs[i], DocumentRevisionDTO.class);
            docRsDTOs[i].setPath(docRs[i].getLocation().getCompletePath());
            docRsDTOs[i] = Tools.createLightDocumentRevisionDTO(docRsDTOs[i]);
            docRsDTOs[i].setIterationSubscription(documentService.isUserIterationChangeEventSubscribedForGivenDocument(workspaceId,docRs[i]));
            docRsDTOs[i].setStateSubscription(documentService.isUserStateChangeEventSubscribedForGivenDocument(workspaceId,docRs[i]));
        }

        return docRsDTOs;
    }

    private DocumentRevisionDTO[] getDocumentsCheckedOutByUser(String workspaceId)
            throws EntityNotFoundException, UserNotActiveException {
        DocumentRevision[] docRs = documentService.getCheckedOutDocumentRevisions(workspaceId);
        DocumentRevisionDTO[] docRsDTOs = new DocumentRevisionDTO[docRs.length];

        for (int i = 0; i < docRs.length; i++) {
            docRsDTOs[i] = mapper.map(docRs[i], DocumentRevisionDTO.class);
            docRsDTOs[i].setPath(docRs[i].getLocation().getCompletePath());
            docRsDTOs[i] = Tools.createLightDocumentRevisionDTO(docRsDTOs[i]);
            docRsDTOs[i].setIterationSubscription(documentService.isUserIterationChangeEventSubscribedForGivenDocument(workspaceId,docRs[i]));
            docRsDTOs[i].setStateSubscription(documentService.isUserStateChangeEventSubscribedForGivenDocument(workspaceId,docRs[i]));
        }

        return docRsDTOs;
    }

    private DocumentRevisionDTO[] getDocumentsWithGivenFolderIdAndWorkspaceId(String workspaceId, String folderId, String configSpecType)
            throws EntityNotFoundException, UserNotActiveException {
        String decodedCompletePath = getPathFromUrlParams(workspaceId, folderId);
        DocumentRevision[] docRs;
        if (configSpecType == null || BASELINE_UNDEFINED.equals(configSpecType) || BASELINE_LATEST.equals(configSpecType)) {
            docRs = documentService.findDocumentRevisionsByFolder(decodedCompletePath);
        } else {
            DocumentConfigSpec configSpec = getConfigSpec(workspaceId, configSpecType);
            docRs = documentConfigSpecService.getFilteredDocumentsByFolder(workspaceId, configSpec, decodedCompletePath);
        }
        DocumentRevisionDTO[] docRsDTOs = new DocumentRevisionDTO[docRs.length];

        for (int i = 0; i < docRs.length; i++) {
            docRsDTOs[i] = mapper.map(docRs[i], DocumentRevisionDTO.class);
            docRsDTOs[i].setPath(docRs[i].getLocation().getCompletePath());
            docRsDTOs[i] = Tools.createLightDocumentRevisionDTO(docRsDTOs[i]);
            if (configSpecType == null || BASELINE_UNDEFINED.equals(configSpecType) || BASELINE_LATEST.equals(configSpecType)) {
                docRsDTOs[i].setLifeCycleState(docRs[i].getLifeCycleState());
                docRsDTOs[i].setIterationSubscription(documentService.isUserIterationChangeEventSubscribedForGivenDocument(workspaceId, docRs[i]));
                docRsDTOs[i].setStateSubscription(documentService.isUserStateChangeEventSubscribedForGivenDocument(workspaceId, docRs[i]));
            }else{
                docRsDTOs[i].setWorkflow(null);
                docRsDTOs[i].setTags(null);
            }
        }

        return docRsDTOs;
    }

    private DocumentRevisionDTO[] getDocumentsWithGivenTagIdAndWorkspaceId(String workspaceId, String tagId, String configSpecType)
            throws EntityNotFoundException, UserNotActiveException {

        DocumentRevision[] docRs;
        TagKey tagKey = new TagKey(workspaceId, tagId);
        if(configSpecType==null || BASELINE_UNDEFINED.equals(configSpecType) || BASELINE_LATEST.equals(configSpecType)) {
            docRs = documentService.findDocumentRevisionsByTag(tagKey);
        }else{
            DocumentConfigSpec configSpec = getConfigSpec(workspaceId,configSpecType);
            docRs = documentConfigSpecService.getFilteredDocumentsByTag(workspaceId,configSpec,tagKey);
        }
        DocumentRevisionDTO[] docRsDTOs = new DocumentRevisionDTO[docRs.length];

        for (int i = 0; i < docRs.length; i++) {
            docRsDTOs[i] = mapper.map(docRs[i], DocumentRevisionDTO.class);
            docRsDTOs[i].setPath(docRs[i].getLocation().getCompletePath());
            docRsDTOs[i] = Tools.createLightDocumentRevisionDTO(docRsDTOs[i]);
            if (configSpecType == null || BASELINE_UNDEFINED.equals(configSpecType) || BASELINE_LATEST.equals(configSpecType)) {
                docRsDTOs[i].setLifeCycleState(docRs[i].getLifeCycleState());
                docRsDTOs[i].setIterationSubscription(documentService.isUserIterationChangeEventSubscribedForGivenDocument(workspaceId, docRs[i]));
                docRsDTOs[i].setStateSubscription(documentService.isUserStateChangeEventSubscribedForGivenDocument(workspaceId, docRs[i]));
            }else{
                docRsDTOs[i].setWorkflow(null);
                docRsDTOs[i].setTags(null);
            }
        }

        return docRsDTOs;
    }

    private DocumentRevisionDTO[] getDocumentsWhereGivenUserHasAssignedTasks(String workspaceId, String assignedUserLogin, String filter)
            throws EntityNotFoundException, UserNotActiveException {

        DocumentRevision[] docRs;
        if(filter == null){
            docRs = documentWorkflowService.getDocumentRevisionsWithAssignedTasksForGivenUser(workspaceId, assignedUserLogin);
        }else{
            if ("in_progress".equals(filter)) {
                docRs = documentWorkflowService.getDocumentRevisionsWithOpenedTasksForGivenUser(workspaceId, assignedUserLogin);
            } else {
                docRs = documentWorkflowService.getDocumentRevisionsWithAssignedTasksForGivenUser(workspaceId, assignedUserLogin);
            }
        }

        List<DocumentRevisionDTO> docRsDTOs = new ArrayList<>();

        for (DocumentRevision docR : docRs) {

            DocumentRevisionDTO docDTO = mapper.map(docR, DocumentRevisionDTO.class);
            docDTO.setPath(docR.getLocation().getCompletePath());
            docDTO = Tools.createLightDocumentRevisionDTO(docDTO);
            docDTO.setIterationSubscription(documentService.isUserIterationChangeEventSubscribedForGivenDocument(workspaceId, docR));
            docDTO.setStateSubscription(documentService.isUserStateChangeEventSubscribedForGivenDocument(workspaceId, docR));
            docRsDTOs.add(docDTO);

        }

        return docRsDTOs.toArray(new DocumentRevisionDTO[docRsDTOs.size()]);
    }

    private DocumentRevisionDTO[] getDocumentsWithSearchQuery(String workspaceId, String pStringQuery, String configSpecType)
            throws EntityNotFoundException, ESServerException, UserNotActiveException {

        DocumentSearchQuery documentSearchQuery = SearchQueryParser.parseDocumentStringQuery(workspaceId, pStringQuery);
        DocumentRevision[] docRs;
        if(configSpecType==null || BASELINE_UNDEFINED.equals(configSpecType) || BASELINE_LATEST.equals(configSpecType)) {
            docRs = documentService.searchDocumentRevisions(documentSearchQuery);
        }else{
            DocumentConfigSpec configSpec = getConfigSpec(workspaceId,configSpecType);
            docRs = documentConfigSpecService.searchFilteredDocuments(workspaceId,configSpec,documentSearchQuery);
        }
        DocumentRevisionDTO[] docRsDTOs = new DocumentRevisionDTO[docRs.length];

        for (int i = 0; i < docRs.length; i++) {
            docRsDTOs[i] = mapper.map(docRs[i], DocumentRevisionDTO.class);
            docRsDTOs[i].setPath(docRs[i].getLocation().getCompletePath());
            docRsDTOs[i] = Tools.createLightDocumentRevisionDTO(docRsDTOs[i]);
            docRsDTOs[i].setIterationSubscription(documentService.isUserIterationChangeEventSubscribedForGivenDocument(workspaceId,docRs[i]));
            docRsDTOs[i].setStateSubscription(documentService.isUserStateChangeEventSubscribedForGivenDocument(workspaceId,docRs[i]));
        }

        return docRsDTOs;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createDocumentMasterInFolder(@PathParam("workspaceId") String workspaceId, DocumentCreationDTO docCreationDTO, @PathParam("folderId") String folderId, @QueryParam("configSpec") String configSpecType)
            throws EntityNotFoundException, EntityAlreadyExistsException, NotAllowedException, CreationException, AccessRightException{

        String pDocMID = docCreationDTO.getReference();
        String pTitle = docCreationDTO.getTitle();
        String pDescription = docCreationDTO.getDescription();

        String decodedCompletePath = getPathFromUrlParams(workspaceId, folderId);

        String pWorkflowModelId = docCreationDTO.getWorkflowModelId();
        RoleMappingDTO[] rolesMappingDTO = docCreationDTO.getRoleMapping();
        String pDocMTemplateId = docCreationDTO.getTemplateId();

        ACLDTO acl = docCreationDTO.getAcl();

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

        if(rolesMappingDTO != null){
            for(RoleMappingDTO roleMappingDTO : rolesMappingDTO){
                roleMappings.put(roleMappingDTO.getRoleName(),roleMappingDTO.getUserLogin());
            }
        }
        DocumentRevision createdDocRs =  documentService.createDocumentMaster(decodedCompletePath, pDocMID, pTitle, pDescription, pDocMTemplateId, pWorkflowModelId, userEntries, userGroupEntries, roleMappings);

        DocumentRevisionDTO docRsDTO = mapper.map(createdDocRs, DocumentRevisionDTO.class);
        docRsDTO.setPath(createdDocRs.getLocation().getCompletePath());
        docRsDTO.setLifeCycleState(createdDocRs.getLifeCycleState());

        try{
            return Response.created(URI.create(URLEncoder.encode(pDocMID + "-" + createdDocRs.getVersion(),"UTF-8"))).entity(docRsDTO).build();
        } catch (UnsupportedEncodingException ex) {
            LOGGER.log(Level.WARNING,null,ex);
            return Response.ok().build();
        }
    }

    private String getPathFromUrlParams(String workspaceId, String folderId) {
        return folderId == null ? Tools.stripTrailingSlash(workspaceId) : Tools.stripTrailingSlash(FolderDTO.replaceColonWithSlash(folderId));
    }

    @GET
    @Path("checkedout")
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO[] getCheckedOutDocMs(@PathParam("workspaceId") String workspaceId)
            throws EntityNotFoundException, UserNotActiveException {

        DocumentRevision[] checkedOutdocRs = documentService.getCheckedOutDocumentRevisions(workspaceId);
        DocumentRevisionDTO[] checkedOutdocRsDTO = new DocumentRevisionDTO[checkedOutdocRs.length];

        for (int i = 0; i < checkedOutdocRs.length; i++) {
            checkedOutdocRsDTO[i] = mapper.map(checkedOutdocRs[i], DocumentRevisionDTO.class);
            checkedOutdocRsDTO[i].setPath(checkedOutdocRs[i].getLocation().getCompletePath());
            checkedOutdocRsDTO[i] = Tools.createLightDocumentRevisionDTO(checkedOutdocRsDTO[i]);
            checkedOutdocRsDTO[i].setIterationSubscription(documentService.isUserIterationChangeEventSubscribedForGivenDocument(workspaceId,checkedOutdocRs[i]));
            checkedOutdocRsDTO[i].setStateSubscription(documentService.isUserStateChangeEventSubscribedForGivenDocument(workspaceId, checkedOutdocRs[i]));
        }

        return checkedOutdocRsDTO;
    }

    @GET
    @Path("docs_last_iter")
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentIterationDTO[] searchDocumentsLastIterationToLink(@PathParam("workspaceId") String workspaceId,@QueryParam("q") String q)
            throws EntityNotFoundException, UserNotActiveException {

        int maxResults = 8;

        DocumentRevision[] docRs = documentService.getDocumentRevisionsWithReference(workspaceId, q, maxResults);

        List<DocumentIterationDTO> docsLastIter = new ArrayList<>();
        for (DocumentRevision docR : docRs) {
            DocumentIteration docLastIter = docR.getLastIteration();
            if (docLastIter != null) {
                DocumentIterationDTO iterationDTO = new DocumentIterationDTO(docLastIter.getWorkspaceId(), docLastIter.getId(), docLastIter.getDocumentVersion(), docLastIter.getIteration());
                iterationDTO.setDocumentTitle(docR.getTitle());
                docsLastIter.add(iterationDTO);
            }
        }

        return docsLastIter.toArray(new DocumentIterationDTO[docsLastIter.size()]);
    }

    @Path("baselines")
    public DocumentBaselinesResource getAllBaselines(@PathParam("workspaceId") String workspaceId){
        return baselinesResource;
    }

    /**
     * Get a configuration specification according a string params
     * @param workspaceId The current workspace
     * @param configSpecType The string discribing the configSpec
     * @return A configuration specification
     * @throws com.docdoku.core.exceptions.UserNotFoundException If the user login-workspace doesn't exist
     * @throws com.docdoku.core.exceptions.UserNotActiveException If the user is disabled
     * @throws com.docdoku.core.exceptions.WorkspaceNotFoundException If the workspace doesn't exist
     * @throws com.docdoku.core.exceptions.BaselineNotFoundException If the baseline doesn't exist
     */
    private DocumentConfigSpec getConfigSpec(String workspaceId, String configSpecType) throws UserNotActiveException, EntityNotFoundException {
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