/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2015 DocDoku SARL
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
import com.docdoku.core.configuration.ProductInstanceIterationKey;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IContextManagerLocal;
import com.docdoku.core.services.IDataManagerLocal;
import com.docdoku.core.services.IProductInstanceManagerLocal;
import com.docdoku.core.services.IPublicEntityManagerLocal;
import com.docdoku.server.helpers.Streams;
import com.docdoku.server.rest.exceptions.*;
import com.docdoku.server.rest.file.util.BinaryResourceDownloadMeta;
import com.docdoku.server.rest.file.util.BinaryResourceDownloadResponseBuilder;
import com.docdoku.server.rest.file.util.BinaryResourceUpload;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.Normalizer;
import java.util.Collection;

/*
 *
 * @author Asmae CHADID on 30/03/15.
 */

@RequestScoped
@Api(hidden = true, value = "product-instance-binary", description = "Operations about product instances files")
@DeclareRoles({UserGroupMapping.REGULAR_USER_ROLE_ID})
@RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
public class ProductInstanceBinaryResource {

    @Inject
    private IDataManagerLocal dataManager;

    @Inject
    private IContextManagerLocal contextManager;

    @Inject
    private IProductInstanceManagerLocal productInstanceManagerLocal;

    @Inject
    private IPublicEntityManagerLocal publicEntityManager;

    @POST
    @ApiOperation(value = "Upload product instance files", response = Response.class)
    @Path("iterations/{iteration}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    public Response uploadFilesToProductInstanceIteration(
            @Context HttpServletRequest request,
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String configurationItemId,
            @ApiParam(required = true, value = "Serial number") @PathParam("serialNumber") String serialNumber,
            @ApiParam(required = true, value = "Product instance iteration") @PathParam("iteration") int iteration)

            throws EntityNotFoundException, UserNotActiveException, NotAllowedException, AccessRightException, EntityAlreadyExistsException, CreationException {


        try {
            String fileName = null;
            ProductInstanceIterationKey iterationKey = new ProductInstanceIterationKey(serialNumber, workspaceId, configurationItemId, iteration);
            Collection<Part> formParts = request.getParts();

            for (Part formPart : formParts) {
                fileName = uploadAFile(workspaceId, formPart, iterationKey);
            }

            if (formParts.size() == 1) {
                return BinaryResourceUpload.tryToRespondCreated(request.getRequestURI() + URLEncoder.encode(fileName, "UTF-8"));
            }
            return Response.ok().build();

        } catch (IOException | ServletException | StorageException e) {
            return BinaryResourceUpload.uploadError(e);
        }

    }


    @GET
    @ApiOperation(value = "Download product instance file", response = Response.class)
    @Path("iterations/{iteration}/{fileName}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadFileFromProductInstance(@Context Request request,
                                                    @HeaderParam("Range") String range,
                                                    @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
                                                    @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String configurationItemId,
                                                    @ApiParam(required = true, value = "Serial number") @PathParam("serialNumber") String serialNumber,
                                                    @ApiParam(required = true, value = "Product instance iteration") @PathParam("iteration") int iteration,
                                                    @ApiParam(required = true, value = "File name") @PathParam("fileName") final String fileName,
                                                    @ApiParam(required = false, value = "Type") @QueryParam("type") String type,
                                                    @ApiParam(required = false, value = "Output") @QueryParam("output") String output)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, com.docdoku.core.exceptions.NotAllowedException, PreconditionFailedException, NotModifiedException, RequestedRangeNotSatisfiableException, UnmatchingUuidException, ExpiredLinkException {


        String fullName = workspaceId + "/product-instances/" + serialNumber + "/iterations/" + iteration + "/" + fileName;
        BinaryResource binaryResource = getBinaryResource(fullName);
        BinaryResourceDownloadMeta binaryResourceDownloadMeta = new BinaryResourceDownloadMeta(binaryResource, output, type);

        // Check cache precondition
        Response.ResponseBuilder rb = request.evaluatePreconditions(binaryResourceDownloadMeta.getLastModified(), binaryResourceDownloadMeta.getETag());
        if (rb != null) {
            return rb.build();
        }
        InputStream binaryContentInputStream = null;
        try {
            binaryContentInputStream = dataManager.getBinaryResourceInputStream(binaryResource);
            return BinaryResourceDownloadResponseBuilder.prepareResponse(binaryContentInputStream, binaryResourceDownloadMeta, range);
        } catch (StorageException e) {
            Streams.close(binaryContentInputStream);
            return BinaryResourceDownloadResponseBuilder.downloadError(e, fullName);
        }

    }


