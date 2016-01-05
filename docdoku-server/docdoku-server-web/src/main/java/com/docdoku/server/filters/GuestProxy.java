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

package com.docdoku.server.filters;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.common.User;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentIterationKey;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.product.PartRevisionKey;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.core.services.IDocumentResourceGetterManagerLocal;
import com.docdoku.core.services.IProductInstanceManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RunAs;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.security.auth.login.LoginException;
import java.io.InputStream;

@DeclareRoles(UserGroupMapping.GUEST_PROXY_ROLE_ID)
@RunAs(UserGroupMapping.GUEST_PROXY_ROLE_ID)
@LocalBean
@Stateless
public class GuestProxy{
    @EJB
    private IProductManagerLocal productService;
    @EJB
    private IProductInstanceManagerLocal productInstanceManagerLocal;
    @EJB
    private IDocumentManagerLocal documentService;
    @EJB
    private IDocumentResourceGetterManagerLocal documentResourceGetterService;


    public PartRevision getPublicPartRevision(PartRevisionKey partRevisionKey) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, LoginException, AccessRightException {

        PartRevision partRevision = productService.getPartRevision(partRevisionKey);
        if(partRevision.isPublicShared()){
            return partRevision;
        }else{
            throw new LoginException();
        }
    }

    public DocumentRevision getPublicDocumentRevision(DocumentRevisionKey documentRevisionKey) throws NotAllowedException, WorkspaceNotFoundException, UserNotFoundException, DocumentRevisionNotFoundException, UserNotActiveException, AccessRightException, LoginException {

        DocumentRevision documentRevision =  documentService.getDocumentRevision(documentRevisionKey);
        if(documentRevision.isPublicShared()){
            return documentRevision;
        }else{
            throw new LoginException();
        }
    }

    public BinaryResource getPublicBinaryResourceForDocument(DocumentRevisionKey docRK, String fullName)
            throws AccessRightException, NotAllowedException, EntityNotFoundException, UserNotActiveException, LoginException{
        getPublicDocumentRevision(docRK);
        return documentService.getBinaryResource(fullName);
    }

    public BinaryResource getPublicBinaryResourceForPart(PartRevisionKey partK, String fullName) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, LoginException, FileNotFoundException, NotAllowedException, AccessRightException {
        getPublicPartRevision(partK);
        return productService.getBinaryResource(fullName);
    }

    public DocumentIteration findDocumentIterationByBinaryResource(BinaryResource binaryResource) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        return documentService.findDocumentIterationByBinaryResource(binaryResource);
    }

    public User whoAmI() {
       return new User("en");
    }

    public BinaryResource getBinaryResourceForSharedDocument(String fullName) throws AccessRightException, NotAllowedException, WorkspaceNotFoundException, UserNotFoundException, FileNotFoundException, UserNotActiveException {
        return documentService.getBinaryResource(fullName);
    }

    public BinaryResource getBinaryResourceForSharedPart(String fullName) throws AccessRightException, NotAllowedException, WorkspaceNotFoundException, UserNotFoundException, FileNotFoundException, UserNotActiveException {
        return productService.getBinaryResource(fullName);
    }



    public boolean canAccess(DocumentIterationKey docIKey)
            throws EntityNotFoundException, UserNotActiveException{
        return documentService.canAccess(docIKey);
    }
    public boolean canAccess(PartIterationKey partIKey)
            throws EntityNotFoundException, UserNotActiveException{
        return productService.canAccess(partIKey);
    }
    public BinaryResource getBinaryResourceForDocument(String fullName)
            throws AccessRightException, NotAllowedException, EntityNotFoundException, UserNotActiveException{
        return documentService.getBinaryResource(fullName);
    }
    public BinaryResource getBinaryResourceForPart(String fullName)
            throws AccessRightException, NotAllowedException, EntityNotFoundException, UserNotActiveException{
        return productService.getBinaryResource(fullName);
    }


    public InputStream getDocumentConvertedResource(String outputFormat, BinaryResource binaryResource) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, ConvertedResourceException {
        return documentResourceGetterService.getDocumentConvertedResource(outputFormat, binaryResource);
    }

    public InputStream getPartConvertedResource(String outputFormat, BinaryResource binaryResource) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, ConvertedResourceException {
        return documentResourceGetterService.getPartConvertedResource(outputFormat, binaryResource);
    }

    public BinaryResource getBinaryResourceForProducInstance(String fullName) throws UserNotActiveException, WorkspaceNotFoundException, UserNotFoundException, FileNotFoundException, NotAllowedException, AccessRightException {
        return productInstanceManagerLocal.getBinaryResource(fullName);
    }

    public BinaryResource getBinaryResourceForPathData(String fullName) throws UserNotActiveException, WorkspaceNotFoundException, UserNotFoundException, FileNotFoundException, NotAllowedException, AccessRightException {
        return productInstanceManagerLocal.getPathDataBinaryResource(fullName);
    }
}
