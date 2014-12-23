/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2014 DocDoku SARL
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
import com.docdoku.core.document.DocumentIterationKey;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.*;
import com.docdoku.core.sharing.SharedDocument;
import com.docdoku.core.sharing.SharedEntity;
import com.docdoku.server.rest.exceptions.FileConversionException;
import com.docdoku.server.rest.exceptions.NotModifiedException;
import com.docdoku.server.rest.exceptions.PreconditionFailedException;
import com.docdoku.server.rest.exceptions.RequestedRangeNotSatisfiableException;
import com.docdoku.server.rest.file.util.BinaryResourceDownloadMeta;
import com.docdoku.server.rest.file.util.BinaryResourceDownloadResponseBuilder;
import com.docdoku.server.rest.file.util.BinaryResourceUpload;
import com.docdoku.server.rest.interceptors.Compress;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
@DeclareRoles({UserGroupMapping.REGULAR_USER_ROLE_ID,UserGroupMapping.GUEST_PROXY_ROLE_ID})
@RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID,UserGroupMapping.GUEST_PROXY_ROLE_ID})
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

    private static final Logger LOGGER = Logger.getLogger(DocumentBinaryResource.class.getName());

    public DocumentBinaryResource() {
    }

    @POST
    @Path("/{iteration}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadDocumentFiles(@Context HttpServletRequest request,
                                        @PathParam("uuid") final String uuid,
                                       @PathParam("workspaceId") final String workspaceId,
                                       @PathParam("documentId") final String documentId,
                                       @PathParam("version") final String version,
                                       @PathParam("iteration") final int iteration)
            throws EntityNotFoundException, EntityAlreadyExistsException, UserNotActiveException, AccessRightException, NotAllowedException, CreationException {

        if(uuid!=null){
            return Response.status(Response.Status.METHOD_NOT_ALLOWED)
                           .build();
        }

        try {
            BinaryResource binaryResource;
            String fileName=null;
            long length;
            DocumentIterationKey docPK = new DocumentIterationKey(workspaceId, documentId, version, iteration);
            Collection<Part> formParts = request.getParts();

            for(Part formPart : formParts){
                fileName = formPart.getSubmittedFileName();
                // Init the binary resource with a null length
                binaryResource= documentService.saveFileInDocument(docPK, fileName, 0);
                OutputStream outputStream = dataManager.getBinaryResourceOutputStream(binaryResource);
                length = BinaryResourceUpload.UploadBinary(outputStream,formPart);
                documentService.saveFileInDocument(docPK, fileName, length);
                documentPostUploaderService.process(binaryResource);
            }

            try {
                if(formParts.size()==1){
                    return Response.created(new URI(request.getRequestURI()+fileName)).build();
                }
            } catch (URISyntaxException e) {
                LOGGER.log(Level.WARNING,null,e);
            }
            return Response.ok().build();

        } catch (IOException | ServletException | StorageException e) {
            return BinaryResourceUpload.uploadError(e);
        }
    }

    @GET
    @Path("/{iteration}/{fileName}{virtualSubResource : (/[^/]+?)?}")
    @Compress
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadDocumentFile(@Context Request request,
                                         @HeaderParam("Range") String range,
                                         @PathParam("uuid") final String uuid,
                                         @PathParam("workspaceId") final String workspaceId,
                                         @PathParam("documentId") final String documentId,
                                         @PathParam("version") final String version,
                                         @PathParam("iteration") final int iteration,
                                         @PathParam("fileName") final String fileName,
                                         @PathParam("virtualSubResource") final String virtualSubResource,
                                         @QueryParam("type") String type,
                                         @QueryParam("output") String output)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException, NotModifiedException, PreconditionFailedException, RequestedRangeNotSatisfiableException{

        String fullName;
        if (uuid != null) {
            SharedEntity sharedEntity = shareService.findSharedEntityForGivenUUID(uuid);
            DocumentRevision documentRevision = ((SharedDocument) sharedEntity).getDocumentRevision();
            fullName = sharedEntity.getWorkspace().getId() +
                    "/documents/" +
                    documentRevision.getId() + "/" +
                    documentRevision.getVersion() + "/" +
                    iteration + "/" +fileName;
        }else {
            // Log guest user                                                                                           // Todo : If Guest, return public binary resource

            // Check access right
            DocumentIterationKey docIK = new DocumentIterationKey(workspaceId, documentId, version, iteration);
            if (!documentService.canAccess(docIK)) {
                throw new NotAllowedException(Locale.getDefault(), "NotAllowedException34");
            }
            fullName = workspaceId + "/documents/" + documentId + "/" + version + "/" + iteration + "/" + fileName;
        }
        return downloadDocumentFile(request, range, fullName, virtualSubResource, type, output);
    }



    private Response downloadDocumentFile(Request request, String range, String fullName, String virtualSubResource, String type, String output)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException, NotModifiedException, PreconditionFailedException, RequestedRangeNotSatisfiableException{
        BinaryResource binaryResource = documentService.getBinaryResource(fullName);
        BinaryResourceDownloadMeta binaryResourceDownloadMeta = new BinaryResourceDownloadMeta(binaryResource,output,type);
        binaryResourceDownloadMeta.setSubResourceVirtualPath(virtualSubResource);

        // Check cache precondition
        Response.ResponseBuilder rb = request.evaluatePreconditions(binaryResourceDownloadMeta.getLastModified(), binaryResourceDownloadMeta.getETag());
        if(rb!= null){
            return rb.build();
        }

        try {
            InputStream binaryContentInputStream;
            if(virtualSubResource!=null && !virtualSubResource.isEmpty()){
                binaryContentInputStream = dataManager.getBinarySubResourceInputStream(binaryResource, fullName+"/"+virtualSubResource);
            }else if(output!=null && !output.isEmpty()){
                binaryContentInputStream = getConvertedBinaryResource(binaryResource, output);
            }else{
                binaryContentInputStream = dataManager.getBinaryResourceInputStream(binaryResource);
            }
            return BinaryResourceDownloadResponseBuilder.prepareResponse(binaryContentInputStream, binaryResourceDownloadMeta, range);
        } catch (StorageException | FileConversionException e) {
            return BinaryResourceDownloadResponseBuilder.downloadError(e, fullName);
        }
    }

    /**
     * Try to convert a binary resource to a specific format
     * @param binaryResource The binary resource
     * @param output The wanted output
     * @return The binary resource stream in the wanted output
     * @throws com.docdoku.server.rest.exceptions.FileConversionException
     */
    private InputStream getConvertedBinaryResource(BinaryResource binaryResource, String output) throws FileConversionException {
        try {
            return documentResourceGetterService.getConvertedResource(output, binaryResource);
        } catch (Exception e) {
            throw new FileConversionException(e);
        }
    }
}