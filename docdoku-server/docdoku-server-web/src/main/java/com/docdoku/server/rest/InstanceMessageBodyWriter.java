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
import com.docdoku.core.product.CADInstance;
import com.docdoku.core.product.Geometry;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.product.PartUsageLink;
import com.docdoku.server.rest.dto.GeometryDTO;
import com.docdoku.server.rest.dto.InstanceAttributeDTO;
import com.docdoku.server.rest.dto.PartInstanceDTO;
import com.docdoku.server.rest.dto.TransformationDTO;
import com.sun.jersey.api.json.JSONJAXBContext;
import com.sun.jersey.api.json.JSONMarshaller;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.apache.commons.lang.StringUtils;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

/**
 *
 * @author Florent Garin
 */
@Provider
@Produces("application/json;charset=UTF-8")
public class InstanceMessageBodyWriter implements MessageBodyWriter<InstanceCollection> {

    private static final String CHARSET = "charset";
    
    private Mapper mapper = DozerBeanMapperSingletonWrapper.getInstance();
    

    private ThreadLocal<byte[]> tlComma = new ThreadLocal<byte[]>();
    private ThreadLocal<Boolean> tlAddComma = new ThreadLocal<Boolean>();
    
    private ThreadLocal<JSONMarshaller> tlMarshaller=new ThreadLocal<JSONMarshaller>();
    private ThreadLocal<OutputStream> tlEntityStream=new ThreadLocal<OutputStream>();
    
    @Context
    protected Providers providers;
    
    

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.equals(InstanceCollection.class);
    }

    @Override
    public long getSize(InstanceCollection t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(InstanceCollection object, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        try {
            //Class<?> domainClass = getDomainClass(genericType);
            setEntityStream(entityStream);
            setMarshaller(((JSONJAXBContext)getJAXBContext(PartInstanceDTO.class, mediaType)).createJSONMarshaller());
            Map<String, String> mediaTypeParameters = mediaType.getParameters();
            String charSet="UTF-8";
            if(mediaTypeParameters.containsKey(CHARSET)) {
                charSet = mediaTypeParameters.get(CHARSET);
                getMarshaller().setProperty(Marshaller.JAXB_ENCODING, charSet);
            }
            
            PartUsageLink rootUsageLink = object.getRootUsageLink();
            List<Integer> usageLinkPaths = object.getUsageLinkPaths();
            byte[] leftSquareBrace = "[".getBytes(charSet);
            byte[] rightSquareBrace = "]".getBytes(charSet);
            setComma(",".getBytes(charSet));
            
            setAddComma(false);
            getEntityStream().write(leftSquareBrace);
            generateInstanceStream(rootUsageLink, new ArrayList<TransformationDTO>(), usageLinkPaths, new ArrayList<Integer>());
            getEntityStream().write(rightSquareBrace);
        } catch (JAXBException ex) {
            throw new WebApplicationException(ex);
        }finally{
            tlEntityStream.remove();
            tlMarshaller.remove();
            tlAddComma.remove();
            tlComma.remove();
        }

    }

    private JAXBContext getJAXBContext(Class<?> type, MediaType mediaType) throws JAXBException {
        ContextResolver<JAXBContext> resolver = providers.getContextResolver(JAXBContext.class, mediaType);
        JAXBContext jaxbContext;
        if (resolver == null || (jaxbContext = resolver.getContext(type)) == null) {
            return JAXBContext.newInstance(type);
        } else {
            return jaxbContext;
        }
    }
    
    
    private void generateInstanceStream(PartUsageLink usageLink, List<TransformationDTO> transformations, List<Integer> filteredPath, List<Integer> instanceIds) throws JAXBException, IOException {
        
        
        //List<PartInstanceDTO> instancesDTO = new ArrayList<PartInstanceDTO>();

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
            List<Integer> copyInstanceIds = new ArrayList<Integer>(instanceIds);
            if(instance.getId()!=-1)
                copyInstanceIds.add(instance.getId());

            transformations.add(new TransformationDTO(instance.getTx(),instance.getTy(),instance.getTz(),instance.getRx(),instance.getRy(),instance.getRz()));

            String id = StringUtils.join(copyInstanceIds.toArray(),"-");
            
            if (!partI.isAssembly() && partI.getGeometries().size() > 0 && filteredPath.isEmpty()) {
                if(getAddComma())
                    getEntityStream().write(getComma());
                
                getMarshaller().marshallToJSON(new PartInstanceDTO(id, partIterationId, transformations, files, attributes), getEntityStream());
                setAddComma(true);
            } else {
                for (PartUsageLink component : partI.getComponents()) {
                    List<Integer> copyInstanceIds2 = new ArrayList<Integer>(copyInstanceIds);
                    List<TransformationDTO> copyTransformations = new ArrayList<>(transformations);

                    if (filteredPath.isEmpty()) {
                        generateInstanceStream(component, copyTransformations, filteredPath, copyInstanceIds2);
                    } else if (component.getId() == filteredPath.get(0)) {
                        List<Integer> copyWithoutCurrentId = new ArrayList<Integer>(filteredPath);
                        copyWithoutCurrentId.remove(0);
                        generateInstanceStream(component, copyTransformations, copyWithoutCurrentId, copyInstanceIds2);
                    }
                }
            }
        }
    }

    /*
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
    */

    
    private Class<?> getDomainClass(Type genericType) {
        if(genericType instanceof Class) {
            return (Class<?>) genericType;
        } else if(genericType instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];
        } else {
            return null;
        }
    }
    
    
    private byte[] getComma(){
        return tlComma.get();
    }
    
    private void setComma(byte[] c){
        tlComma.set(c);
    }
    
    private boolean getAddComma(){
        return tlAddComma.get();
    }
    
    private void setAddComma(boolean b){
        tlAddComma.set(b);
    }
    
    private OutputStream getEntityStream(){
        return tlEntityStream.get();
    }
    
    private void setEntityStream(OutputStream out){
        tlEntityStream.set(out);
    }
    
    private JSONMarshaller getMarshaller(){
        return tlMarshaller.get();
    }
    
    private void setMarshaller(JSONMarshaller m){
        tlMarshaller.set(m);
    }
    
}