    @POST
    @ApiOperation(value = "Upload path data file", response = Response.class)
    @Path("pathdata/{pathDataId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    public Response uploadFilesToPathData(
            @Context HttpServletRequest request,
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String configurationItemId,
            @ApiParam(required = true, value = "Serial number") @PathParam("serialNumber") String serialNumber,
            @ApiParam(required = true, value = "Product instance iteration") @PathParam("iteration") int iteration,
            @ApiParam(required = true, value = "Path data id") @PathParam("pathDataId") int pathDataId)
            throws EntityNotFoundException, UserNotActiveException, NotAllowedException, AccessRightException, EntityAlreadyExistsException, CreationException {

        // TODO: determine if this WS is really used...

        try {
            String fileName = null;
            Collection<Part> formParts = request.getParts();

            for (Part formPart : formParts) {
                fileName = uploadAFileToPathData(workspaceId, formPart, configurationItemId, serialNumber, pathDataId, iteration);
            }

            if (formParts.size() == 1) {
                return BinaryResourceUpload.tryToRespondCreated(request.getRequestURI() + URLEncoder.encode(fileName, "UTF-8"));
            }
            return Response.ok().build();

        } catch (IOException | ServletException | StorageException e) {
            return BinaryResourceUpload.uploadError(e);
        }

    }

    @POST
    @ApiOperation(value = "Upload path data iteration file", response = Response.class)
    @Path("pathdata/{path}/iterations/{iteration}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    public Response uploadFilesToPathDataIteration(
            @Context HttpServletRequest request,
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String configurationItemId,
            @ApiParam(required = true, value = "Serial number") @PathParam("serialNumber") String serialNumber,
            @ApiParam(required = true, value = "Product instance iteration") @PathParam("iteration") int iteration,
            @ApiParam(required = true, value = "Complete path") @PathParam("path") int path)
            throws EntityNotFoundException, UserNotActiveException, NotAllowedException, AccessRightException, EntityAlreadyExistsException, CreationException {

        try {
            String fileName = null;
            Collection<Part> formParts = request.getParts();

            for (Part formPart : formParts) {
                fileName = uploadAFileToPathDataIteration(workspaceId, formPart, configurationItemId, serialNumber, path, iteration);
            }

            if (formParts.size() == 1) {
                return BinaryResourceUpload.tryToRespondCreated(request.getRequestURI() + URLEncoder.encode(fileName, "UTF-8"));
            }
            return Response.ok().build();

        } catch (IOException | ServletException | StorageException e) {
            return BinaryResourceUpload.uploadError(e);
        }

    }

    @GET
    @ApiOperation(value = "Download path data file", response = Response.class)
    @Path("pathdata/{pathDataId}/{fileName}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadFileFromPathData(@Context Request request,
                                             @HeaderParam("Range") String range,
                                             @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
                                             @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String configurationItemId,
                                             @ApiParam(required = true, value = "Serial number") @PathParam("serialNumber") String serialNumber,
                                             @ApiParam(required = true, value = "Path data master id") @PathParam("pathDataId") final int pathDataId,
                                             @ApiParam(required = true, value = "File name") @PathParam("fileName") final String fileName,
                                             @ApiParam(required = false, value = "Type") @QueryParam("type") String type,
                                             @ApiParam(required = false, value = "Output") @QueryParam("output") String output)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, com.docdoku.core.exceptions.NotAllowedException, PreconditionFailedException, NotModifiedException, RequestedRangeNotSatisfiableException, UnmatchingUuidException, ExpiredLinkException {


        String fullName = workspaceId + "/product-instances/" + serialNumber + "/pathdata/" + pathDataId + "/" + fileName;
        BinaryResource binaryResource = getPathDataBinaryResource(fullName);
        BinaryResourceDownloadMeta binaryResourceDownloadMeta = new BinaryResourceDownloadMeta(binaryResource, output, type);

        // Check cache precondition
        Response.ResponseBuilder rb = request.evaluatePreconditions(binaryResourceDownloadMeta.getLastModified(), binaryResourceDownloadMeta.getETag());
        if (rb != null) {
            return rb.build();
        }

        InputStream binaryContentInputStream = null;
        try {
            binaryContentInputStream = dataManager.getBinaryResourceInputStream(binaryResource);
            return BinaryResourceDownloadResponseBuilder.prepareResponse(binaryContentInputStream, binaryResourceDownloadMeta, range);
        } catch (StorageException e) {
            Streams.close(binaryContentInputStream);
            return BinaryResourceDownloadResponseBuilder.downloadError(e, fullName);
        }
    }

