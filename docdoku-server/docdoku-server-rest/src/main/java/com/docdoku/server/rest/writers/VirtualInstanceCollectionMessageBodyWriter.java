/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
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
package com.docdoku.server.rest.writers;

import com.docdoku.core.product.CADInstance;
import com.docdoku.core.product.PartLink;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartSubstituteLink;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.server.rest.collections.VirtualInstanceCollection;
import com.docdoku.server.rest.util.InstanceBodyWriterTools;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
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
import java.util.List;

/**
 * @author Morgan Guimard
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class VirtualInstanceCollectionMessageBodyWriter implements MessageBodyWriter<VirtualInstanceCollection> {

    @Inject
    private IProductManagerLocal productService;

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.equals(VirtualInstanceCollection.class);
    }

    @Override
    public long getSize(VirtualInstanceCollection virtualInstanceCollection, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(VirtualInstanceCollection virtualInstanceCollection, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws UnsupportedEncodingException {
        String charSet = "UTF-8";
        JsonGenerator jg = Json.createGenerator(new OutputStreamWriter(entityStream, charSet));
        jg.writeStartArray();

        Matrix4d gM = new Matrix4d();
        gM.setIdentity();

        PartLink virtualRootPartLink = getVirtualRootPartLink(virtualInstanceCollection);
        List<PartLink> path = new ArrayList<>();
        path.add(virtualRootPartLink);
        InstanceBodyWriterTools.generateInstanceStreamWithGlobalMatrix(productService, path, gM, virtualInstanceCollection, new ArrayList<>(), jg);
        jg.writeEnd();
        jg.flush();
    }


    private PartLink getVirtualRootPartLink(VirtualInstanceCollection virtualInstanceCollection) {
        return new PartLink() {
            @Override
            public int getId() {
                return 1;
            }

            @Override
            public Character getCode() {
                return '-';
            }

            @Override
            public String getFullId() {
                return "-1";
            }

            @Override
            public double getAmount() {
                return 1;
            }

            @Override
            public String getUnit() {
                return null;
            }

            @Override
            public String getComment() {
                return virtualInstanceCollection.getRootPart().getDescription();
            }

            @Override
            public boolean isOptional() {
                return false;
            }

            @Override
            public PartMaster getComponent() {
                return virtualInstanceCollection.getRootPart().getPartMaster();
            }

            @Override
            public List<PartSubstituteLink> getSubstitutes() {
                return null;
            }

            @Override
            public String getReferenceDescription() {
                return virtualInstanceCollection.getRootPart().getDescription();
            }

            @Override
            public List<CADInstance> getCadInstances() {
                CADInstance virtualInstance = new CADInstance(0, 0, 0, 0, 0, 0);
                List<CADInstance> virtualCadInstances = new ArrayList<>();
                virtualCadInstances.add(virtualInstance);
                return virtualCadInstances;
            }
        };

    }
}
