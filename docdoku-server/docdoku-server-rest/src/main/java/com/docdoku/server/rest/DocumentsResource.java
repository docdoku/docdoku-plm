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

import com.docdoku.core.configuration.DocumentConfigSpec;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.query.DocumentSearchQuery;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IDocumentConfigSpecManagerLocal;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.core.services.IDocumentWorkflowManagerLocal;
import com.docdoku.server.rest.dto.CountDTO;
import com.docdoku.server.rest.dto.DocumentRevisionDTO;
import com.docdoku.server.rest.util.ConfigSpecHelper;
import com.docdoku.server.rest.util.SearchQueryParser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

import javax.annotation.PostConstruct;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@RequestScoped
@Api(hidden = true, value = "documents", description = "Operations about documents")
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class DocumentsResource {

    private static final Logger LOGGER = Logger.getLogger(DocumentsResource.class.getName());
    @Inject
    private IDocumentManagerLocal documentService;
    @Inject
    private IDocumentConfigSpecManagerLocal documentConfigSpecService;
    @Inject
    private IDocumentWorkflowManagerLocal documentWorkflowService;
    @Inject
    private DocumentBaselinesResource baselinesResource;
    @Inject
    private DocumentResource documentResource;
    private Mapper mapper;

    public DocumentsResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @ApiOperation(value = "SubResource : DocumentResource")
    @Path("{documentId: [^/].*}-{documentVersion:[A-Z]+}")
    public DocumentResource getDocumentResource() {
        return documentResource;
    }

    @GET
    @ApiOperation(value = "Get documents in workspace", response = DocumentRevisionDTO.class, responseContainer = "List")
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO[] getDocumentsInWorkspace(@PathParam("workspaceId") String workspaceId,
                                                         @QueryParam("start") int start,
                                                         @QueryParam("max") int max,
                                                         @QueryParam("configSpec") String configSpecType)
            throws UserNotActiveException, ESServerException, WorkspaceNotFoundException, UserNotFoundException, BaselineNotFoundException, DocumentRevisionNotFoundException {

        int maxResult = max != 0 ? max : 20;

        DocumentRevision[] docRs;
        if (configSpecType == null || ConfigSpecHelper.BASELINE_UNDEFINED.equals(configSpecType) || ConfigSpecHelper.BASELINE_LATEST.equals(configSpecType)) {
            docRs = documentService.getAllDocumentsInWorkspace(workspaceId, start, maxResult);
        } else {
            DocumentConfigSpec configSpec = ConfigSpecHelper.getConfigSpec(workspaceId, configSpecType, documentConfigSpecService);
            docRs = documentConfigSpecService.getFilteredDocuments(workspaceId, configSpec, start, maxResult);
        }
        DocumentRevisionDTO[] docRsDTOs = new DocumentRevisionDTO[docRs.length];

        for (int i = 0; i < docRs.length; i++) {
            docRsDTOs[i] = mapper.map(docRs[i], DocumentRevisionDTO.class);
            docRsDTOs[i].setPath(docRs[i].getLocation().getCompletePath());
            docRsDTOs[i] = Tools.createLightDocumentRevisionDTO(docRsDTOs[i]);
            docRsDTOs[i].setIterationSubscription(documentService.isUserIterationChangeEventSubscribedForGivenDocument(workspaceId, docRs[i]));
            docRsDTOs[i].setStateSubscription(documentService.isUserStateChangeEventSubscribedForGivenDocument(workspaceId, docRs[i]));
        }

        return docRsDTOs;
    }


    @GET
    @ApiOperation(value = "Search documents", response = DocumentRevisionDTO.class, responseContainer = "List")
    @Path("search")
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO[] searchDocumentRevision(@Context UriInfo uri,
                                                        @PathParam("workspaceId") String workspaceId,
                                                        @QueryParam("q") String q) throws EntityNotFoundException, UserNotActiveException, ESServerException {
        MultivaluedMap<String, String> params = uri.getQueryParameters();
        String configSpecType = params.containsKey("configSpec") ? params.get("configSpec").get(0) : null;

        DocumentSearchQuery documentSearchQuery = SearchQueryParser.parseDocumentStringQuery(workspaceId, params);

        DocumentRevision[] docRs;
        if (configSpecType == null || ConfigSpecHelper.BASELINE_UNDEFINED.equals(configSpecType) || ConfigSpecHelper.BASELINE_LATEST.equals(configSpecType)) {
            docRs = documentService.searchDocumentRevisions(documentSearchQuery);
        } else {
            DocumentConfigSpec configSpec = ConfigSpecHelper.getConfigSpec(workspaceId, configSpecType, documentConfigSpecService);
            docRs = documentConfigSpecService.searchFilteredDocuments(workspaceId, configSpec, documentSearchQuery);
        }
        DocumentRevisionDTO[] docRsDTOs = new DocumentRevisionDTO[docRs.length];

        for (int i = 0; i < docRs.length; i++) {
            docRsDTOs[i] = mapper.map(docRs[i], DocumentRevisionDTO.class);
            docRsDTOs[i].setPath(docRs[i].getLocation().getCompletePath());
            docRsDTOs[i] = Tools.createLightDocumentRevisionDTO(docRsDTOs[i]);
            docRsDTOs[i].setIterationSubscription(documentService.isUserIterationChangeEventSubscribedForGivenDocument(workspaceId, docRs[i]));
            docRsDTOs[i].setStateSubscription(documentService.isUserStateChangeEventSubscribedForGivenDocument(workspaceId, docRs[i]));
        }

        return docRsDTOs;
    }

    @GET
    @ApiOperation(value = "Count documents", response = CountDTO.class)
    @Path("count")
    @Produces(MediaType.APPLICATION_JSON)
    public CountDTO getDocumentsInWorkspaceCount(@PathParam("workspaceId") String workspaceId)
            throws EntityNotFoundException, UserNotActiveException {
        return new CountDTO(documentService.getDocumentsInWorkspaceCount(Tools.stripTrailingSlash(workspaceId)));
    }


    @GET
    @ApiOperation(value = "Get checked out documents", response = DocumentRevisionDTO.class, responseContainer = "List")
    @Path("checkedout")
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO[] getCheckedOutDocuments(@PathParam("workspaceId") String workspaceId)
            throws EntityNotFoundException, UserNotActiveException {

        DocumentRevision[] checkedOutDocumentRevisions = documentService.getCheckedOutDocumentRevisions(workspaceId);
        DocumentRevisionDTO[] documentRevisionDTOs = new DocumentRevisionDTO[checkedOutDocumentRevisions.length];

        for (int i = 0; i < checkedOutDocumentRevisions.length; i++) {
            documentRevisionDTOs[i] = mapper.map(checkedOutDocumentRevisions[i], DocumentRevisionDTO.class);
            documentRevisionDTOs[i].setPath(checkedOutDocumentRevisions[i].getLocation().getCompletePath());
            documentRevisionDTOs[i] = Tools.createLightDocumentRevisionDTO(documentRevisionDTOs[i]);
            documentRevisionDTOs[i].setIterationSubscription(documentService.isUserIterationChangeEventSubscribedForGivenDocument(workspaceId, checkedOutDocumentRevisions[i]));
            documentRevisionDTOs[i].setStateSubscription(documentService.isUserStateChangeEventSubscribedForGivenDocument(workspaceId, checkedOutDocumentRevisions[i]));
        }

        return documentRevisionDTOs;
    }

    @GET
    @ApiOperation(value = "Count checked out documents", response = DocumentRevisionDTO.class, responseContainer = "List")
    @Path("countCheckedOut")
    @Produces(MediaType.APPLICATION_JSON)
    public CountDTO countCheckedOutDocs(@PathParam("workspaceId") String workspaceId)
            throws WorkspaceNotFoundException, UserNotActiveException, UserNotFoundException {
        return new CountDTO(documentService.getCheckedOutDocumentRevisions(workspaceId).length);
    }


    @GET
    @ApiOperation(value = "searchDocumentRevisionsToLink : todo doc", response = DocumentRevisionDTO.class, responseContainer = "List")
    @Path("doc_revs")
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO[] searchDocumentRevisionsToLink(@PathParam("workspaceId") String workspaceId,
                                                               @QueryParam("q") String q,
                                                               @QueryParam("l") int limit)
            throws EntityNotFoundException, UserNotActiveException {

        int maxResults = limit == 0 ? 15 : limit;
        DocumentRevision[] docRs = documentService.getDocumentRevisionsWithReferenceOrTitle(workspaceId, q, maxResults);

        List<DocumentRevisionDTO> docRevDTOS = new ArrayList<>();
        for (DocumentRevision docR : docRs) {
            DocumentRevisionDTO docRevDTO = new DocumentRevisionDTO(docR.getWorkspaceId(), docR.getDocumentMasterId(), docR.getTitle(), docR.getVersion());
            docRevDTOS.add(docRevDTO);
        }

        return docRevDTOS.toArray(new DocumentRevisionDTO[docRevDTOS.size()]);
    }

    @ApiOperation(value = "SubResource : DocumentBaselinesResource")
    @Path("baselines")
    public DocumentBaselinesResource getAllBaselines(@PathParam("workspaceId") String workspaceId) {
        return baselinesResource;
    }

}