    @GET
    @ApiOperation(value = "Download path data iteration file", response = Response.class)
    @Path("pathdata/{pathDataId}/iterations/{iteration}/{fileName}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadFileFromPathDataIteration(@Context Request request,
                                                      @HeaderParam("Range") String range,
                                                      @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") final String workspaceId,
                                                      @ApiParam(required = true, value = "Serial number") @PathParam("serialNumber") final String serialNumber,
                                                      @ApiParam(required = true, value = "Path data master id")  @PathParam("pathDataId") String pathDataId,
                                                      @ApiParam(required = true, value = "Path data iteration number") @PathParam("iteration") final int iteration,
                                                      @ApiParam(required = true, value = "File name id")  @PathParam("fileName") final String fileName,
                                                      @ApiParam(required = false, value = "Type") @QueryParam("type") String type,
                                                      @ApiParam(required = false, value = "Output") @QueryParam("output") String output)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, com.docdoku.core.exceptions.NotAllowedException, PreconditionFailedException, NotModifiedException, RequestedRangeNotSatisfiableException, UnmatchingUuidException, ExpiredLinkException {


        String fullName = workspaceId + "/product-instances/" + serialNumber + "/pathdata/" + pathDataId + "/iterations/" + iteration + '/' + fileName;
        BinaryResource binaryResource = getPathDataBinaryResource(fullName);
        BinaryResourceDownloadMeta binaryResourceDownloadMeta = new BinaryResourceDownloadMeta(binaryResource, output, type);

        // Check cache precondition
        Response.ResponseBuilder rb = request.evaluatePreconditions(binaryResourceDownloadMeta.getLastModified(), binaryResourceDownloadMeta.getETag());
        if (rb != null) {
            return rb.build();
        }

        InputStream binaryContentInputStream = null;

        try {
            binaryContentInputStream = dataManager.getBinaryResourceInputStream(binaryResource);
            return BinaryResourceDownloadResponseBuilder.prepareResponse(binaryContentInputStream, binaryResourceDownloadMeta, range);
        } catch (StorageException e) {
            Streams.close(binaryContentInputStream);
            return BinaryResourceDownloadResponseBuilder.downloadError(e, fullName);
        }
    }

    private BinaryResource getBinaryResource(String fullName)
            throws NotAllowedException, AccessRightException, UserNotActiveException, EntityNotFoundException {

        if (contextManager.isCallerInRole(UserGroupMapping.REGULAR_USER_ROLE_ID)) {
            return productInstanceManagerLocal.getBinaryResource(fullName);
        } else {
            return publicEntityManager.getBinaryResourceForProductInstance(fullName);
        }
    }

    private BinaryResource getPathDataBinaryResource(String fullName)
            throws NotAllowedException, AccessRightException, UserNotActiveException, EntityNotFoundException {
        if (contextManager.isCallerInRole(UserGroupMapping.REGULAR_USER_ROLE_ID)) {
            return productInstanceManagerLocal.getPathDataBinaryResource(fullName);
        } else {
            return publicEntityManager.getBinaryResourceForPathData(fullName);
        }
    }


    private String uploadAFile(String workspaceId, Part formPart, ProductInstanceIterationKey pdtIterationKey)
            throws EntityNotFoundException, EntityAlreadyExistsException, AccessRightException, NotAllowedException, CreationException, UserNotActiveException, StorageException, IOException {

        String fileName = Normalizer.normalize(formPart.getSubmittedFileName(), Normalizer.Form.NFC);
        // Init the binary resource with a null length
        BinaryResource binaryResource = productInstanceManagerLocal.saveFileInProductInstance(workspaceId, pdtIterationKey, fileName, 0);
        OutputStream outputStream = dataManager.getBinaryResourceOutputStream(binaryResource);
        long length = BinaryResourceUpload.uploadBinary(outputStream, formPart);
        productInstanceManagerLocal.saveFileInProductInstance(workspaceId, pdtIterationKey, fileName, (int) length);
        return fileName;
    }

    private String uploadAFileToPathData(String workspaceId, Part formPart, String configurationItemId, String serialNumber, int pathDataId, int iteration)
            throws EntityNotFoundException, EntityAlreadyExistsException, AccessRightException, NotAllowedException, CreationException, UserNotActiveException, StorageException, IOException {

        String fileName = Normalizer.normalize(formPart.getSubmittedFileName(), Normalizer.Form.NFC);
        // Init the binary resource with a null length
        BinaryResource binaryResource = productInstanceManagerLocal.saveFileInPathData(workspaceId, configurationItemId, serialNumber, pathDataId, iteration, fileName, 0);
        OutputStream outputStream = dataManager.getBinaryResourceOutputStream(binaryResource);
        long length = BinaryResourceUpload.uploadBinary(outputStream, formPart);
        productInstanceManagerLocal.saveFileInPathData(workspaceId, configurationItemId, serialNumber, pathDataId, iteration, fileName, (int) length);
        return fileName;
    }

    private String uploadAFileToPathDataIteration(String workspaceId, Part formPart, String configurationItemId, String serialNumber, int pathDataId, int iteration)
            throws EntityNotFoundException, EntityAlreadyExistsException, AccessRightException, NotAllowedException, CreationException, UserNotActiveException, StorageException, IOException {

        String fileName = Normalizer.normalize(formPart.getSubmittedFileName(), Normalizer.Form.NFC);
        // Init the binary resource with a null length
        BinaryResource binaryResource = productInstanceManagerLocal.saveFileInPathDataIteration(workspaceId, configurationItemId, serialNumber, pathDataId, iteration, fileName, 0);
        OutputStream outputStream = dataManager.getBinaryResourceOutputStream(binaryResource);
        long length = BinaryResourceUpload.uploadBinary(outputStream, formPart);
        productInstanceManagerLocal.saveFileInPathDataIteration(workspaceId, configurationItemId, serialNumber, pathDataId, iteration, fileName, (int) length);
        return fileName;
    }
}
