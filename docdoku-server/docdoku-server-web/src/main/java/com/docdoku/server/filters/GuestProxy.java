package com.docdoku.server.filters;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.common.User;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.product.PartRevisionKey;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RunAs;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.security.auth.login.LoginException;

@DeclareRoles("guest-proxy")
@RunAs("guest-proxy")
@LocalBean
@Stateless
public class GuestProxy{

    @EJB
    private IProductManagerLocal productService;

    @EJB
    private IDocumentManagerLocal documentService;

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

    public BinaryResource getPublicBinaryResourceForDocument(DocumentRevisionKey docRK, String fullName) throws AccessRightException, NotAllowedException, WorkspaceNotFoundException, UserNotFoundException, FileNotFoundException, UserNotActiveException, LoginException, DocumentRevisionNotFoundException {
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
}
