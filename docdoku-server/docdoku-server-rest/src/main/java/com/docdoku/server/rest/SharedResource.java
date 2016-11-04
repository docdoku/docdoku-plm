package com.docdoku.server.rest;

import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.product.PartRevisionKey;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.*;
import com.docdoku.core.sharing.SharedDocument;
import com.docdoku.core.sharing.SharedEntity;
import com.docdoku.core.sharing.SharedPart;
import com.docdoku.server.rest.dto.DocumentRevisionDTO;
import com.docdoku.server.rest.dto.PartRevisionDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.MessageDigest;
import java.util.Date;

@RequestScoped
@Api(value = "shared", description = "Operations about shared entities")
@Path("shared")
public class SharedResource {

    @Inject
    private IPublicEntityManagerLocal publicEntityManager;

    @Inject
    private IDocumentManagerLocal documentManager;

    @Inject
    private IProductManagerLocal productManager;

    @Inject
    private IShareManagerLocal shareManager;

    @Inject
    private IContextManagerLocal contextManager;


    private Mapper mapper;

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @Path("{workspaceId}/documents/{documentId}-{documentVersion}")
    @ApiOperation(value = "Get document revision", response = DocumentRevisionDTO.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPublicSharedDocumentRevision(@ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
                                                    @ApiParam(required = true, value = "Document master id") @PathParam("documentId") String documentId,
                                                    @ApiParam(required = true, value = "Document version") @PathParam("documentVersion") String documentVersion)
            throws AccessRightException, NotAllowedException, WorkspaceNotFoundException, UserNotFoundException,
            DocumentRevisionNotFoundException, UserNotActiveException, WorkspaceNotEnabledException {

        // Try public shared
        DocumentRevisionKey docKey = new DocumentRevisionKey(workspaceId, documentId, documentVersion);
        DocumentRevision documentRevision = publicEntityManager.getPublicDocumentRevision(docKey);

        // Try authenticated
        if (documentRevision == null && contextManager.isCallerInRole(UserGroupMapping.REGULAR_USER_ROLE_ID)) {
            documentRevision = documentManager.getDocumentRevision(docKey);
        }

        if (documentRevision != null) {
            DocumentRevisionDTO documentRevisionDTO = mapper.map(documentRevision, DocumentRevisionDTO.class);
            documentRevisionDTO.setRoutePath(documentRevision.getLocation().getRoutePath());
            return Response.ok().entity(documentRevisionDTO).build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

    }

    @GET
    @Path("{workspaceId}/parts/{partNumber}-{partVersion}")
    @ApiOperation(value = "Get part revision", response = PartRevisionDTO.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPublicSharedPartRevision(@ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
                                                @ApiParam(required = true, value = "Part number") @PathParam("partNumber") String partNumber,
                                                @ApiParam(required = true, value = "Part version") @PathParam("partVersion") String partVersion) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException, WorkspaceNotEnabledException {

        // Try public shared
        PartRevisionKey partKey = new PartRevisionKey(workspaceId, partNumber, partVersion);
        PartRevision partRevision = publicEntityManager.getPublicPartRevision(partKey);

        // Try authenticated
        if (partRevision == null && contextManager.isCallerInRole(UserGroupMapping.REGULAR_USER_ROLE_ID)) {
            partRevision = productManager.getPartRevision(partKey);
        }

        if (partRevision != null) {
            return Response.ok().entity(Tools.mapPartRevisionToPartDTO(partRevision)).build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

    }

    @GET
    @Path("{uuid}/documents")
    @ApiOperation(value = "Get shared document", response = DocumentRevisionDTO.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDocumentWithSharedEntity(@ApiParam(required = false, value = "Password for resource") @HeaderParam("password") String password,
                                                @ApiParam(required = true, value = "Resource token")  @PathParam("uuid") String uuid) throws SharedEntityNotFoundException {

        SharedEntity sharedEntity = shareManager.findSharedEntityForGivenUUID(uuid);

        // check if expire - delete it - send 404
        if (sharedEntity.getExpireDate() != null && sharedEntity.getExpireDate().getTime() < new Date().getTime()) {
            shareManager.deleteSharedEntityIfExpired(sharedEntity);
            return createExpiredEntityResponse();
        }

        if (!checkPasswordAccess(sharedEntity.getPassword(), password)) {
            return createPasswordProtectedResponse();
        }

        DocumentRevision documentRevision = ((SharedDocument) sharedEntity).getDocumentRevision();
        return Response.ok().entity(mapper.map(documentRevision, DocumentRevisionDTO.class)).build();
    }

    @GET
    @Path("{uuid}/parts")
    @ApiOperation(value = "Get shared part", response = PartRevisionDTO.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPartWithSharedEntity(@ApiParam(required = false, value = "Password for resource") @HeaderParam("password") String password,
                                            @ApiParam(required = true, value = "Resource token") @PathParam("uuid") String uuid) throws SharedEntityNotFoundException {

        SharedEntity sharedEntity = shareManager.findSharedEntityForGivenUUID(uuid);

        // check if expire - delete it - send 404
        if (sharedEntity.getExpireDate() != null && sharedEntity.getExpireDate().getTime() < new Date().getTime()) {
            shareManager.deleteSharedEntityIfExpired(sharedEntity);
            return createExpiredEntityResponse();
        }

        if (!checkPasswordAccess(sharedEntity.getPassword(), password)) {
            return createPasswordProtectedResponse();
        }

        PartRevision partRevision = ((SharedPart) sharedEntity).getPartRevision();
        return Response.ok().entity(Tools.mapPartRevisionToPartDTO(partRevision)).build();

    }

    private boolean checkPasswordAccess(String entityPassword, String password) {
        return entityPassword == null || entityPassword.isEmpty() || entityPassword.equals(md5Sum(password));
    }

    private Response createPasswordProtectedResponse() {
        return Response.status(Response.Status.FORBIDDEN)
                .header("Reason-Phrase", "password-protected")
                .entity("{\"forbidden\":\"password-protected\"}")
                .build();
    }

    private Response createExpiredEntityResponse() {
        return Response.status(Response.Status.NOT_FOUND).header("Reason-Phrase", "entity-expired")
                .header("Reason-Phrase", "entity-expired")
                .entity("{\"forbidden\":\"entity-expired\"}")
                .build();
    }

    private String md5Sum(String pText) {
        byte[] digest;
        try {
            digest = MessageDigest.getInstance("MD5").digest(pText.getBytes());
        } catch (Exception e) {
            return null;
        }
        StringBuffer hexString = new StringBuffer();
        for (byte aDigest : digest) {
            String hex = Integer.toHexString(0xFF & aDigest);
            if (hex.length() == 1) {
                hexString.append("0" + hex);
            } else {
                hexString.append(hex);
            }
        }
        return hexString.toString();
    }

}
