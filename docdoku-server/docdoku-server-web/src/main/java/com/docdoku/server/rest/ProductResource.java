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
import com.docdoku.server.rest.dto.CADInstanceDTO;
import com.docdoku.server.rest.dto.ConfigurationItemDTO;
import com.docdoku.server.rest.dto.GeometryDTO;
import com.docdoku.server.rest.dto.InstanceAttributeDTO;
import com.docdoku.server.rest.dto.PartDTO;
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
    public PartDTO filterProductStructure(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId, @QueryParam("configSpec") String configSpecType) {
        try {
            ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);
            ConfigSpec cs = new LatestConfigSpec();
            PartMaster root = productService.filterProductStructure(ciKey, cs);

            PartUsageLink rootUsageLink = new PartUsageLink();
            rootUsageLink.setComponent(root);

            return createDTO(rootUsageLink);
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }
    
    @Path("{ciId}/layers")
    @Produces("application/json;charset=UTF-8")
    public LayerResource processLayers(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId) {
        return layerResource;
    }

    private PartDTO createDTO(PartUsageLink usageLink) {
        PartMaster pm = usageLink.getComponent();
        
        PartDTO dto = new PartDTO(pm.getWorkspaceId(), pm.getNumber());
        dto.setDescription(pm.getDescription());
        dto.setName(pm.getName());
        dto.setStandardPart(pm.isStandardPart());

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
            for (PartUsageLink component : partI.getComponents()) {
                components.add(createDTO(component));
            }
            dto.setVersion(partI.getPartVersion());
            dto.setIteration(partI.getIteration());
        }
        
        
        dto.setFiles(lstFiles);
        dto.setInstances(lstInstances);
        dto.setAttributes(lstAttributes);
        dto.setComponents(components);
        return dto;
    }
}
