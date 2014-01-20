/* DocDoku, Professional Open Source
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
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.document.TagKey;
import com.docdoku.core.query.DocumentSearchQuery;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.ACLUserEntry;
import com.docdoku.core.security.ACLUserGroupEntry;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.server.rest.dto.*;
import com.docdoku.server.rest.exceptions.ApplicationException;
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

@Stateless
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class DocumentsResource {

    @EJB
    private IDocumentManagerLocal documentService;

    @EJB
    private DocumentResource document;

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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO[] getDocuments(@PathParam("workspaceId") String workspaceId, @PathParam("folderId") String folderId, @PathParam("tagId") String tagId, @PathParam("query") String query, @PathParam("assignedUserLogin") String assignedUserLogin, @PathParam("checkoutUser") String checkoutUser, @QueryParam("filter") String filter , @QueryParam("start") int start) {

        if(checkoutUser != null){
            return getDocumentsCheckedOutByUser(workspaceId);
        }
        if(query != null){
            return getDocumentsWithSearchQuery(workspaceId, query);
        }
        else if(tagId != null){
            return getDocumentsWithGivenTagIdAndWorkspaceId(workspaceId,tagId);
        }
        else if(assignedUserLogin !=null){
            return getDocumentsWhereGivenUserHasAssignedTasks(workspaceId, assignedUserLogin, filter);
        }
        else if(folderId != null){
            return getDocumentsWithGivenFolderIdAndWorkspaceId(workspaceId,folderId);
        }else{
            return getDocumentsInWorkspace(workspaceId, start);
        }

    }

    @GET
    @Path("count")
    @Produces(MediaType.APPLICATION_JSON)
    public int getDocumentsInWorkspaceCount(@PathParam("workspaceId") String workspaceId) {
        try {
            return documentService.getDocumentsInWorkspaceCount(Tools.stripTrailingSlash(workspaceId));
        } catch (com.docdoku.core.exceptions.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    private DocumentRevisionDTO[] getDocumentsInWorkspace(String workspaceId, int start) {
        int maxResult = 20;
        try {
            DocumentRevision[] docRs = documentService.getAllDocumentsInWorkspace(workspaceId, start, maxResult);
            DocumentRevisionDTO[] docRsDTOs = new DocumentRevisionDTO[docRs.length];

            for (int i = 0; i < docRs.length; i++) {
                docRsDTOs[i] = mapper.map(docRs[i], DocumentRevisionDTO.class);
                docRsDTOs[i].setPath(docRs[i].getLocation().getCompletePath());
                docRsDTOs[i] = Tools.createLightDocumentRevisionDTO(docRsDTOs[i]);
                docRsDTOs[i].setIterationSubscription(documentService.isUserIterationChangeEventSubscribedForGivenDocument(workspaceId,docRs[i]));
                docRsDTOs[i].setStateSubscription(documentService.isUserStateChangeEventSubscribedForGivenDocument(workspaceId,docRs[i]));
            }

            return docRsDTOs;

        } catch (com.docdoku.core.exceptions.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    private DocumentRevisionDTO[] getDocumentsCheckedOutByUser(String workspaceId) {

        try {
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
        } catch (com.docdoku.core.exceptions.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }

    }

    private DocumentRevisionDTO[] getDocumentsWithGivenFolderIdAndWorkspaceId(String workspaceId, String folderId){
        try {
            String decodedCompletePath = getPathFromUrlParams(workspaceId, folderId);
            DocumentRevision[] docRs = documentService.findDocumentRevisionsByFolder(decodedCompletePath);
            DocumentRevisionDTO[] docRsDTOs = new DocumentRevisionDTO[docRs.length];

            for (int i = 0; i < docRs.length; i++) {
                docRsDTOs[i] = mapper.map(docRs[i], DocumentRevisionDTO.class);
                docRsDTOs[i].setPath(docRs[i].getLocation().getCompletePath());
                docRsDTOs[i] = Tools.createLightDocumentRevisionDTO(docRsDTOs[i]);
                docRsDTOs[i].setLifeCycleState(docRs[i].getLifeCycleState());
                docRsDTOs[i].setIterationSubscription(documentService.isUserIterationChangeEventSubscribedForGivenDocument(workspaceId,docRs[i]));
                docRsDTOs[i].setStateSubscription(documentService.isUserStateChangeEventSubscribedForGivenDocument(workspaceId,docRs[i]));
            }

            return docRsDTOs;
        } catch (com.docdoku.core.exceptions.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    private DocumentRevisionDTO[] getDocumentsWithGivenTagIdAndWorkspaceId(String workspaceId, String tagId){
        try{
            DocumentRevision[] docRs = documentService.findDocumentRevisionsByTag(new TagKey(workspaceId, tagId));
            DocumentRevisionDTO[] docRsDTOs = new DocumentRevisionDTO[docRs.length];

            for (int i = 0; i < docRs.length; i++) {
                docRsDTOs[i] = mapper.map(docRs[i], DocumentRevisionDTO.class);
                docRsDTOs[i].setPath(docRs[i].getLocation().getCompletePath());
                docRsDTOs[i] = Tools.createLightDocumentRevisionDTO(docRsDTOs[i]);
                docRsDTOs[i].setIterationSubscription(documentService.isUserIterationChangeEventSubscribedForGivenDocument(workspaceId,docRs[i]));
                docRsDTOs[i].setStateSubscription(documentService.isUserStateChangeEventSubscribedForGivenDocument(workspaceId,docRs[i]));
            }

            return docRsDTOs;
        } catch (com.docdoku.core.exceptions.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    private DocumentRevisionDTO[] getDocumentsWhereGivenUserHasAssignedTasks(String workspaceId, String assignedUserLogin, String filter){
        try{
            DocumentRevision[] docRs;

            if(filter == null){
                docRs = documentService.getDocumentRevisionsWithAssignedTasksForGivenUser(workspaceId, assignedUserLogin);
            }
            else{
                switch (filter){
                    case "in_progress":
                        docRs = documentService.getDocumentRevisionsWithOpenedTasksForGivenUser(workspaceId, assignedUserLogin);
                        break;
                    default :
                        docRs = documentService.getDocumentRevisionsWithAssignedTasksForGivenUser(workspaceId, assignedUserLogin);
                        break;
                }
            }

            ArrayList<DocumentRevisionDTO> docRsDTOs = new ArrayList<>();

            for (DocumentRevision docR : docRs) {

                DocumentRevisionDTO docDTO = mapper.map(docR, DocumentRevisionDTO.class);
                docDTO.setPath(docR.getLocation().getCompletePath());
                docDTO = Tools.createLightDocumentRevisionDTO(docDTO);
                docDTO.setIterationSubscription(documentService.isUserIterationChangeEventSubscribedForGivenDocument(workspaceId, docR));
                docDTO.setStateSubscription(documentService.isUserStateChangeEventSubscribedForGivenDocument(workspaceId, docR));
                docRsDTOs.add(docDTO);

            }

            return docRsDTOs.toArray(new DocumentRevisionDTO[docRsDTOs.size()]);

        } catch (com.docdoku.core.exceptions.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    private DocumentRevisionDTO[] getDocumentsWithSearchQuery(String workspaceId, String pStringQuery){
        try{
            DocumentSearchQuery documentSearchQuery = SearchQueryParser.parseDocumentStringQuery(workspaceId, pStringQuery);
            DocumentRevision[] docRs = documentService.searchDocumentRevisions(documentSearchQuery);
            DocumentRevisionDTO[] docRsDTOs = new DocumentRevisionDTO[docRs.length];

            for (int i = 0; i < docRs.length; i++) {
                docRsDTOs[i] = mapper.map(docRs[i], DocumentRevisionDTO.class);
                docRsDTOs[i].setPath(docRs[i].getLocation().getCompletePath());
                docRsDTOs[i] = Tools.createLightDocumentRevisionDTO(docRsDTOs[i]);
                docRsDTOs[i].setIterationSubscription(documentService.isUserIterationChangeEventSubscribedForGivenDocument(workspaceId,docRs[i]));
                docRsDTOs[i].setStateSubscription(documentService.isUserStateChangeEventSubscribedForGivenDocument(workspaceId,docRs[i]));
            }

            return docRsDTOs;
        } catch (com.docdoku.core.exceptions.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createDocumentMasterInFolder(@PathParam("workspaceId") String workspaceId, DocumentCreationDTO docCreationDTO, @PathParam("folderId") String folderId) throws UnsupportedEncodingException {

        String pDocMID = docCreationDTO.getReference();
        String pTitle = docCreationDTO.getTitle();
        String pDescription = docCreationDTO.getDescription();

        String decodedCompletePath = getPathFromUrlParams(workspaceId, folderId);

        String pWorkflowModelId = docCreationDTO.getWorkflowModelId();
        RoleMappingDTO[] rolesMappingDTO = docCreationDTO.getRoleMapping();
        String pDocMTemplateId = docCreationDTO.getTemplateId();

        /* Null value for test purpose only */
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

            return Response.created(URI.create(URLEncoder.encode(pDocMID + "-" + createdDocRs.getVersion(),"UTF-8"))).entity(docRsDTO).build();

        } catch (com.docdoku.core.exceptions.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }

    }

    private String getPathFromUrlParams(String workspaceId, String folderId) {
        return folderId == null ? Tools.stripTrailingSlash(workspaceId) : Tools.stripTrailingSlash(Tools.replaceColonWithSlash(folderId));
    }

    @GET
    @Path("checkedout")
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO[] getCheckedOutDocMs(@PathParam("workspaceId") String workspaceId) throws ApplicationException {

        try {
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

        } catch (com.docdoku.core.exceptions.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @GET
    @Path("docs_last_iter")
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentIterationDTO[] searchDocumentsLastIterationToLink(@PathParam("workspaceId") String workspaceId,@QueryParam("q") String q) {
        try {

            int maxResults = 8;

            DocumentRevision[] docRs = documentService.getDocumentRevisionsWithReference(workspaceId, q, maxResults);

            List<DocumentIterationDTO> docsLastIter = new ArrayList<>();
            for (DocumentRevision docR : docRs) {
                DocumentIteration docLastIter = docR.getLastIteration();
                if (docLastIter != null)
                    docsLastIter.add(new DocumentIterationDTO(docLastIter.getWorkspaceId(), docLastIter.getId(), docLastIter.getDocumentVersion(), docLastIter.getIteration()));
            }

            return docsLastIter.toArray(new DocumentIterationDTO[docsLastIter.size()]);

        } catch (com.docdoku.core.exceptions.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

}