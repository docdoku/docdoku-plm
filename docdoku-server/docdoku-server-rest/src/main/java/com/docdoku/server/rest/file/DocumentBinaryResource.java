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
package com.docdoku.server.rest.file;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentIterationKey;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.*;
import com.docdoku.core.sharing.SharedDocument;
import com.docdoku.core.sharing.SharedEntity;
import com.docdoku.server.helpers.Streams;
import com.docdoku.server.rest.exceptions.*;
import com.docdoku.server.rest.file.util.BinaryResourceDownloadMeta;
import com.docdoku.server.rest.file.util.BinaryResourceDownloadResponseBuilder;
import com.docdoku.server.rest.file.util.BinaryResourceUpload;
import com.docdoku.server.rest.interceptors.Compress;
import io.swagger.annotations.*;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.Normalizer;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

@RequestScoped
@Api(hidden = true, value = "documentBinary", description = "Operations about document files")
@DeclareRoles({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.GUEST_ROLE_ID})
@RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.GUEST_ROLE_ID})
public class DocumentBinaryResource {

    private static final Logger LOGGER = Logger.getLogger(DocumentBinaryResource.class.getName());

    @Inject
    private IBinaryStorageManagerLocal storageManager;
    @Inject
    private IDocumentManagerLocal documentService;
    @Inject
    private IContextManagerLocal contextManager;
    @Inject
    private IOnDemandConverterManagerLocal onDemandConverterManager;
    @Inject
    private IShareManagerLocal shareService;
    @Inject
    private IPublicEntityManagerLocal publicEntityManager;

    public DocumentBinaryResource() {
    }

