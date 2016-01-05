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
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentIterationKey;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.*;
import com.docdoku.core.sharing.SharedDocument;
import com.docdoku.core.sharing.SharedEntity;
import com.docdoku.server.filters.GuestProxy;
import com.docdoku.server.helpers.Streams;
import com.docdoku.server.rest.exceptions.*;
import com.docdoku.server.rest.file.util.BinaryResourceDownloadMeta;
import com.docdoku.server.rest.file.util.BinaryResourceDownloadResponseBuilder;
import com.docdoku.server.rest.file.util.BinaryResourceUpload;
import com.docdoku.server.rest.interceptors.Compress;

import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
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
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
@DeclareRoles({UserGroupMapping.REGULAR_USER_ROLE_ID,UserGroupMapping.GUEST_PROXY_ROLE_ID})
public class DocumentBinaryResource {
    @EJB
    private IDataManagerLocal dataManager;
    @EJB
    private IDocumentManagerLocal documentService;
    @EJB
    private IDocumentResourceGetterManagerLocal documentResourceGetterService;
    @EJB
    private IDocumentPostUploaderManagerLocal documentPostUploaderService;
    @EJB
    private IShareManagerLocal shareService;
    @EJB
    private GuestProxy guestProxy;

    @Resource
    private SessionContext ctx;

    private static final Logger LOGGER = Logger.getLogger(DocumentBinaryResource.class.getName());

    public DocumentBinaryResource() {
    }

    @POST
    @Path("/{iteration}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    public Response uploadDocumentFiles(@Context HttpServletRequest request,
                                        @PathParam("workspaceId") final String workspaceId,
                                        @PathParam("documentId") final String documentId,
                                        @PathParam("version") final String version,
                                        @PathParam("iteration") final int iteration)
            throws EntityNotFoundException, EntityAlreadyExistsException, UserNotActiveException, AccessRightException, NotAllowedException, CreationException {
        try {
            String fileName=null;
            DocumentIterationKey docPK = new DocumentIterationKey(workspaceId, documentId, version, iteration);
            Collection<Part> formParts = request.getParts();

            for(Part formPart : formParts){
                fileName = uploadAFile(formPart,docPK);
            }

            if(formParts.size()==1) {
                return BinaryResourceUpload.tryToRespondCreated(request.getRequestURI()+ URLEncoder.encode(fileName, "UTF-8"));
            }
            return Response.ok().build();

        } catch (IOException | ServletException | StorageException e) {
            return BinaryResourceUpload.uploadError(e);
        }
    }

    private String uploadAFile(Part formPart,DocumentIterationKey docPK)
            throws EntityNotFoundException, EntityAlreadyExistsException, AccessRightException, NotAllowedException, CreationException, UserNotActiveException, StorageException, IOException {

        String fileName = Normalizer.normalize(formPart.getSubmittedFileName(), Normalizer.Form.NFC);
        // Init the binary resource with a null length
        BinaryResource binaryResource = documentService.saveFileInDocument(docPK, fileName, 0);
        OutputStream outputStream = dataManager.getBinaryResourceOutputStream(binaryResource);
        long length = BinaryResourceUpload.uploadBinary(outputStream, formPart);
        documentService.saveFileInDocument(docPK, fileName, length);
        documentPostUploaderService.process(binaryResource);
        return fileName;
    }

    @GET
    @Path("/{iteration}/{fileName}{uuid:(/uuid/[^/]+?)?}{virtualSubResource : (/[^/]+?)?}")
    @Compress
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadDocumentFile(@Context Request request,
                                         @HeaderParam("Range") String range,
                                         @HeaderParam("Referer") String referer,
                                         @PathParam("workspaceId") final String workspaceId,
                                         @PathParam("documentId") final String documentId,
                                         @PathParam("version") final String version,
                                         @PathParam("iteration") final int iteration,
                                         @PathParam("fileName") final String fileName,
                                         @PathParam("virtualSubResource") final String virtualSubResource,
                                         @QueryParam("type") String type,
                                         @QueryParam("output") String output,
                                         @PathParam("uuid") final String pUuid)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException, NotModifiedException, PreconditionFailedException, RequestedRangeNotSatisfiableException, UnmatchingUuidException, ExpiredLinkException {

        String fullName;
        if (pUuid != null && !pUuid.isEmpty()) {
            String uuid = pUuid.split("/")[2];
            SharedEntity sharedEntity = shareService.findSharedEntityForGivenUUID(uuid);

            // Check uuid & access right
            checkUuidValidity(sharedEntity,workspaceId,documentId,version,iteration, referer);

            DocumentRevision documentRevision = ((SharedDocument) sharedEntity).getDocumentRevision();
            fullName = sharedEntity.getWorkspace().getId() +
                    "/documents/" +
                    documentRevision.getId() + "/" +
                    documentRevision.getVersion() + "/" +
                    iteration + "/" +fileName;

        }else {
            // Check access right
            DocumentIterationKey docIK = new DocumentIterationKey(workspaceId, documentId, version, iteration);
            if (!canAccess(docIK)) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            fullName = workspaceId + "/documents/" + documentId + "/" + version + "/" + iteration + "/" + fileName;
        }
        return downloadDocumentFile(request, range, fullName, virtualSubResource, type, output);
    }



