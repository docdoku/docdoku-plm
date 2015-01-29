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

import com.docdoku.core.exceptions.*;
import com.docdoku.core.meta.TagKey;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.server.rest.dto.TagDTO;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

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

    public TagResource() {
    }

    @Path("{tagId}/documents/")
    public DocumentsResource getDocumentsResource() {
        return documentsResource;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<TagDTO> getTagsInWorkspace (@PathParam("workspaceId") String workspaceId)
            throws EntityNotFoundException, UserNotActiveException {

        String[] tagsName = documentService.getTags(workspaceId);
        List<TagDTO> tagsDTO = new ArrayList<>();
        for (String tagName : tagsName) {
            tagsDTO.add(new TagDTO(tagName,workspaceId));
        }
        return tagsDTO;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TagDTO createTag(@PathParam("workspaceId") String workspaceId, TagDTO tag)
            throws EntityNotFoundException, EntityAlreadyExistsException, UserNotActiveException, AccessRightException, CreationException {

        documentService.createTag(workspaceId, tag.getLabel());
        return new TagDTO(tag.getLabel());
    }

    @POST
    @Path("/multiple")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createTags(@PathParam("workspaceId") String workspaceId, List<TagDTO> tagsDTO)
            throws EntityNotFoundException, EntityAlreadyExistsException, UserNotActiveException, AccessRightException, CreationException {

        for(TagDTO tagDTO : tagsDTO){
            documentService.createTag(workspaceId, tagDTO.getLabel());
        }
        return Response.ok().build();
    }

    @DELETE
    @Path("{tagId}")
    public Response deleteTag(@PathParam("workspaceId") String workspaceId, @PathParam("tagId") String tagId)
            throws EntityNotFoundException, AccessRightException {

        documentService.deleteTag(new TagKey(workspaceId, tagId));
        return Response.ok().build();
    }
}