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
import com.docdoku.core.services.ICommandLocal;
import com.docdoku.server.rest.dto.DocumentMasterDTO;
import javax.annotation.PostConstruct;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
    private ICommandLocal commandService;
    
    private Mapper mapper;

    public TagResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    } 
    
    
    @GET
    @Path("{tagId}/documents/")
    @Produces("application/json;charset=UTF-8")
    public DocumentMasterDTO[] getMasterDocumentsWithSpecifiedTagJson(@PathParam("workspaceId") String workspaceId,@PathParam("tagId") String tagId) {
        try{
        DocumentMaster[] docMs = commandService.findDocumentMastersByTag(new TagKey(workspaceId, tagId));
        DocumentMasterDTO[] docMsDTO = new DocumentMasterDTO[docMs.length];

        for (int i = 0; i < docMs.length; i++) {
            docMsDTO[i] = mapper.map(docMs[i], DocumentMasterDTO.class);
            docMsDTO[i] = Tools.createLightDocumentMasterDTO(docMsDTO[i]); 
        }

        return docMsDTO; 
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RESTException(ex.toString(), ex.getMessage());
        }        
    } 
    
    
}
