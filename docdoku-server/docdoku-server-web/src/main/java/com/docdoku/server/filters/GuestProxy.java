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
    public InputStream getConvertedResource(String outputFormat, BinaryResource binaryResource) throws Exception {
        return documentResourceGetterService.getConvertedResource(outputFormat, binaryResource);
    }

}
