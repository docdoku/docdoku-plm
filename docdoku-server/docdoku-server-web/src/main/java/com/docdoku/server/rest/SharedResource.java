package com.docdoku.server.rest;

import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.exceptions.*;
import com.docdoku.server.filters.GuestProxy;
import com.docdoku.server.rest.dto.DocumentRevisionDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.security.auth.login.LoginException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RequestScoped
@Api(value = "shared", description = "Operations about shared entities")
@Path("shared")
public class SharedResource {

    @Inject
    private GuestProxy guestProxy;

    private Mapper mapper;

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @Path("{workspaceId}/documents/{documentId}-{documentVersion}")
    @ApiOperation(value = "Get document", response = DocumentRevisionDTO.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPublicSharedDocumentRevision(@PathParam("workspaceId") String workspaceId,
                                                               @PathParam("documentId") String documentId,
                                                               @PathParam("documentVersion") String documentVersion) throws AccessRightException, NotAllowedException, WorkspaceNotFoundException, UserNotFoundException, DocumentRevisionNotFoundException, UserNotActiveException, LoginException {

        DocumentRevision documentRevision = guestProxy.getPublicDocumentRevision(new DocumentRevisionKey(workspaceId, documentId, documentVersion));
        if(documentRevision != null){
            return Response.ok().entity(mapper.map(documentRevision, DocumentRevisionDTO.class)).build();
        }else {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

}
