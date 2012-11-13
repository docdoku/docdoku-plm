/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
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
package com.docdoku.server.rest;

import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.product.CADInstance;
import com.docdoku.core.product.ConfigSpec;
import com.docdoku.core.product.ConfigurationItem;
import com.docdoku.core.product.ConfigurationItemKey;
import com.docdoku.core.product.Geometry;
import com.docdoku.core.product.LatestConfigSpec;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.product.PartUsageLink;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.server.rest.dto.*;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.*;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

/**
 *
 * @author Florent Garin
 */
@Stateless
@Path("workspaces/{workspaceId}/products")
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class ProductResource {

    @EJB
    private IProductManagerLocal productService;
    private Mapper mapper;

    @EJB
    private LayerResource layerResource;
            
    public ProductResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    public ConfigurationItemDTO[] getRootProducts(@PathParam("workspaceId") String workspaceId) {
        try {

            String wksId = Tools.stripTrailingSlash(workspaceId);
            List<ConfigurationItem> cis = productService.getConfigurationItems(wksId);
            ConfigurationItemDTO[] dtos = new ConfigurationItemDTO[cis.size()];

            for (int i = 0; i < cis.size(); i++) {
                dtos[i] = new ConfigurationItemDTO(cis.get(i).getId(), cis.get(i).getWorkspaceId(), cis.get(i).getDescription());
            }

            return dtos;
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }

    }

    @GET
    @Path("{ciId}")
    @Produces("application/json;charset=UTF-8")
    public PartDTO filterProductStructure(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId, @QueryParam("configSpec") String configSpecType, @QueryParam("partUsageLink") Integer partUsageLink, @QueryParam("depth") Integer depth) {
        try {
            ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);
            ConfigSpec cs = new LatestConfigSpec();
         
            PartUsageLink rootUsageLink = productService.filterProductStructure(ciKey, cs, partUsageLink);
            
            if (depth==null)
                return createDTO(rootUsageLink, -1);
            else
                return createDTO(rootUsageLink, depth);
            
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }
    
    @Path("{ciId}/layers")
    public LayerResource processLayers(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId) {
        return layerResource;
    }

    private PartDTO createDTO(PartUsageLink usageLink, int depth) {
        PartMaster pm = usageLink.getComponent();
        
        PartDTO dto = new PartDTO();
        dto.setNumber(pm.getNumber());
        dto.setPartUsageLinkId(usageLink.getId());
        dto.setDescription(pm.getDescription());
        dto.setName(pm.getName());
        dto.setStandardPart(pm.isStandardPart());
        dto.setAuthor(pm.getAuthor().getName());

        List<GeometryDTO> lstFiles = new ArrayList<GeometryDTO>();
        List<CADInstanceDTO> lstInstances = new ArrayList<CADInstanceDTO>();
        List<InstanceAttributeDTO> lstAttributes = new ArrayList<InstanceAttributeDTO>();
        List<PartDTO> components = new ArrayList<PartDTO>();

        for (CADInstance cadInstance : usageLink.getCadInstances()) {
            lstInstances.add(mapper.map(cadInstance, CADInstanceDTO.class));
        }
        PartRevision partR = pm.getLastRevision();
        PartIteration partI = null;
        if(partR !=null)
            partI = partR.getLastIteration();
        
        if(partI !=null){
            for (Geometry geometry : partI.getGeometries()) {
                lstFiles.add(mapper.map(geometry, GeometryDTO.class));
            }
            for (InstanceAttribute attr : partI.getInstanceAttributes().values()) {
                lstAttributes.add(mapper.map(attr, InstanceAttributeDTO.class));
            }
            if(depth!=0){
                depth--;
                for (PartUsageLink component : partI.getComponents()) {
                    components.add(createDTO(component, depth));
                }
            }
            dto.setAssembly(partI.isAssembly());
            dto.setVersion(partI.getPartVersion());
            dto.setIteration(partI.getIteration());
        }
        
        
        dto.setFiles(lstFiles);
        dto.setInstances(lstInstances);
        dto.setAttributes(lstAttributes);
        dto.setComponents(components);
        return dto;
    }

    @GET
    @Path("{ciId}/instances")
    @Produces("application/json;charset=UTF-8")
    public ArrayList<InstanceDTO> getInstances(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId, @QueryParam("configSpec") String configSpecType, @QueryParam("path") String path) {
        try {

            ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);
            ConfigSpec cs = new LatestConfigSpec();

            String[] partUsageIdsString = path.split("-");
            ArrayList<Integer> usageLinksPath = new ArrayList<Integer>();

            for (int i = 0; i < partUsageIdsString.length; i++) {
                usageLinksPath.add(Integer.parseInt(partUsageIdsString[i]));
            }

            PartUsageLink rootUsageLink = productService.filterProductStructure(ciKey, cs, usageLinksPath.get(0));

            usageLinksPath.remove(0);

            ArrayList<InstanceDTO> instancesDTO = getInstancesDTO(rootUsageLink, 0, 0, 0, 0, 0, 0, usageLinksPath);

            return instancesDTO;

        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    public ArrayList<InstanceDTO> getInstancesDTO(PartUsageLink usageLink, double tx, double ty, double tz, double rx, double ry, double rz, ArrayList<Integer> filterPath) {

        ArrayList<InstanceDTO> instancesDTO = new ArrayList<InstanceDTO>();

        PartMaster pm = usageLink.getComponent();
        PartRevision partR = pm.getLastRevision();
        PartIteration partI = partR.getLastIteration();

        String partIterationId = new StringBuilder().append(pm.getNumber())
                .append("-")
                .append(partR.getVersion())
                .append("-")
                .append(partI.getIteration()).toString();

        for (CADInstance instance : usageLink.getCadInstances()) {

            //compute absolutes values
            double atx = tx + instance.getTx();
            double aty = ty + instance.getTy();
            double atz = tz + instance.getTz();
            double arx = rx + instance.getRx();
            double ary = ry + instance.getRy();
            double arz = rz + instance.getRz();

            if (partI.getGeometries().size() > 0) {
                instancesDTO.add(new InstanceDTO(partIterationId, atx, aty, atz, arx, ary, arz));
            } else {
                for (PartUsageLink component : partI.getComponents()) {
                    if (filterPath.isEmpty()) {
                        instancesDTO.addAll(getInstancesDTO(component, atx, aty, atz, arx, ary, arz, filterPath));
                    } else if (component.getId() == filterPath.get(0)) {
                        ArrayList<Integer> copyWithoutCurrentId = new ArrayList<Integer>(filterPath);
                        copyWithoutCurrentId.remove(0);
                        instancesDTO.addAll(getInstancesDTO(component, atx, aty, atz, arx, ary, arz, copyWithoutCurrentId));
                    }
                }
            }

        }

        return instancesDTO;
    }
}
