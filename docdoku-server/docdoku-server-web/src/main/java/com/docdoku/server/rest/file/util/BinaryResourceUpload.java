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
package com.docdoku.server.rest.file.util;

import com.google.common.io.ByteStreams;

import javax.servlet.http.Part;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
     * @param formPart The formulaire part list
     * @return The lenght of the file uploaded
     */
    public static long UploadBinary(OutputStream outputStream, Part formPart){
        InputStream inputStream = null;
        long length = -1;
        try {
            inputStream = formPart.getInputStream();
            length = ByteStreams.copy(inputStream, outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeInputStream(inputStream);
            closeOutputStream(outputStream);
        }

        return length;
    }


    private static void closeInputStream(InputStream inputStream){
        if(inputStream!=null){
            try {
                inputStream.close();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE,"A inputStream can not be close",e);
            }
        }
    }

    private static void closeOutputStream(OutputStream outputStream){
        if(outputStream!=null){
            try {
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "A outputStream can not be close", e);
            }
        }
    }

    /**
     * Log error & return a 500 error.
     * @param e The catched exception which cause the error.
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
}
