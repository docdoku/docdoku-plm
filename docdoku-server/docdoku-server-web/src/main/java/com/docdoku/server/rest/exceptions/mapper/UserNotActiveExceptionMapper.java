package com.docdoku.server.rest.exceptions.mapper;

import com.docdoku.core.exceptions.UserNotActiveException;

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
public class UserNotActiveExceptionMapper implements ExceptionMapper<UserNotActiveException> {
    private static final Logger LOGGER = Logger.getLogger(UserNotActiveExceptionMapper.class.getName());
    public UserNotActiveExceptionMapper() {
    }

    @Override
    public Response toResponse(UserNotActiveException e) {
        LOGGER.log(Level.WARNING,e.getMessage());
        LOGGER.log(Level.FINE,null,e);
        return Response.status(Response.Status.FORBIDDEN)
                       .header("Reason-Phrase", e.getMessage())
                       .entity(e.toString())
                       .type(MediaType.TEXT_PLAIN)
                       .build();
    }
}
