/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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
package com.docdoku.server.rest;

import com.docdoku.core.configuration.ConfigSpec;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.product.*;
import com.docdoku.server.rest.dto.GeometryDTO;
import com.docdoku.server.rest.dto.InstanceAttributeDTO;
import org.apache.commons.lang.StringUtils;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
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
 *
 * @author Florent Garin
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class InstanceMessageBodyWriter implements MessageBodyWriter<InstanceCollection> {

    private Mapper mapper = DozerBeanMapperSingletonWrapper.getInstance();


    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.equals(InstanceCollection.class);
    }

    @Override
    public long getSize(InstanceCollection t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(InstanceCollection object, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws UnsupportedEncodingException {
        String charSet="UTF-8";
        JsonGenerator jg = Json.createGenerator(new OutputStreamWriter(entityStream, charSet));
        PartUsageLink rootUsageLink = object.getRootUsageLink();
        List<Integer> usageLinkPaths = object.getUsageLinkPaths();
        ConfigSpec cs=object.getConfigSpec();
        jg.writeStartArray();
        Matrix4d gM=new Matrix4d();
        gM.setIdentity();
        generateInstanceStreamWithGlobalMatrix(rootUsageLink, gM, usageLinkPaths, cs, new ArrayList<Integer>(), jg);
        jg.writeEnd();
        jg.flush();
    }



    private void generateInstanceStreamWithGlobalMatrix(PartUsageLink usageLink, Matrix4d matrix, List<Integer> filteredPath, ConfigSpec cs, List<Integer> instanceIds, JsonGenerator jg) {

        PartMaster pm = usageLink.getComponent();
        PartIteration partI = cs.filterConfigSpec(pm);
        PartRevision partR = partI.getPartRevision();

        String partIterationId = new StringBuilder().append(pm.getNumber())
                .append("-")
                .append(partR.getVersion())
                .append("-")
                .append(partI.getIteration()).toString();

        List<GeometryDTO> files = new ArrayList<>();
        List<InstanceAttributeDTO> attributes = new ArrayList<>();

        for (Geometry geometry : partI.getGeometries()) {
            files.add(mapper.map(geometry, GeometryDTO.class));
        }

        for (InstanceAttribute attr : partI.getInstanceAttributes().values()) {
            attributes.add(mapper.map(attr, InstanceAttributeDTO.class));
        }


        for (CADInstance instance : usageLink.getCadInstances()) {
            List<Integer> copyInstanceIds = new ArrayList<>(instanceIds);
            if(instance.getId()!=-1)
                copyInstanceIds.add(instance.getId());


            Matrix4d combinedMatrix=combineTransformation(matrix,instance);

            String id = StringUtils.join(copyInstanceIds.toArray(),"-");

            if (!partI.isAssembly() && partI.getGeometries().size() > 0 && filteredPath.isEmpty()) {

                jg.writeStartObject();
                jg.write("id",id);
                jg.write("partIterationId",partIterationId);

                jg.writeStartArray("matrix");
                for(int i = 0;i<4;i++)
                    for(int j = 0;j<4;j++)
                        jg.write(combinedMatrix.getElement(i,j));

                jg.writeEnd();

                jg.writeStartArray("files");
                for(GeometryDTO g:files){
                    jg.writeStartObject();
                    jg.write("fullName", g.getFullName());
                    jg.write("quality", g.getQuality());
                    jg.write("radius", g.getRadius());
                    jg.writeEnd();
                }
                jg.writeEnd();

                jg.writeStartArray("attributes");
                for(InstanceAttributeDTO a:attributes){
                    jg.writeStartObject();
                    jg.write("name", a.getName());
                    jg.write("type", a.getType().toString());
                    jg.write("value", a.getValue());
                    jg.writeEnd();
                }
                jg.writeEnd();
                jg.writeEnd();
                jg.flush();

            } else {
                for (PartUsageLink component : partI.getComponents()) {
                    //List<Integer> copyInstanceIds2 = new ArrayList<>(copyInstanceIds);
                    //List<TransformationDTO> copyTransformations = new ArrayList<>(transformations);

                    if (filteredPath.isEmpty()) {
                        generateInstanceStreamWithGlobalMatrix(component, combinedMatrix, filteredPath, cs, copyInstanceIds, jg);

                    } else if (component.getId() == filteredPath.get(0)) {
                        List<Integer> copyWithoutCurrentId = new ArrayList<>(filteredPath);
                        copyWithoutCurrentId.remove(0);
                        generateInstanceStreamWithGlobalMatrix(component, combinedMatrix, copyWithoutCurrentId, cs, copyInstanceIds, jg);
                    }
                }
            }
        }
    }

    private Matrix4d combineTransformation(Matrix4d matrix, CADInstance i){
        Matrix4d gM=new Matrix4d(matrix);
        Matrix4d m=new Matrix4d();

        m.setIdentity();
        m.setTranslation(new Vector3d(i.getTx(),i.getTy(),i.getTz()));
        gM.mul(m);

        m.setIdentity();
        m.rotZ(i.getRz());
        gM.mul(m);

        m.setIdentity();
        m.rotY(i.getRy());
        gM.mul(m);

        m.setIdentity();
        m.rotX(i.getRx());
        gM.mul(m);

        return gM;
    }


}
