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

package com.docdoku.server.rest.util;

import com.docdoku.core.configuration.PSFilter;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.product.CADInstance;
import com.docdoku.core.product.Geometry;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartUsageLink;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.server.rest.dto.InstanceAttributeDTO;
import org.apache.commons.lang.StringUtils;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

import javax.json.stream.JsonGenerator;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Taylor LABEJOF
 */
public class InstanceBodyWriterTools {
    private static Context context;
    private static IProductManagerLocal productService;
    private static final Logger LOGGER = Logger.getLogger(InstanceBodyWriterTools.class.getName());
    private static Mapper mapper;

    static {
        try {
            context = new InitialContext();
            productService = (IProductManagerLocal) context.lookup("java:global/docdoku-server-ear/docdoku-server-ejb/ProductManagerBean");
            mapper = DozerBeanMapperSingletonWrapper.getInstance();
        } catch (NamingException e) {
            LOGGER.log(Level.WARNING, null, e);
        }
    }

    private InstanceBodyWriterTools(){
        super();
    }

    public static Matrix4d combineTransformation(Matrix4d matrix, Vector3d translation, Vector3d rotation){
        Matrix4d gM=new Matrix4d(matrix);
        Matrix4d m=new Matrix4d();

        m.setIdentity();
        m.setTranslation(translation);
        gM.mul(m);

        m.setIdentity();
        m.rotZ(rotation.z);
        gM.mul(m);

        m.setIdentity();
        m.rotY(rotation.y);
        gM.mul(m);

        m.setIdentity();
        m.rotX(rotation.x);
        gM.mul(m);

        return gM;
    }

    public static void generateInstanceStreamWithGlobalMatrix(PartUsageLink pUsageLink, Matrix4d matrix, List<Integer> filteredPath, PSFilter filter, List<Integer> instanceIds, JsonGenerator jg) {
        try {
            PartUsageLink usageLink = productService.getPartUsageLinkFiltered(pUsageLink, filter, 0);
            PartIteration partI = usageLink.getComponent().getLastRevision().getLastIteration();

            for (CADInstance instance : usageLink.getCadInstances()) {
                List<Integer> copyInstanceIds = new ArrayList<>(instanceIds);
                if (instance.getId() != -1) {
                    copyInstanceIds.add(instance.getId());
                }

                Vector3d instanceTranslation = new Vector3d(instance.getTx(), instance.getTy(), instance.getTz());
                Vector3d instanceRotation = new Vector3d(instance.getRx(), instance.getRy(), instance.getRz());
                Matrix4d combinedMatrix = combineTransformation(matrix, instanceTranslation, instanceRotation);

                if (!partI.isAssembly() && !partI.getGeometries().isEmpty() && filteredPath.isEmpty()) {
                    writeLeaf(partI,combinedMatrix,copyInstanceIds,jg);
                } else {
                    writeNode(partI,filter,filteredPath,combinedMatrix,copyInstanceIds,jg);
                }
            }
        } catch (NotAllowedException | UserNotFoundException | UserNotActiveException | AccessRightException e) {
            LOGGER.log(Level.WARNING, "You have no right to filter the usageLink : " + pUsageLink.getId(), e);
        } catch (WorkspaceNotFoundException | ConfigurationItemNotFoundException | PartUsageLinkNotFoundException e) {
            LOGGER.log(Level.WARNING, "Some resources are missing :", e);
        }
    }

    private static void writeNode(PartIteration partI, PSFilter filter, List<Integer> filteredPath, Matrix4d combinedMatrix, List<Integer> copyInstanceIds, JsonGenerator jg){
        for (PartUsageLink component : partI.getComponents()) {
            if (filteredPath.isEmpty()) {
                generateInstanceStreamWithGlobalMatrix(component, combinedMatrix, filteredPath, filter, copyInstanceIds, jg);

            } else if (component.getId() == filteredPath.get(0)) {
                List<Integer> copyWithoutCurrentId = new ArrayList<>(filteredPath);
                copyWithoutCurrentId.remove(0);
                generateInstanceStreamWithGlobalMatrix(component, combinedMatrix, copyWithoutCurrentId, filter, copyInstanceIds, jg);
            }
        }
    }
    private static void writeLeaf(PartIteration partI, Matrix4d combinedMatrix, List<Integer> copyInstanceIds, JsonGenerator jg){
        String id = StringUtils.join(copyInstanceIds.toArray(), "-");
        String partIterationId = partI.toString();
        List<InstanceAttributeDTO> attributes = new ArrayList<>();
        for (InstanceAttribute attr : partI.getInstanceAttributes()) {
            attributes.add(mapper.map(attr, InstanceAttributeDTO.class));
        }

        jg.writeStartObject();
        jg.write("id", id);
        jg.write("partIterationId", partIterationId);

        writeMatrix(combinedMatrix,jg);
        writeGeometries(partI.getSortedGeometries(),jg);
        writeAttributes(attributes,jg);

        jg.writeEnd();
        jg.flush();
    }

    private static void writeMatrix(Matrix4d matrix, JsonGenerator jg){
        jg.writeStartArray("matrix");
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                jg.write(matrix.getElement(i, j));
            }
        }
        jg.writeEnd();
    }
    private static void writeGeometries(List<Geometry> files,JsonGenerator jg){
        jg.write("qualities",files.size());

        if(!files.isEmpty()){
            Geometry geometry = files.get(0);
            jg.write("xMin", geometry.getxMin());
            jg.write("yMin", geometry.getyMin());
            jg.write("zMin", geometry.getzMin());
            jg.write("xMax", geometry.getxMax());
            jg.write("yMax", geometry.getyMax());
            jg.write("zMax", geometry.getzMax());
        }

        jg.writeStartArray("files");

        for (Geometry g : files) {
            jg.writeStartObject();
            jg.write("fullName", "api/files/" + g.getFullName());
            jg.writeEnd();
        }
        jg.writeEnd();
    }
    private static void writeAttributes(List<InstanceAttributeDTO> attributes, JsonGenerator jg){
        jg.writeStartArray("attributes");
        for (InstanceAttributeDTO a : attributes) {
            jg.writeStartObject();
            jg.write("name", a.getName());
            jg.write("type", a.getType().toString());
            jg.write("value", a.getValue());
            jg.writeEnd();
        }
        jg.writeEnd();
    }
}
