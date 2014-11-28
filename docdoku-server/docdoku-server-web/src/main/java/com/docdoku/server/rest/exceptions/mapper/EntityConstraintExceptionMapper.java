package com.docdoku.server.rest.exceptions.mapper;

import com.docdoku.core.exceptions.EntityConstraintException;

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
public class EntityConstraintExceptionMapper implements ExceptionMapper<EntityConstraintException> {
    private static final Logger LOGGER = Logger.getLogger(EntityConstraintExceptionMapper.class.getName());
    public EntityConstraintExceptionMapper() {
    }

    @Override
    public Response toResponse(EntityConstraintException e) {
        LOGGER.log(Level.WARNING,e.getMessage());
        LOGGER.log(Level.FINE,null,e);
        return Response.status(Response.Status.BAD_REQUEST)
                       .header("Reason-Phrase", e.getMessage())
                       .entity(e.toString())
                       .type(MediaType.TEXT_PLAIN)
                       .build();
    }
}
