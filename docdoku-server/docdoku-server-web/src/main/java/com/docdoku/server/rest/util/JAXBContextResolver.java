/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.docdoku.server.rest.util;

import com.docdoku.server.rest.InstanceCollection;
import com.docdoku.server.rest.dto.InstanceDTO;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;

/**
 * @author Julien Maffre
 */
@Provider
public class JAXBContextResolver implements ContextResolver<JAXBContext> {

    private JAXBContext context;
    private Class[] types = {InstanceDTO.class};

    public JAXBContextResolver() throws Exception {
        this.context = new JSONJAXBContext(
                JSONConfiguration.natural().rootUnwrapping(true).build(),
                types);
    }

    @Override
    public JAXBContext getContext(Class<?> objectType) {
        for (Class type : types) {
            if (type.equals(objectType)) {
                return context;
            }
        }
        return null;

    }
}
