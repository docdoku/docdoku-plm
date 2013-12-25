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

import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.product.*;
import com.docdoku.server.rest.dto.GeometryDTO;
import com.docdoku.server.rest.dto.InstanceAttributeDTO;
import com.docdoku.server.rest.dto.TransformationDTO;
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
import java.util.Map;

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
        jg.writeStartArray();
        generateInstanceStreamWithGlobalMatrix(rootUsageLink, new ArrayList<TransformationDTO>(), usageLinkPaths, new ArrayList<Integer>(), jg);
        jg.writeEnd();
        jg.flush();
    }



    private void generateInstanceStreamWithGlobalMatrix(PartUsageLink usageLink, List<TransformationDTO> transformations, List<Integer> filteredPath, List<Integer> instanceIds, JsonGenerator jg) {

        PartMaster pm = usageLink.getComponent();
        PartRevision partR = pm.getLastRevision();
        PartIteration partI = partR.getLastIteration();

        String partIterationId = new StringBuilder().append(pm.getNumber())
                .append("-")
                .append(partR.getVersion())
                .append("-")
                .append(partI.getIteration()).toString();

        List<GeometryDTO> files = new ArrayList<GeometryDTO>();
        List<InstanceAttributeDTO> attributes = new ArrayList<InstanceAttributeDTO>();

        for (Geometry geometry : partI.getGeometries()) {
            files.add(mapper.map(geometry, GeometryDTO.class));
        }

        for (InstanceAttribute attr : partI.getInstanceAttributes().values()) {
            attributes.add(mapper.map(attr, InstanceAttributeDTO.class));
        }


        for (CADInstance instance : usageLink.getCadInstances()) {
            if(instance.getId()!=-1)
                instanceIds.add(instance.getId());

            transformations.add(new TransformationDTO(instance.getTx(),instance.getTy(),instance.getTz(),instance.getRx(),instance.getRy(),instance.getRz()));

            String id = StringUtils.join(instanceIds.toArray(),"-");

            if (!partI.isAssembly() && partI.getGeometries().size() > 0 && filteredPath.isEmpty()) {

                jg.writeStartObject();
                jg.write("id",id);
                jg.write("partIterationId",partIterationId);

                jg.write("transformations", combineTransformations(transformations).toString());

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
                    List<Integer> copyInstanceIds = new ArrayList<>(instanceIds);
                    List<TransformationDTO> copyTransformations = new ArrayList<>(transformations);

                    if (filteredPath.isEmpty()) {
                        generateInstanceStreamWithGlobalMatrix(component, copyTransformations, filteredPath, copyInstanceIds, jg);

                    } else if (component.getId() == filteredPath.get(0)) {
                        List<Integer> copyWithoutCurrentId = new ArrayList<>(filteredPath);
                        copyWithoutCurrentId.remove(0);
                        generateInstanceStreamWithGlobalMatrix(component, copyTransformations, copyWithoutCurrentId, copyInstanceIds, jg);
                    }
                }
            }
        }
    }

    private Matrix4d combineTransformations(List<TransformationDTO> transformations){
        Matrix4d gM=new Matrix4d();
        gM.setIdentity();
        Matrix4d m=new Matrix4d();
        for(TransformationDTO t:transformations){
            m.setIdentity();
            m.setTranslation(new Vector3d(t.getTx(),t.getTy(),t.getTz()));
            gM.mul(m);

            m.setIdentity();
            m.rotZ(t.getRz());
            gM.mul(m);

            m.setIdentity();
            m.rotY(t.getRy());
            gM.mul(m);

            m.setIdentity();
            m.rotX(t.getRx());
            gM.mul(m);
        }

        return gM;
    }



    private void generateInstanceStreamWithAllTransformations(PartUsageLink usageLink, List<TransformationDTO> transformations, List<Integer> filteredPath, List<Integer> instanceIds, JsonGenerator jg) {

        PartMaster pm = usageLink.getComponent();
        PartRevision partR = pm.getLastRevision();
        PartIteration partI = partR.getLastIteration();

        String partIterationId = new StringBuilder().append(pm.getNumber())
                .append("-")
                .append(partR.getVersion())
                .append("-")
                .append(partI.getIteration()).toString();

        List<GeometryDTO> files = new ArrayList<GeometryDTO>();
        List<InstanceAttributeDTO> attributes = new ArrayList<InstanceAttributeDTO>();

        for (Geometry geometry : partI.getGeometries()) {
            files.add(mapper.map(geometry, GeometryDTO.class));
        }

        for (InstanceAttribute attr : partI.getInstanceAttributes().values()) {
            attributes.add(mapper.map(attr, InstanceAttributeDTO.class));
        }


        for (CADInstance instance : usageLink.getCadInstances()) {
            if(instance.getId()!=-1)
                instanceIds.add(instance.getId());

            transformations.add(new TransformationDTO(instance.getTx(),instance.getTy(),instance.getTz(),instance.getRx(),instance.getRy(),instance.getRz()));

            String id = StringUtils.join(instanceIds.toArray(),"-");
            
            if (!partI.isAssembly() && partI.getGeometries().size() > 0 && filteredPath.isEmpty()) {

                jg.writeStartObject();
                jg.write("id",id);
                jg.write("partIterationId",partIterationId);

                jg.writeStartArray("transformations");
                for(TransformationDTO t:transformations){
                    jg.writeStartObject();
                    jg.write("tx",t.getTx());
                    jg.write("ty",t.getTy());
                    jg.write("tz",t.getTz());
                    jg.write("rx",t.getRx());
                    jg.write("ry",t.getRy());
                    jg.write("rz",t.getRz());
                    jg.writeEnd();
                }
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
                    List<Integer> copyInstanceIds = new ArrayList<>(instanceIds);
                    List<TransformationDTO> copyTransformations = new ArrayList<>(transformations);

                    if (filteredPath.isEmpty()) {
                        generateInstanceStreamWithAllTransformations(component, copyTransformations, filteredPath, copyInstanceIds, jg);

                    } else if (component.getId() == filteredPath.get(0)) {
                        List<Integer> copyWithoutCurrentId = new ArrayList<>(filteredPath);
                        copyWithoutCurrentId.remove(0);
                        generateInstanceStreamWithAllTransformations(component, copyTransformations, copyWithoutCurrentId, copyInstanceIds, jg);
                    }
                }
            }
        }
    }
    
}
