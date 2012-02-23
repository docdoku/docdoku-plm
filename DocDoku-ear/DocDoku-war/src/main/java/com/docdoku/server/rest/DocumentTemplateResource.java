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

import com.docdoku.core.document.DocumentMasterTemplate;
import com.docdoku.core.document.DocumentMasterTemplateKey;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.ICommandLocal;
import com.docdoku.server.rest.dto.DocumentMasterTemplateDTO;
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
@Path("workspaces/{workspaceId}/templates")
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class DocumentTemplateResource {

    @EJB
    private ICommandLocal commandService;
    private Mapper mapper;

    public DocumentTemplateResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    public DocumentMasterTemplateDTO[] getDocumentMasterTemplates(@PathParam("workspaceId") String workspaceId) {
        try {

            DocumentMasterTemplate[] docMsTemplates = commandService.getDocumentMasterTemplates(workspaceId);
            DocumentMasterTemplateDTO[] dtos = new DocumentMasterTemplateDTO[docMsTemplates.length];

            for (int i = 0; i < docMsTemplates.length; i++) {
                dtos[i] = mapper.map(docMsTemplates[i], DocumentMasterTemplateDTO.class);
            }

            return dtos;

        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RESTException(ex.toString(), ex.getMessage());
        }
    }
    
    @GET
    @Path("{templateId}")
    @Produces("application/json;charset=UTF-8")
    public DocumentMasterTemplateDTO getDocumentMasterTemplates(@PathParam("workspaceId") String workspaceId, @PathParam("templateId") String templateId) {
        try {

            DocumentMasterTemplate docMsTemplate = commandService.getDocumentMasterTemplate(new DocumentMasterTemplateKey(workspaceId,templateId));
            DocumentMasterTemplateDTO dto = new DocumentMasterTemplateDTO();
            dto = mapper.map(docMsTemplate, DocumentMasterTemplateDTO.class);

            return dto;

        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RESTException(ex.toString(), ex.getMessage());
        }
    }    
}