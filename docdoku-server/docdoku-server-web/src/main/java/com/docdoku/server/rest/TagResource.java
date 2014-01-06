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

import com.docdoku.core.document.TagKey;
import com.docdoku.core.exceptions.ApplicationException;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.server.rest.dto.TagDTO;
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

/**
 *
 * @author Yassine Belouad
 */
@Stateless
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class TagResource {

    @EJB
    private IDocumentManagerLocal documentService;

    @EJB
    private DocumentsResource documentsResource;

    @EJB
    private DocumentResource documentResource;

    private Mapper mapper;

    public TagResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    } 

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public TagDTO[] getTagsInWorkspace (@PathParam("workspaceId") String workspaceId){
        
        try{    
        
            String[] tagsName = documentService.getTags(workspaceId);
            TagDTO[] tagDtos = new TagDTO[tagsName.length];
            for (int i = 0; i < tagsName.length; i++) {                
                tagDtos[i] = new TagDTO();
                tagDtos[i].setWorkspaceId(workspaceId);
                tagDtos[i].setLabel(tagsName[i]);
            }            
            
            return tagDtos;
        } catch (ApplicationException ex) {
        
            throw new RestApiException(ex.toString(), ex.getMessage());
        }          
    }

    @Path("{tagId}/documents/")
    public DocumentsResource getDocumentsResource() {
        return documentsResource;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TagDTO createTag(@PathParam("workspaceId") String workspaceId, TagDTO tag) {
        try {

            documentService.createTag(workspaceId, tag.getLabel());
            return new TagDTO(tag.getLabel());
            
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @POST
    @Path("/multiple")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createTags(@PathParam("workspaceId") String workspaceId, TagDTO[] tagsDTO) {
        try {

            for(TagDTO tagDTO : tagsDTO){
                documentService.createTag(workspaceId, tagDTO.getLabel());
            }

            return Response.ok().build();

        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @DELETE
    @Path("{tagId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteTag(@PathParam("workspaceId") String workspaceId, @PathParam("tagId") String tagId) {
        try {
            documentService.deleteTag(new TagKey(workspaceId, tagId));
            return Response.status(Response.Status.OK).build();
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

}
