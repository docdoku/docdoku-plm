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
import com.docdoku.core.document.DocumentMasterTemplateKey;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IDataManagerLocal;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.core.services.IDocumentResourceGetterManagerLocal;
import com.docdoku.server.helpers.Streams;
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
import java.net.URLEncoder;
import java.util.Collection;
import java.util.logging.Logger;

@Stateless
@DeclareRoles({UserGroupMapping.REGULAR_USER_ROLE_ID})
@RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
public class DocumentTemplateBinaryResource {
    @EJB
    private IDataManagerLocal dataManager;
    @EJB
    private IDocumentManagerLocal documentService;
    @EJB
    private IDocumentResourceGetterManagerLocal documentResourceGetterService;

    private static final Logger LOGGER = Logger.getLogger(DocumentTemplateBinaryResource.class.getName());


    public DocumentTemplateBinaryResource() {
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadDocumentTemplateFiles(@Context HttpServletRequest request,
                                                @PathParam("workspaceId") final String workspaceId,
                                                @PathParam("templateId") final String templateId)
            throws EntityNotFoundException, EntityAlreadyExistsException, UserNotActiveException, AccessRightException, NotAllowedException, CreationException {

        try {
            BinaryResource binaryResource;
            String fileName=null;
            long length;
            DocumentMasterTemplateKey templatePK = new DocumentMasterTemplateKey(workspaceId, templateId);
            Collection<Part> formParts = request.getParts();

            for(Part formPart : formParts){
                fileName = formPart.getSubmittedFileName();
                // Init the binary resource with a null length
                binaryResource= documentService.saveFileInTemplate(templatePK, fileName, 0);
                OutputStream outputStream = dataManager.getBinaryResourceOutputStream(binaryResource);
                length = BinaryResourceUpload.uploadBinary(outputStream, formPart);
                documentService.saveFileInTemplate(templatePK, fileName, length);
            }

            if(formParts.size()==1) {
                return BinaryResourceUpload.tryToRespondCreated(request.getRequestURI()+ URLEncoder.encode(fileName, "UTF-8"));
            }
            return Response.ok().build();

        } catch (IOException | ServletException | StorageException e) {
            return BinaryResourceUpload.uploadError(e);
        }
    }



    @GET
    @Path("/{fileName}")
    @Compress
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadDocumentTemplateFile(@Context Request request,
                                                 @HeaderParam("Range") String range,
                                                 @PathParam("workspaceId") final String workspaceId,
                                                 @PathParam("templateId") final String templateId,
                                                 @PathParam("fileName") final String fileName,
                                                 @QueryParam("type") String type,
                                                 @QueryParam("output") String output)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException, PreconditionFailedException, NotModifiedException, RequestedRangeNotSatisfiableException {


        String fullName = workspaceId + "/document-templates/" + templateId + "/" + fileName;
        BinaryResource binaryResource = documentService.getTemplateBinaryResource(fullName);
        BinaryResourceDownloadMeta binaryResourceDownloadMeta = new BinaryResourceDownloadMeta(binaryResource);

        // Check cache precondition
        Response.ResponseBuilder rb = request.evaluatePreconditions(binaryResourceDownloadMeta.getLastModified(), binaryResourceDownloadMeta.getETag());
        if(rb!= null){
            return rb.build();
        }

        InputStream binaryContentInputStream = null;
        try {
            if(output!=null && !output.isEmpty()){
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