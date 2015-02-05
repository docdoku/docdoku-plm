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
import com.docdoku.core.exceptions.AccessRightException;
import com.docdoku.core.exceptions.EntityNotFoundException;
import com.docdoku.core.exceptions.UserNotActiveException;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IDocumentBaselineManagerLocal;
import com.docdoku.server.rest.dto.FolderDTO;
import com.docdoku.server.rest.dto.baseline.DocumentBaselineDTO;
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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Taylor LABEJOF
 */
@Stateless
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class DocumentBaselinesResource {

    @EJB
    private IDocumentBaselineManagerLocal documentBaselineService;

    private static final Logger LOGGER = Logger.getLogger(DocumentBaselinesResource.class.getName());
    private Mapper mapper;

    public DocumentBaselinesResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }


    /**
     * Get all document baselines of a specific workspace
     * @param workspaceId The id of the specific workspace
     * @return The list of baselines
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<DocumentBaselineDTO> getBaselines(@PathParam("workspaceId") String workspaceId)
            throws EntityNotFoundException, UserNotActiveException{
        List<DocumentBaseline> documentBaselines;
        documentBaselines = documentBaselineService.getBaselines(workspaceId);
        List<DocumentBaselineDTO> baselinesDTO = new ArrayList<>();
        for(DocumentBaseline documentBaseline : documentBaselines){
            DocumentBaselineDTO documentBaselineDTO = mapper.map(documentBaseline,DocumentBaselineDTO.class);
            documentBaselineDTO.setWorkspaceId(workspaceId);
            baselinesDTO.add(documentBaselineDTO);
        }
        return baselinesDTO;
    }

    /**
     * Create a baseline
     * @param workspaceId The current workspace
     * @param documentBaselineDTO Description of the baseline to create
     * @return Reponse of the transaction
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createBaseline(@PathParam("workspaceId") String workspaceId, DocumentBaselineDTO documentBaselineDTO)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException{
        DocumentBaseline baseline = documentBaselineService.createBaseline(workspaceId,documentBaselineDTO.getName(),documentBaselineDTO.getDescription());
        DocumentBaselineDTO baselineDTO= mapper.map(baseline,DocumentBaselineDTO.class);
        return prepareCreateResponse(baselineDTO);
    }

    /**
     * Try to put a document baseline in a response
     * @param baselineDTO The document baseline to add
     * @return The reponse with the document baseline
     */
    private Response prepareCreateResponse(DocumentBaselineDTO baselineDTO) {
        try {
            return Response.created(URI.create(URLEncoder.encode(String.valueOf(baselineDTO.getId()), "UTF-8"))).entity(baselineDTO).build();
        } catch (UnsupportedEncodingException ex) {
            LOGGER.log(Level.WARNING,null,ex);
            return Response.ok().build();
        }
    }

    /**
     * Delete a specific document baseline
     * @param baselineId The id of the specific document baseline
     * @return A response if the baseline was deleted
     */
    @DELETE
    @Path("{baselineId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteBaseline(@PathParam("baselineId") int baselineId)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException{
        documentBaselineService.deleteBaseline(baselineId);
        return Response.ok().build();
    }

    /**
     * Get a specific document baseline ( with document list and folder list )
     * @param workspaceId The workspace of the specific baseline
     * @param baselineId The id of the specific document baseline
     * @return The specif baseline
     */
    @GET
    @Path("{baselineId}")
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentBaselineDTO getBaseline(@PathParam("workspaceId") String workspaceId, @PathParam("baselineId") int baselineId)
            throws EntityNotFoundException, UserNotActiveException{
        DocumentBaseline documentBaseline = documentBaselineService.getBaseline(baselineId);
        DocumentBaselineDTO baselineDTO = getBaselineLight(workspaceId,baselineId);
        List<FolderDTO> folderDTOs = Tools.mapBaselinedFoldersToFolderDTO(documentBaseline);
        baselineDTO.setBaselinedFolders(folderDTOs);
        return baselineDTO;
    }

    /**
     * Get a specific document baseline
     * @param workspaceId The workspace of the specific baseline
     * @param baselineId The id of the specific document baseline
     * @return The specif baseline
     */
    @GET
    @Path("{baselineId}-light")
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentBaselineDTO getBaselineLight(@PathParam("workspaceId") String workspaceId, @PathParam("baselineId") int baselineId)
            throws EntityNotFoundException, UserNotActiveException{
        DocumentBaseline documentBaseline = documentBaselineService.getBaseline(baselineId);
        return mapper.map(documentBaseline,DocumentBaselineDTO.class);
    }
}