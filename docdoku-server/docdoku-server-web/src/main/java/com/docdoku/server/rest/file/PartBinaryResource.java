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
import com.docdoku.core.exceptions.*;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.*;
import com.docdoku.core.sharing.SharedEntity;
import com.docdoku.core.sharing.SharedPart;
import com.docdoku.server.filters.GuestProxy;
import com.docdoku.server.helpers.Streams;
import com.docdoku.server.rest.exceptions.*;
import com.docdoku.server.rest.file.util.BinaryResourceDownloadMeta;
import com.docdoku.server.rest.file.util.BinaryResourceDownloadResponseBuilder;
import com.docdoku.server.rest.file.util.BinaryResourceUpload;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.Normalizer;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

@RequestScoped
@Api(hidden = true, value = "part-binary", description = "Operations about part files")
@DeclareRoles({UserGroupMapping.REGULAR_USER_ROLE_ID,UserGroupMapping.GUEST_PROXY_ROLE_ID})
public class PartBinaryResource{

    @Inject
    private IDataManagerLocal dataManager;

    @Inject
    private IProductManagerLocal productService;

    @Inject
    private IContextManagerLocal contextManager;

    @Inject
    private IConverterManagerLocal converterService;

    @Inject
    private IShareManagerLocal shareService;

    @Inject
    private GuestProxy guestProxy;

    @Inject
    private IDocumentResourceGetterManagerLocal documentResourceGetterService;

    private static final Logger LOGGER = Logger.getLogger(PartBinaryResource.class.getName());
    private static final String NATIVE_CAD_SUBTYPE = "nativecad";
    private static final String ATTACHED_FILES_SUBTYPE = "attachedfiles";
    private static final String UTF8_ENCODING = "UTF-8";


    public PartBinaryResource() {
    }

