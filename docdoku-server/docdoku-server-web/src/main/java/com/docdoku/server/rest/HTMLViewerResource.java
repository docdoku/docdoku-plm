package com.docdoku.server.rest;


import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.core.services.IFileViewerManagerLocal;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

import javax.annotation.PostConstruct;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@RequestScoped
@Api(value = "viewer", description = "Operations about html viewer")
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
@Path("viewer")
public class HTMLViewerResource {


    @Inject
    private IFileViewerManagerLocal viewerManager;

    @Inject
    private IDocumentManagerLocal documentManager;

    private Mapper mapper;

    public HTMLViewerResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @Path("document")
    @Produces(MediaType.TEXT_HTML)
    @ApiOperation(value = "Get html viewer for document")
    public String getHtmlViewerForFile(@QueryParam("fileName") final String fileName) throws AccessRightException, NotAllowedException, WorkspaceNotFoundException, UserNotFoundException, FileNotFoundException, UserNotActiveException {
        BinaryResource binaryResource = documentManager.getBinaryResource(fileName);
        return viewerManager.getHtmlForViewer(binaryResource,null);
    }


}
