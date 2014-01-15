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
import com.docdoku.core.document.DocumentMaster;
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
import java.util.*;

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
    public DocumentMasterDTO[] getDocuments(@PathParam("workspaceId") String workspaceId, @PathParam("folderId") String folderId, @PathParam("tagId") String tagId, @PathParam("query") String query, @PathParam("assignedUserLogin") String assignedUserLogin, @PathParam("checkoutUser") String checkoutUser, @QueryParam("filter") String filter , @QueryParam("start") int start) {

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

    private DocumentMasterDTO[] getDocumentsInWorkspace(String workspaceId, int start) {
        int maxResult = 20;
        try {
            DocumentMaster[] docMs = documentService.getAllDocumentsInWorkspace(workspaceId, start, maxResult);
            DocumentMasterDTO[] docMsDTOs = new DocumentMasterDTO[docMs.length];

            for (int i = 0; i < docMs.length; i++) {
                docMsDTOs[i] = mapper.map(docMs[i], DocumentMasterDTO.class);
                docMsDTOs[i].setPath(docMs[i].getLocation().getCompletePath());
                docMsDTOs[i] = Tools.createLightDocumentMasterDTO(docMsDTOs[i]);
                docMsDTOs[i].setIterationSubscription(documentService.isUserIterationChangeEventSubscribedForGivenDocument(workspaceId,docMs[i]));
                docMsDTOs[i].setStateSubscription(documentService.isUserStateChangeEventSubscribedForGivenDocument(workspaceId,docMs[i]));
                ACL acl = docMs[i].getACL();
                if(acl != null){
                    docMsDTOs[i].setAcl(Tools.mapACLtoACLDTO(acl));
                }
            }

            return docMsDTOs;

        } catch (com.docdoku.core.exceptions.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    private DocumentMasterDTO[] getDocumentsCheckedOutByUser(String workspaceId) {

        try {
            DocumentMaster[] docMs = documentService.getCheckedOutDocumentMasters(workspaceId);
            DocumentMasterDTO[] docMsDTOs = new DocumentMasterDTO[docMs.length];

            for (int i = 0; i < docMs.length; i++) {
                docMsDTOs[i] = mapper.map(docMs[i], DocumentMasterDTO.class);
                docMsDTOs[i].setPath(docMs[i].getLocation().getCompletePath());
                docMsDTOs[i] = Tools.createLightDocumentMasterDTO(docMsDTOs[i]);
                docMsDTOs[i].setIterationSubscription(documentService.isUserIterationChangeEventSubscribedForGivenDocument(workspaceId,docMs[i]));
                docMsDTOs[i].setStateSubscription(documentService.isUserStateChangeEventSubscribedForGivenDocument(workspaceId,docMs[i]));
                ACL acl = docMs[i].getACL();
                if(acl != null){
                    docMsDTOs[i].setAcl(Tools.mapACLtoACLDTO(acl));
                }
            }

            return docMsDTOs;
        } catch (com.docdoku.core.exceptions.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }

    }

    private DocumentMasterDTO[] getDocumentsWithGivenFolderIdAndWorkspaceId(String workspaceId, String folderId){
        try {
            String decodedCompletePath = getPathFromUrlParams(workspaceId, folderId);
            DocumentMaster[] docMs = documentService.findDocumentMastersByFolder(decodedCompletePath);
            DocumentMasterDTO[] docMsDTOs = new DocumentMasterDTO[docMs.length];

            for (int i = 0; i < docMs.length; i++) {
                docMsDTOs[i] = mapper.map(docMs[i], DocumentMasterDTO.class);
                docMsDTOs[i].setPath(docMs[i].getLocation().getCompletePath());
                docMsDTOs[i] = Tools.createLightDocumentMasterDTO(docMsDTOs[i]);
                docMsDTOs[i].setLifeCycleState(docMs[i].getLifeCycleState());
                docMsDTOs[i].setIterationSubscription(documentService.isUserIterationChangeEventSubscribedForGivenDocument(workspaceId,docMs[i]));
                docMsDTOs[i].setStateSubscription(documentService.isUserStateChangeEventSubscribedForGivenDocument(workspaceId,docMs[i]));
                ACL acl = docMs[i].getACL();
                if(acl != null){
                    docMsDTOs[i].setAcl(Tools.mapACLtoACLDTO(acl));
                }
            }

            return docMsDTOs;
        } catch (com.docdoku.core.exceptions.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    private DocumentMasterDTO[] getDocumentsWithGivenTagIdAndWorkspaceId(String workspaceId, String tagId){
        try{
            DocumentMaster[] docMs = documentService.findDocumentMastersByTag(new TagKey(workspaceId, tagId));
            DocumentMasterDTO[] docMsDTOs = new DocumentMasterDTO[docMs.length];

            for (int i = 0; i < docMs.length; i++) {
                docMsDTOs[i] = mapper.map(docMs[i], DocumentMasterDTO.class);
                docMsDTOs[i].setPath(docMs[i].getLocation().getCompletePath());
                docMsDTOs[i] = Tools.createLightDocumentMasterDTO(docMsDTOs[i]);
                docMsDTOs[i].setIterationSubscription(documentService.isUserIterationChangeEventSubscribedForGivenDocument(workspaceId,docMs[i]));
                docMsDTOs[i].setStateSubscription(documentService.isUserStateChangeEventSubscribedForGivenDocument(workspaceId,docMs[i]));
                ACL acl = docMs[i].getACL();
                if(acl != null){
                    docMsDTOs[i].setAcl(Tools.mapACLtoACLDTO(acl));
                }
            }

            return docMsDTOs;
        } catch (com.docdoku.core.exceptions.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    private DocumentMasterDTO[] getDocumentsWhereGivenUserHasAssignedTasks(String workspaceId, String assignedUserLogin, String filter){
        try{
            DocumentMaster[] docMs ;

            if(filter == null){
                docMs = documentService.getDocumentMastersWithAssignedTasksForGivenUser(workspaceId, assignedUserLogin);
            }
            else{
                switch (filter){
                    case "in_progress":
                        docMs = documentService.getDocumentMastersWithOpenedTasksForGivenUser(workspaceId, assignedUserLogin);
                        break;
                    default :
                        docMs = documentService.getDocumentMastersWithAssignedTasksForGivenUser(workspaceId, assignedUserLogin);
                        break;
                }
            }

            ArrayList<DocumentMasterDTO> docMsDTOs = new ArrayList<DocumentMasterDTO>();

            for (int i = 0; i < docMs.length; i++) {

                DocumentMasterDTO docDTO = mapper.map(docMs[i], DocumentMasterDTO.class);
                docDTO.setPath(docMs[i].getLocation().getCompletePath());
                docDTO = Tools.createLightDocumentMasterDTO(docDTO);
                docDTO.setIterationSubscription(documentService.isUserIterationChangeEventSubscribedForGivenDocument(workspaceId, docMs[i]));
                docDTO.setStateSubscription(documentService.isUserStateChangeEventSubscribedForGivenDocument(workspaceId, docMs[i]));
                ACL acl = docMs[i].getACL();
                if(acl != null){
                    docDTO.setAcl(Tools.mapACLtoACLDTO(acl));
                }
                docMsDTOs.add(docDTO);

            }

            return docMsDTOs.toArray(new DocumentMasterDTO[docMsDTOs.size()]);

        } catch (com.docdoku.core.exceptions.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    private DocumentMasterDTO[] getDocumentsWithSearchQuery(String workspaceId, String pStringQuery){
        try{

            DocumentSearchQuery documentSearchQuery = SearchQueryParser.parseDocumentStringQuery(workspaceId, pStringQuery);

            DocumentMaster[] docMs = com.docdoku.core.util.Tools.resetParentReferences(
                    documentService.searchDocumentMasters(documentSearchQuery)
            );

            DocumentMasterDTO[] docMsDTOs = new DocumentMasterDTO[docMs.length];

            for (int i = 0; i < docMs.length; i++) {
                docMsDTOs[i] = mapper.map(docMs[i], DocumentMasterDTO.class);
                docMsDTOs[i].setPath(docMs[i].getLocation().getCompletePath());
                docMsDTOs[i] = Tools.createLightDocumentMasterDTO(docMsDTOs[i]);
                docMsDTOs[i].setIterationSubscription(documentService.isUserIterationChangeEventSubscribedForGivenDocument(workspaceId,docMs[i]));
                docMsDTOs[i].setStateSubscription(documentService.isUserStateChangeEventSubscribedForGivenDocument(workspaceId,docMs[i]));
                ACL acl = docMs[i].getACL();
                if(acl != null){
                    docMsDTOs[i].setAcl(Tools.mapACLtoACLDTO(acl));
                }
            }

            return docMsDTOs;
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

            Map<String, String> roleMappings = new HashMap<String,String>();

            if(rolesMappingDTO != null){
                for(RoleMappingDTO roleMappingDTO : rolesMappingDTO){
                    roleMappings.put(roleMappingDTO.getRoleName(),roleMappingDTO.getUserLogin());
                }
            }
            DocumentMaster createdDocMs =  documentService.createDocumentMaster(decodedCompletePath, pDocMID, pTitle, pDescription, pDocMTemplateId, pWorkflowModelId, userEntries, userGroupEntries, roleMappings);

            DocumentMasterDTO docMsDTO = mapper.map(createdDocMs, DocumentMasterDTO.class);
            docMsDTO.setPath(createdDocMs.getLocation().getCompletePath());
            docMsDTO.setLifeCycleState(createdDocMs.getLifeCycleState());

            return Response.created(URI.create(URLEncoder.encode(pDocMID + "-" + createdDocMs.getVersion(),"UTF-8"))).entity(docMsDTO).build();

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
    public DocumentMasterDTO[] getCheckedOutDocMs(@PathParam("workspaceId") String workspaceId) throws ApplicationException {

        try {
            DocumentMaster[] checkedOutdocMs = documentService.getCheckedOutDocumentMasters(workspaceId);
            DocumentMasterDTO[] checkedOutdocMsDTO = new DocumentMasterDTO[checkedOutdocMs.length];

            for (int i = 0; i < checkedOutdocMs.length; i++) {
                checkedOutdocMsDTO[i] = mapper.map(checkedOutdocMs[i], DocumentMasterDTO.class);
                checkedOutdocMsDTO[i].setPath(checkedOutdocMs[i].getLocation().getCompletePath());
                checkedOutdocMsDTO[i] = Tools.createLightDocumentMasterDTO(checkedOutdocMsDTO[i]);
                checkedOutdocMsDTO[i].setIterationSubscription(documentService.isUserIterationChangeEventSubscribedForGivenDocument(workspaceId,checkedOutdocMs[i]));
                checkedOutdocMsDTO[i].setStateSubscription(documentService.isUserStateChangeEventSubscribedForGivenDocument(workspaceId,checkedOutdocMs[i]));
            }

            return checkedOutdocMsDTO;

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

            DocumentMaster[] docMs = com.docdoku.core.util.Tools.resetParentReferences(
                    documentService.getDocumentMastersWithReference(workspaceId, q, maxResults)
            );

            List<DocumentIterationDTO> docsLastIter = new ArrayList<DocumentIterationDTO>();
            for (int i = 0; i < docMs.length; i++) {
                DocumentIteration docLastIter = docMs[i].getLastIteration();
                if(docLastIter != null)
                    docsLastIter.add(new DocumentIterationDTO(docLastIter.getWorkspaceId(), docLastIter.getDocumentMasterId(), docLastIter.getDocumentMasterVersion(), docLastIter.getIteration()));
            }

            return docsLastIter.toArray(new DocumentIterationDTO[docsLastIter.size()]);

        } catch (com.docdoku.core.exceptions.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

}