package com.docdoku.server.rest;


import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.core.services.IFileViewerManagerLocal;
import com.docdoku.server.filters.GuestProxy;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

import javax.annotation.PostConstruct;
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
    private IDocumentManagerLocal documentManager;

    @Inject
    private GuestProxy guestProxy;

    private Mapper mapper;

    public HTMLViewerResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_HTML)
    @ApiOperation(value = "Get html viewer for document")
    public Response getHtmlViewerForFile(@QueryParam("fileName") final String fileName) throws AccessRightException, NotAllowedException, EntityNotFoundException, UserNotActiveException {

        String holderType = BinaryResource.parseHolderType(fileName);

        if("documents".equals(holderType)){

            BinaryResource binaryResource = guestProxy.getBinaryResourceForDocument(fileName);
            DocumentRevisionKey documentRevisionKey = new DocumentRevisionKey(binaryResource.getWorkspaceId(), binaryResource.getHolderId(), binaryResource.getHolderRevision());

            DocumentRevision documentRevision = guestProxy.getPublicDocumentRevision(documentRevisionKey);
            if(documentRevision != null){
                return Response.ok().entity(viewerManager.getHtmlForViewer(binaryResource,null)).build();
            }
            else{
                documentManager.getDocumentRevision(documentRevisionKey);
                return Response.ok().entity(viewerManager.getHtmlForViewer(binaryResource, null)).build();
            }

        } else if("parts".equals(holderType)){
            return Response.status(Response.Status.ACCEPTED).build();
        }
        else{
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

}
