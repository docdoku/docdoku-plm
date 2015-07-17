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

import com.google.common.io.ByteStreams;

import javax.servlet.http.Part;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Taylor LABEJOF
 */
public class BinaryResourceUpload {
    private static final Logger LOGGER = Logger.getLogger(BinaryResourceUpload.class.getName());

    private BinaryResourceUpload(){
        super();
    }


    /**
     * Upload a form file in a specific output
     * @param outputStream BinaryResource output stream (in server vault repository)
     * @param formPart The form part list
     * @return The length of the file uploaded
     */
    public static long uploadBinary(OutputStream outputStream, Part formPart)
            throws IOException {
        long length;
        try(InputStream in = formPart.getInputStream();OutputStream out=outputStream) {
            length = ByteStreams.copy(in, out);
        }
        return length;
    }

    /**
     * Log error & return a 500 error.
     * @param e The exception which cause the error.
     * @return A 500 error.
     */
    public static Response uploadError(Exception e){
        String message = "Error while uploading the file(s).";
        LOGGER.log(Level.SEVERE, message, e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .header("Reason-Phrase", message)
                .entity(message)
                .type(MediaType.TEXT_PLAIN)
                .build();
    }

    public static Response tryToRespondCreated(String uri){
        try {
            return Response.created(new URI(uri)).build();
        } catch (URISyntaxException e) {
            LOGGER.log(Level.WARNING,null,e);
            return Response.ok().build();
        }
    }
}
