/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2014 DocDoku SARL
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

import com.docdoku.core.exceptions.EntityNotFoundException;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IShareManagerLocal;
import com.docdoku.core.sharing.SharedDocument;
import com.docdoku.core.sharing.SharedEntity;
import com.docdoku.core.sharing.SharedPart;
import com.docdoku.server.rest.exceptions.ExpiredLinkException;
import com.docdoku.server.rest.file.DocumentBinaryResource;
import com.docdoku.server.rest.file.DocumentTemplateBinaryResource;
import com.docdoku.server.rest.file.PartBinaryResource;
import com.docdoku.server.rest.file.PartTemplateBinaryResource;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.Date;

@Stateless
@Path("files")
@DeclareRoles({UserGroupMapping.REGULAR_USER_ROLE_ID,UserGroupMapping.GUEST_PROXY_ROLE_ID})
@RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID,UserGroupMapping.GUEST_PROXY_ROLE_ID})
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
    private IShareManagerLocal shareService;

    public FileResource() {
    }

    @Path("/{workspaceId}/documents/{documentId}/{version}")
    public DocumentBinaryResource downloadDocumentFile(){
        return documentBinaryResource;
    }

    @Path("/{workspaceId}/parts/{partNumber}/{version}")
    public PartBinaryResource downloadPartFile(){
        return partBinaryResource;
    }

    @Path("/{workspaceId}/document-templates/{templateId}/")
    public DocumentTemplateBinaryResource downloadDocumentTemplateFile(){
        return documentTemplateBinaryResource;
    }

    @Path("/{workspaceId}/part-templates/{templateId}/")
    public PartTemplateBinaryResource downloadPartTemplateFile(){
        return partTemplateBinaryResource;
    }

    @Path("/{uuid}")
    public Object downloadShareFile(@PathParam("uuid") final String uuid)
            throws EntityNotFoundException, ExpiredLinkException {

        // Get shared entity$
        SharedEntity sharedEntity = shareService.findSharedEntityForGivenUUID(uuid);

        // Check shared entity access

        // Check shared entity expired
        if(sharedEntity.getExpireDate() != null && sharedEntity.getExpireDate().getTime() < new Date().getTime()){
            shareService.deleteSharedEntityIfExpired(sharedEntity);
            throw new ExpiredLinkException();
        }

        if(sharedEntity instanceof SharedDocument){
            return documentBinaryResource;
        }else if(sharedEntity instanceof SharedPart){
            return partBinaryResource;
        }else{
            return null;
        }
    }
}