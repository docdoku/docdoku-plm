/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,  
 * but WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
 * GNU General Public License for more details.  
 *  
 * You should have received a copy of the GNU General Public License  
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.  
 */
package com.docdoku.server.rest;

import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.document.TagKey;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.server.rest.dto.DocumentMasterDTO;
import com.docdoku.server.rest.dto.TagDTO;
import javax.annotation.PostConstruct;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

/**
 *
 * @author Yassine Belouad
 */
@Stateless
@Path("workspaces/{workspaceId}/tags")
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class TagResource {

    @EJB
    private IDocumentManagerLocal documentService;
    
    private Mapper mapper;

    public TagResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    } 

    @GET
    @Produces("application/json;charset=UTF-8")
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
        } catch (com.docdoku.core.services.ApplicationException ex) {
        
            throw new RestApiException(ex.toString(), ex.getMessage());
        }          
    }
    
    @GET
    @Path("{tagId}/documents/")
    @Produces("application/json;charset=UTF-8")
    public DocumentMasterDTO[] getMasterDocumentsWithSpecifiedTagJson(@PathParam("workspaceId") String workspaceId,@PathParam("tagId") String tagId) {
        try{
        DocumentMaster[] docMs = documentService.findDocumentMastersByTag(new TagKey(workspaceId, tagId));
        DocumentMasterDTO[] docMsDTO = new DocumentMasterDTO[docMs.length];

        for (int i = 0; i < docMs.length; i++) {
            docMsDTO[i] = mapper.map(docMs[i], DocumentMasterDTO.class);
            docMsDTO[i] = Tools.createLightDocumentMasterDTO(docMsDTO[i]); 
        }

        return docMsDTO; 
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }        
    } 
    
    @POST
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    public TagDTO createTag(@PathParam("workspaceId") String workspaceId, TagDTO tag) {
        try {

            documentService.createTag(workspaceId, tag.getLabel());
            return new TagDTO(tag.getLabel());
            
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }
    
    /**
     * DELETE method for deleting an instance of TagResource
     *
     * @param parent folder path
     * @return the array of the documents that have also been deleted
     */
    @DELETE
    @Path("{tagId}")
    @Produces("application/json;charset=UTF-8")
    public Response deleteTag(@PathParam("workspaceId") String workspaceId, @PathParam("tagId") String tagId) {
        try {
            
            documentService.deleteTag(new TagKey(workspaceId, tagId));
            
            return Response.status(Response.Status.OK).build();
            
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }    
}
