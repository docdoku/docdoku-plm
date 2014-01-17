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
import com.docdoku.core.configuration.Baseline;
import com.docdoku.core.configuration.ConfigSpec;
import com.docdoku.core.configuration.LatestConfigSpec;
import com.docdoku.core.exceptions.ApplicationException;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.product.*;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.server.rest.dto.*;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

import javax.annotation.PostConstruct;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.*;

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

    @EJB
    private LayerResource layerResource;

    private Mapper mapper;

    public ProductResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ConfigurationItemDTO[] getRootProducts(@PathParam("workspaceId") String workspaceId) {
        try {

            String wksId = Tools.stripTrailingSlash(workspaceId);
            List<ConfigurationItem> cis = productService.getConfigurationItems(wksId);
            ConfigurationItemDTO[] dtos = new ConfigurationItemDTO[cis.size()];

            for (int i = 0; i < cis.size(); i++) {
                dtos[i] = new ConfigurationItemDTO(cis.get(i).getId(), cis.get(i).getWorkspaceId(), cis.get(i).getDescription(), cis.get(i).getDesignItem().getNumber());
            }

            return dtos;
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }

    }

    @GET
    @Path("{ciId}/bom")
    @Produces(MediaType.APPLICATION_JSON)
    public PartDTO[] filterPart(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId, @QueryParam("configSpec") String configSpecType, @QueryParam("partUsageLink") Integer partUsageLink) {

        try {

            ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);

            ConfigSpec cs ;

            if(configSpecType == null || configSpecType.equals("latest")){
                cs = new LatestConfigSpec();
            }else{
                cs = productService.getConfigSpecForBaseline(ciKey,Integer.parseInt(configSpecType));
            }

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

        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @GET
    @Path("{ciId}")
    @Produces(MediaType.APPLICATION_JSON)
    public ComponentDTO filterProductStructure(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId, @QueryParam("configSpec") String configSpecType, @QueryParam("partUsageLink") Integer partUsageLink, @QueryParam("depth") Integer depth) {
        try {
            ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);
            ConfigSpec cs ;

            if(configSpecType == null || configSpecType.equals("latest")){
                cs = new LatestConfigSpec();
            }else{
                cs = productService.getConfigSpecForBaseline(ciKey,Integer.parseInt(configSpecType));
            }

            PartUsageLink rootUsageLink = productService.filterProductStructure(ciKey, cs, partUsageLink, depth);

            if (depth == null) {
                return createDTO(rootUsageLink, -1);
            } else {
                return createDTO(rootUsageLink, depth);
            }

        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @DELETE
    @Path("{ciId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteProduct(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId) {
        try {
            ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);
            productService.deleteConfigurationItem(ciKey);
            return Response.ok().build();
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @GET
    @Path("{ciId}/paths")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PathDTO> searchPaths(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId, @QueryParam("partNumber") String partNumber) {
        try {

            ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);
            List<PartUsageLink[]> usagePaths = productService.findPartUsages(ciKey, new PartMasterKey(workspaceId,partNumber));

            List<PathDTO> pathsDTO = new ArrayList<PathDTO>();

            for(PartUsageLink[] usagePath : usagePaths){
                StringBuilder sb=new StringBuilder();

                for(PartUsageLink link:usagePath){
                    sb.append(link.getId());
                    sb.append("-");
                }
                sb.deleteCharAt(sb.length()-1);
                pathsDTO.add(new PathDTO(sb.toString()));
            }



            return pathsDTO;

        } catch (ApplicationException ex) {
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
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInstances(@Context Request request, @PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId, @QueryParam("configSpec") String configSpecType, @QueryParam("path") String path) {
        try {
            //Because some AS (like Glassfish) forbids the use of CacheControl
            //when authenticated we use the LastModified header to fake
            //a similar behavior (15 minutes of cache)
            Calendar cal = new GregorianCalendar();
            cal.add(Calendar.MINUTE, -15);
            Response.ResponseBuilder rb = request.evaluatePreconditions(cal.getTime());
            if (rb != null) {
                return rb.build();
            } else {

                CacheControl cc = new CacheControl();
                //this request is resources consuming so we cache the response for 30 minutes
                cc.setMaxAge(60 * 15);

                ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);

                ConfigSpec cs;

                if(configSpecType == null || configSpecType.equals("latest") || configSpecType.equals("undefined")){
                    cs = new LatestConfigSpec();
                }else{
                    cs = productService.getConfigSpecForBaseline(ciKey,Integer.parseInt(configSpecType));
                }

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
                
                return Response.ok().lastModified(new Date()).cacheControl(cc).entity(new InstanceCollection(rootUsageLink, usageLinkPaths, cs)).build();
            }
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response createConfigurationItem(ConfigurationItemDTO configurationItemDTO) throws UnsupportedEncodingException {
        try {
            ConfigurationItem configurationItem = productService.createConfigurationItem(configurationItemDTO.getWorkspaceId(), configurationItemDTO.getId(), configurationItemDTO.getDescription(), configurationItemDTO.getDesignItemNumber());
            ConfigurationItemDTO configurationItemDTOCreated = mapper.map(configurationItem, ConfigurationItemDTO.class);
            configurationItemDTOCreated.setDesignItemNumber(configurationItem.getDesignItem().getNumber());
            return Response.created(URI.create(URLEncoder.encode(configurationItemDTOCreated.getId(),"UTF-8"))).entity(configurationItemDTOCreated).build();
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @GET
    @Path("{ciId}/baseline")
    @Produces(MediaType.APPLICATION_JSON)
    public List<BaselineDTO> getBaselines(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId){
        try {
            ConfigurationItemKey configurationItemKey = new ConfigurationItemKey(workspaceId,ciId);
            List<Baseline> baselines = productService.getBaselines(configurationItemKey);
            List<BaselineDTO> baselinesDTO = new ArrayList<BaselineDTO>();
            for(Baseline baseline:baselines){
                baselinesDTO.add(mapper.map(baseline,BaselineDTO.class));
            }
            return baselinesDTO;
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @GET
    @Path("{ciId}/baseline/{baselineId}")
    @Produces(MediaType.APPLICATION_JSON)
    public BaselineDTO getBaseline(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId, @PathParam("baselineId") String baselineId){
        try {
            ConfigurationItemKey configurationItemKey = new ConfigurationItemKey(workspaceId,ciId);
            Baseline baseline = productService.getBaseline(configurationItemKey,Integer.parseInt(baselineId));
            BaselineDTO baselineDTO = mapper.map(baseline,BaselineDTO.class);
            baselineDTO.setBaselinedParts(Tools.mapBaselinedPartsToBaselinedPartDTO(baseline));
            return baselineDTO;
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @POST
    @Path("{ciId}/baseline")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createBaseline(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId, BaselineCreationDTO baselineCreationDTO){
        try {
            productService.createBaseline(new ConfigurationItemKey(workspaceId,ciId),baselineCreationDTO.getName(),baselineCreationDTO.getDescription());
            return Response.ok().build();
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @PUT
    @Path("{ciId}/baseline/{baselineId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateBaseline(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId, @PathParam("baselineId") String baselineId, BaselineDTO baselineDTO){
        try {

            List<PartIterationKey> partIterationKeys = new ArrayList<PartIterationKey>();

            for(BaselinedPartDTO baselinedPartDTO : baselineDTO.getBaselinedParts()){
                partIterationKeys.add(new PartIterationKey(workspaceId, baselinedPartDTO.getNumber(),baselinedPartDTO.getVersion(),baselinedPartDTO.getIteration()));
            }

            productService.updateBaseline(new ConfigurationItemKey(workspaceId,ciId),Integer.parseInt(baselineId),baselineDTO.getName(),baselineDTO.getDescription(),partIterationKeys);
            return Response.ok().build();
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @POST
    @Path("{ciId}/baseline/{baselineId}/duplicate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public BaselineDTO duplicateBaseline(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId, @PathParam("baselineId") String baselineId,  BaselineCreationDTO baselineCreationDTO){
        try {
            Baseline baseline = productService.duplicateBaseline(new ConfigurationItemKey(workspaceId, ciId), Integer.parseInt(baselineId), baselineCreationDTO.getName(), baselineCreationDTO.getDescription());
            return mapper.map(baseline, BaselineDTO.class);
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @DELETE
    @Path("{ciId}/baseline/{baselineId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteBaseline(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId, @PathParam("baselineId") String baselineId){
        try {
            productService.deleteBaseline(new ConfigurationItemKey(workspaceId,ciId), Integer.parseInt(baselineId));
            return Response.ok().build();
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

}