    @POST
    @ApiOperation(value = "Upload CAD file" , response = Response.class)
    @Path("/{iteration}/"+NATIVE_CAD_SUBTYPE)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    public Response uploadNativeCADFile(@Context HttpServletRequest request,
                                    @PathParam("workspaceId") final String workspaceId,
                                    @PathParam("partNumber") final String partNumber,
                                    @PathParam("version") final String version,
                                    @PathParam("iteration") final int iteration)
            throws EntityNotFoundException, EntityAlreadyExistsException, UserNotActiveException, AccessRightException, NotAllowedException, CreationException {
        try {

            PartIterationKey partPK = new PartIterationKey(workspaceId, partNumber, version, iteration);
            Collection<Part> parts = request.getParts();

            if(parts.isEmpty() || parts.size() > 1){
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            Part part = parts.iterator().next();
            String fileName = part.getSubmittedFileName();
            BinaryResource binaryResource = productService.saveNativeCADInPartIteration(partPK, fileName, 0);
            OutputStream outputStream = dataManager.getBinaryResourceOutputStream(binaryResource);
            long length = BinaryResourceUpload.uploadBinary(outputStream, part);
            productService.saveNativeCADInPartIteration(partPK, fileName, length);
            tryToConvertCADFileToOBJ(partPK,binaryResource);

            return BinaryResourceUpload.tryToRespondCreated(request.getRequestURI() +  URLEncoder.encode(fileName, UTF8_ENCODING));

        } catch (IOException | ServletException | StorageException e) {
            return BinaryResourceUpload.uploadError(e);
        }
    }

    @POST
    @ApiOperation(value = "Upload attached file" , response = Response.class)
    @Path("/{iteration}/"+ATTACHED_FILES_SUBTYPE)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    public Response uploadAttachedFiles(@Context HttpServletRequest request,
                                    @PathParam("workspaceId") final String workspaceId,
                                    @PathParam("partNumber") final String partNumber,
                                    @PathParam("version") final String version,
                                    @PathParam("iteration") final int iteration)
            throws EntityNotFoundException, EntityAlreadyExistsException, UserNotActiveException, AccessRightException, NotAllowedException, CreationException {
        try {

            PartIterationKey partPK = new PartIterationKey(workspaceId, partNumber, version, iteration);
            Collection<Part> formParts = request.getParts();

            String fileName = null;

            for(Part formPart : formParts){
                fileName = Normalizer.normalize(formPart.getSubmittedFileName(), Normalizer.Form.NFC);
                BinaryResource binaryResource = productService.saveFileInPartIteration(partPK, fileName, ATTACHED_FILES_SUBTYPE, 0);
                OutputStream outputStream = dataManager.getBinaryResourceOutputStream(binaryResource);
                long length = BinaryResourceUpload.uploadBinary(outputStream, formPart);
                productService.saveFileInPartIteration(partPK, fileName, ATTACHED_FILES_SUBTYPE, length);
            }

            if(formParts.size()==1) {
                return BinaryResourceUpload.tryToRespondCreated(request.getRequestURI() +  URLEncoder.encode(fileName, UTF8_ENCODING));
            }

            return Response.ok().build();

        } catch (IOException | ServletException | StorageException e) {
            return BinaryResourceUpload.uploadError(e);
        }
    }

    // Split on several methods because of Path conflict when we use regex
    @GET
    @ApiOperation(value = "Download direct part file" , response = Response.class)
    @Path("/{iteration}/{fileName}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadDirectPartFile(@Context Request request,
                                     @HeaderParam("Range") String range,
                                     @PathParam("workspaceId") final String workspaceId,
                                     @PathParam("partNumber") final String partNumber,
                                     @PathParam("version") final String version,
                                     @PathParam("iteration") final int iteration,
                                     @PathParam("fileName") final String fileName,
                                     @QueryParam("type") String type,
                                     @QueryParam("output") String output)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException, PreconditionFailedException, NotModifiedException, RequestedRangeNotSatisfiableException, UnmatchingUuidException, ExpiredLinkException {
        return downloadPartFile(request,range,null,workspaceId,partNumber,version,iteration,null,fileName,type,output,null);
    }

    @GET
    @ApiOperation(value = "Upload part file from uuid" , response = Response.class)
    @Path("/{iteration}/{fileName}/uuid/{uuid}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadPartFileWithUuid(@Context Request request,
                                           @HeaderParam("Range") String range,
                                           @HeaderParam("Referer") String referer,
                                           @PathParam("workspaceId") final String workspaceId,
                                           @PathParam("partNumber") final String partNumber,
                                           @PathParam("version") final String version,
                                           @PathParam("iteration") final int iteration,
                                           @PathParam("fileName") final String fileName,
                                           @QueryParam("type") String type,
                                           @QueryParam("output") String output,
                                           @PathParam("uuid") final String uuid)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException, PreconditionFailedException, NotModifiedException, RequestedRangeNotSatisfiableException, UnmatchingUuidException, ExpiredLinkException {
        return downloadPartFile(request,range,referer,workspaceId,partNumber,version,iteration,null,fileName,type,output,uuid);
    }

    @GET
    @ApiOperation(value = "Download part file with subtype" , response = Response.class)
    @Path("/{iteration}/{subType}/{fileName}/")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadPartFileWithSubtype(@Context Request request,
                                               @HeaderParam("Range") String range,
                                               @PathParam("workspaceId") final String workspaceId,
                                               @PathParam("partNumber") final String partNumber,
                                               @PathParam("version") final String version,
                                               @PathParam("iteration") final int iteration,
                                               @PathParam("subType") final String subType,
                                               @PathParam("fileName") final String fileName,
                                               @QueryParam("type") String type,
                                               @QueryParam("output") String output)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException, PreconditionFailedException, NotModifiedException, RequestedRangeNotSatisfiableException, UnmatchingUuidException, ExpiredLinkException {
        return downloadPartFile(request,range,null,workspaceId,partNumber,version,iteration,subType,fileName,type,output,null);
    }


    @GET
    @ApiOperation(value = "Download part file" , response = Response.class)
    @Path("/{iteration}/{subType}/{fileName}/uuid/{uuid}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadPartFile(@Context Request request,
                                     @HeaderParam("Range") String range,
                                     @HeaderParam("Referer") String referer,
                                     @PathParam("workspaceId") final String workspaceId,
                                     @PathParam("partNumber") final String partNumber,
                                     @PathParam("version") final String version,
                                     @PathParam("iteration") final int iteration,
                                     @PathParam("subType") final String subType,
                                     @PathParam("fileName") final String fileName,
                                     @QueryParam("type") String type,
                                     @QueryParam("output") String output,
                                     @PathParam("uuid") final String pUuid)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException, PreconditionFailedException, NotModifiedException, RequestedRangeNotSatisfiableException, UnmatchingUuidException, ExpiredLinkException {

        String fullName;
        if (pUuid != null && !pUuid.isEmpty()) {
            String uuid = pUuid.split("/")[2];
            SharedEntity sharedEntity = shareService.findSharedEntityForGivenUUID(uuid);

            // Check uuid & access right
            checkUuidValidity(sharedEntity,workspaceId,partNumber,version,iteration,referer);

            PartRevision partRevision = ((SharedPart) sharedEntity).getPartRevision();
            fullName = sharedEntity.getWorkspace().getId() +
                    "/parts/" +
                    partRevision.getPartNumber() + "/" +
                    partRevision.getVersion() + "/" +
                    iteration + "/";
        }else {
            // Check access right
            PartIterationKey partIK = new PartIterationKey(workspaceId, partNumber, version,iteration);
            if(!canAccess(partIK)){
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            fullName = workspaceId+"/parts/" + partNumber + "/" + version + "/" + iteration + "/";
        }

        String decodedFileName = fileName;

        try {
            decodedFileName  = URLDecoder.decode(fileName, UTF8_ENCODING);
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE,"Cannot decode filename");
            LOGGER.log(Level.FINER, null, e);
        }

        fullName += (subType!= null && !subType.isEmpty()) ? subType + "/" +decodedFileName : decodedFileName;

        return downloadPartFile(request,range,fullName,subType,type,output);
    }


    private Response downloadPartFile(Request request, String range, String fullName, String subType, String type, String output)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException, PreconditionFailedException, NotModifiedException, RequestedRangeNotSatisfiableException {
        BinaryResource binaryResource = getBinaryResource(fullName);
        BinaryResourceDownloadMeta binaryResourceDownloadMeta = new BinaryResourceDownloadMeta(binaryResource,output,type);

        // Check cache precondition
        Response.ResponseBuilder rb = request.evaluatePreconditions(binaryResourceDownloadMeta.getLastModified(), binaryResourceDownloadMeta.getETag());
        if(rb!= null){
            return rb.build();
        }

        //attachedfiles is not a valid subtype
        if(subType!= null && !subType.isEmpty() && !subType.equals("attachedfiles")){
            binaryResourceDownloadMeta.setSubResourceVirtualPath(subType);
        }

        InputStream binaryContentInputStream = null;
        try {
            if("attachedfiles".equals(subType) && output!=null && !output.isEmpty()) {
                binaryResourceDownloadMeta.setSubResourceVirtualPath(null);
                binaryContentInputStream = getConvertedBinaryResource(binaryResource,output);
            } else {
                binaryContentInputStream = dataManager.getBinaryResourceInputStream(binaryResource);
            }
            return BinaryResourceDownloadResponseBuilder.prepareResponse(binaryContentInputStream, binaryResourceDownloadMeta, range);
        } catch (StorageException | FileConversionException e) {
            Streams.close(binaryContentInputStream);
            return BinaryResourceDownloadResponseBuilder.downloadError(e, fullName);
        }
    }

    /**
     * Try to convert a binary resource to a specific format
     * @param binaryResource The binary resource
     * @param outputFormat The wanted output
     * @return The binary resource stream in the wanted output
     * @throws com.docdoku.server.rest.exceptions.FileConversionException
     */
    private InputStream getConvertedBinaryResource(BinaryResource binaryResource, String outputFormat) throws FileConversionException {
        try {
            if(contextManager.isCallerInRole(UserGroupMapping.REGULAR_USER_ROLE_ID)){
                return documentResourceGetterService.getPartConvertedResource(outputFormat, binaryResource);
            }else{
                return guestProxy.getPartConvertedResource(outputFormat, binaryResource);
            }
        } catch (Exception e) {
            throw new FileConversionException(e);
        }
    }

    private void tryToConvertCADFileToOBJ(PartIterationKey partPK, BinaryResource binaryResource){
        try {
            //TODO: Should be put in a DocumentPostUploader plugin
            converterService.convertCADFileToOBJ(partPK, binaryResource);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "A CAD file conversion can not be done", e);
        }
    }

