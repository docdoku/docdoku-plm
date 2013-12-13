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
import org.apache.commons.lang.StringUtils;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
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
@Produces("application/json;charset=UTF-8")
public class InstanceMessageBodyWriter implements MessageBodyWriter<InstanceCollection> {

    private static final String CHARSET = "charset";
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
        Map<String, String> mediaTypeParameters = mediaType.getParameters();
        if(mediaTypeParameters.containsKey(CHARSET)) {
            charSet = mediaTypeParameters.get(CHARSET);
        }
        JsonGenerator jg = Json.createGenerator(new OutputStreamWriter(entityStream, charSet));

        PartUsageLink rootUsageLink = object.getRootUsageLink();
        List<Integer> usageLinkPaths = object.getUsageLinkPaths();
        jg.writeStartArray();
        generateInstanceStream(rootUsageLink, 0, 0, 0, 0, 0, 0, usageLinkPaths, new ArrayList<Integer>(),jg);
        jg.writeEnd();
        jg.close();
    }
    
    
    private void generateInstanceStream(PartUsageLink usageLink, double tx, double ty, double tz, double rx, double ry, double rz, List<Integer> filteredPath, List<Integer> instanceIds, JsonGenerator jg) {
        
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
            ArrayList<Integer> copyInstanceIds = new ArrayList<Integer>(instanceIds);
            if(instance.getId()!=-1)
                copyInstanceIds.add(instance.getId());

            //compute absolutes values
            double atx = tx + getRelativeTxAfterParentRotation(rx, ry, rz, instance.getTx(), instance.getTy(), instance.getTz());
            double aty = ty + getRelativeTyAfterParentRotation(rx, ry, rz, instance.getTx(), instance.getTy(), instance.getTz());
            double atz = tz + getRelativeTzAfterParentRotation(rx, ry, rz, instance.getTx(), instance.getTy(), instance.getTz());
            double arx = rx + instance.getRx();
            double ary = ry + instance.getRy();
            double arz = rz + instance.getRz();
            String id = StringUtils.join(copyInstanceIds.toArray(),"-");
            
            if (!partI.isAssembly() && partI.getGeometries().size() > 0 && filteredPath.isEmpty()) {

                jg.writeStartObject();
                jg.write("id",id);
                jg.write("partIterationId",partIterationId);
                jg.write("tx",atx);
                jg.write("ty",aty);
                jg.write("tz",atz);
                jg.write("rx",arx);
                jg.write("ry",ary);
                jg.write("rz",arz);
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
                    ArrayList<Integer> copyInstanceIds2 = new ArrayList<Integer>(copyInstanceIds);
                    if (filteredPath.isEmpty()) {
                        generateInstanceStream(component, atx, aty, atz, arx, ary, arz, filteredPath, copyInstanceIds2, jg);
                    } else if (component.getId() == filteredPath.get(0)) {
                        ArrayList<Integer> copyWithoutCurrentId = new ArrayList<Integer>(filteredPath);
                        copyWithoutCurrentId.remove(0);
                        generateInstanceStream(component, atx, aty, atz, arx, ary, arz, copyWithoutCurrentId, copyInstanceIds2, jg);
                    }
                }
            }
        }
    }

    private double[] afterRx(double rx, double tx, double ty, double tz){
        double[] pts=new double[3];
        pts[0]=tx;
        pts[1]=ty*Math.cos(rx) - tz*Math.sin(rx);
        pts[2]=ty*Math.sin(rx) + tz*Math.cos(rx);
        return pts;
    }
    private double[] afterRy(double ry, double tx, double ty, double tz){
        double[] pts=new double[3];
        pts[0]=tz*Math.sin(ry)+tx*Math.cos(ry);
        pts[1]=ty;
        pts[2]=tz*Math.cos(ry) - tx*Math.sin(ry);
        return pts;
    }
    private double[] afterRz(double rz, double tx, double ty, double tz){
        double[] pts=new double[3];
        pts[0]=tx*Math.cos(rz)-ty*Math.sin(rz);
        pts[1]=tx*Math.sin(rz)+ ty*Math.cos(rz);
        pts[2]=tz;
        return pts;
    }
    private double[] getTAfterParentRotation(double rx, double ry, double rz, double tx, double ty, double tz){
        double[] pts=afterRx(rx,tx,ty,tz);
        pts=afterRy(ry,pts[0],pts[1],pts[2]);
        pts=afterRz(rz,pts[0],pts[1],pts[2]);
        return pts;
    }


    private double getRelativeTxAfterParentRotation(double rx, double ry, double rz, double tx, double ty, double tz){

        double a = Math.cos(ry) * Math.cos(rz);
        double b = -Math.cos(rx) * Math.sin(rz) + Math.sin(rx) * Math.sin(ry) * Math.cos(rz);
        double c = Math.sin(rx) * Math.sin(rz) + Math.cos(rx) * Math.sin(ry) * Math.cos(rz);

        return a*tx + b*ty + c*tz;
    }

    private double getRelativeTyAfterParentRotation(double rx, double ry, double rz, double tx, double ty, double tz){

        double d = Math.cos(ry) * Math.sin(rz);
        double e = Math.cos(rx) * Math.cos(rz) + Math.sin(rx) * Math.sin(ry) * Math.sin(rz);
        double f = -Math.sin(rx) * Math.cos(rz) + Math.cos(rx) * Math.sin(ry) * Math.sin(rz);

        return d*tx + e*ty + f*tz;
    }

    private double getRelativeTzAfterParentRotation(double rx, double ry, double rz, double tx, double ty, double tz){

        double g = -Math.sin(ry);
        double h = Math.sin(rx) * Math.cos(ry);
        double i = Math.cos(rx) * Math.cos(ry);

        return g*tx + h*ty + i*tz;
    }
    /*

    private JAXBContext getJAXBContext(Class<?> type, MediaType mediaType) throws JAXBException {
        ContextResolver<JAXBContext> resolver = providers.getContextResolver(JAXBContext.class, mediaType);
        JAXBContext jaxbContext;
        if (resolver == null || (jaxbContext = resolver.getContext(type)) == null) {
            return JAXBContext.newInstance(type);
        } else {
            return jaxbContext;
        }
    }

    private Class<?> getDomainClass(Type genericType) {
        if(genericType instanceof Class) {
            return (Class<?>) genericType;
        } else if(genericType instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];
        } else {
            return null;
        }
    }

    */
    
}
