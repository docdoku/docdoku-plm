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
package com.docdoku.server.rest;

import com.docdoku.core.common.User;
import com.docdoku.core.configuration.ConfigSpec;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.product.*;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IProductConfigSpecManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.server.rest.collections.InstanceCollection;
import com.docdoku.server.rest.collections.PathFilteredListInstanceCollection;
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
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private IProductConfigSpecManagerLocal productConfigSpecService;

    @EJB
    private LayerResource layerResource;
    @EJB
    private BaselinesResource baselinesResource;
    @EJB
    private ProductInstancesResource productInstancesResource;


    private static final Logger LOGGER = Logger.getLogger(ProductResource.class.getName());
    private Mapper mapper;

    public ProductResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ConfigurationItemDTO[] getRootProducts(@PathParam("workspaceId") String workspaceId)
            throws EntityNotFoundException, UserNotActiveException {

        String wksId = Tools.stripTrailingSlash(workspaceId);
        List<ConfigurationItem> cis = productService.getConfigurationItems(wksId);
        ConfigurationItemDTO[] dtos = new ConfigurationItemDTO[cis.size()];

        for (int i = 0; i < cis.size(); i++) {
            dtos[i] = new ConfigurationItemDTO(cis.get(i).getId(), cis.get(i).getWorkspaceId(), cis.get(i).getDescription(), cis.get(i).getDesignItem().getNumber());
        }

        return dtos;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response createConfigurationItem(ConfigurationItemDTO configurationItemDTO)
            throws EntityNotFoundException, EntityAlreadyExistsException, CreationException, AccessRightException, NotAllowedException {

        ConfigurationItem configurationItem = productService.createConfigurationItem(configurationItemDTO.getWorkspaceId(), configurationItemDTO.getId(), configurationItemDTO.getDescription(), configurationItemDTO.getDesignItemNumber());
        ConfigurationItemDTO configurationItemDTOCreated = mapper.map(configurationItem, ConfigurationItemDTO.class);
        configurationItemDTOCreated.setDesignItemNumber(configurationItem.getDesignItem().getNumber());

        try{
            return Response.created(URI.create(URLEncoder.encode(configurationItemDTOCreated.getId(),"UTF-8"))).entity(configurationItemDTOCreated).build();
        } catch (UnsupportedEncodingException ex) {
            LOGGER.log(Level.WARNING,null,ex);
            return Response.ok().build();
        }
    }

    @GET
    @Path("{ciId}/bom")
    @Produces(MediaType.APPLICATION_JSON)
    public PartDTO[] filterPart(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId, @QueryParam("configSpec") String configSpecType, @QueryParam("partUsageLink") Integer partUsageLink)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException {

        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);
        ConfigSpec cs = getConfigSpec(workspaceId,configSpecType,ciId);

        PartUsageLink rootUsageLink = productConfigSpecService.filterProductStructure(ciKey, cs, partUsageLink, 1);

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
    }

    @GET
    @Path("{ciId}")
    @Produces(MediaType.APPLICATION_JSON)
    public ComponentDTO filterProductStructure(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId, @QueryParam("configSpec") String configSpecType, @QueryParam("partUsageLink") Integer partUsageLink, @QueryParam("depth") Integer depth)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException {

        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);
        ConfigSpec cs = getConfigSpec(workspaceId,configSpecType,ciId);

        PartUsageLink rootUsageLink = productConfigSpecService.filterProductStructure(ciKey, cs, partUsageLink, depth);

        if (depth == null) {
            return createDTO(rootUsageLink, -1);
        } else {
            return createDTO(rootUsageLink, depth);
        }
    }

    @DELETE
    @Path("{ciId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteProduct(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId)
            throws EntityNotFoundException, NotAllowedException, AccessRightException, UserNotActiveException, EntityConstraintException {

        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);
        productService.deleteConfigurationItem(ciKey);
        return Response.ok().build();
    }

    @GET
    @Path("{ciId}/paths")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PathDTO> searchPaths(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId, @QueryParam("partNumber") String partNumber)
            throws EntityNotFoundException, UserNotActiveException {

        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);
        List<PartUsageLink[]> usagePaths = productService.findPartUsages(ciKey, new PartMasterKey(workspaceId,partNumber));

        List<PathDTO> pathsDTO = new ArrayList<>();

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
    }
    
    @Path("{ciId}/layers")
    public LayerResource processLayers(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId) {
        return layerResource;
    }

    private ComponentDTO createDTO(PartUsageLink usageLink, int depth)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {

        PartMaster pm = usageLink.getComponent();
        PartRevision partR = pm.getLastRevision();
        int newdepth = depth;

        ComponentDTO dto = new ComponentDTO();
        dto.setNumber(pm.getNumber());
        dto.setPartUsageLinkId(usageLink.getId());
        dto.setName(pm.getName());
        dto.setStandardPart(pm.isStandardPart());
        dto.setAuthor(pm.getAuthor().getName());
        dto.setAuthorLogin(pm.getAuthor().getLogin());
        dto.setAmount(usageLink.getAmount());
        dto.setReleased(partR.isReleased());
        dto.setUnit(usageLink.getUnit());

        List<InstanceAttributeDTO> lstAttributes = new ArrayList<>();
        List<ComponentDTO> components = new ArrayList<>();

        if (partR != null) {
            dto.setDescription(partR.getDescription());
            PartIteration partI = partR.getLastIteration();

            User checkoutUser = pm.getLastRevision().getCheckOutUser();
            if (checkoutUser != null) {
                dto.setCheckOutUser(mapper.map(pm.getLastRevision().getCheckOutUser(), UserDTO.class));
                dto.setCheckOutDate(pm.getLastRevision().getCheckOutDate());
            }

            dto.setVersion(partR.getVersion());
            try {
                productService.checkPartRevisionReadAccess(partR.getKey());
                dto.setAccessDeny(false);
                dto.setLastIterationNumber(productService.getNumberOfIteration(partR.getKey()));
            }catch (Exception e){
                LOGGER.log(Level.FINEST,null,e);
                dto.setLastIterationNumber(-1);
                dto.setAccessDeny(true);
            }

            if (partI != null) {
                for (InstanceAttribute attr : partI.getInstanceAttributes().values()) {
                    lstAttributes.add(mapper.map(attr, InstanceAttributeDTO.class));
                }
                if (newdepth != 0) {
                    newdepth--;
                    for (PartUsageLink component : partI.getComponents()) {
                        components.add(createDTO(component, newdepth));
                    }
                }
                dto.setAssembly(partI.isAssembly());
                dto.setIteration(partI.getIteration());
            }
        }

        dto.setAttributes(lstAttributes);
        dto.setComponents(components);
        return dto;
    }

    @GET
    @Path("{ciId}/instances")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInstances(@Context Request request, @PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId, @QueryParam("configSpec") String configSpecType, @QueryParam("path") String path)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException {

        Response.ResponseBuilder rb = fakeSimilarBehavior(request);
        if (rb != null) {
            return rb.build();
        } else {
            CacheControl cc = new CacheControl();
            //this request is resources consuming so we cache the response for 30 minutes
            cc.setMaxAge(60 * 15);

            ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);
            ConfigSpec cs = getConfigSpec(workspaceId, configSpecType,ciId);

            InstanceCollection instanceCollection = getInstancesCollection(ciKey,cs,path);

            return Response.ok().lastModified(new Date()).cacheControl(cc).entity(instanceCollection).build();
        }
    }

    @POST
    @Path("{ciId}/instances")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInstancesByMultiplePath(@Context Request request, @PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId, PathListDTO pathsDTO)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException {

        Response.ResponseBuilder rb = fakeSimilarBehavior(request);
        if (rb != null) {
            return rb.build();
        } else {
            CacheControl cc = new CacheControl();
            //this request is resources consuming so we cache the response for 30 minutes
            cc.setMaxAge(60 * 15);

            ConfigSpec cs = getConfigSpec(workspaceId,pathsDTO.getConfigSpec(),ciId);
            List<InstanceCollection> instanceCollections = getInstancesCollectionsList(workspaceId,ciId, cs,pathsDTO.getPaths());

            return Response.ok().lastModified(new Date()).cacheControl(cc).entity(new PathFilteredListInstanceCollection(instanceCollections, cs)).build();
        }
    }

    private List<InstanceCollection> getInstancesCollectionsList(String workspaceId, String ciId, ConfigSpec cs, String[] paths)
            throws EntityNotFoundException, UserNotActiveException, NotAllowedException, AccessRightException{

        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);

        List<InstanceCollection> instanceCollections = new ArrayList<>();
        for(String path : paths){
            InstanceCollection instanceCollection = getInstancesCollection(ciKey,cs,path);

            if(path == null || "null".equals(path) || "".equals(path)){
                instanceCollections.clear();
            }
            instanceCollections.add(instanceCollection);
        }
        return instanceCollections;
    }

    @GET
    @Path("{ciId}/releases/last")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLastRelease(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException {
        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);

        PartRevision partRevision = productService.getLastReleasePartRevision(ciKey);
        PartDTO partDTO = mapper.map(partRevision,PartDTO.class);
        partDTO.setNumber(partRevision.getPartNumber());
        partDTO.setPartKey(partRevision.getPartNumber() + "-" + partRevision.getVersion());
        partDTO.setName(partRevision.getPartMaster().getName());
        partDTO.setStandardPart(partRevision.getPartMaster().isStandardPart());

        return Response.ok(partDTO).build();
    }

    @Path("baselines")
    public BaselinesResource getAllBaselines(@PathParam("workspaceId") String workspaceId){
        return baselinesResource;
    }

    @Path("{ciId}/baselines")
    public BaselinesResource getBaselines(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId){
        return baselinesResource;
    }

    @Path("product-instances")
    public ProductInstancesResource getAllProductInstances(@PathParam("workspaceId") String workspaceId){
        return productInstancesResource;
    }

    @Path("{ciId}/product-instances")
    public ProductInstancesResource getProductInstances(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId){
        return productInstancesResource;
    }

    /**
     * Get a configuration specification
     * @param workspaceId The current workspace
     * @param configSpecType The configuration specification type
     * @return A configuration specification
     * @throws UserNotFoundException If the user login-workspace doesn't exist
     * @throws UserNotActiveException If the user is disabled
     * @throws WorkspaceNotFoundException If the workspace doesn't exist
     * @throws BaselineNotFoundException If the baseline doesn't exist
     */
    private ConfigSpec getConfigSpec(String workspaceId, String configSpecType, String ciId)
            throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, BaselineNotFoundException, ProductInstanceMasterNotFoundException {
        if(configSpecType==null){
            return productConfigSpecService.getLatestConfigSpec(workspaceId);
        }

        ConfigSpec cs;
        switch (configSpecType) {
            case "latest":
            case "undefined":
                cs = productConfigSpecService.getLatestConfigSpec(workspaceId);
                break;
            case "released":
                cs = productConfigSpecService.getLatestReleasedConfigSpec(workspaceId);
                break;
            default:
                if(configSpecType.startsWith("pi-")){
                    String serialNumber = configSpecType.substring(3);
                    cs = productConfigSpecService.getConfigSpecForProductInstance(workspaceId,ciId,serialNumber);
                }else{
                    cs = productConfigSpecService.getConfigSpecForBaseline(Integer.parseInt(configSpecType));
                }
                break;
        }
        return cs;
    }

    /**
     * Because some AS (like Glassfish) forbids the use of CacheControl
     * when authenticated we use the LastModified header to fake
     * a similar behavior (15 minutes of cache)
     * @param request The incomming request
     * @return Nothing if there still have cache
     */
    private Response.ResponseBuilder fakeSimilarBehavior(Request request){
        Calendar cal = new GregorianCalendar();
        cal.add(Calendar.MINUTE, -15);
        return request.evaluatePreconditions(cal.getTime());
    }

    /**
     * Return a InstanceCollection matching with a configurationItem, a configurationSpec and a node path
     * @param ciKey The ConfigurationItem wanted
     * @param cs The Specific ConfigurationSpec
     * @param path The path of the root node
     * @return The wanted InstanceCollection
     * @throws AccessRightException If the user can not get the configuration item
     * @throws NotAllowedException  If the user can not get the configuration item
     * @throws WorkspaceNotFoundException If the workspace was not found
     * @throws UserNotActiveException If the user was disabled
     * @throws UserNotFoundException If the user doesn't exist
     * @throws ConfigurationItemNotFoundException If the Configuration Item doesn't exist
     * @throws PartUsageLinkNotFoundException If the Part Usage Link doesn't exist
     */
    private InstanceCollection getInstancesCollection(ConfigurationItemKey ciKey, ConfigSpec cs, String path) throws AccessRightException, NotAllowedException, WorkspaceNotFoundException, UserNotActiveException, UserNotFoundException, ConfigurationItemNotFoundException, PartUsageLinkNotFoundException {
        PartUsageLink rootUsageLink;
        rootUsageLink = productConfigSpecService.getRootPartUsageLink(ciKey);
        List<Integer> usageLinkPaths = new ArrayList<>();
        if(path != null && !"null".equals(path) && !"".equals(path)) {
            String[] partUsageIdsString = path.split("-");
            for (String partUsageIdString : partUsageIdsString) {
                usageLinkPaths.add(Integer.parseInt(partUsageIdString));
            }
        }
        return new InstanceCollection(rootUsageLink, usageLinkPaths, cs);
    }
}