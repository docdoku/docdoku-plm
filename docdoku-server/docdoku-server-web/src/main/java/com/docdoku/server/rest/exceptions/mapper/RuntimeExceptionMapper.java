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
package com.docdoku.server.rest.exceptions.mapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Morgan Guimard
 */
@Provider
public class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {

    private static final Logger LOGGER = Logger.getLogger(RuntimeExceptionMapper.class.getName());

    private static final String MESSAGE_PREFIX = "Unhandled system error";

    public RuntimeExceptionMapper() {
    }

    @Override
    public Response toResponse(RuntimeException e) {

        LOGGER.log(Level.SEVERE, e.getMessage());
        LOGGER.log(Level.FINE,null,e);

        Throwable cause = e;
        while(cause.getCause() != null) {
            cause = cause.getCause();
        }

        StackTraceElement[] stackTrace = cause.getStackTrace();
        StackTraceElement firstTraceElement = stackTrace[0];
        String fileName = firstTraceElement.getFileName();
        String methodName = firstTraceElement.getMethodName();
        int lineNumber = firstTraceElement.getLineNumber();
        String className = firstTraceElement.getClassName();

        String fullMessage = MESSAGE_PREFIX
                            + " : "
                            + className +"."+methodName
                            + " threw "
                            + cause.toString()
                            + " in "
                            + fileName
                            + " at line "
                            + lineNumber;

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header("Reason-Phrase",fullMessage)
                    .entity(fullMessage)
                    .type(MediaType.TEXT_PLAIN)
                    .build();
    }

}
