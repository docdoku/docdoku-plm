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
package com.docdoku.server.rest.file.util;

import com.docdoku.server.rest.exceptions.RequestedRangeNotSatisfiableException;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Taylor LABEJOF
 */

public class BinaryResourceDownloadResponseBuilder {
    private static final Logger LOGGER = Logger.getLogger(BinaryResourceDownloadResponseBuilder.class.getName());
    private static final int CACHE_SECOND = 60 * 60 * 24;


    private BinaryResourceDownloadResponseBuilder(){
        super();
    }


    /**
     * Set the header of the downloading response.
     * @param binaryContentInputStream The stream of the binary content to download.
     * @param binaryResourceDownloadMeta The header parameters for the binary content download.
     * @param range The string of the queried range. Null if no range are specified
     * @return A response builder with the header & the content.
     * @throws RequestedRangeNotSatisfiableException If the range is not satisfiable.
     */
    public static Response prepareResponse(InputStream binaryContentInputStream,BinaryResourceDownloadMeta binaryResourceDownloadMeta, String range)
            throws RequestedRangeNotSatisfiableException{

        Response.ResponseBuilder responseBuilder;

        if(range==null || range.isEmpty()){
            long length = binaryResourceDownloadMeta.getLength();
            responseBuilder = Response.ok()
                    .header("Content-Disposition", binaryResourceDownloadMeta.getContentDisposition())
                    .header("Content-Type", binaryResourceDownloadMeta.getContentType())
                    .entity(new BinaryResourceBinaryStreamingOutput(binaryContentInputStream, 0, length - 1, length));

            // Converting files modify its length so we don't specify the length on converted content
            if(!binaryResourceDownloadMeta.isConverted()){
                responseBuilder.header("Content-Length", length);
            }
        }else{
            responseBuilder = prepareStreamingDownloadResponse(binaryResourceDownloadMeta, binaryContentInputStream, range);
        }

        responseBuilder = applyCachePolicyToResponse(responseBuilder, binaryResourceDownloadMeta.getETag(), binaryResourceDownloadMeta.getLastModified());
        return responseBuilder.build();
    }

    private static Response.ResponseBuilder prepareStreamingDownloadResponse(BinaryResourceDownloadMeta binaryResourceDownloadMeta, InputStream binaryContentInputStream, String range) throws RequestedRangeNotSatisfiableException {
        long length = binaryResourceDownloadMeta.getLength();

        // Range header should match format "bytes=n-n,n-n,n-n...". If not, then return 416.
        if (!range.matches("^bytes=\\d*-\\d*(,\\d*-\\d*)*$")) {
            throw new RequestedRangeNotSatisfiableException("",length);
        }

        String[] ranges = range.split("=")[1].split("-");
        final long from = Integer.parseInt(ranges[0]);
        long to;
        if(ranges.length>1){
            to = Integer.parseInt(ranges[1]);
            to = (to> length -1) ? length-1 : to;
        }else{
            to = length - 1;
        }

        final String responseRange = String.format("bytes %d-%d/%d", from, to, length);

        return Response.status(Response.Status.PARTIAL_CONTENT)
                .header("Content-Disposition", binaryResourceDownloadMeta.getContentDisposition())
                .header("Accept-Ranges", "bytes")
                .header("Content-Length", length - from)
                .header("Content-Range", responseRange)
                .header("Content-Type", binaryResourceDownloadMeta.getContentType())
                .entity(new BinaryResourceBinaryStreamingOutput(binaryContentInputStream, from, length - 1, length));
    }

    /**
     * Log error & return a 500 error.
     * @param e The catched exception which cause the error.
     * @param fullName The full name of the wanted file.
     * @return A 500 error.
     */
    public static Response downloadError(Exception e, String fullName){
        String message = "Error while downloading the file : "+fullName;
        LOGGER.log(Level.SEVERE, message, e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .header("Reason-Phrase", message)
                .entity(message)
                .type(MediaType.TEXT_PLAIN)
                .build();
    }

    /**
     * Apply cache policy to a response.
     * @param response The response builder.
     * @param eTag The ETag of the resource.
     * @param lastModified The last modified date of the resource.
     * @return The response builder with the cache policy.
     */
    private static Response.ResponseBuilder applyCachePolicyToResponse(Response.ResponseBuilder response, EntityTag eTag,Date lastModified){
        CacheControl cc = new CacheControl();
        cc.setMaxAge(CACHE_SECOND);
        cc.setNoTransform(false);
        cc.setPrivate(false);

        Calendar expirationDate = Calendar.getInstance();
        expirationDate.add(Calendar.SECOND, CACHE_SECOND);

        return response.cacheControl(cc)
                .expires(expirationDate.getTime())
                .lastModified(lastModified)
                .tag(eTag);
    }

}