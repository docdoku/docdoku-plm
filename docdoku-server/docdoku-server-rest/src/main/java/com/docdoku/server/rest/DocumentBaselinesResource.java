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

import com.docdoku.core.configuration.DocumentBaseline;
import com.docdoku.core.configuration.DocumentCollection;
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IDocumentBaselineManagerLocal;
import com.docdoku.server.rest.dto.baseline.BaselinedDocumentDTO;
import com.docdoku.server.rest.dto.baseline.DocumentBaselineDTO;
import com.docdoku.server.rest.util.FileDownloadTools;
import com.docdoku.server.rest.util.FileExportDocumentEntity;
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
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Taylor LABEJOF
 */

@RequestScoped
@Api(hidden = true, value = "document-baseline", description = "Operations about document baselines")
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class DocumentBaselinesResource {

    private static final Logger LOGGER = Logger.getLogger(DocumentBaselinesResource.class.getName());

    @Inject
    private IDocumentBaselineManagerLocal documentBaselineService;

    private Mapper mapper;

    public DocumentBaselinesResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    /**
     * Get all document baselines of a specific workspace
     *
     * @param workspaceId The id of the specific workspace
     * @return The list of baselines
     */
    @GET
    @ApiOperation(value = "Get baselines",
            response = DocumentBaselineDTO.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of checked out DocumentBaselineDTOs. It can be an empty list."),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDocumentBaselines(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId)
            throws EntityNotFoundException, UserNotActiveException {
        List<DocumentBaseline> documentBaselines = documentBaselineService.getBaselines(workspaceId);
        List<DocumentBaselineDTO> baselinesDTO = new ArrayList<>();
        for (DocumentBaseline documentBaseline : documentBaselines) {
            DocumentBaselineDTO documentBaselineDTO = mapper.map(documentBaseline, DocumentBaselineDTO.class);
            baselinesDTO.add(documentBaselineDTO);
        }
        return Response.ok(new GenericEntity<List<DocumentBaselineDTO>>((List<DocumentBaselineDTO>) baselinesDTO) {
        }).build();
    }

    /**
     * Create a baseline
     *
     * @param workspaceId         The current workspace
     * @param documentBaselineDTO Description of the baseline to create
     * @return Reponse of the transaction
     */
    @POST
    @ApiOperation(value = "Create baseline",
            response = DocumentBaselineDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of created DocumentBaselineDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createDocumentBaseline(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Document baseline to create") DocumentBaselineDTO documentBaselineDTO)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException {

        List<BaselinedDocumentDTO> baselinedDocumentsDTO = documentBaselineDTO.getBaselinedDocuments();
        List<DocumentRevisionKey> documentRevisionKeys = new ArrayList<>();

        for (BaselinedDocumentDTO document : baselinedDocumentsDTO) {
            documentRevisionKeys.add(new DocumentRevisionKey(workspaceId, document.getDocumentMasterId(), document.getVersion()));
        }

        DocumentBaseline baseline = documentBaselineService.createBaseline(workspaceId, documentBaselineDTO.getName(), documentBaselineDTO.getType(), documentBaselineDTO.getDescription(), documentRevisionKeys);
        return prepareCreateResponse(getBaseline(workspaceId, baseline.getId()));
    }


    /**
     * Delete a specific document baseline
     *
     * @param workspaceId The workspace of the specific baseline
     * @param baselineId  The id of the specific document baseline
     * @return A response if the baseline was deleted
     */
    @DELETE
    @ApiOperation(value = "Delete a baseline",
            response = Response.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful deletion of DocumentBaselineDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{baselineId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteBaseline(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Baseline id") @PathParam("baselineId") int baselineId)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException {

        documentBaselineService.deleteBaseline(workspaceId, baselineId);
        return Response.ok().build();
    }

    /**
     * Get a specific document baseline ( with documents list )
     *
     * @param workspaceId The workspace of the specific baseline
     * @param baselineId  The id of the specific document baseline
     * @return The specif baseline
     */
    @GET
    @ApiOperation(value = "Get baseline",
            response = DocumentBaselineDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of DocumentBaselineDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{baselineId}")
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentBaselineDTO getBaseline(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Baseline id") @PathParam("baselineId") int baselineId)
            throws EntityNotFoundException, UserNotActiveException {

        DocumentBaseline documentBaseline = documentBaselineService.getBaselineLight(workspaceId, baselineId);
        DocumentCollection documentCollection = documentBaselineService.getACLFilteredDocumentCollection(workspaceId, baselineId);
        List<BaselinedDocumentDTO> baselinedDocumentDTOs = Tools.mapBaselinedDocumentsToBaselinedDocumentDTOs(documentCollection);
        DocumentBaselineDTO baselineDTO = mapper.map(documentBaseline, DocumentBaselineDTO.class);
        baselineDTO.setBaselinedDocuments(baselinedDocumentDTOs);
        return baselineDTO;
    }

    /**
     * Get a specific document baseline
     *
     * @param workspaceId The workspace of the specific baseline
     * @param baselineId  The id of the specific document baseline
     * @return The specif baseline
     */
    @GET
    @ApiOperation(value = "Get document baseline in a light format",
            response = DocumentBaselineDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of DocumentBaselineDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{baselineId}-light")
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentBaselineDTO getBaselineLight(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Baseline id") @PathParam("baselineId") int baselineId)
            throws EntityNotFoundException, UserNotActiveException {
        DocumentBaseline documentBaseline = documentBaselineService.getBaselineLight(workspaceId, baselineId);
        return mapper.map(documentBaseline, DocumentBaselineDTO.class);
    }

    // TODO : test this class in API. This method will trigger a file download, generated API should be able to retrieve it.
    @GET
    @ApiOperation(value = "Export files",
            response = Response.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful export, trigger a download as download file"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{baselineId}/export-files")
    public Response exportFiles(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Baseline id") @PathParam("baselineId") int baselineId)
            throws BaselineNotFoundException, WorkspaceNotFoundException, UserNotActiveException, UserNotFoundException, WorkspaceNotEnabledException {

        FileExportDocumentEntity fileExportEntity = new FileExportDocumentEntity(workspaceId, baselineId);

        DocumentBaseline documentBaseline = documentBaselineService.getBaselineLight(workspaceId, baselineId);
        String fileName = FileDownloadTools.getFileName(documentBaseline.getName() + "-export", "zip");
        String contentDisposition = FileDownloadTools.getContentDisposition("attachment", fileName);

        return Response.ok()
                .header("Content-Type", "application/download")
                .header("Content-Disposition", contentDisposition)
                .entity(fileExportEntity).build();
    }


    /**
     * Try to put a document baseline in a response
     *
     * @param baselineDTO The document baseline to add
     * @return The reponse with the document baseline
     */
    private Response prepareCreateResponse(DocumentBaselineDTO baselineDTO) {
        try {
            return Response.created(URI.create(URLEncoder.encode(String.valueOf(baselineDTO.getId()), "UTF-8"))).entity(baselineDTO).build();
        } catch (UnsupportedEncodingException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            return Response.ok().build();
        }
    }
}