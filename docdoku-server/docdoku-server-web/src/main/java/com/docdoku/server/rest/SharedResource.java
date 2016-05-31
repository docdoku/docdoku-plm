package com.docdoku.server.rest;

import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.product.PartRevisionKey;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.core.services.IShareManagerLocal;
import com.docdoku.core.sharing.SharedDocument;
import com.docdoku.core.sharing.SharedEntity;
import com.docdoku.core.sharing.SharedPart;
import com.docdoku.server.filters.GuestProxy;
import com.docdoku.server.rest.dto.DocumentRevisionDTO;
import com.docdoku.server.rest.dto.PartRevisionDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;

@RequestScoped
@Api(value = "shared", description = "Operations about shared entities")
@Path("shared")
public class SharedResource {

    @Inject
    private GuestProxy guestProxy;

    @Inject
    private IDocumentManagerLocal documentManager;

    @Inject
    private IProductManagerLocal productManager;

    @Inject
    private IShareManagerLocal shareManager;

    private Mapper mapper;

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @Path("{workspaceId}/documents/{documentId}-{documentVersion}")
    @ApiOperation(value = "Get document revision", response = DocumentRevisionDTO.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPublicSharedDocumentRevision(@PathParam("workspaceId") String workspaceId,
                                                               @PathParam("documentId") String documentId,
                                                               @PathParam("documentVersion") String documentVersion) throws AccessRightException, NotAllowedException, WorkspaceNotFoundException, UserNotFoundException, DocumentRevisionNotFoundException, UserNotActiveException {

        // Tries public
        DocumentRevisionKey docKey = new DocumentRevisionKey(workspaceId, documentId, documentVersion);
        DocumentRevision documentRevision = guestProxy.getPublicDocumentRevision(docKey);

        if(documentRevision == null) {
            // Ties authenticated
            documentRevision = documentManager.getDocumentRevision(docKey);
        }

        if(documentRevision != null){
            return Response.ok().entity(mapper.map(documentRevision, DocumentRevisionDTO.class)).build();
        }else{
            return Response.status(Response.Status.FORBIDDEN).build();
        }

    }

    @GET
    @Path("{workspaceId}/parts/{partNumber}-{partVersion}")
    @ApiOperation(value = "Get part revision", response = PartRevisionDTO.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPublicSharedPartRevision(@PathParam("workspaceId") String workspaceId,
                                                    @PathParam("partNumber") String partNumber,
                                                    @PathParam("partVersion") String partVersion) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException {

        // Tries public
        PartRevisionKey partKey = new PartRevisionKey(workspaceId, partNumber, partVersion);
        PartRevision partRevision = guestProxy.getPublicPartRevision(partKey);

        if(partRevision == null) {
            // Ties authenticated
            partRevision = productManager.getPartRevision(partKey);
        }

        if(partRevision != null){
            return Response.ok().entity(Tools.mapPartRevisionToPartDTO(partRevision)).build();
        }else{
            return Response.status(Response.Status.FORBIDDEN).build();
        }

    }

    @GET
    @Path("{uuid}/documents")
    @ApiOperation(value = "Get shared part", response = PartRevisionDTO.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDocumentWithSharedEntity(@PathParam("uuid") String uuid) throws SharedEntityNotFoundException {

        SharedEntity sharedEntity = shareManager.findSharedEntityForGivenUUID(uuid);

        // check if expire - delete it - send 404
        if(sharedEntity.getExpireDate() != null && sharedEntity.getExpireDate().getTime() < new Date().getTime()){
            shareManager.deleteSharedEntityIfExpired(sharedEntity);
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if(sharedEntity.getPassword() != null && !sharedEntity.getPassword().isEmpty()){
            return Response.status(Response.Status.FORBIDDEN).header("Reason-Phrase","password-protected").build();
        }

        DocumentRevision documentRevision = ((SharedDocument) sharedEntity).getDocumentRevision();
        return Response.ok().entity(mapper.map(documentRevision, DocumentRevisionDTO.class)).build();

    }

    @GET
    @Path("{uuid}/parts")
    @ApiOperation(value = "Get shared part", response = PartRevisionDTO.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPartWithSharedEntity(@PathParam("uuid") String uuid) throws SharedEntityNotFoundException {

        SharedEntity sharedEntity = shareManager.findSharedEntityForGivenUUID(uuid);

        // check if expire - delete it - send 404
        if(sharedEntity.getExpireDate() != null && sharedEntity.getExpireDate().getTime() < new Date().getTime()){
            shareManager.deleteSharedEntityIfExpired(sharedEntity);
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if(sharedEntity.getPassword() != null && !sharedEntity.getPassword().isEmpty()){
            return Response.status(Response.Status.FORBIDDEN).header("Reason-Phrase","protected").build();
        }

        PartRevision partRevision = ((SharedPart) sharedEntity).getPartRevision();
        return Response.ok().entity(Tools.mapPartRevisionToPartDTO(partRevision)).build();

    }
}
