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
import com.docdoku.core.exceptions.*;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.product.PartRevisionKey;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.*;
import com.docdoku.core.sharing.SharedEntity;
import com.docdoku.core.sharing.SharedPart;
import com.docdoku.core.util.FileIO;
import com.docdoku.core.util.HashUtils;
import com.docdoku.server.auth.AuthConfig;
import com.docdoku.server.auth.jwt.JWTokenFactory;
import com.docdoku.server.rest.exceptions.*;
import com.docdoku.server.rest.file.util.BinaryResourceDownloadMeta;
import com.docdoku.server.rest.file.util.BinaryResourceDownloadResponseBuilder;
import com.docdoku.server.rest.file.util.BinaryResourceUpload;
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
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

@RequestScoped
@Api(hidden = true, value = "partBinary", description = "Operations about part files")
@DeclareRoles({UserGroupMapping.REGULAR_USER_ROLE_ID})
public class PartBinaryResource {

    private static final Logger LOGGER = Logger.getLogger(PartBinaryResource.class.getName());
    public static final String NATIVE_CAD_SUBTYPE = "nativecad";
    public static final String ATTACHED_FILES_SUBTYPE = "attachedfiles";
    private static final String UTF8_ENCODING = "UTF-8";


    private IBinaryStorageManagerLocal storageManager;
    private IProductManagerLocal productService;
    private IContextManagerLocal contextManager;
    private IConverterManagerLocal converterService;
    private IShareManagerLocal shareService;
    private IPublicEntityManagerLocal publicEntityManager;
    private IOnDemandConverterManagerLocal onDemandConverterManager;
    private AuthConfig authConfig;

    @Inject
    public PartBinaryResource(IBinaryStorageManagerLocal storageManager, IProductManagerLocal productService, IContextManagerLocal contextManager, IConverterManagerLocal converterService, IShareManagerLocal shareService, IPublicEntityManagerLocal publicEntityManager, IOnDemandConverterManagerLocal onDemandConverterManager, AuthConfig authConfig) {
        this.storageManager = storageManager;
        this.productService = productService;
        this.contextManager = contextManager;
        this.converterService = converterService;
        this.shareService = shareService;
        this.publicEntityManager = publicEntityManager;
        this.onDemandConverterManager = onDemandConverterManager;
        this.authConfig = authConfig;
    }

