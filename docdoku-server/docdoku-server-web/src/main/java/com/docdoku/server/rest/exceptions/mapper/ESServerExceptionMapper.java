package com.docdoku.server.rest.exceptions.mapper;

import com.docdoku.core.exceptions.ESServerException;

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
public class ESServerExceptionMapper implements ExceptionMapper<ESServerException> {
    private static final Logger LOGGER = Logger.getLogger(ESServerExceptionMapper.class.getName());
    public ESServerExceptionMapper() {
    }

    @Override
    public Response toResponse(ESServerException e) {
        LOGGER.log(Level.SEVERE,e.getMessage());
        LOGGER.log(Level.FINE,null,e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                       .header("Reason-Phrase", e.getMessage())
                       .entity(e.toString())
                       .type(MediaType.TEXT_PLAIN)
                       .build();
    }
}
