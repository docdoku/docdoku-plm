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
import com.docdoku.core.configuration.CascadeResult;
import com.docdoku.core.configuration.PathChoice;
import com.docdoku.core.configuration.*;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentIterationLink;
import com.docdoku.core.document.DocumentLink;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.product.*;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.ICascadeActionManagerLocal;
import com.docdoku.core.services.IPSFilterManagerLocal;
import com.docdoku.core.services.IProductBaselineManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.server.rest.collections.InstanceCollection;
import com.docdoku.server.rest.dto.*;
import com.docdoku.server.rest.dto.baseline.BaselinedPartDTO;
import com.docdoku.server.rest.dto.baseline.PathChoiceDTO;
import com.docdoku.server.rest.util.FileDownloadTools;
import com.docdoku.server.rest.util.FileExportProductEntity;
import io.swagger.annotations.*;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

import javax.annotation.PostConstruct;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Florent Garin
 */
@RequestScoped
@Api(hidden = true, value = "products", description = "Operations about products")
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class ProductResource {

    private static final Logger LOGGER = Logger.getLogger(ProductResource.class.getName());
    @Inject
    private IProductManagerLocal productService;
    @Inject
    private IProductBaselineManagerLocal productBaselineService;
    @Inject
    private ICascadeActionManagerLocal cascadeActionService;
    @Inject
    private IPSFilterManagerLocal psFilterService;
    @Inject
    private LayerResource layerResource;
    @Inject
    private ProductConfigurationsResource productConfigurationsResource;
    @Inject
    private ProductBaselinesResource productBaselinesResource;
    @Inject
    private ProductInstancesResource productInstancesResource;
    private Mapper mapper;

    public ProductResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @ApiOperation(value = "Get configuration items",
            response = ConfigurationItemDTO.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of ConfigurationItemDTOs. It can be an empty list."),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public ConfigurationItemDTO[] getConfigurationItems(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId)
            throws EntityNotFoundException, UserNotActiveException, EntityConstraintException, NotAllowedException {

        String wksId = Tools.stripTrailingSlash(workspaceId);
        List<ConfigurationItem> cis = productService.getConfigurationItems(wksId);
        ConfigurationItemDTO[] dtos = new ConfigurationItemDTO[cis.size()];

        for (int i = 0; i < cis.size(); i++) {
            ConfigurationItem ci = cis.get(i);
            dtos[i] = new ConfigurationItemDTO(mapper.map(ci.getAuthor(), UserDTO.class), ci.getId(), ci.getWorkspaceId(),
                    ci.getDescription(), ci.getDesignItem().getNumber(), ci.getDesignItem().getName(), ci.getDesignItem().getLastRevision().getVersion());
            dtos[i].setPathToPathLinks(getPathToPathLinksForGivenConfigurationItem(ci));
            // TODO : find a better way to detect modification notifications on products. Too heavy for big structures.
            //dtos[i].setHasModificationNotification(productService.hasModificationNotification(ci.getKey()));
        }

        return dtos;
    }

    @GET
    @ApiOperation(value = "Search configuration items", response = ConfigurationItemDTO.class, responseContainer = "List")
    @Path("numbers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchConfigurationItemId(@ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
                                              @ApiParam(required = true, value = "Query") @QueryParam("q") String q)
            throws UserNotActiveException, UserNotFoundException, WorkspaceNotEnabledException, WorkspaceNotFoundException {

        String wksId = Tools.stripTrailingSlash(workspaceId);
        List<ConfigurationItem> cis = productService.getConfigurationItems(wksId);
        List<ConfigurationItemDTO> ciDTOs = new ArrayList<>();

        for(int i=0; i<cis.size() && ciDTOs.size() < 8; i++) {
            ConfigurationItem ci = cis.get(i);

            if(ci.getId().contains(q)) {
                ciDTOs.add(new ConfigurationItemDTO(mapper.map(ci.getAuthor(), UserDTO.class), ci.getId(), ci.getWorkspaceId(),
                        ci.getDescription(), ci.getDesignItem().getNumber(), ci.getDesignItem().getName(), ci.getDesignItem().getLastRevision().getVersion()));
            }
        }

        return Response.ok(new GenericEntity<List<ConfigurationItemDTO>>(ciDTOs) {
        }).build();
    }

    @POST
    @ApiOperation(value = "Create configuration item",
            response = ConfigurationItemDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful retrieval of ConfigurationItemDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response createConfigurationItem(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Product to create") ConfigurationItemDTO configurationItemDTO)
            throws EntityNotFoundException, EntityAlreadyExistsException, CreationException, AccessRightException,
            NotAllowedException {

        ConfigurationItem configurationItem = productService.createConfigurationItem(configurationItemDTO.getWorkspaceId(), configurationItemDTO.getId(), configurationItemDTO.getDescription(), configurationItemDTO.getDesignItemNumber());
        ConfigurationItemDTO configurationItemDTOCreated = mapper.map(configurationItem, ConfigurationItemDTO.class);
        configurationItemDTOCreated.setDesignItemNumber(configurationItem.getDesignItem().getNumber());
        configurationItemDTOCreated.setDesignItemLatestVersion(configurationItem.getDesignItem().getLastRevision().getVersion());

        try {
            return Response.created(URI.create(URLEncoder.encode(configurationItemDTOCreated.getId(), "UTF-8"))).entity(configurationItemDTOCreated).build();
        } catch (UnsupportedEncodingException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            return Response.ok().entity(configurationItemDTOCreated).build();
        }
    }


    @GET
    @ApiOperation(value = "Filter part",
            response = PartRevisionDTO.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of PartRevisionDTOs. It can be an empty list."),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{ciId}/bom")
    @Produces(MediaType.APPLICATION_JSON)
    public PartRevisionDTO[] filterPart(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String ciId,
            @ApiParam(required = false, value = "Config spec") @QueryParam("configSpec") String configSpecType,
            @ApiParam(required = false, value = "Complete path of part") @QueryParam("path") String path,
            @ApiParam(required = false, value = "Discover substitute links") @QueryParam("diverge") boolean diverge)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException,
            EntityConstraintException {

        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);
        ProductStructureFilter filter = psFilterService.getPSFilter(ciKey, configSpecType, diverge);
        List<PartLink> decodedPath = productService.decodePath(ciKey, path);
        Component component = productService.filterProductStructure(ciKey, filter, decodedPath, 1);

        List<Component> components = component.getComponents();
        List<PartRevisionDTO> partsRevisions = new ArrayList<>();
        for (int i = 0; i < components.size(); i++) {
            PartIteration retainedIteration = components.get(i).getRetainedIteration();
            //If no iteration has been retained, then take the last revision (the first one).
            PartRevision partRevision = retainedIteration == null ? components.get(i).getPartMaster().getLastRevision() : retainedIteration.getPartRevision();
            if (!productService.canAccess(partRevision.getKey())) {
                continue;
            }
            PartRevisionDTO dto = mapper.map(partRevision, PartRevisionDTO.class);
            dto.getPartIterations().clear();
            //specify the iteration only if an iteration has been retained.
            if (retainedIteration != null) {
                dto.getPartIterations().add(mapper.map(retainedIteration, PartIterationDTO.class));
            }
            dto.setNumber(partRevision.getPartNumber());
            dto.setPartKey(partRevision.getPartNumber() + "-" + partRevision.getVersion());
            dto.setName(partRevision.getPartMaster().getName());
            dto.setStandardPart(partRevision.getPartMaster().isStandardPart());

            List<ModificationNotificationDTO> notificationDTOs = getModificationNotificationDTOs(partRevision);
            dto.setNotifications(notificationDTOs);
            partsRevisions.add(dto);
        }

        return partsRevisions.toArray(new PartRevisionDTO[partsRevisions.size()]);
    }

    @GET
    @ApiOperation(value = "Filter product structure",
            response = ComponentDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of ComponentDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{ciId}/filter")
    @Produces(MediaType.APPLICATION_JSON)
    public ComponentDTO filterProductStructure(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String ciId,
            @ApiParam(required = false, value = "Config spec") @QueryParam("configSpec") String configSpecType,
            @ApiParam(required = false, value = "Complete path of part") @QueryParam("path") String path,
            @ApiParam(required = false, value = "Depth to stop at") @QueryParam("depth") Integer depth,
            @ApiParam(required = false, value = "Type link to filter") @QueryParam("linkType") String linkType,
            @ApiParam(required = false, value = "Discover substitute links") @QueryParam("diverge") boolean diverge)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException,
            EntityConstraintException {

        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);
        ProductStructureFilter filter = psFilterService.getPSFilter(ciKey, configSpecType, diverge);
        Component component;

        if (linkType == null) {
            List<PartLink> decodedPath = productService.decodePath(ciKey, path);
            component = productService.filterProductStructure(ciKey, filter, decodedPath, depth);
        } else {
            component = productService.filterProductStructureOnLinkType(ciKey, filter, configSpecType, path, linkType);
        }

        if (component == null) {
            throw new IllegalArgumentException();
        }

        String serialNumber = null;
        if (configSpecType.startsWith("pi-")) {
            serialNumber = configSpecType.substring(3);
        }

        return createComponentDTO(component, workspaceId, ciId, serialNumber);
    }

    @GET
    @ApiOperation(value = "Get configuration item",
            response = ConfigurationItemDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of ConfigurationItemDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{ciId}")
    @Produces(MediaType.APPLICATION_JSON)
    public ConfigurationItemDTO getConfigurationItem(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String ciId)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException, EntityConstraintException {
        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);
        ConfigurationItem ci = productService.getConfigurationItem(ciKey);

        ConfigurationItemDTO dto = new ConfigurationItemDTO(mapper.map(ci.getAuthor(), UserDTO.class), ci.getId(), ci.getWorkspaceId(),
                ci.getDescription(), ci.getDesignItem().getNumber(), ci.getDesignItem().getName(),
                ci.getDesignItem().getLastRevision().getVersion());
        dto.setPathToPathLinks(getPathToPathLinksForGivenConfigurationItem(ci));
        return dto;
    }

    @DELETE
    @ApiOperation(value = "Delete configuration item",
            response = Response.class)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successful deletion of ConfigurationItemDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{ciId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteConfigurationItem(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String ciId)
            throws EntityNotFoundException, NotAllowedException, AccessRightException, UserNotActiveException,
            EntityConstraintException {

        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);
        productService.deleteConfigurationItem(ciKey);
        return Response.noContent().build();
    }

    @GET
    @ApiOperation(value = "Search paths",
            response = PathDTO.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of PathDTOs. It can be an empty list."),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{ciId}/paths")
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchPaths(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String ciId,
            @ApiParam(required = true, value = "Search value") @QueryParam("search") String search,
            @ApiParam(required = false, value = "Config spec") @QueryParam("configSpec") String configSpecType,
            @ApiParam(required = false, value = "Discover substitute links") @QueryParam("diverge") boolean diverge)
            throws EntityNotFoundException, UserNotActiveException, EntityConstraintException, NotAllowedException {

        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);
        ProductStructureFilter filter = psFilterService.getPSFilter(ciKey, configSpecType, diverge);
        List<PartLink[]> usagePaths = productService.findPartUsages(ciKey, filter, search);

        List<PathDTO> pathsDTO = new ArrayList<>();

        for (PartLink[] usagePath : usagePaths) {
            String pathAsString = com.docdoku.core.util.Tools.getPathAsString(Arrays.asList(usagePath));
            pathsDTO.add(new PathDTO(pathAsString));
        }

        return Response.ok(new GenericEntity<List<PathDTO>>((List<PathDTO>) pathsDTO) {
        }).build();
    }

    @ApiOperation(value = "SubResource : LayerResource")
    @Path("{ciId}/layers")
    public LayerResource processLayers(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String ciId) {
        return layerResource;
    }

    @GET
    @ApiOperation(value = "Get baseline creation path choices",
            response = PathChoiceDTO.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of PathChoiceDTOs. It can be an empty list."),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{ciId}/path-choices")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBaselineCreationPathChoices(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String ciId,
            @ApiParam(required = false, value = "Baseline type") @QueryParam("type") String pType)
            throws ConfigurationItemNotFoundException, WorkspaceNotFoundException, UserNotActiveException,
            UserNotFoundException, PartMasterNotFoundException, NotAllowedException,
            EntityConstraintException, WorkspaceNotEnabledException {

        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);

        ProductBaselineType type;

        if (pType == null || "LATEST".equals(pType)) {
            type = ProductBaselineType.LATEST;
        } else if ("RELEASED".equals(pType)) {
            type = ProductBaselineType.RELEASED;
        } else {
            throw new IllegalArgumentException("Type must be either RELEASED or LATEST");
        }

        List<PathChoice> choices = productBaselineService.getBaselineCreationPathChoices(ciKey, type);

        List<PathChoiceDTO> pathChoiceDTOs = new ArrayList<>();

        for (PathChoice choice : choices) {
            pathChoiceDTOs.add(Tools.mapPathChoiceDTO(choice));
        }

        return Response.ok(new GenericEntity<List<PathChoiceDTO>>((List<PathChoiceDTO>) pathChoiceDTOs) {
        }).build();
    }

    @GET
    @ApiOperation(value = "Get baseline creation version choices",
            response = BaselinedPartDTO.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of BaselinedPartDTOs. It can be an empty list."),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{ciId}/versions-choices")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBaselineCreationVersionsChoices(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String ciId)
            throws ConfigurationItemNotFoundException, WorkspaceNotFoundException, UserNotActiveException,
            UserNotFoundException, PartMasterNotFoundException, NotAllowedException,
            EntityConstraintException, WorkspaceNotEnabledException {

        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);

        List<PartIteration> parts = productBaselineService.getBaselineCreationVersionsChoices(ciKey);

        // Serve the latest first
        parts.sort(Comparator.<PartIteration>reverseOrder());

        Map<PartMaster, List<PartIteration>> map = new HashMap<>();

        for (PartIteration partIteration : parts) {
            PartMaster partMaster = partIteration.getPartRevision().getPartMaster();
            if (map.get(partMaster) == null) {
                map.put(partMaster, new ArrayList<>());
            }
            map.get(partMaster).add(partIteration);
        }

        List<BaselinedPartDTO> partsDTO = new ArrayList<>();

        for (Map.Entry<PartMaster, List<PartIteration>> entry : map.entrySet()) {
            List<PartIteration> availableParts = entry.getValue();
            if (availableParts.size() == 1 && !availableParts.get(0).getPartRevision().isReleased() || availableParts.size() > 1) {
                partsDTO.add(Tools.createBaselinedPartDTOFromPartList(availableParts));
            }
        }

        return Response.ok(new GenericEntity<List<BaselinedPartDTO>>((List<BaselinedPartDTO>) partsDTO) {
        }).build();
    }


    @GET
    @ApiOperation(value = "Get instances under given path, and config spec",
            response = LeafDTO.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of LeafDTOs. It can be an empty list."),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{ciId}/instances")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFilteredInstances(
            @Context Request request,
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String ciId,
            @ApiParam(required = true, value = "Config spec") @QueryParam("configSpec") String configSpecType,
            @ApiParam(required = true, value = "Complete path to start from") @QueryParam("path") String path,
            @ApiParam(required = false, value = "Discover substitute links") @QueryParam("diverge") boolean diverge)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException {

        Response.ResponseBuilder rb = fakeSimilarBehavior(request);
        if (rb != null) {
            return rb.build();
        } else {
            CacheControl cc = new CacheControl();
            //this request is resources consuming so we cache the response for 30 minutes
            cc.setMaxAge(60 * 15);
            ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);
            ProductStructureFilter filter = psFilterService.getPSFilter(ciKey, configSpecType, diverge);
            List<PartLink> decodedPath = productService.decodePath(ciKey, path);

            List<List<PartLink>> paths = new ArrayList<>();

            if (decodedPath != null) {
                paths.add(decodedPath);
            }

            InstanceCollection instanceCollection = new InstanceCollection(ciKey, filter, paths);

            return Response.ok().lastModified(new Date()).cacheControl(cc).entity(instanceCollection).build();
        }
    }

    @POST
    @ApiOperation(value = "Get instances for multiple paths",
            response = LeafDTO.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of LeafDTOs. It can be an empty list."),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{ciId}/instances")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInstancesForMultiplePath(
            @Context Request request,
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String ciId,
            @ApiParam(required = false, value = "Discover substitute links") @QueryParam("diverge") boolean diverge,
            @ApiParam(required = true, value = "List of paths to start from") PathListDTO pathsDTO)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, NotAllowedException {

        Response.ResponseBuilder rb = fakeSimilarBehavior(request);
        if (rb != null) {
            return rb.build();
        } else {
            CacheControl cc = new CacheControl();
            //this request is resources consuming so we cache the response for 30 minutes
            cc.setMaxAge(60 * 15);

            ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);

            ProductStructureFilter filter = psFilterService.getPSFilter(ciKey, pathsDTO.getConfigSpec(), diverge);

            List<List<PartLink>> paths = new ArrayList<>();

            for (String path : pathsDTO.getPaths()) {
                List<PartLink> decodedPath = productService.decodePath(ciKey, path);
                if (decodedPath != null) {
                    paths.add(decodedPath);
                }
            }

            InstanceCollection instanceCollection = new InstanceCollection(ciKey, filter, paths);

            return Response.ok().lastModified(new Date()).cacheControl(cc).entity(instanceCollection).build();
        }
    }

    @GET
    @ApiOperation(value = "Get last release of part",
            response = PartRevisionDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of PartRevisionDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{ciId}/releases/last")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLastRelease(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String ciId)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException {

        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);

        PartRevision partRevision = productService.getLastReleasePartRevision(ciKey);
        PartRevisionDTO partRevisionDTO = mapper.map(partRevision, PartRevisionDTO.class);
        partRevisionDTO.setNumber(partRevision.getPartNumber());
        partRevisionDTO.setPartKey(partRevision.getPartNumber() + "-" + partRevision.getVersion());
        partRevisionDTO.setName(partRevision.getPartMaster().getName());
        partRevisionDTO.setStandardPart(partRevision.getPartMaster().isStandardPart());

        return Response.ok(partRevisionDTO).build();
    }

    // TODO : set the appropriate response class for generated API usage
    @GET
    @ApiOperation(value = "Export files",
            response = Response.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful export"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{ciId}/export-files")
    public Response exportProductFiles(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String ciId,
            @ApiParam(required = false, value = "Config spec") @QueryParam("configSpecType") String configSpecType,
            @ApiParam(required = false, value = "Export native cad files flag") @QueryParam("exportNativeCADFiles") boolean exportNativeCADFiles,
            @ApiParam(required = false, value = "Export linked documents attached files flag") @QueryParam("exportDocumentLinks") boolean exportDocumentLinks)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, BaselineNotFoundException,
            ProductInstanceMasterNotFoundException, WorkspaceNotEnabledException {

        if (configSpecType == null) {
            configSpecType = "wip";
        }

        FileExportProductEntity fileExportEntity = new FileExportProductEntity();
        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);
        ProductStructureFilter psFilter = psFilterService.getPSFilter(ciKey, configSpecType, false);

        fileExportEntity.setPsFilter(psFilter);
        fileExportEntity.setConfigurationItemKey(ciKey);

        fileExportEntity.setExportNativeCADFile(exportNativeCADFiles);
        fileExportEntity.setExportDocumentLinks(exportDocumentLinks);

        if (configSpecType.startsWith("pi-")) {
            String serialNumber = configSpecType.substring(3);
            fileExportEntity.setSerialNumber(serialNumber);
            fileExportEntity.setBaselineId(productService.loadProductBaselineForProductInstanceMaster(ciKey, serialNumber).getId());

        } else if (!"wip".equals(configSpecType) && !"latest".equals(configSpecType) && !"released".equals(configSpecType)) {
            try {
                fileExportEntity.setBaselineId(Integer.parseInt(configSpecType));
            } catch (NumberFormatException e) {
                LOGGER.log(Level.FINEST, null, e);
            }
        }

        String fileName = FileDownloadTools.getFileName(ciId + "-" + configSpecType + "-export", "zip");
        String contentDisposition = FileDownloadTools.getContentDisposition("attachment", fileName);

        return Response.ok()
                .header("Content-Type", "application/download")
                .header("Content-Disposition", contentDisposition)
                .entity(fileExportEntity).build();
    }

    @POST
    @ApiOperation(value = "Create path to path link",
            response = LightPathToPathLinkDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of created LightPathToPathLinkDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{ciId}/path-to-path-links")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public LightPathToPathLinkDTO createPathToPathLink(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String ciId,
            @ApiParam(required = true, value = "Product instance serial number") @PathParam("serialNumber") String serialNumber,
            @ApiParam(required = true, value = "Path to path link to create") LightPathToPathLinkDTO pathToPathLinkDTO)
            throws PathToPathLinkAlreadyExistsException, UserNotActiveException, WorkspaceNotFoundException,
            CreationException, UserNotFoundException, ProductInstanceMasterNotFoundException,
            AccessRightException, PathToPathCyclicException, ConfigurationItemNotFoundException,
            PartUsageLinkNotFoundException, NotAllowedException, WorkspaceNotEnabledException {

        PathToPathLink pathToPathLink = productService.createPathToPathLink(workspaceId, ciId, pathToPathLinkDTO.getType(), pathToPathLinkDTO.getSourcePath(), pathToPathLinkDTO.getTargetPath(), pathToPathLinkDTO.getDescription());
        return mapper.map(pathToPathLink, LightPathToPathLinkDTO.class);
    }

    @PUT
    @ApiOperation(value = "Update path to path link",
            response = LightPathToPathLinkDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of updated LightPathToPathLinkDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{ciId}/path-to-path-links/{pathToPathLinkId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public LightPathToPathLinkDTO updatePathToPathLink(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String ciId,
            @ApiParam(required = true, value = "Product instance serial number") @PathParam("serialNumber") String serialNumber,
            @ApiParam(required = true, value = "Path to path link id") @PathParam("pathToPathLinkId") int pathToPathLinkId,
            @ApiParam(required = true, value = "Path to path link to update") LightPathToPathLinkDTO pathToPathLinkDTO)
            throws PathToPathLinkAlreadyExistsException, UserNotActiveException, WorkspaceNotFoundException,
            CreationException, UserNotFoundException, ProductInstanceMasterNotFoundException,
            AccessRightException, PathToPathCyclicException, ConfigurationItemNotFoundException,
            PartUsageLinkNotFoundException, NotAllowedException, PathToPathLinkNotFoundException,
            WorkspaceNotEnabledException {

        PathToPathLink pathToPathLink = productService.updatePathToPathLink(workspaceId, ciId, pathToPathLinkId, pathToPathLinkDTO.getDescription());
        return mapper.map(pathToPathLink, LightPathToPathLinkDTO.class);
    }

    @DELETE
    @ApiOperation(value = "Delete path to path link",
            response = Response.class)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successful deletion of PathToPathLink"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{ciId}/path-to-path-links/{pathToPathLinkId}")
    public Response deletePathToPathLink(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String ciId,
            @ApiParam(required = true, value = "Product instance serial number") @PathParam("serialNumber") String serialNumber,
            @ApiParam(required = true, value = "Path to path link id") @PathParam("pathToPathLinkId") int pathToPathLinkId)
            throws PathToPathLinkNotFoundException, UserNotActiveException, WorkspaceNotFoundException,
            UserNotFoundException, ProductInstanceMasterNotFoundException, AccessRightException,
            ConfigurationItemNotFoundException, WorkspaceNotEnabledException {

        productService.deletePathToPathLink(workspaceId, ciId, pathToPathLinkId);
        return Response.noContent().build();
    }

    @GET
    @ApiOperation(value = "Get path to path links from source and target",
            response = PathToPathLinkDTO.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of PathToPathLinkDTOs. It can be an empty list."),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{ciId}/path-to-path-links/source/{sourcePath}/target/{targetPath}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPathToPathLinkInProduct(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String ciId,
            @ApiParam(required = true, value = "Complete source path") @PathParam("sourcePath") String sourcePathAsString,
            @ApiParam(required = true, value = "Complete target path") @PathParam("targetPath") String targetPathAsString)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException,
            ConfigurationItemNotFoundException, PartUsageLinkNotFoundException, WorkspaceNotEnabledException {

        List<PathToPathLink> pathToPathLinks = productService.getPathToPathLinkFromSourceAndTarget(workspaceId, ciId, sourcePathAsString, targetPathAsString);
        List<PathToPathLinkDTO> dtos = new ArrayList<>();

        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);

        for (PathToPathLink pathToPathLink : pathToPathLinks) {
            PathToPathLinkDTO pathToPathLinkDTO = mapper.map(pathToPathLink, PathToPathLinkDTO.class);
            List<LightPartLinkDTO> sourceLightPartLinkDTOs = new ArrayList<>();

            List<PartLink> sourcePath = productService.decodePath(ciKey, pathToPathLink.getSourcePath());
            List<PartLink> targetPath = productService.decodePath(ciKey, pathToPathLink.getTargetPath());

            for (PartLink partLink : sourcePath) {
                LightPartLinkDTO lightPartLinkDTO = new LightPartLinkDTO(partLink.getComponent().getNumber(), partLink.getComponent().getName(),partLink.getReferenceDescription(),partLink.getFullId());
                sourceLightPartLinkDTOs.add(lightPartLinkDTO);
            }

            List<LightPartLinkDTO> targetLightPartLinkDTOs = new ArrayList<>();
            for (PartLink partLink : targetPath) {
                LightPartLinkDTO lightPartLinkDTO = new LightPartLinkDTO(partLink.getComponent().getNumber(), partLink.getComponent().getName(),partLink.getReferenceDescription(),partLink.getFullId());
                targetLightPartLinkDTOs.add(lightPartLinkDTO);
            }

            pathToPathLinkDTO.setSourceComponents(sourceLightPartLinkDTOs);
            pathToPathLinkDTO.setTargetComponents(targetLightPartLinkDTOs);
            dtos.add(pathToPathLinkDTO);
        }
        return Response.ok(new GenericEntity<List<PathToPathLinkDTO>>((List<PathToPathLinkDTO>) dtos) {
        }).build();
    }

    @GET
    @ApiOperation(value = "Get path to path links types",
            response = LightPathToPathLinkDTO.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of LightPathToPathLinkDTOs. It can be an empty list."),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{ciId}/path-to-path-links-types")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPathToPathLinkTypesInProduct(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String ciId)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException,
            AccessRightException, ProductInstanceMasterNotFoundException, ConfigurationItemNotFoundException,
            WorkspaceNotEnabledException {

        List<String> pathToPathLinkTypes = productService.getPathToPathLinkTypes(workspaceId, ciId);
        List<LightPathToPathLinkDTO> pathToPathLinkDTOs = new ArrayList<>();
        for (String type : pathToPathLinkTypes) {
            LightPathToPathLinkDTO pathToPathLinkDTO = new LightPathToPathLinkDTO();
            pathToPathLinkDTO.setType(type);
            pathToPathLinkDTOs.add(pathToPathLinkDTO);
        }
        return Response.ok(new GenericEntity<List<LightPathToPathLinkDTO>>((List<LightPathToPathLinkDTO>) pathToPathLinkDTOs) {
        }).build();
    }

    @GET
    @ApiOperation(value = "Decode string path",
            response = LightPartLinkDTO.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of LightPartLinkDTOs. It can be an empty list."),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{ciId}/decode-path/{path}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response decodePath(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String ciId,
            @ApiParam(required = true, value = "Complete path to decode") @PathParam("path") String pathAsString)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException,
            BaselineNotFoundException, ConfigurationItemNotFoundException, PartUsageLinkNotFoundException,
            WorkspaceNotEnabledException {

        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);
        List<PartLink> path = productService.decodePath(ciKey, pathAsString);
        List<LightPartLinkDTO> lightPartLinkDTOs = new ArrayList<>();
        for (PartLink partLink : path) {
            LightPartLinkDTO lightPartLinkDTO = new LightPartLinkDTO(partLink.getComponent().getNumber(), partLink.getComponent().getName(),partLink.getReferenceDescription(),partLink.getFullId());
            lightPartLinkDTOs.add(lightPartLinkDTO);
        }
        return Response.ok(new GenericEntity<List<LightPartLinkDTO>>((List<LightPartLinkDTO>) lightPartLinkDTOs) {
        }).build();
    }

    @GET
    @ApiOperation(value = "Get document links for given part operation",
            response = DocumentIterationLinkDTO.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of DocumentIterationLinkDTOs. It can be an empty list."),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{ciId}/document-links/{partNumber: [^/].*}-{partVersion:[A-Z]+}-{partIteration:[0-9]+}/{configSpec}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDocumentLinksForGivenPartIteration(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String ciId,
            @ApiParam(required = true, value = "Part number") @PathParam("partNumber") String partNumber,
            @ApiParam(required = true, value = "Part version") @PathParam("partVersion") String partVersion,
            @ApiParam(required = true, value = "Part iteration") @PathParam("partIteration") int partIteration,
            @ApiParam(required = false, value = "Config spec") @PathParam("configSpec") String configSpec)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException,
            AccessRightException, ProductInstanceMasterNotFoundException, BaselineNotFoundException,
            ConfigurationItemNotFoundException, PartUsageLinkNotFoundException, PartIterationNotFoundException,
            WorkspaceNotEnabledException {

        List<DocumentIterationLinkDTO> dtos = new ArrayList<>();
        PartIterationKey partIterationKey = new PartIterationKey(workspaceId, partNumber, partVersion, partIteration);
        List<DocumentIterationLink> documentIterationLinkList = productService.getDocumentLinksAsDocumentIterations(workspaceId, ciId, configSpec, partIterationKey);
        for (DocumentIterationLink documentIterationLink : documentIterationLinkList) {

            DocumentIteration documentIteration = documentIterationLink.getDocumentIteration();
            DocumentLink documentLink = documentIterationLink.getDocumentLink();

            DocumentIterationLinkDTO documentIterationLinkDTO = new DocumentIterationLinkDTO();
            documentIterationLinkDTO.setDocumentMasterId(documentIteration.getId());
            documentIterationLinkDTO.setVersion(documentIteration.getVersion());
            documentIterationLinkDTO.setTitle(documentIteration.getTitle());
            documentIterationLinkDTO.setIteration(documentIteration.getIteration());
            documentIterationLinkDTO.setWorkspaceId(documentIteration.getWorkspaceId());
            documentIterationLinkDTO.setCommentLink(documentLink.getComment());

            dtos.add(documentIterationLinkDTO);

        }
        return Response.ok(new GenericEntity<List<DocumentIterationLinkDTO>>((List<DocumentIterationLinkDTO>) dtos) {
        }).build();
    }

    @PUT
    @ApiOperation(value = "Cascade check out",
            response = CascadeResult.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of CascadeResult"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{ciId}/cascade-checkout")
    @Produces(MediaType.APPLICATION_JSON)
    public Response cascadeCheckout(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String ciId,
            @ApiParam(required = false, value = "Config spec") @QueryParam("configSpec") String configSpecType,
            @ApiParam(required = true, value = "Complete path to checkout from") @QueryParam("path") String path,
            @ApiParam(name = "body", defaultValue = "") String body)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException,
            ConfigurationItemNotFoundException, PartMasterNotFoundException, EntityConstraintException,
            NotAllowedException, PartUsageLinkNotFoundException, WorkspaceNotEnabledException {

        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);
        CascadeResult cascadeResult = cascadeActionService.cascadeCheckout(ciKey, path);
        return Response.ok(cascadeResult).build();
    }

    @PUT
    @ApiOperation(value = "Cascade check in",
            response = CascadeResult.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of CascadeResult"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{ciId}/cascade-checkin")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cascadeCheckin(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String ciId,
            @ApiParam(required = false, value = "Config spec") @QueryParam("configSpec") String configSpecType,
            @ApiParam(required = true, value = "Complete path to checkin from") @QueryParam("path") String path,
            @ApiParam(required = true, value = "Iteration note to add") IterationNoteDTO iterationNoteDTO)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException,
            ConfigurationItemNotFoundException, PartMasterNotFoundException, EntityConstraintException,
            NotAllowedException, PartUsageLinkNotFoundException, WorkspaceNotEnabledException {

        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);
        CascadeResult cascadeResult = cascadeActionService.cascadeCheckin(ciKey, path, iterationNoteDTO.getIterationNote());
        return Response.ok(cascadeResult).build();
    }

    @PUT
    @ApiOperation(value = "Cascade undo check out",
            response = CascadeResult.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of CascadeResult"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{ciId}/cascade-undocheckout")
    @Produces(MediaType.APPLICATION_JSON)
    public Response cascadeUndoCheckOut(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Configuration item id") @PathParam("ciId") String ciId,
            @ApiParam(required = false, value = "Config spec") @QueryParam("configSpec") String configSpecType,
            @ApiParam(required = true, value = "Complete path to undo checkout from") @QueryParam("path") String path,
            @ApiParam(name = "body", defaultValue = "") String body)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException,
            ConfigurationItemNotFoundException, PartMasterNotFoundException, EntityConstraintException,
            NotAllowedException, PartUsageLinkNotFoundException, WorkspaceNotEnabledException {
        
        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, ciId);
        CascadeResult cascadeResult = cascadeActionService.cascadeUndocheckout(ciKey, path);
        return Response.ok(cascadeResult).build();
    }


    /**
     * Because some AS (like Glassfish) forbids the use of CacheControl
     * when authenticated we use the LastModified header to fake
     * a similar behavior (15 minutes of cache)
     *
     * @param request The incoming request
     * @return Nothing if there still have cache
     */
    private Response.ResponseBuilder fakeSimilarBehavior(Request request) {
        Calendar cal = new GregorianCalendar();
        cal.add(Calendar.MINUTE, -15);
        return request.evaluatePreconditions(cal.getTime());
    }

    private ComponentDTO createComponentDTO(Component component, String workspaceId, String configurationItemId, String serialNumber)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {

        PartMaster pm = component.getPartMaster();
        PartIteration retainedIteration = component.getRetainedIteration();

        if (retainedIteration == null) {
            return null;
        }

        PartRevision partR = retainedIteration.getPartRevision();

        // Filter ACL on partR
        if (!component.isVirtual() && !productService.canAccess(partR.getKey())) {
            return null;
        }

        List<PartLink> path = component.getPath();
        PartLink usageLink = path.get(path.size() - 1);

        ComponentDTO dto = new ComponentDTO();

        dto.setPath(com.docdoku.core.util.Tools.getPathAsString(path));
        dto.setVirtual(component.isVirtual());
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
        if (substitutes != null) {
            List<String> substituteIds = new ArrayList<>();
            for (PartSubstituteLink substituteLink : substitutes) {
                substituteIds.add(substituteLink.getFullId());
            }
            dto.setSubstituteIds(substituteIds);
        }

        List<InstanceAttributeDTO> lstAttributes = new ArrayList<>();
        List<ComponentDTO> components = new ArrayList<>();

        User checkoutUser = partR.getCheckOutUser();
        if (checkoutUser != null) {
            dto.setCheckOutUser(mapper.map(partR.getCheckOutUser(), UserDTO.class));
            dto.setCheckOutDate(partR.getCheckOutDate());
        }

        if (!component.isVirtual()) {
            try {
                productService.checkPartRevisionReadAccess(partR.getKey());
                dto.setAccessDeny(false);
                dto.setLastIterationNumber(productService.getNumberOfIteration(partR.getKey()));
            } catch (Exception e) {
                LOGGER.log(Level.FINEST, null, e);
                dto.setLastIterationNumber(-1);
                dto.setAccessDeny(true);
            }
        } else {
            dto.setAccessDeny(false);
        }

        for (InstanceAttribute attr : retainedIteration.getInstanceAttributes()) {
            lstAttributes.add(mapper.map(attr, InstanceAttributeDTO.class));
        }

        if (!component.isVirtual() && serialNumber != null) {
            PathDataMasterDTO pathData = productInstancesResource.getPathData(workspaceId, configurationItemId, serialNumber, com.docdoku.core.util.Tools.getPathAsString(path));
            dto.setHasPathData(!pathData.getPathDataIterations().isEmpty());
        }

        for (Component subComponent : component.getComponents()) {
            ComponentDTO componentDTO = createComponentDTO(subComponent, workspaceId, configurationItemId, serialNumber);
            if (componentDTO != null) {
                components.add(componentDTO);
            }
        }

        dto.setAssembly(retainedIteration.isAssembly());
        dto.setAttributes(lstAttributes);

        if (!component.isVirtual()) {
            dto.setNotifications(getModificationNotificationDTOs(partR));
        }

        dto.setComponents(components);

        return dto;
    }

    /**
     * Return a list of ModificationNotificationDTO matching with a given PartRevision
     *
     * @param partRevision The specified PartRevision
     * @return A list of ModificationNotificationDTO
     * @throws EntityNotFoundException If an entity doesn't exist
     * @throws AccessRightException    If the user can not get the modification notifications
     * @throws UserNotActiveException  If the user is disabled
     */
    private List<ModificationNotificationDTO> getModificationNotificationDTOs(PartRevision partRevision)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException {

        PartIterationKey iterationKey = new PartIterationKey(partRevision.getKey(), partRevision.getLastIterationNumber());
        List<ModificationNotification> notifications = productService.getModificationNotifications(iterationKey);
        return Tools.mapModificationNotificationsToModificationNotificationDTO(notifications);
    }

    private List<PathToPathLinkDTO> getPathToPathLinksForGivenConfigurationItem(ConfigurationItem configurationItem) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, ConfigurationItemNotFoundException, PartUsageLinkNotFoundException, WorkspaceNotEnabledException {
        List<PathToPathLink> pathToPathLinkTypes = configurationItem.getPathToPathLinks();
        List<PathToPathLinkDTO> pathToPathLinkDTOs = new ArrayList<>();

        for (PathToPathLink pathToPathLink : pathToPathLinkTypes) {
            PathToPathLinkDTO pathToPathLinkDTO = mapper.map(pathToPathLink, PathToPathLinkDTO.class);
            List<PartLink> sourcePath = productService.decodePath(configurationItem.getKey(), pathToPathLink.getSourcePath());
            List<PartLink> targetPath = productService.decodePath(configurationItem.getKey(), pathToPathLink.getTargetPath());

            List<LightPartLinkDTO> sourceLightPartLinkDTOs = new ArrayList<>();
            for (PartLink partLink : sourcePath) {
                LightPartLinkDTO lightPartLinkDTO = new LightPartLinkDTO(partLink.getComponent().getNumber(), partLink.getComponent().getName(),partLink.getReferenceDescription(),partLink.getFullId());
                sourceLightPartLinkDTOs.add(lightPartLinkDTO);
            }

            List<LightPartLinkDTO> targetLightPartLinkDTOs = new ArrayList<>();
            for (PartLink partLink : targetPath) {
                LightPartLinkDTO lightPartLinkDTO = new LightPartLinkDTO(partLink.getComponent().getNumber(), partLink.getComponent().getName(),partLink.getReferenceDescription(),partLink.getFullId());
                targetLightPartLinkDTOs.add(lightPartLinkDTO);
            }

            pathToPathLinkDTO.setSourceComponents(sourceLightPartLinkDTOs);
            pathToPathLinkDTO.setTargetComponents(targetLightPartLinkDTOs);
            pathToPathLinkDTOs.add(pathToPathLinkDTO);
        }
        return pathToPathLinkDTOs;
    }
}
