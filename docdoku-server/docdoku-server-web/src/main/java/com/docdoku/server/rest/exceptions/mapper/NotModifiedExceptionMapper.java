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

import com.docdoku.server.rest.exceptions.NotModifiedException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Taylor LABEJOF
 */
@Provider
public class NotModifiedExceptionMapper implements ExceptionMapper<NotModifiedException> {
    private static final Logger LOGGER = Logger.getLogger(NotModifiedExceptionMapper.class.getName());
    public NotModifiedExceptionMapper() {
    }

    @Override
    public Response toResponse(NotModifiedException e) {
        LOGGER.log(Level.FINE,null,e);
        return Response.status(Response.Status.NOT_MODIFIED)
                       .expires(e.getExpireDate())
                       .tag(e.getETag())
                       .build();
    }
}
