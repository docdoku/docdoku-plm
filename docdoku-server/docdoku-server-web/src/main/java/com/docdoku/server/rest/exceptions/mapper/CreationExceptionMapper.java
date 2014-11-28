package com.docdoku.server.rest.exceptions.mapper;

import com.docdoku.core.exceptions.CreationException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Taylor LABEJOF
 */
@Provider
public class CreationExceptionMapper implements ExceptionMapper<CreationException> {
    private static final Logger LOGGER = Logger.getLogger(CreationExceptionMapper.class.getName());
    public CreationExceptionMapper() {
    }

    @Override
    public Response toResponse(CreationException e) {
        LOGGER.log(Level.SEVERE,e.getMessage());
        LOGGER.log(Level.FINE,null,e);
        return Response.status(Response.Status.BAD_REQUEST)
                       .header("Reason-Phrase", e.getMessage())
                       .entity(e.toString())
                       .type(MediaType.TEXT_PLAIN)
                       .build();
    }
}
