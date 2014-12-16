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
import com.docdoku.core.exceptions.*;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.services.IDataManagerLocal;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.core.services.IDocumentResourceGetterManagerLocal;
import com.docdoku.server.rest.exceptions.FileConversionException;
import com.docdoku.server.rest.exceptions.NotModifiedException;
import com.docdoku.server.rest.exceptions.PreconditionFailedException;
import com.docdoku.server.rest.exceptions.RequestedRangeNotSatisfiableException;
import com.docdoku.server.rest.file.util.BinaryResourceMeta;
import com.docdoku.server.rest.file.util.BinaryResourceResponse;
import com.docdoku.server.rest.interceptors.Compress;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import java.io.InputStream;

@Stateless
public class DocumentTemplateBinaryResource {
    @EJB
    private IDataManagerLocal dataManager;
    @EJB
    private IDocumentManagerLocal documentService;
    @EJB
    private IDocumentResourceGetterManagerLocal documentResourceGetterService;


    public DocumentTemplateBinaryResource() {
    }

    @GET
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
        // Todo : If Guest, return public binary resource
        BinaryResource binaryResource = documentService.getTemplateBinaryResource(fullName);
        BinaryResourceMeta binaryResourceMeta = new BinaryResourceMeta(binaryResource);

        // Check cache precondition
        Response.ResponseBuilder rb = request.evaluatePreconditions(binaryResourceMeta.getLastModified(), binaryResourceMeta.getETag());
        if(rb!= null){
            return rb.build();
        }

        try {
            InputStream binaryContentInputStream;
            if(output!=null && !output.isEmpty()){
                binaryContentInputStream = getConvertedBinaryResource(binaryResource, output);
            }else{
                binaryContentInputStream = dataManager.getBinaryResourceInputStream(binaryResource);
            }
            return BinaryResourceResponse.prepareResponse(binaryContentInputStream, binaryResourceMeta, range);
        } catch (StorageException | FileConversionException e) {
            return BinaryResourceResponse.downloadError(e, fullName);
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