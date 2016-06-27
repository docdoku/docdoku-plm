package com.docdoku.server.rest.exceptions.mapper;

import javax.ejb.AccessLocalException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class AccessLocalExceptionMapper implements ExceptionMapper<AccessLocalException> {

    private static final Logger LOGGER = Logger.getLogger(AccessLocalExceptionMapper.class.getName());

    @Override
    public Response toResponse(AccessLocalException e) {
        LOGGER.log(Level.SEVERE, "Access denied : " + e.getMessage(), e);
        return Response.status(Response.Status.UNAUTHORIZED)
                .header("Reason-Phrase", "Access denied")
                .build();
    }
}