    private boolean canAccess(PartIterationKey partIKey) throws UserNotActiveException, EntityNotFoundException {
        if(contextManager.isCallerInRole(UserGroupMapping.REGULAR_USER_ROLE_ID)){
            return productService.canAccess(partIKey);
        }else{
            return guestProxy.canAccess(partIKey);
        }
    }

    private BinaryResource getBinaryResource(String fullName)
            throws NotAllowedException, AccessRightException, UserNotActiveException, EntityNotFoundException {
        if(contextManager.isCallerInRole(UserGroupMapping.REGULAR_USER_ROLE_ID)){
            return productService.getBinaryResource(fullName);
        }else{
            return guestProxy.getBinaryResourceForPart(fullName);
        }
    }

    private void checkUuidValidity(SharedEntity sharedEntity, String workspaceId, String partNumber, String version, int iteration, String referer)
            throws UnmatchingUuidException, ExpiredLinkException, NotAllowedException {
        if(!(sharedEntity instanceof SharedPart)){
            throw new UnmatchingUuidException();
        }

        checkUuidReferer(sharedEntity, referer);
        checkUuidExpiredDate(sharedEntity);

        String shareEntityWorkspaceId = sharedEntity.getWorkspace().getId();
        PartRevision partRevision = ((SharedPart) sharedEntity).getPartRevision();
        PartIteration lastCheckedInIteration = partRevision.getLastCheckedInIteration();
        if(!shareEntityWorkspaceId.equals(workspaceId) ||
                !partRevision.getPartMasterNumber().equals(partNumber) ||
                !partRevision.getVersion().equals(version) ||
                (null != lastCheckedInIteration && lastCheckedInIteration.getIteration() < iteration)){
            throw new UnmatchingUuidException();
        }
    }

    private void checkUuidExpiredDate(SharedEntity sharedEntity) throws ExpiredLinkException {
        // Check shared entity expired
        if(sharedEntity.getExpireDate() != null && sharedEntity.getExpireDate().getTime() < new Date().getTime()){
            shareService.deleteSharedEntityIfExpired(sharedEntity);
            throw new ExpiredLinkException();
        }
    }

    private void checkUuidReferer(SharedEntity sharedEntity,String referer) throws NotAllowedException {
        if(referer==null || referer.isEmpty()){
            throw new NotAllowedException(Locale.getDefault(),"NotAllowedException18");
        }

        String[] refererPath = referer.split("/");
        String refererUUID = refererPath[refererPath.length-1];
        if(sharedEntity.getPassword()!=null && !sharedEntity.getUuid().equals(refererUUID)){
            throw new NotAllowedException(Locale.getDefault(),"NotAllowedException18");
        }
    }
}