    private Response downloadDocumentFile(Request request, String range, String fullName, String virtualSubResource, String type, String output)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException, NotModifiedException, PreconditionFailedException, RequestedRangeNotSatisfiableException{
        BinaryResource binaryResource = getBinaryResource(fullName);
        BinaryResourceDownloadMeta binaryResourceDownloadMeta = new BinaryResourceDownloadMeta(binaryResource,output,type);
        binaryResourceDownloadMeta.setSubResourceVirtualPath(virtualSubResource);

        // Check cache precondition
        Response.ResponseBuilder rb = request.evaluatePreconditions(binaryResourceDownloadMeta.getLastModified(), binaryResourceDownloadMeta.getETag());
        if(rb!= null){
            return rb.build();
        }

        InputStream binaryContentInputStream = null;
        try {
            if(virtualSubResource!=null && !virtualSubResource.isEmpty()){
                binaryContentInputStream = dataManager.getBinarySubResourceInputStream(binaryResource, fullName+"/"+virtualSubResource);
            }else if(output!=null && !output.isEmpty()){
                binaryContentInputStream = getConvertedBinaryResource(binaryResource, output);
            }else{
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
            if(ctx.isCallerInRole(UserGroupMapping.REGULAR_USER_ROLE_ID)){
                return documentResourceGetterService.getDocumentConvertedResource(outputFormat, binaryResource);
            }else{
                return guestProxy.getDocumentConvertedResource(outputFormat, binaryResource);
            }
        } catch (Exception e) {
            throw new FileConversionException(e);
        }
    }

    private boolean canAccess(DocumentIterationKey docIKey) throws UserNotActiveException, EntityNotFoundException {
        if(ctx.isCallerInRole(UserGroupMapping.REGULAR_USER_ROLE_ID)){
            return documentService.canAccess(docIKey);
        }else{
            return guestProxy.canAccess(docIKey);
        }
    }

    private BinaryResource getBinaryResource(String fullName)
            throws NotAllowedException, AccessRightException, UserNotActiveException, EntityNotFoundException {
        if(ctx.isCallerInRole(UserGroupMapping.REGULAR_USER_ROLE_ID)){
            return documentService.getBinaryResource(fullName);
        }else{
            return guestProxy.getBinaryResourceForDocument(fullName);
        }
    }

    private void checkUuidValidity(SharedEntity sharedEntity, String workspaceId, String documentId, String version, int iteration, String referer)
            throws UnmatchingUuidException, ExpiredLinkException, NotAllowedException {
        if(!(sharedEntity instanceof SharedDocument)){
            throw new UnmatchingUuidException();
        }

        checkUuidReferer(sharedEntity, referer);
        checkUuidExpiredDate(sharedEntity);

        String shareEntityWorkspaceId = sharedEntity.getWorkspace().getId();
        DocumentRevision documentRevision = ((SharedDocument) sharedEntity).getDocumentRevision();
        DocumentIteration lastCheckedInIteration = documentRevision.getLastCheckedInIteration();
        if(!shareEntityWorkspaceId.equals(workspaceId) ||
                !documentRevision.getDocumentMasterId().equals(documentId) ||
                !documentRevision.getVersion().equals(version) ||
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

        String refererPath[] = referer.split("/");
        String refererUUID = refererPath[refererPath.length-1];
        if(sharedEntity.getPassword()!=null && !sharedEntity.getUuid().equals(refererUUID)){
            throw new NotAllowedException(Locale.getDefault(),"NotAllowedException18");
        }
    }
}