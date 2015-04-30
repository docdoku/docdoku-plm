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

import com.docdoku.core.change.ModificationNotification;
import com.docdoku.core.common.User;
import com.docdoku.core.configuration.PSFilter;
import com.docdoku.core.configuration.PathChoice;
import com.docdoku.core.configuration.ProductBaseline;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.product.*;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IProductBaselineManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.server.rest.collections.InstanceCollection;
import com.docdoku.server.rest.dto.*;
import com.docdoku.server.rest.dto.baseline.BaselinedPartDTO;
import com.docdoku.server.rest.dto.baseline.PathChoiceDTO;
import com.docdoku.server.rest.util.FileExportEntity;
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
    private IProductBaselineManagerLocal productBaselineService;

    @EJB
    private LayerResource layerResource;
    @EJB
    private ProductConfigurationsResource productConfigurationsResource;
    @EJB
    private ProductBaselinesResource productBaselinesResource;
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
            ConfigurationItem ci = cis.get(i);
            dtos[i] = new ConfigurationItemDTO(mapper.map(ci.getAuthor(),UserDTO.class),ci.getId(), ci.getWorkspaceId(), ci.getDescription(), ci.getDesignItem().getNumber(),ci.getDesignItem().getName(), ci.getDesignItem().getLastRevision().getVersion());
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
        configurationItemDTOCreated.setDesignItemLatestVersion(configurationItem.getDesignItem().getLastRevision().getVersion());

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
    public PartDTO[] filterPart(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId, @QueryParam("configSpec") String configSpecType, @QueryParam("path") String path)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException, EntityConstraintException {

        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);
        PSFilter filter = productService.getPSFilter(ciKey, configSpecType);
        List<PartLink> decodedPath = productService.decodePath(ciKey, path);
        Component component = productService.filterProductStructure(ciKey, filter, decodedPath, 1);

        List<Component> components = component.getComponents();
        PartDTO[] partsDTO = new PartDTO[components.size()];

        for (int i = 0; i < components.size(); i++) {
            PartRevision lastRevision = components.get(i).getPartMaster().getLastRevision();
            partsDTO[i] = mapper.map(lastRevision, PartDTO.class);
            partsDTO[i].setNumber(lastRevision.getPartNumber());
            partsDTO[i].setPartKey(lastRevision.getPartNumber() + "-" + lastRevision.getVersion());
            partsDTO[i].setName(lastRevision.getPartMaster().getName());
            partsDTO[i].setStandardPart(lastRevision.getPartMaster().isStandardPart());

            List<ModificationNotificationDTO> notificationDTOs = getModificationNotificationDTOs(lastRevision);
            partsDTO[i].setNotifications(notificationDTOs);
        }

        return partsDTO;
    }

    @GET
    @Path("{ciId}/filter")
    @Produces(MediaType.APPLICATION_JSON)
    public ComponentDTO filterProductStructure(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId, @QueryParam("configSpec") String configSpecType, @QueryParam("path") String path, @QueryParam("depth") Integer depth)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException, EntityConstraintException {
        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);
        PSFilter filter = productService.getPSFilter(ciKey, configSpecType);
        List<PartLink> decodedPath = productService.decodePath(ciKey, path);
        Component component = productService.filterProductStructure(ciKey,filter,decodedPath,depth);
        String serialNumber = null;
        if(configSpecType != null && configSpecType.startsWith("pi-")){
            serialNumber = configSpecType.substring(3);
        }
        return createComponentDTO(component,workspaceId,ciId,serialNumber);
    }

    @GET
    @Path("{ciId}")
    @Produces(MediaType.APPLICATION_JSON)
    public ConfigurationItemDTO getConfigurationItem(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException, EntityConstraintException {
        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);
        ConfigurationItem ci = productService.getConfigurationItem(ciKey);

        return new ConfigurationItemDTO(mapper.map(ci.getAuthor(),UserDTO.class),ci.getId(), ci.getWorkspaceId(), ci.getDescription(), ci.getDesignItem().getNumber(),ci.getDesignItem().getName(), ci.getDesignItem().getLastRevision().getVersion());
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
    public List<PathDTO> searchPaths(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId, @QueryParam("partNumber") String partNumber, @QueryParam("configSpec") String configSpecType)
            throws EntityNotFoundException, UserNotActiveException, EntityConstraintException, NotAllowedException {

        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);
        PSFilter filter = productService.getPSFilter(ciKey, configSpecType);
        List<PartLink[]> usagePaths = productService.findPartUsages(ciKey, filter,partNumber);

        List<PathDTO> pathsDTO = new ArrayList<>();

        for(PartLink[] usagePath : usagePaths){
            String pathAsString = com.docdoku.core.util.Tools.getPathAsString(Arrays.asList(usagePath));
            pathsDTO.add(new PathDTO(pathAsString));
        }

        return pathsDTO;
    }
    
    @Path("{ciId}/layers")
    public LayerResource processLayers(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId) {
        return layerResource;
    }

    @GET
    @Path("{ciId}/path-choices")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PathChoiceDTO> getBaselineCreationPathChoices(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId, @QueryParam("type") String pType) throws ConfigurationItemNotFoundException, WorkspaceNotFoundException, UserNotActiveException, UserNotFoundException, PartMasterNotFoundException, NotAllowedException, EntityConstraintException {

        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);

        ProductBaseline.BaselineType type;

        if(pType == null || "LATEST".equals(pType)){
            type = ProductBaseline.BaselineType.LATEST;
        }else if("RELEASED".equals(pType)){
            type = ProductBaseline.BaselineType.RELEASED;
        }else{
            throw new IllegalArgumentException("Type must be either RELEASED or LATEST");
        }

        List<PathChoice> choices = productBaselineService.getBaselineCreationPathChoices(ciKey, type);

        List<PathChoiceDTO> pathChoiceDTOs = new ArrayList<>();

        for(PathChoice choice :choices){
            pathChoiceDTOs.add(new PathChoiceDTO(choice));
        }

        return pathChoiceDTOs;
    }

    @GET
    @Path("{ciId}/versions-choices")
    @Produces(MediaType.APPLICATION_JSON)
    public List<BaselinedPartDTO> getBaselineCreationVersionsChoices(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId) throws ConfigurationItemNotFoundException, WorkspaceNotFoundException, UserNotActiveException, UserNotFoundException, PartMasterNotFoundException, NotAllowedException, EntityConstraintException {

        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);

        List<PartIteration> parts = productBaselineService.getBaselineCreationVersionsChoices(ciKey);

        // Serve the latest first
        parts.sort(Comparator.<PartIteration>reverseOrder());

        HashMap<PartMaster,List<PartIteration>> map = new HashMap<>();

        for(PartIteration partIteration : parts){
            PartMaster partMaster = partIteration.getPartRevision().getPartMaster();
            if(map.get(partMaster) == null){
                map.put(partMaster,new ArrayList<>());
            }
            map.get(partMaster).add(partIteration);
        }

        List<BaselinedPartDTO> partsDTO = new ArrayList<>();

        for(Map.Entry<PartMaster, List<PartIteration>> entry : map.entrySet()){
            List<PartIteration> availableParts = entry.getValue();
            if(availableParts.size() == 1 && !availableParts.get(0).getPartRevision().isReleased() || availableParts.size() > 1){
                partsDTO.add(new BaselinedPartDTO(availableParts));
            }
        }

        return partsDTO;
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
            PSFilter filter = productService.getPSFilter(ciKey, configSpecType);
            List<PartLink> decodedPath = productService.decodePath(ciKey,path);

            List<List<PartLink>> paths = new ArrayList<>();

            if(decodedPath != null){
                paths.add(decodedPath);
            }

            InstanceCollection instanceCollection = new InstanceCollection(ciKey, filter, paths);

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

            ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);

            PSFilter filter = productService.getPSFilter(ciKey, pathsDTO.getConfigSpec());

            List<List<PartLink>> paths = new ArrayList<>();

            for(String path : pathsDTO.getPaths()){
                List<PartLink> decodedPath = productService.decodePath(ciKey,path);
                if(decodedPath != null) {
                    paths.add(decodedPath);
                }
            }

            InstanceCollection instanceCollection = new InstanceCollection(ciKey,filter,paths);

            return Response.ok().lastModified(new Date()).cacheControl(cc).entity(instanceCollection).build();
        }
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

    @Path("configurations")
    public ProductConfigurationsResource getAllConfigurations(@PathParam("workspaceId") String workspaceId){
        return productConfigurationsResource;
    }

    @Path("{ciId}/configurations")
    public ProductConfigurationsResource getConfigurations(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId){
        return productConfigurationsResource;
    }


    @Path("baselines")
    public ProductBaselinesResource getAllBaselines(@PathParam("workspaceId") String workspaceId){
        return productBaselinesResource;
    }

    @Path("{ciId}/baselines")
    public ProductBaselinesResource getBaselines(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId){
        return productBaselinesResource;
    }

    @Path("product-instances")
    public ProductInstancesResource getAllProductInstances(@PathParam("workspaceId") String workspaceId){
        return productInstancesResource;
    }

    @Path("{ciId}/product-instances")
    public ProductInstancesResource getProductInstances(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId){
        return productInstancesResource;
    }

    @GET
    @Path("{ciId}/export-files")
    public Response exportFiles(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String ciId, @QueryParam("configSpecType") String configSpecType) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, BaselineNotFoundException, ProductInstanceMasterNotFoundException {
        FileExportEntity fileExportEntity = new FileExportEntity();
        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);
        PSFilter psFilter = productService.getPSFilter(ciKey,configSpecType);
        fileExportEntity.setPsFilter(psFilter);
        fileExportEntity.setConfigurationItemKey(ciKey);
        return Response.ok()
                .header("Content-Type", "application/download")
                .header("Content-Disposition", "attachment; filename=\"export.zip\"")
                .entity(fileExportEntity).build();
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

    private ComponentDTO createComponentDTO(Component component, String workspaceId, String configurationItemId, String serialNumber)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {

        PartMaster pm = component.getPartMaster();
        PartIteration retainedIteration = component.getRetainedIteration();

        if(retainedIteration == null){
            return null;
        }

        PartRevision partR = retainedIteration.getPartRevision();

        List<PartLink> path = component.getPath();
        PartLink usageLink = path.get(path.size()-1);

        ComponentDTO dto = new ComponentDTO();
        dto.setNumber(pm.getNumber());
        dto.setPartUsageLinkId(usageLink.getFullId());
        dto.setName(pm.getName());
        dto.setStandardPart(pm.isStandardPart());
        dto.setAuthor(pm.getAuthor().getName());
        dto.setAuthorLogin(pm.getAuthor().getLogin());
        dto.setAmount(usageLink.getAmount());
        dto.setUnit(usageLink.getUnit());
        dto.setSubstitute(usageLink instanceof PartSubstituteLink);
        dto.setVersion(retainedIteration.getVersion());
        dto.setIteration(retainedIteration.getIteration());
        dto.setReleased(partR.isReleased());
        dto.setObsolete(partR.isObsolete());
        dto.setDescription(partR.getDescription());
        dto.setPartUsageLinkReferenceDescription(usageLink.getReferenceDescription());
        dto.setOptional(usageLink.isOptional());

        List<PartSubstituteLink> substitutes = usageLink.getSubstitutes();
        if(substitutes != null){
            dto.setHasSubstitutes(!substitutes.isEmpty());
        }else{
            dto.setHasSubstitutes(false);
        }

        List<InstanceAttributeDTO> lstAttributes = new ArrayList<>();
        List<ComponentDTO> components = new ArrayList<>();

        User checkoutUser = partR.getCheckOutUser();
        if (checkoutUser != null) {
            dto.setCheckOutUser(mapper.map(partR.getCheckOutUser(), UserDTO.class));
            dto.setCheckOutDate(partR.getCheckOutDate());
        }

        try {
            productService.checkPartRevisionReadAccess(partR.getKey());
            dto.setAccessDeny(false);
            dto.setLastIterationNumber(productService.getNumberOfIteration(partR.getKey()));
        }catch (Exception e){
            LOGGER.log(Level.FINEST,null,e);
            dto.setLastIterationNumber(-1);
            dto.setAccessDeny(true);
        }


        for (InstanceAttribute attr : retainedIteration.getInstanceAttributes()) {
            lstAttributes.add(mapper.map(attr, InstanceAttributeDTO.class));
        }

        if(serialNumber != null){
            PathDataMasterDTO pathData = productInstancesResource.getPathData(workspaceId, configurationItemId, serialNumber, com.docdoku.core.util.Tools.getPathAsString(path));
            dto.setHasPathData(!pathData.getPathDataIterations().isEmpty());
        }

        for (Component subComponent : component.getComponents()) {
            ComponentDTO componentDTO = createComponentDTO(subComponent, workspaceId,configurationItemId,serialNumber);
            if(componentDTO != null) {
                components.add(componentDTO);
            }
        }

        dto.setAssembly(retainedIteration.isAssembly());
        dto.setAttributes(lstAttributes);
        dto.setNotifications(getModificationNotificationDTOs(partR));
        dto.setComponents(components);

        return dto;
    }

    /**
     * Return a list of ModificationNotificationDTO matching with a given PartRevision
     * @param partRevision The specified PartRevision
     * @return A list of ModificationNotificationDTO
     * @throws EntityNotFoundException If an entity doesn't exist
     * @throws AccessRightException If the user can not get the modification notifications
     * @throws UserNotActiveException If the user is disabled
     */
    private List<ModificationNotificationDTO> getModificationNotificationDTOs(PartRevision partRevision)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException {

        PartIterationKey iterationKey = new PartIterationKey(partRevision.getKey(), partRevision.getLastIterationNumber());
        List<ModificationNotification> notifications = productService.getModificationNotifications(iterationKey);
        return Tools.mapModificationNotificationsToModificationNotificationDTO(notifications);
    }
}