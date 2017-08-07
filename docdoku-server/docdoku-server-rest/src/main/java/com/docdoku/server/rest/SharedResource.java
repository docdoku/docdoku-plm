/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
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
import com.docdoku.core.util.HashUtils;
import com.docdoku.server.auth.AuthConfig;
import com.docdoku.server.auth.jwt.JWTokenFactory;
import com.docdoku.server.rest.dto.DocumentRevisionDTO;
import com.docdoku.server.rest.dto.PartRevisionDTO;
import io.swagger.annotations.*;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

@RequestScoped
@Api(value = "shared", description = "Operations about shared entities")
@Path("shared")
public class SharedResource {

    private IPublicEntityManagerLocal publicEntityManager;
    private IDocumentManagerLocal documentManager;
    private IProductManagerLocal productManager;
    private IShareManagerLocal shareManager;
    private IContextManagerLocal contextManager;
    private AuthConfig authConfig;

    private Mapper mapper;

    @Inject
    public SharedResource(IPublicEntityManagerLocal publicEntityManager, IDocumentManagerLocal documentManager, IProductManagerLocal productManager, IShareManagerLocal shareManager, IContextManagerLocal contextManager, AuthConfig authConfig) {
        this.publicEntityManager = publicEntityManager;
        this.documentManager = documentManager;
        this.productManager = productManager;
        this.shareManager = shareManager;
        this.contextManager = contextManager;
        this.authConfig = authConfig;
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @Path("{workspaceId}/documents/{documentId}-{documentVersion}")
    @ApiOperation(value = "Get document revision",
            response = DocumentRevisionDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of DocumentRevisionDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPublicSharedDocumentRevision(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
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
            String entityToken = JWTokenFactory.createEntityToken(authConfig.getJWTKey(), documentRevision.getKey().toString());
            return Response.ok().header("entity-token", entityToken).entity(documentRevisionDTO).build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

    }

    @GET
    @Path("{workspaceId}/parts/{partNumber}-{partVersion}")
    @ApiOperation(value = "Get part revision",
            response = PartRevisionDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of DocumentRevisionDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPublicSharedPartRevision(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Part number") @PathParam("partNumber") String partNumber,
            @ApiParam(required = true, value = "Part version") @PathParam("partVersion") String partVersion)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException, WorkspaceNotEnabledException {

        // Try public shared
        PartRevisionKey partKey = new PartRevisionKey(workspaceId, partNumber, partVersion);
        PartRevision partRevision = publicEntityManager.getPublicPartRevision(partKey);

        // Try authenticated
        if (partRevision == null && contextManager.isCallerInRole(UserGroupMapping.REGULAR_USER_ROLE_ID)) {
            partRevision = productManager.getPartRevision(partKey);
        }

        if (partRevision != null) {
            String entityToken = JWTokenFactory.createEntityToken(authConfig.getJWTKey(), partRevision.getKey().toString());
            return Response.ok().header("entity-token", entityToken).entity(Tools.mapPartRevisionToPartDTO(partRevision)).build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

    }

    @GET
    @Path("{uuid}/documents")
    @ApiOperation(value = "Get shared document",
            response = DocumentRevisionDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of DocumentRevisionDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDocumentWithSharedEntity(
            @ApiParam(required = false, value = "Password for resource") @HeaderParam("password") String password,
            @ApiParam(required = true, value = "Resource token") @PathParam("uuid") String uuid)
            throws SharedEntityNotFoundException {

        SharedEntity sharedEntity = shareManager.findSharedEntityForGivenUUID(uuid);

        // check if expire - delete it - send 404
        if (sharedEntity.getExpireDate() != null && sharedEntity.getExpireDate().getTime() < new Date().getTime()) {
            shareManager.deleteSharedEntityIfExpired(sharedEntity);
            return createExpiredEntityResponse();
        }

        if (!checkPasswordAccess(sharedEntity.getPassword(), password)) {
            return createPasswordProtectedResponse();
        }

        String sharedEntityToken = JWTokenFactory.createSharedEntityToken(authConfig.getJWTKey(), sharedEntity);
        DocumentRevision documentRevision = ((SharedDocument) sharedEntity).getDocumentRevision();
        return Response.ok().header("shared-entity-token", sharedEntityToken).entity(mapper.map(documentRevision, DocumentRevisionDTO.class)).build();
    }

    @GET
    @Path("{uuid}/parts")
    @ApiOperation(value = "Get shared part",
            response = PartRevisionDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of PartRevisionDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPartWithSharedEntity(
            @ApiParam(required = false, value = "Password for resource") @HeaderParam("password") String password,
            @ApiParam(required = true, value = "Resource token") @PathParam("uuid") String uuid)
            throws SharedEntityNotFoundException {

        SharedEntity sharedEntity = shareManager.findSharedEntityForGivenUUID(uuid);

        // check if expire - delete it - send 404
        if (sharedEntity.getExpireDate() != null && sharedEntity.getExpireDate().getTime() < new Date().getTime()) {
            shareManager.deleteSharedEntityIfExpired(sharedEntity);
            return createExpiredEntityResponse();
        }

        if (!checkPasswordAccess(sharedEntity.getPassword(), password)) {
            return createPasswordProtectedResponse();
        }

        String sharedEntityToken = JWTokenFactory.createSharedEntityToken(authConfig.getJWTKey(), sharedEntity);
        PartRevision partRevision = ((SharedPart) sharedEntity).getPartRevision();
        return Response.ok().header("shared-entity-token", sharedEntityToken).entity(Tools.mapPartRevisionToPartDTO(partRevision)).build();

    }

    private boolean checkPasswordAccess(String entityPassword, String password) {
        if (entityPassword == null || entityPassword.isEmpty()) {
            return true;
        }
        try {
            return password != null && entityPassword.equals(HashUtils.md5Sum(password));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            return false;
        }
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

}
