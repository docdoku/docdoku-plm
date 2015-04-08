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

import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.server.rest.file.*;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Path;

@Stateless
@Path("files")
@DeclareRoles({UserGroupMapping.REGULAR_USER_ROLE_ID,UserGroupMapping.GUEST_PROXY_ROLE_ID})
public class FileResource {
    @EJB
    private DocumentBinaryResource documentBinaryResource;
    @EJB
    private PartBinaryResource partBinaryResource;
    @EJB
    private DocumentTemplateBinaryResource documentTemplateBinaryResource;
    @EJB
    private PartTemplateBinaryResource partTemplateBinaryResource;
    @EJB
    private ProductInstanceBinaryResource productInstanceBinaryResource;

    public FileResource() {
    }

    @Path("/{workspaceId}/documents/{documentId}/{version}")
    public DocumentBinaryResource documentFile(){
        return documentBinaryResource;
    }

    @Path("/{workspaceId}/parts/{partNumber}/{version}")
    public PartBinaryResource partFile(){
        return partBinaryResource;
    }

    @Path("/{workspaceId}/document-templates/{templateId}/")
    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    public DocumentTemplateBinaryResource documentTemplateFile(){
        return documentTemplateBinaryResource;
    }

    @Path("/{workspaceId}/part-templates/{templateId}/")
    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    public PartTemplateBinaryResource partTemplateFile(){
        return partTemplateBinaryResource;
    }

    @Path("/{workspaceId}/product-instances/{serialNumber}/{ciId}/")
    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    public ProductInstanceBinaryResource productInstanceFile(){
        return productInstanceBinaryResource;
    }

}