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


import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.product.PartRevisionKey;
import com.docdoku.core.services.*;
import com.docdoku.core.sharing.SharedDocument;
import com.docdoku.core.sharing.SharedEntity;
import com.docdoku.core.sharing.SharedPart;
import com.docdoku.server.rest.exceptions.ExpiredLinkException;
import com.docdoku.server.rest.exceptions.UnmatchingUuidException;
import io.swagger.annotations.*;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;

@RequestScoped
@Api(value = "viewer", description = "Operations about html viewer")
@Path("viewer")
public class HTMLViewerResource {

    @Inject
    private IFileViewerManagerLocal viewerManager;

    @Inject
    private IProductManagerLocal productManager;

    @Inject
    private IDocumentManagerLocal documentManager;

    @Inject
    private IShareManagerLocal shareManager;

    @Inject
    private IPublicEntityManagerLocal publicEntityManager;

    public HTMLViewerResource() {
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @ApiOperation(value = "Get html viewer for document",
            response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of Viewer"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    public Response getHtmlViewerForFile(
            @ApiParam(required = false, value = "Uuid of shared entity") @QueryParam("uuid") final String uuid,
            @ApiParam(required = true, value = "File name") @QueryParam("fileName") final String fileName)
            throws AccessRightException, NotAllowedException, EntityNotFoundException, UserNotActiveException,
            ExpiredLinkException, UnmatchingUuidException {

        if (uuid != null && !uuid.isEmpty()) {
            SharedEntity sharedEntity = shareManager.findSharedEntityForGivenUUID(uuid);
            BinaryResource sharedResource = checkUuidValidity(sharedEntity, fileName);
            return Response.ok().entity(viewerManager.getHtmlForViewer(sharedResource, uuid)).build();
        }

        String holderType = BinaryResource.parseHolderType(fileName);
        if ("documents".equals(holderType)) {
            return getDocumentHTMLViewer(fileName);
        } else if ("parts".equals(holderType)) {
            return getPartHTMLViewer(fileName);
        } else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }


    private Response getPartHTMLViewer(String fileName) throws NotAllowedException, AccessRightException,
            UserNotActiveException, EntityNotFoundException, ExpiredLinkException, UnmatchingUuidException {

        BinaryResource binaryResource = publicEntityManager.getPublicBinaryResourceForPart(fileName);
        if (binaryResource != null) {
            return Response.ok().entity(viewerManager.getHtmlForViewer(binaryResource, null)).build();
        } else {
            binaryResource = productManager.getBinaryResource(fileName);
            return Response.ok().entity(viewerManager.getHtmlForViewer(binaryResource, null)).build();
        }
    }

    public Response getDocumentHTMLViewer(String fileName) throws NotAllowedException, AccessRightException,
            UserNotActiveException, EntityNotFoundException {

        BinaryResource binaryResource = publicEntityManager.getPublicBinaryResourceForDocument(fileName);
        if (binaryResource != null) {
            return Response.ok().entity(viewerManager.getHtmlForViewer(binaryResource, null)).build();
        } else {
            binaryResource = documentManager.getBinaryResource(fileName);
            return Response.ok().entity(viewerManager.getHtmlForViewer(binaryResource, null)).build();
        }
    }


    private BinaryResource checkUuidValidity(SharedEntity sharedEntity, String fileName)
            throws UnmatchingUuidException, NotAllowedException, WorkspaceNotFoundException, AccessRightException,
            FileNotFoundException, UserNotFoundException, UserNotActiveException,
            ExpiredLinkException, WorkspaceNotEnabledException {

        // Compare types
        String holderType = BinaryResource.parseHolderType(fileName);
        if ("parts".equals(holderType) && !(sharedEntity instanceof SharedPart) ||
                "documents".equals(holderType) && !(sharedEntity instanceof SharedDocument)) {
            throw new UnmatchingUuidException();
        }

        checkUuidExpiredDate(sharedEntity);

        String workspaceId = BinaryResource.parseWorkspaceId(fileName);
        String holderId = BinaryResource.parseHolderId(fileName);
        String holderRevision = BinaryResource.parseHolderRevision(fileName);
        Integer holderIteration = BinaryResource.parseHolderIteration(fileName);

        if ("parts".equals(holderType)){
            PartRevisionKey partRPK = new PartRevisionKey(workspaceId, holderId, holderRevision);
            PartRevision partRevision = ((SharedPart) sharedEntity).getPartRevision();
            PartIteration lastCheckedInIteration = partRevision.getLastCheckedInIteration();
            if(partRevision.getKey().equals(partRPK) && (null != lastCheckedInIteration && lastCheckedInIteration.getIteration() <= holderIteration)){
                return publicEntityManager.getBinaryResourceForSharedPart(fileName);
            }else{
                throw new UnmatchingUuidException();
            }
        }
        if ("documents".equals(holderType)){
            DocumentRevisionKey docRPK = new DocumentRevisionKey(workspaceId, holderId, holderRevision);
            DocumentRevision documentRevision = ((SharedDocument) sharedEntity).getDocumentRevision();
            DocumentIteration lastCheckedInIteration = documentRevision.getLastCheckedInIteration();
            if(documentRevision.getKey().equals(docRPK) && (null != lastCheckedInIteration && lastCheckedInIteration.getIteration() <= holderIteration)){
                return publicEntityManager.getBinaryResourceForSharedDocument(fileName);
            }else{
                throw new UnmatchingUuidException();
            }
        }
        throw new UnmatchingUuidException();
    }

    private void checkUuidExpiredDate(SharedEntity sharedEntity) throws ExpiredLinkException {
        // Check shared entity expired
        if (sharedEntity.getExpireDate() != null && sharedEntity.getExpireDate().getTime() < new Date().getTime()) {
            shareManager.deleteSharedEntityIfExpired(sharedEntity);
            throw new ExpiredLinkException();
        }
    }

}
