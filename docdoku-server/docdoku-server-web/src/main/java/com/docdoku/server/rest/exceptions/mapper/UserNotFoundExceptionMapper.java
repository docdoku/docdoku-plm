package com.docdoku.server.rest.exceptions.mapper;

import com.docdoku.core.exceptions.UserNotFoundException;

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
public class UserNotFoundExceptionMapper implements ExceptionMapper<UserNotFoundException> {
    private static final Logger LOGGER = Logger.getLogger(UserNotFoundExceptionMapper.class.getName());
    public UserNotFoundExceptionMapper() {
    }

    @Override
    public Response toResponse(UserNotFoundException e) {
        LOGGER.log(Level.WARNING,e.getMessage());
        LOGGER.log(Level.FINE,null,e);
        return Response.status(Response.Status.FORBIDDEN)
                       .header("Reason-Phrase", e.getMessage())
                       .entity(e.toString())
                       .type(MediaType.TEXT_PLAIN)
                       .build();
    }
}
