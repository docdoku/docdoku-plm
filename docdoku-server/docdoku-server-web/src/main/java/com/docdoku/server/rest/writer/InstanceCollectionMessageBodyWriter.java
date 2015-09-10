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
package com.docdoku.server.rest.writer;

import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.server.rest.collections.InstanceCollection;
import com.docdoku.server.rest.util.InstanceBodyWriterTools;

import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.vecmath.Matrix4d;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Florent Garin
 */
@Stateless
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class InstanceCollectionMessageBodyWriter implements MessageBodyWriter<InstanceCollection> {

    private Context context;

    private static final Logger LOGGER = Logger.getLogger(InstanceCollectionMessageBodyWriter.class.getName());

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.equals(InstanceCollection.class);
    }

    @Override
    public long getSize(InstanceCollection t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(InstanceCollection instanceCollection, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws UnsupportedEncodingException {

        try {
            context = new InitialContext();
            IProductManagerLocal productService = (IProductManagerLocal) context.lookup("java:global/docdoku-server-ear/docdoku-server-ejb/ProductManagerBean");

            String charSet="UTF-8";
            JsonGenerator jg = Json.createGenerator(new OutputStreamWriter(entityStream, charSet));
            jg.writeStartArray();

            Matrix4d gM=new Matrix4d();
            gM.setIdentity();
            InstanceBodyWriterTools.generateInstanceStreamWithGlobalMatrix(productService, null, gM, instanceCollection, new ArrayList<>(), jg);
            jg.writeEnd();
            jg.flush();

        } catch (NamingException e) {
            LOGGER.log(Level.SEVERE,null,e);
        }

    }
}
