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
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.ACLUserEntry;
import com.docdoku.core.security.ACLUserGroupEntry;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.server.rest.dto.*;
import com.docdoku.server.rest.exceptions.ApplicationException;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

import javax.annotation.PostConstruct;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.net.URI;
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

    @Path("{docKey}")
    @Produces("application/json;charset=UTF-8")
    public DocumentResource getDocument() {
        return document;
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    public DocumentMasterDTO[] getDocuments(@PathParam("workspaceId") String workspaceId, @PathParam("folderId") String folderId, @PathParam("tagId") String tagId, @PathParam("query") String query) {

        if(query != null){
            return getDocumentsWithSearchQuery(workspaceId, query);
        }
        else if(tagId != null){
            return getDocumentsWithGivenTagIdAndWorkspaceId(workspaceId,tagId);
        }else {
            return getDocumentsWithGivenFolderIdAndWorkspaceId(workspaceId,folderId);
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
                docMsDTOs[i].setIterationSubscription(documentService.isUserIterationChangeEventSubscribedForGivenDocument(workspaceId,docMs[i]));
                docMsDTOs[i].setStateSubscription(documentService.isUserStateChangeEventSubscribedForGivenDocument(workspaceId,docMs[i]));
            }

            return docMsDTOs;
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    private DocumentMasterDTO[] getDocumentsWithGivenTagIdAndWorkspaceId(String workspaceId, String tagId){
        try{
            DocumentMaster[] docMs = documentService.findDocumentMastersByTag(new TagKey(workspaceId, tagId));
            DocumentMasterDTO[] docMsDTOs = new DocumentMasterDTO[docMs.length];

            for (int i = 0; i < docMs.length; i++) {
                docMsDTOs[i] = mapper.map(docMs[i], DocumentMasterDTO.class);
                docMsDTOs[i] = Tools.createLightDocumentMasterDTO(docMsDTOs[i]);
                docMsDTOs[i].setIterationSubscription(documentService.isUserIterationChangeEventSubscribedForGivenDocument(workspaceId,docMs[i]));
                docMsDTOs[i].setStateSubscription(documentService.isUserStateChangeEventSubscribedForGivenDocument(workspaceId,docMs[i]));
            }

            return docMsDTOs;
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    private DocumentMasterDTO[] getDocumentsWithSearchQuery(String workspaceId, String query){
        try{

            String pDocMId = query;
            String pTitle = null;
            String pVersion = null;
            String pAuthor = null;
            String pType = null;
            Date pCreationDateFrom = null;
            Date pCreationDateTo = null;
            SearchQuery.AbstractAttributeQuery[] pAttributes = null;
            String[] pTags = null;
            String pContent = null;

            System.out.println("#### SEARCH : "+query);

            DocumentMaster[] docMs = com.docdoku.core.util.Tools.resetParentReferences(
                documentService.searchDocumentMasters(
                    new SearchQuery(workspaceId, pDocMId, pTitle, pVersion, pAuthor,
                    pType, pCreationDateFrom, pCreationDateTo, pAttributes, pTags, pContent)
                )
            );

            //DocumentMaster[] docMs = documentService.findDocumentMastersByTag(new TagKey(workspaceId, query));
            DocumentMasterDTO[] docMsDTOs = new DocumentMasterDTO[docMs.length];

            for (int i = 0; i < docMs.length; i++) {
                docMsDTOs[i] = mapper.map(docMs[i], DocumentMasterDTO.class);
                docMsDTOs[i] = Tools.createLightDocumentMasterDTO(docMsDTOs[i]);
                docMsDTOs[i].setIterationSubscription(documentService.isUserIterationChangeEventSubscribedForGivenDocument(workspaceId,docMs[i]));
                docMsDTOs[i].setStateSubscription(documentService.isUserStateChangeEventSubscribedForGivenDocument(workspaceId,docMs[i]));
            }

            return docMsDTOs;
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @POST
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    public Response createDocumentMasterInFolder(@PathParam("workspaceId") String workspaceId, DocumentCreationDTO docCreationDTO, @PathParam("folderId") String folderId) {

        String pDocMID = docCreationDTO.getReference();
        String pTitle = docCreationDTO.getTitle();
        String pDescription = docCreationDTO.getDescription();

        String decodedCompletePath = getPathFromUrlParams(workspaceId, folderId);

        String pWorkflowModelId = docCreationDTO.getWorkflowModelId();
        String pDocMTemplateId = docCreationDTO.getTemplateId();

        /* Null value for test purpose only */
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
                    userEntries[i].setPrincipal(new User(new Workspace(workspaceId), entry.getKey()));
                    userEntries[i++].setPermission(ACL.Permission.valueOf(entry.getValue().name()));
                }
                i = 0;
                for (Map.Entry<String, ACLDTO.Permission> entry : acl.getGroupEntries().entrySet()) {
                    userGroupEntries[i] = new ACLUserGroupEntry();
                    userGroupEntries[i].setPrincipal(new UserGroup(new Workspace(workspaceId), entry.getKey()));
                    userGroupEntries[i++].setPermission(ACL.Permission.valueOf(entry.getValue().name()));
                }
            }

            DocumentMaster createdDocMs =  documentService.createDocumentMaster(decodedCompletePath, pDocMID, pTitle, pDescription, pDocMTemplateId, pWorkflowModelId, userEntries, userGroupEntries);
            DocumentMasterDTO docMsDTO = mapper.map(createdDocMs, DocumentMasterDTO.class);
            docMsDTO.setPath(createdDocMs.getLocation().getCompletePath());
            docMsDTO.setLifeCycleState(createdDocMs.getLifeCycleState());

            return Response.created(URI.create(pDocMID + "-" + createdDocMs.getVersion())).entity(docMsDTO).build();

        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }

    }

    private String getPathFromUrlParams(String workspaceId, String folderId) {
        return folderId == null ? Tools.stripTrailingSlash(workspaceId) : Tools.stripTrailingSlash(Tools.replaceColonWithSlash(folderId));
    }

    @GET
    @Path("checkedout")
    @Produces("application/json;charset=UTF-8")
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

        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }


//    @GET
//    @Path()
//    @Produces("application/json;charset=UTF-8")
//    public DocumentMasterDTO[] getIterationChangeEventSubscriptions(@PathParam("workspaceId") String workspaceId) {
//
//        try {
//
//            DocumentMasterKey[] docMKey = documentService.getIterationChangeEventSubscriptions(workspaceId);
//            DocumentMasterDTO[] data = new DocumentMasterDTO[docMKey.length];
//
//            for (int i = 0; i < docMKey.length; i++) {
//                DocumentMasterDTO dto = new DocumentMasterDTO();
//                dto.setWorkspaceID(docMKey[i].getWorkspaceId());
//                dto.setId(docMKey[i].getId());
//                dto.setReference(docMKey[i].getId());
//                dto.setVersion(docMKey[i].getVersion());
//                data[i] = dto;
//            }
//
//            return data;
//
//        } catch (com.docdoku.core.services.ApplicationException ex) {
//            throw new RESTException(ex.toString(), ex.getMessage());
//        }
//
//    }
//
//    @GET
//    @Path()
//    @Produces("application/json;charset=UTF-8")
//    public DocumentMasterDTO[] getStateChangeEventSubscriptions(@PathParam("workspaceId") String workspaceId) {
//
//        try {
//
//            DocumentMasterKey[] docMKey = documentService.getStateChangeEventSubscriptions(workspaceId);
//            DocumentMasterDTO[] data = new DocumentMasterDTO[docMKey.length];
//
//            for (int i = 0; i < docMKey.length; i++) {
//                DocumentMasterDTO dto = new DocumentMasterDTO();
//                dto.setWorkspaceID(docMKey[i].getWorkspaceId());
//                dto.setId(docMKey[i].getId());
//                dto.setReference(docMKey[i].getId());
//                dto.setVersion(docMKey[i].getVersion());
//                data[i] = dto;
//            }
//
//            return data;
//
//        } catch (com.docdoku.core.services.ApplicationException ex) {
//            throw new RESTException(ex.toString(), ex.getMessage());
//        }
//
//    }
//

}