    @POST
    @ApiOperation(value = "Upload CAD file",
            response = Response.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "upload", paramType = "formData", dataType = "file", required = true)
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Upload success"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("/{iteration}/" + NATIVE_CAD_SUBTYPE)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    public Response uploadNativeCADFile(
            @Context HttpServletRequest request,
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") final String workspaceId,
            @ApiParam(required = true, value = "Part number") @PathParam("partNumber") final String partNumber,
            @ApiParam(required = true, value = "Part version") @PathParam("version") final String version,
            @ApiParam(required = true, value = "Part iteration") @PathParam("iteration") final int iteration)
            throws EntityNotFoundException, EntityAlreadyExistsException, UserNotActiveException,
            AccessRightException, NotAllowedException, CreationException {

        try {

            PartIterationKey partPK = new PartIterationKey(workspaceId, partNumber, version, iteration);
            Collection<Part> parts = request.getParts();

            if (parts.isEmpty() || parts.size() > 1) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            Part part = parts.iterator().next();
            String fileName = part.getSubmittedFileName();
            BinaryResource binaryResource = productService.saveNativeCADInPartIteration(partPK, fileName, 0);
            OutputStream outputStream = storageManager.getBinaryResourceOutputStream(binaryResource);
            long length = BinaryResourceUpload.uploadBinary(outputStream, part);
            productService.saveNativeCADInPartIteration(partPK, fileName, length);
            converterService.convertCADFileToOBJ(partPK, binaryResource);

            return BinaryResourceUpload.tryToRespondCreated(request.getRequestURI() + URLEncoder.encode(fileName, UTF8_ENCODING));

        } catch (IOException | ServletException | StorageException e) {
            return BinaryResourceUpload.uploadError(e);
        }
    }

    @POST
    @ApiOperation(value = "Upload attached file",
            response = Response.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "upload", paramType = "formData", dataType = "file", required = true)
    })
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Upload success"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("/{iteration}/" + ATTACHED_FILES_SUBTYPE)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    public Response uploadAttachedFiles(
            @Context HttpServletRequest request,
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") final String workspaceId,
            @ApiParam(required = true, value = "Part number") @PathParam("partNumber") final String partNumber,
            @ApiParam(required = true, value = "Part version") @PathParam("version") final String version,
            @ApiParam(required = true, value = "Part iteration") @PathParam("iteration") final int iteration)
            throws EntityNotFoundException, EntityAlreadyExistsException, UserNotActiveException, AccessRightException,
            NotAllowedException, CreationException {

        try {

            PartIterationKey partPK = new PartIterationKey(workspaceId, partNumber, version, iteration);
            Collection<Part> formParts = request.getParts();

            String fileName = null;

            for (Part formPart : formParts) {
                fileName = Normalizer.normalize(formPart.getSubmittedFileName(), Normalizer.Form.NFC);
                BinaryResource binaryResource = productService.saveFileInPartIteration(partPK, fileName, ATTACHED_FILES_SUBTYPE, 0);
                OutputStream outputStream = storageManager.getBinaryResourceOutputStream(binaryResource);
                long length = BinaryResourceUpload.uploadBinary(outputStream, formPart);
                productService.saveFileInPartIteration(partPK, fileName, ATTACHED_FILES_SUBTYPE, length);
            }

            if (formParts.size() == 1) {
                return BinaryResourceUpload.tryToRespondCreated(request.getRequestURI() + URLEncoder.encode(fileName, UTF8_ENCODING));
            }

            return Response.noContent().build();

        } catch (IOException | ServletException | StorageException e) {
            return BinaryResourceUpload.uploadError(e);
        }
    }

    @GET
    @ApiOperation(value = "Download part file",
            response = File.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Download success"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("/{iteration}/{fileName}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadDirectPartFile(
            @Context Request request,
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") final String workspaceId,
            @ApiParam(required = true, value = "Part number") @PathParam("partNumber") final String partNumber,
            @ApiParam(required = true, value = "Part version") @PathParam("version") final String version,
            @ApiParam(required = true, value = "Part iteration") @PathParam("iteration") final int iteration,
            @ApiParam(required = true, value = "File name") @PathParam("fileName") final String fileName,
            @ApiParam(required = false, value = "Type") @QueryParam("type") String type,
            @ApiParam(required = false, value = "Output") @QueryParam("output") String output,
            @ApiParam(required = false, value = "Range") @HeaderParam("Range") String range,
            @ApiParam(required = false, value = "Shared entity uuid") @QueryParam("uuid") final String uuid,
            @ApiParam(required = false, value = "Password for private resource") @HeaderParam("password") String password,
            @ApiParam(required = false, value = "Shared entity token") @QueryParam("token") String accessToken)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException,
            PreconditionFailedException, NotModifiedException, RequestedRangeNotSatisfiableException,
            UnMatchingUuidException, SharedResourceAccessException {
        return downloadPartFile(request, workspaceId, partNumber, version, iteration, null, fileName, type, output, range, uuid, password, accessToken);
    }

    @GET
    @ApiOperation(value = "Download part file",
            response = File.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Download success"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("/{iteration}/{subType}/{fileName}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadPartFile(
            @Context Request request,
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") final String workspaceId,
            @ApiParam(required = true, value = "Part number") @PathParam("partNumber") final String partNumber,
            @ApiParam(required = true, value = "Part version") @PathParam("version") final String version,
            @ApiParam(required = true, value = "Part iteration") @PathParam("iteration") final int iteration,
            @ApiParam(required = true, value = "File sub type") @PathParam("subType") String subType,
            @ApiParam(required = true, value = "File name") @PathParam("fileName") final String fileName,
            @ApiParam(required = false, value = "Type") @QueryParam("type") String type,
            @ApiParam(required = false, value = "Output") @QueryParam("output") String output,
            @ApiParam(required = false, value = "Range") @HeaderParam("Range") String range,
            @ApiParam(required = false, value = "Shared entity uuid") @QueryParam("uuid") final String uuid,
            @ApiParam(required = false, value = "Password for private resource") @HeaderParam("password") String password,
            @ApiParam(required = false, value = "Shared entity token") @QueryParam("token") String accessToken)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException,
            PreconditionFailedException, NotModifiedException, RequestedRangeNotSatisfiableException,
            UnMatchingUuidException, SharedResourceAccessException {

        BinaryResource binaryResource;
        String decodedFileName = fileName;
        InputStream binaryContentInputStream;

        try {
            decodedFileName = URLDecoder.decode(fileName, UTF8_ENCODING);
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE, "Cannot decode filename", e);
        }

        String fullName = workspaceId + "/parts/" + FileIO.encode(partNumber) + "/" + version + "/" + iteration + "/";

        if (subType != null && !subType.isEmpty()) {
            fullName = fullName + subType + "/";
        }

        fullName = fullName + decodedFileName;

        if (uuid != null && !uuid.isEmpty()) {

            SharedEntity sharedEntity = shareService.findSharedEntityForGivenUUID(uuid);

            if (accessToken != null && !accessToken.isEmpty()) {
                String decodedUUID = JWTokenFactory.validateSharedResourceToken(authConfig.getJWTKey(), accessToken);
                if (null == decodedUUID || !decodedUUID.equals(sharedEntity.getUuid())) {
                    throw new SharedResourceAccessException();
                }
            } else {
                // Check uuid & access right
                checkUuidValidity(sharedEntity, workspaceId, partNumber, version, iteration, password);
            }

            binaryResource = publicEntityManager.getBinaryResourceForSharedEntity(fullName);

        } else {
            // Check access right

            if (accessToken != null && !accessToken.isEmpty()) {
                String decodedEntityKey = JWTokenFactory.validateEntityToken(authConfig.getJWTKey(), accessToken);
                boolean tokenValid = new PartRevisionKey(workspaceId, partNumber, version).toString().equals(decodedEntityKey);
                if (!tokenValid) {
                    throw new SharedResourceAccessException();
                }
                binaryResource = publicEntityManager.getBinaryResourceForSharedEntity(fullName);
            } else {
                if (!canAccess(new PartIterationKey(workspaceId, partNumber, version, iteration))) {
                    throw new SharedResourceAccessException();
                }
                binaryResource = getBinaryResource(fullName);
            }
        }

        BinaryResourceDownloadMeta binaryResourceDownloadMeta = new BinaryResourceDownloadMeta(binaryResource, output, type);

        // Check cache precondition
        Response.ResponseBuilder rb = request.evaluatePreconditions(binaryResourceDownloadMeta.getLastModified(), binaryResourceDownloadMeta.getETag());
        if (rb != null) {
            return rb.build();
        }
        try {
            if (ATTACHED_FILES_SUBTYPE.equals(subType) && output != null && !output.isEmpty()) {
                binaryContentInputStream = getConvertedBinaryResource(binaryResource, output);
                if (range == null || range.isEmpty()) {
                    binaryResourceDownloadMeta.setLength(0);
                }
            } else {
                binaryContentInputStream = storageManager.getBinaryResourceInputStream(binaryResource);
            }
            return BinaryResourceDownloadResponseBuilder.prepareResponse(binaryContentInputStream, binaryResourceDownloadMeta, range);
        } catch (StorageException | FileConversionException e) {
            return BinaryResourceDownloadResponseBuilder.downloadError(e, fullName);
        }
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
            return onDemandConverterManager.getPartConvertedResource(outputFormat, binaryResource);
        } catch (Exception e) {
            throw new FileConversionException(e);
        }
    }

    private boolean canAccess(PartIterationKey partIKey) throws UserNotActiveException, EntityNotFoundException {
        return publicEntityManager.canAccess(partIKey) || contextManager.isCallerInRole(UserGroupMapping.REGULAR_USER_ROLE_ID) && productService.canAccess(partIKey);
    }

    private BinaryResource getBinaryResource(String fullName)
            throws NotAllowedException, AccessRightException, UserNotActiveException, EntityNotFoundException {
        BinaryResource publicBinaryResourceForPart = publicEntityManager.getPublicBinaryResourceForPart(fullName);
        if (publicBinaryResourceForPart != null) {
            return publicBinaryResourceForPart;
        }
        return productService.getBinaryResource(fullName);
    }

    private void checkUuidValidity(SharedEntity sharedEntity, String workspaceId, String partNumber, String version, int iteration, String password)
            throws UnMatchingUuidException, SharedResourceAccessException, NotAllowedException {
        if (!(sharedEntity instanceof SharedPart)) {
            throw new UnMatchingUuidException();
        }

        checkUuidExpiredDate(sharedEntity);
        checkUuidPassword(sharedEntity, password);

        String shareEntityWorkspaceId = sharedEntity.getWorkspace().getId();
        PartRevision partRevision = ((SharedPart) sharedEntity).getPartRevision();
        PartIteration lastCheckedInIteration = partRevision.getLastCheckedInIteration();
        if (!shareEntityWorkspaceId.equals(workspaceId) ||
                !partRevision.getPartMasterNumber().equals(partNumber) ||
                !partRevision.getVersion().equals(version) ||
                (null != lastCheckedInIteration && lastCheckedInIteration.getIteration() < iteration)) {
            throw new UnMatchingUuidException();
        }
    }

    private void checkUuidPassword(SharedEntity sharedEntity, String password) throws SharedResourceAccessException {
        String entityPassword = sharedEntity.getPassword();
        if (entityPassword != null && !entityPassword.isEmpty()) {
            try {
                if (password == null || password.isEmpty() || !entityPassword.equals(HashUtils.md5Sum(password))) {
                    throw new SharedResourceAccessException();
                }
            } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                throw new SharedResourceAccessException();
            }
        }
    }

    private void checkUuidExpiredDate(SharedEntity sharedEntity) throws SharedResourceAccessException {
        // Check shared entity expired
        if (sharedEntity.getExpireDate() != null && sharedEntity.getExpireDate().getTime() < new Date().getTime()) {
            shareService.deleteSharedEntityIfExpired(sharedEntity);
            throw new SharedResourceAccessException();
        }
    }

}