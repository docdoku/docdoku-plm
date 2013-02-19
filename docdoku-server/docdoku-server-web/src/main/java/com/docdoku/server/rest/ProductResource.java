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

import com.docdoku.core.common.User;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.product.ConfigSpec;
import com.docdoku.core.product.ConfigurationItem;
import com.docdoku.core.product.ConfigurationItemKey;
import com.docdoku.core.product.LatestConfigSpec;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartMasterKey;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.product.PartUsageLink;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.server.rest.dto.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

/**
 *
 * @author Florent Garin
 */
@Stateless
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
                dtos[i] = new ConfigurationItemDTO(cis.get(i).getId(), cis.get(i).getWorkspaceId(), cis.get(i).getDescription(), null);
            }

            return dtos;
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }

    }

    @GET
    @Path("{ciId}/bom")
    @Produces("application/json;charset=UTF-8")
    public PartDTO[] filterPart(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId, @QueryParam("configSpec") String configSpecType, @QueryParam("partUsageLink") Integer partUsageLink) {

        try {

            ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);
            ConfigSpec cs = new LatestConfigSpec();

            PartUsageLink rootUsageLink = productService.filterProductStructure(ciKey, cs, partUsageLink, 1);

            List<PartUsageLink> components = rootUsageLink.getComponent().getLastRevision().getLastIteration().getComponents();

            PartDTO[] partsDTO = new PartDTO[components.size()];

            for (int i = 0; i < components.size(); i++) {
                PartRevision lastRevision = components.get(i).getComponent().getLastRevision();
                partsDTO[i] = mapper.map(lastRevision, PartDTO.class);
                partsDTO[i].setNumber(lastRevision.getPartNumber());
                partsDTO[i].setPartKey(lastRevision.getPartNumber() + "-" + lastRevision.getVersion());
                partsDTO[i].setName(lastRevision.getPartMaster().getName());
                partsDTO[i].setStandardPart(lastRevision.getPartMaster().isStandardPart());
            }

            return partsDTO;

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

            if (depth == null) {
                return createDTO(rootUsageLink, -1);
            } else {
                return createDTO(rootUsageLink, depth);
            }

        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @GET
    @Path("{ciId}/paths")
    @Produces("application/json;charset=UTF-8")
    public String[] searchPaths(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId, @QueryParam("partNumber") String partNumber) {
        try {
            ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);
            List<PartUsageLink[]> usagePaths = productService.findPartUsages(ciKey, new PartMasterKey(workspaceId,partNumber));
            String[] paths=new String[usagePaths.size()];
            
            for(int i=0;i<usagePaths.size();i++){
                StringBuilder sb=new StringBuilder();
                PartUsageLink[] usagePath=usagePaths.get(i);
                for(PartUsageLink link:usagePath){
                    sb.append(link.getId());
                    sb.append("-");
                }
                sb.deleteCharAt(sb.length()-1);
                paths[i]=sb.toString();
            }
            return paths;
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
        dto.setAuthorLogin(pm.getAuthor().getLogin());
        dto.setAmount(usageLink.getCadInstances().size());

        List<InstanceAttributeDTO> lstAttributes = new ArrayList<InstanceAttributeDTO>();
        List<ComponentDTO> components = new ArrayList<ComponentDTO>();

        PartRevision partR = pm.getLastRevision();
        PartIteration partI = null;
        if (partR != null) {
            partI = partR.getLastIteration();

            User checkoutUser = pm.getLastRevision().getCheckOutUser();
            if (checkoutUser != null) {
                dto.setCheckOutUser(mapper.map(pm.getLastRevision().getCheckOutUser(), UserDTO.class));
                dto.setCheckOutDate(pm.getLastRevision().getCheckOutDate());
            }

            dto.setVersion(partR.getVersion());
        }

        if (partI != null) {
            for (InstanceAttribute attr : partI.getInstanceAttributes().values()) {
                lstAttributes.add(mapper.map(attr, InstanceAttributeDTO.class));
            }
            if (depth != 0) {
                depth--;
                for (PartUsageLink component : partI.getComponents()) {
                    components.add(createDTO(component, depth));
                }
            }
            dto.setAssembly(partI.isAssembly());
            dto.setIteration(partI.getIteration());
        }

        dto.setAttributes(lstAttributes);
        dto.setComponents(components);
        return dto;
    }

    @GET
    @Path("{ciId}/instances")
    @Produces("application/json;charset=UTF-8")
    public Response getInstances(@Context Request request, @PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId, @QueryParam("configSpec") String configSpecType, @QueryParam("path") String path) {
        try {
            //Because some AS (like Glassfish) forbids the use of CacheControl
            //when authenticated we use the LastModified header to fake
            //a similar behavior (12 hours of cache)
            Calendar cal = new GregorianCalendar();
            cal.add(Calendar.HOUR, -12);
            Response.ResponseBuilder rb = request.evaluatePreconditions(cal.getTime());
            if (rb != null) {
                return rb.build();
            } else {

                CacheControl cc = new CacheControl();
                //this request is resources consuming so we cache the response for 12 hours
                cc.setMaxAge(60 * 60 * 12);

                ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);
                //TODO configSpecType should be used
                ConfigSpec cs = new LatestConfigSpec();
                PartUsageLink rootUsageLink;
                List<Integer> usageLinkPaths = new ArrayList<Integer>();
                if(path != null && !path.equals("null")){
                    String[] partUsageIdsString = path.split("-");

                    for (int i = 0; i < partUsageIdsString.length; i++) {
                        usageLinkPaths.add(Integer.parseInt(partUsageIdsString[i]));
                    }

                    rootUsageLink = productService.filterProductStructure(ciKey, cs, usageLinkPaths.get(0), 0);
                    usageLinkPaths.remove(0);
                }else{
                    rootUsageLink = productService.filterProductStructure(ciKey, cs, null, 0);                  
                }
                
                return Response.ok().lastModified(new Date()).cacheControl(cc).entity(new InstanceCollection(rootUsageLink, usageLinkPaths)).build();
            }
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @POST
    @Produces("application/json;charset=UTF-8")
    public Response createConfigurationItem(ConfigurationItemDTO configurationItemDTO) {
        try {
            ConfigurationItem configurationItem = productService.createConfigurationItem(configurationItemDTO.getWorkspaceId(), configurationItemDTO.getId(), configurationItemDTO.getDescription(), configurationItemDTO.getDesignItemNumber());
            ConfigurationItemDTO configurationItemDTOCreated = mapper.map(configurationItem, ConfigurationItemDTO.class);
            return Response.created(URI.create(configurationItemDTOCreated.getId())).entity(configurationItemDTOCreated).build();
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

}
