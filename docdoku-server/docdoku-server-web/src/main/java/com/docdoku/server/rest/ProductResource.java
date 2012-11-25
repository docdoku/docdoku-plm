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
import com.docdoku.core.product.ConfigSpec;
import com.docdoku.core.product.ConfigurationItem;
import com.docdoku.core.product.ConfigurationItemKey;
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
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;
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
    public ComponentDTO filterProductStructure(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId, @QueryParam("configSpec") String configSpecType, @QueryParam("partUsageLink") Integer partUsageLink, @QueryParam("depth") Integer depth) {
        try {
            ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);
            ConfigSpec cs = new LatestConfigSpec();
         
            PartUsageLink rootUsageLink = productService.filterProductStructure(ciKey, cs, partUsageLink, depth);
            
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

    private ComponentDTO createDTO(PartUsageLink usageLink, int depth) {
        PartMaster pm = usageLink.getComponent();
        ComponentDTO dto = new ComponentDTO();
        dto.setNumber(pm.getNumber());
        dto.setPartUsageLinkId(usageLink.getId());
        dto.setDescription(pm.getDescription());
        dto.setName(pm.getName());
        dto.setStandardPart(pm.isStandardPart());
        dto.setAuthor(pm.getAuthor().getName());
        dto.setAmount(usageLink.getCadInstances().size());

        List<InstanceAttributeDTO> lstAttributes = new ArrayList<InstanceAttributeDTO>();
        List<ComponentDTO> components = new ArrayList<ComponentDTO>();

        PartRevision partR = pm.getLastRevision();
        PartIteration partI = null;
        if(partR !=null)
            partI = partR.getLastIteration();
        
        if(partI !=null){
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

        dto.setAttributes(lstAttributes);
        dto.setComponents(components);
        return dto;
    }

    @GET
    @Path("{ciId}/instances")
    @Produces("application/json;charset=UTF-8")
    public Response getInstances(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId, @QueryParam("configSpec") String configSpecType, @QueryParam("path") String path) {
        try {
            ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);
            ConfigSpec cs = new LatestConfigSpec();

            String[] partUsageIdsString = path.split("-");
            List<Integer> usageLinkPaths = new ArrayList<Integer>();

            for (int i = 0; i < partUsageIdsString.length; i++) {
                usageLinkPaths.add(Integer.parseInt(partUsageIdsString[i]));
            }

            PartUsageLink rootUsageLink = productService.filterProductStructure(ciKey, cs, usageLinkPaths.get(0), 0);

            usageLinkPaths.remove(0);
            CacheControl cc = new CacheControl();
            //this request is resources consuming so we cache the response for 1 hour
            cc.setMaxAge(60*60);
            cc.setNoCache(false);
            return Response.ok().cacheControl(cc).entity(new InstanceCollection(rootUsageLink, usageLinkPaths)).build();

        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

}
