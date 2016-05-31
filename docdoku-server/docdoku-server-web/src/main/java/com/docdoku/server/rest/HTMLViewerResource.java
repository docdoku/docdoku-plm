package com.docdoku.server.rest;


import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.exceptions.AccessRightException;
import com.docdoku.core.exceptions.EntityNotFoundException;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.exceptions.UserNotActiveException;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.core.services.IFileViewerManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.server.filters.GuestProxy;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RequestScoped
@Api(value = "viewer", description = "Operations about html viewer")
@DeclareRoles({UserGroupMapping.REGULAR_USER_ROLE_ID,UserGroupMapping.GUEST_PROXY_ROLE_ID})
@RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID,UserGroupMapping.GUEST_PROXY_ROLE_ID})
@Path("viewer")
public class HTMLViewerResource {

    @Inject
    private IFileViewerManagerLocal viewerManager;

    @Inject
    private IProductManagerLocal productManager;

    @Inject
    private IDocumentManagerLocal documentManager;

    @Inject
    private GuestProxy guestProxy;

    public HTMLViewerResource() {
    }

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_HTML)
    @ApiOperation(value = "Get html viewer for document")
    public Response getHtmlViewerForFile(@QueryParam("fileName") final String fileName) throws AccessRightException, NotAllowedException, EntityNotFoundException, UserNotActiveException {

        String holderType = BinaryResource.parseHolderType(fileName);

        if ("documents".equals(holderType)) {
            return getDocumentHTMLViewer(fileName);
        } else if ("parts".equals(holderType)) {
            return getPartHTMLViewer(fileName);
        } else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    private Response getPartHTMLViewer(String fileName) throws NotAllowedException, AccessRightException, UserNotActiveException, EntityNotFoundException {
        BinaryResource binaryResource = guestProxy.getPublicBinaryResourceForPart(fileName);
        if (binaryResource != null) {
            return Response.ok().entity(viewerManager.getHtmlForViewer(binaryResource, null)).build();
        } else {
            binaryResource = productManager.getBinaryResource(fileName);
            return Response.ok().entity(viewerManager.getHtmlForViewer(binaryResource, null)).build();
        }
    }


    public Response getDocumentHTMLViewer(String fileName) throws NotAllowedException, AccessRightException, UserNotActiveException, EntityNotFoundException {
        BinaryResource binaryResource = guestProxy.getPublicBinaryResourceForDocument(fileName);
        if (binaryResource != null) {
            return Response.ok().entity(viewerManager.getHtmlForViewer(binaryResource, null)).build();
        } else {
            binaryResource = documentManager.getBinaryResource(fileName);
            return Response.ok().entity(viewerManager.getHtmlForViewer(binaryResource, null)).build();
        }
    }
}
