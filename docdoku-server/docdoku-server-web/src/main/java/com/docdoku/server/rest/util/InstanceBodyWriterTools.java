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
import com.docdoku.core.product.*;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.core.util.Tools;
import com.docdoku.server.rest.collections.InstanceCollection;
import com.docdoku.server.rest.collections.VirtualInstanceCollection;
import com.docdoku.server.rest.dto.InstanceAttributeDTO;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

import javax.json.stream.JsonGenerator;
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

    private static final Logger LOGGER = Logger.getLogger(InstanceBodyWriterTools.class.getName());
    private static Mapper mapper = DozerBeanMapperSingletonWrapper.getInstance();

    public static void generateInstanceStreamWithGlobalMatrix(IProductManagerLocal productService, List<PartLink> currentPath, Matrix4d matrix, InstanceCollection instanceCollection, List<Integer> instanceIds, JsonGenerator jg) {

        try {

            if(currentPath == null){
                PartLink rootPartUsageLink = productService.getRootPartUsageLink(instanceCollection.getCiKey());
                currentPath = new ArrayList<>();
                currentPath.add(rootPartUsageLink);
            }

            Component component = productService.filterProductStructure(instanceCollection.getCiKey(),
                    instanceCollection.getFilter(), currentPath,1);

            PartLink partLink = component.getPartLink();
            PartIteration partI = component.getRetainedIteration();

            for (CADInstance instance : partLink.getCadInstances()) {

                List<Integer> copyInstanceIds = new ArrayList<>(instanceIds);
                copyInstanceIds.add(instance.getId());

                Vector3d instanceTranslation = new Vector3d(instance.getTx(), instance.getTy(), instance.getTz());
                Vector3d instanceRotation = new Vector3d(instance.getRx(), instance.getRy(), instance.getRz());
                Matrix4d combinedMatrix = combineTransformation(matrix, instanceTranslation, instanceRotation);

                if (!partI.isAssembly() && !partI.getGeometries().isEmpty() && instanceCollection.isFiltered(currentPath)) {
                    writeLeaf(currentPath, copyInstanceIds, partI, combinedMatrix, jg);
                } else {
                    for (Component subComponent : component.getComponents()) {
                        generateInstanceStreamWithGlobalMatrix(productService, subComponent.getPath(), combinedMatrix, instanceCollection, copyInstanceIds, jg);
                    }
                }
            }

        } catch (PartMasterNotFoundException | PartUsageLinkNotFoundException | UserNotFoundException | WorkspaceNotFoundException | ConfigurationItemNotFoundException e) {
            LOGGER.log(Level.FINEST, null, e);
        } catch (AccessRightException | EntityConstraintException | NotAllowedException | UserNotActiveException e) {
            LOGGER.log(Level.FINEST, null, e);
        }

    }

    public static void generateInstanceStreamWithGlobalMatrix(List<PartLink> currentPath, Matrix4d matrix, VirtualInstanceCollection virtualInstanceCollection, List<Integer> instanceIds, JsonGenerator jg) {

        PartLink partLink = currentPath.get(currentPath.size()-1);
        PSFilter filter = virtualInstanceCollection.getFilter();
        List<PartIteration> filteredPartIterations = filter.filter(partLink.getComponent());

        if(!filteredPartIterations.isEmpty()){

            PartIteration partI = filteredPartIterations.iterator().next();

            for (CADInstance instance : partLink.getCadInstances()) {

                List<Integer> copyInstanceIds = new ArrayList<>(instanceIds);
                copyInstanceIds.add(instance.getId());

                Vector3d instanceTranslation = new Vector3d(instance.getTx(), instance.getTy(), instance.getTz());
                Vector3d instanceRotation = new Vector3d(instance.getRx(), instance.getRy(), instance.getRz());
                Matrix4d combinedMatrix = combineTransformation(matrix, instanceTranslation, instanceRotation);

                if (!partI.isAssembly() && !partI.getGeometries().isEmpty()) {
                    writeLeaf(currentPath, copyInstanceIds, partI, combinedMatrix, jg);
                } else {
                    for (PartLink subLink : partI.getComponents()) {
                        List<PartLink> subPath = new ArrayList<>(currentPath);
                        subPath.add(subLink);
                        generateInstanceStreamWithGlobalMatrix(subPath, combinedMatrix, virtualInstanceCollection, copyInstanceIds, jg);
                    }
                }
            }
        }

    }

    private static Matrix4d combineTransformation(Matrix4d matrix, Vector3d translation, Vector3d rotation){
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

    private static void writeLeaf(List<PartLink> currentPath, List<Integer> copyInstanceIds, PartIteration partI, Matrix4d combinedMatrix, JsonGenerator jg){
        String partIterationId = partI.toString();
        List<InstanceAttributeDTO> attributes = new ArrayList<>();
        for (InstanceAttribute attr : partI.getInstanceAttributes()) {
            attributes.add(mapper.map(attr, InstanceAttributeDTO.class));
        }

        jg.writeStartObject();
        jg.write("id", Tools.getPathInstanceAsString(currentPath, copyInstanceIds));
        jg.write("partIterationId", partIterationId);
        jg.write("path", Tools.getPathAsString(currentPath));

        writeMatrix(combinedMatrix, jg);
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