    @POST
    @ApiOperation(value = "Upload document file",
            response = Response.class)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Upload success"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "upload", paramType = "formData", dataType = "file", required = true)
    })
    @Path("/{iteration}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    public Response uploadDocumentFiles(
            @Context HttpServletRequest request,
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") final String workspaceId,
            @ApiParam(required = true, value = "Document master id") @PathParam("documentId") final String documentId,
            @ApiParam(required = true, value = "Workspace version") @PathParam("version") final String version,
            @ApiParam(required = true, value = "Document iteration") @PathParam("iteration") final int iteration)
            throws EntityNotFoundException, EntityAlreadyExistsException, UserNotActiveException, AccessRightException,
            NotAllowedException, CreationException {

        try {
            String fileName = null;
            DocumentIterationKey docPK = new DocumentIterationKey(workspaceId, documentId, version, iteration);
            Collection<Part> formParts = request.getParts();

            for (Part formPart : formParts) {
                fileName = uploadAFile(formPart, docPK);
            }

            if (formParts.size() == 1) {
                // todo prevent NPE for filename
                return BinaryResourceUpload.tryToRespondCreated(request.getRequestURI() + URLEncoder.encode(fileName, "UTF-8"));
            }
            return Response.noContent().build();

        } catch (IOException | ServletException | StorageException e) {
            return BinaryResourceUpload.uploadError(e);
        }
    }

    // TODO use uuid as QueryParam
    @GET
    @ApiOperation(value = "Download document file",
            response = File.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Download success"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("/{iteration}/{fileName}{uuid:(/uuid/[^/]+?)?}")
    @Compress
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadDocumentFile(
            @Context Request request,
            @ApiParam(required = false, value = "Range") @HeaderParam("Range") String range,
            @ApiParam(required = false, value = "referer") @HeaderParam("Referer") String referer,
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") final String workspaceId,
            @ApiParam(required = true, value = "Document master id") @PathParam("documentId") final String documentId,
            @ApiParam(required = true, value = "Workspace version") @PathParam("version") final String version,
            @ApiParam(required = true, value = "Document iteration") @PathParam("iteration") final int iteration,
            @ApiParam(required = true, value = "File name") @PathParam("fileName") final String fileName,
            @ApiParam(required = false, value = "Type") @QueryParam("type") String type,
            @ApiParam(required = false, value = "Output") @QueryParam("output") String output,
            @ApiParam(required = false, value = "Resource token") @PathParam("uuid") final String pUuid)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException,
            NotModifiedException, PreconditionFailedException, RequestedRangeNotSatisfiableException,
            UnmatchingUuidException, ExpiredLinkException {

        String fullName;
        if (pUuid != null && !pUuid.isEmpty()) {
            String uuid = pUuid.split("/")[2];
            SharedEntity sharedEntity = shareService.findSharedEntityForGivenUUID(uuid);

            // Check uuid & access right
            checkUuidValidity(sharedEntity, workspaceId, documentId, version, iteration, referer);

            DocumentRevision documentRevision = ((SharedDocument) sharedEntity).getDocumentRevision();
            fullName = sharedEntity.getWorkspace().getId() +
                    "/documents/" +
                    documentRevision.getId() + "/" +
                    documentRevision.getVersion() + "/" +
                    iteration + "/" + fileName;

        } else {
            // Check access right
            DocumentIterationKey docIK = new DocumentIterationKey(workspaceId, documentId, version, iteration);
            if (!canAccess(docIK)) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            fullName = workspaceId + "/documents/" + documentId + "/" + version + "/" + iteration + "/" + fileName;
        }

        BinaryResource binaryResource = getBinaryResource(fullName);
        BinaryResourceDownloadMeta binaryResourceDownloadMeta = new BinaryResourceDownloadMeta(binaryResource, output, type);

        // Check cache precondition
        Response.ResponseBuilder rb = request.evaluatePreconditions(binaryResourceDownloadMeta.getLastModified(), binaryResourceDownloadMeta.getETag());
        if (rb != null) {
            return rb.build();
        }

        InputStream binaryContentInputStream = null;

        try {

            if (output != null && !output.isEmpty()) {
                binaryContentInputStream = getConvertedBinaryResource(binaryResource, output);
                if(range == null || range.isEmpty()) {
                    binaryResourceDownloadMeta.setLength(0);
                }
            } else {
                binaryContentInputStream = storageManager.getBinaryResourceInputStream(binaryResource);
            }

            return BinaryResourceDownloadResponseBuilder.prepareResponse(binaryContentInputStream, binaryResourceDownloadMeta, range);

        } catch (StorageException | FileConversionException e) {
            Streams.close(binaryContentInputStream);
            return BinaryResourceDownloadResponseBuilder.downloadError(e, fullName);
        }

    }

    private String uploadAFile(Part formPart, DocumentIterationKey docPK)
            throws EntityNotFoundException, EntityAlreadyExistsException, AccessRightException, NotAllowedException, CreationException, UserNotActiveException, StorageException, IOException {

        String fileName = Normalizer.normalize(formPart.getSubmittedFileName(), Normalizer.Form.NFC);
        // Init the binary resource with a null length
        BinaryResource binaryResource = documentService.saveFileInDocument(docPK, fileName, 0);
        OutputStream outputStream = storageManager.getBinaryResourceOutputStream(binaryResource);
        long length = BinaryResourceUpload.uploadBinary(outputStream, formPart);
        documentService.saveFileInDocument(docPK, fileName, length);
        return fileName;
    }

    /**
     * Try to convert a binary resource to a specific format
     *
     * @param binaryResource The binary resource
     * @param outputFormat   The wanted output
     * @return The binary resource stream in the wanted output
     * @throws com.docdoku.server.rest.exceptions.FileConversionException
     */
    private InputStream getConvertedBinaryResource(BinaryResource binaryResource, String outputFormat) throws FileConversionException {
        try {
            if (contextManager.isCallerInRole(UserGroupMapping.REGULAR_USER_ROLE_ID)) {
                return onDemandConverterManager.getDocumentConvertedResource(outputFormat, binaryResource);
            } else {
                return publicEntityManager.getDocumentConvertedResource(outputFormat, binaryResource);
            }
        } catch (Exception e) {
            throw new FileConversionException(e);
        }
    }

    private boolean canAccess(DocumentIterationKey docIKey) throws AccessRightException, NotAllowedException, WorkspaceNotFoundException, UserNotFoundException, DocumentRevisionNotFoundException, UserNotActiveException, WorkspaceNotEnabledException {
        DocumentRevision publicDocumentRevision = publicEntityManager.getPublicDocumentRevision(docIKey.getDocumentRevision());
        return publicDocumentRevision != null || contextManager.isCallerInRole(UserGroupMapping.REGULAR_USER_ROLE_ID) && documentService.canAccess(docIKey);

    }

    private BinaryResource getBinaryResource(String fullName)
            throws NotAllowedException, AccessRightException, UserNotActiveException, EntityNotFoundException {
        BinaryResource binaryResource = publicEntityManager.getPublicBinaryResourceForDocument(fullName);
        if (binaryResource != null) {
            return binaryResource;
        } else {
            return documentService.getBinaryResource(fullName);
        }
    }

    private void checkUuidValidity(SharedEntity sharedEntity, String workspaceId, String documentId, String version, int iteration, String referer)
            throws UnmatchingUuidException, ExpiredLinkException, NotAllowedException {
        if (!(sharedEntity instanceof SharedDocument)) {
            throw new UnmatchingUuidException();
        }

        checkUuidReferer(sharedEntity, referer);
        checkUuidExpiredDate(sharedEntity);

        String shareEntityWorkspaceId = sharedEntity.getWorkspace().getId();
        DocumentRevision documentRevision = ((SharedDocument) sharedEntity).getDocumentRevision();
        DocumentIteration lastCheckedInIteration = documentRevision.getLastCheckedInIteration();
        if (!shareEntityWorkspaceId.equals(workspaceId) ||
                !documentRevision.getDocumentMasterId().equals(documentId) ||
                !documentRevision.getVersion().equals(version) ||
                (null != lastCheckedInIteration && lastCheckedInIteration.getIteration() < iteration)) {
            throw new UnmatchingUuidException();
        }
    }

    private void checkUuidExpiredDate(SharedEntity sharedEntity) throws ExpiredLinkException {
        // Check shared entity expired
        if (sharedEntity.getExpireDate() != null && sharedEntity.getExpireDate().getTime() < new Date().getTime()) {
            shareService.deleteSharedEntityIfExpired(sharedEntity);
            throw new ExpiredLinkException();
        }
    }

    private void checkUuidReferer(SharedEntity sharedEntity, String referer) throws NotAllowedException {
        if (referer == null || referer.isEmpty()) {
            throw new NotAllowedException(Locale.getDefault(), "NotAllowedException18");
        }

        String refererPath[] = referer.split("/");
        String refererUUID = refererPath[refererPath.length - 1];
        if (sharedEntity.getPassword() != null && !sharedEntity.getUuid().equals(refererUUID)) {
            throw new NotAllowedException(Locale.getDefault(), "NotAllowedException18");
        }
    }
}