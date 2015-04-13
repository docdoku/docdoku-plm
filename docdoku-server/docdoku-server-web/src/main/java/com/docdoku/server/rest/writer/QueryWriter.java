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

import com.docdoku.core.product.PartRevision;
import com.docdoku.server.rest.collections.QueryResult;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;


@Provider
@Produces(MediaType.APPLICATION_JSON)
public class QueryWriter implements MessageBodyWriter<QueryResult> {

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.equals(QueryResult.class);
    }

    @Override
    public long getSize(QueryResult queryResult, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(QueryResult queryResult, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> multivaluedMap, OutputStream outputStream) throws IOException, WebApplicationException {

        String charSet="UTF-8";
        JsonGenerator jg = Json.createGenerator(new OutputStreamWriter(outputStream, charSet));
        jg.writeStartArray();

        Set<String> selects = queryResult.getQuery().getSelects();

        for(PartRevision part : queryResult.getParts()){

            jg.writeStartObject();

            if(selects.contains("p.number")){
                jg.write("p.number",part.getPartNumber());
            }

            if(selects.contains("p.name")){
                String sName = part.getPartName();
                jg.write("p.name",sName != null ? sName : "");
            }

            if(selects.contains("p.type")){
                String sType = part.getType();
                jg.write("p.name",sType != null ? sType : "");
            }

            if(selects.contains("p.date")){
                jg.write("p.date", part.getLastIteration().getModificationDate().getTime());
            }

            if(selects.contains("p.life_cycle_state")){
                jg.write("p.life_cycle_state",part.getLifeCycleState());
            }

            jg.writeEnd();
        }

        jg.writeEnd();
        jg.flush();

    }

}
