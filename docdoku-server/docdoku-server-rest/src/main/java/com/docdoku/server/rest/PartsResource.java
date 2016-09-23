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
import com.docdoku.core.common.Account;
import com.docdoku.core.common.User;
import com.docdoku.core.common.UserGroup;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.configuration.PSFilter;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.product.*;
import com.docdoku.core.query.PartSearchQuery;
import com.docdoku.core.query.Query;
import com.docdoku.core.query.QueryResultRow;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.ACLUserEntry;
import com.docdoku.core.security.ACLUserGroupEntry;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IImporterManagerLocal;
import com.docdoku.core.services.IPSFilterManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.core.util.FileIO;
import com.docdoku.server.export.ExcelGenerator;
import com.docdoku.server.rest.collections.QueryResult;
import com.docdoku.server.rest.dto.*;
import com.docdoku.server.rest.file.util.BinaryResourceUpload;
import com.docdoku.server.rest.util.SearchQueryParser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

import javax.annotation.PostConstruct;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.*;

@RequestScoped
@Api(hidden = true, value = "parts", description = "Operation about parts")
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class PartsResource {

    @Inject
    private IProductManagerLocal productService;

    @Inject
    private IUserManagerLocal userManager;

    @Inject
    private PartResource partResource;

    @Inject
    private IPSFilterManagerLocal filterService;

    @EJB
    private IImporterManagerLocal importerService;
    private Mapper mapper;

    public PartsResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @ApiOperation(value = "SubResource : PartResource")
    @Path("{partNumber: [^/].*}-{partVersion:[A-Z]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public PartResource getPartResource() {
        return partResource;
    }

    @GET
    @ApiOperation(value = "Get part revisions", response = PartRevisionDTO.class, responseContainer = "List")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPartRevisions(@PathParam("workspaceId") String workspaceId,
                                     @QueryParam("start") int start,
                                     @QueryParam("length") int length)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException {

        List<PartRevision> partRevisions = productService.getPartRevisions(Tools.stripTrailingSlash(workspaceId), start, length);
        List<PartRevisionDTO> partRevisionDTOs = new ArrayList<>();

        for (PartRevision partRevision : partRevisions) {
            PartRevisionDTO partRevisionDTO = Tools.mapPartRevisionToPartDTO(partRevision);

            List<ModificationNotificationDTO> notificationDTOs = getModificationNotificationDTOs(partRevision);
            partRevisionDTO.setNotifications(notificationDTOs);

            partRevisionDTOs.add(partRevisionDTO);
        }
        return Response.ok(new GenericEntity<List<PartRevisionDTO>>((List<PartRevisionDTO>) partRevisionDTOs) {
        }).build();
    }

    @GET
    @ApiOperation(value = "Count part revisions", response = CountDTO.class)
    @Path("count")
    @Produces(MediaType.APPLICATION_JSON)
    public CountDTO getTotalNumberOfParts(@PathParam("workspaceId") String workspaceId)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException {

        return new CountDTO(productService.getTotalNumberOfParts(Tools.stripTrailingSlash(workspaceId)));
    }

    @GET
    @ApiOperation(value = "Get part revisions", response = PartRevisionDTO.class, responseContainer = "List")
    @Path("tags/{tagId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPartRevisions(@PathParam("workspaceId") String workspaceId,
                                     @PathParam("tagId") String tagId)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException {

        PartRevision[] partRevisions = productService.findPartRevisionsByTag(Tools.stripTrailingSlash(workspaceId), tagId);
        List<PartRevisionDTO> partRevisionDTOs = new ArrayList<>();

        for (PartRevision partRevision : partRevisions) {
            PartRevisionDTO partRevisionDTO = Tools.mapPartRevisionToPartDTO(partRevision);

            List<ModificationNotificationDTO> notificationDTOs = getModificationNotificationDTOs(partRevision);
            partRevisionDTO.setNotifications(notificationDTOs);

            partRevisionDTOs.add(partRevisionDTO);
        }
        return Response.ok(new GenericEntity<List<PartRevisionDTO>>((List<PartRevisionDTO>) partRevisionDTOs) {
        }).build();
    }

    @GET
    @ApiOperation(value = "Search part revisions", response = PartRevisionDTO.class, responseContainer = "List")
    @Path("search")
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchPartRevisions(@Context UriInfo uri,
                                        @PathParam("workspaceId") String workspaceId,
                                        @QueryParam("q") String q)
            throws EntityNotFoundException, ESServerException, UserNotActiveException, AccessRightException {

        PartSearchQuery partSearchQuery = SearchQueryParser.parsePartStringQuery(workspaceId, uri.getQueryParameters());

        List<PartRevision> partRevisions = productService.searchPartRevisions(partSearchQuery);
        List<PartRevisionDTO> partRevisionDTOs = new ArrayList<>();

        for (PartRevision partRevision : partRevisions) {
            PartRevisionDTO partRevisionDTO = Tools.mapPartRevisionToPartDTO(partRevision);

            List<ModificationNotificationDTO> notificationDTOs = getModificationNotificationDTOs(partRevision);
            partRevisionDTO.setNotifications(notificationDTOs);

            partRevisionDTOs.add(partRevisionDTO);
        }

        return Response.ok(new GenericEntity<List<PartRevisionDTO>>((List<PartRevisionDTO>) partRevisionDTOs) {
        }).build();
    }

    @GET
    @ApiOperation(value = "Get custom queries", response = QueryDTO.class, responseContainer = "List")
    @Path("queries")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCustomQueries(@PathParam("workspaceId") String workspaceId)
            throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        List<Query> queries = productService.getQueries(workspaceId);
        List<QueryDTO> queryDTOs = new ArrayList<>();
        for (Query query : queries) {
            queryDTOs.add(mapper.map(query, QueryDTO.class));
        }
        return Response.ok(new GenericEntity<List<QueryDTO>>((List<QueryDTO>) queryDTOs) {
        }).build();
    }

    @POST
    @ApiOperation(value = "Run custom queries", response = QueryResult.class, responseContainer = "List")
    @Path("queries")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM})
    public Response runCustomQuery(@PathParam("workspaceId") String workspaceId,
                                   @QueryParam("save") boolean save,
                                   @QueryParam("export") String exportType,
                                   @ApiParam(required = true, value = "Query to run") QueryDTO queryDTO)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, CreationException, QueryAlreadyExistsException, EntityConstraintException, NotAllowedException {
        Query query = mapper.map(queryDTO, Query.class);
        QueryResult queryResult = getQueryResult(workspaceId, query, exportType);

        if (save) {
            productService.createQuery(workspaceId, query);
        }

        return Response.ok(new GenericEntity<QueryResult>((QueryResult) queryResult) {
        }).build();
    }


    @GET
    @ApiOperation(value = "Filter part master with config spec", response = PartIterationDTO.class)
    @Path("{partNumber}/filter/{baselineId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response filterPartMasterInBaseline(@PathParam("workspaceId") String workspaceId,
                                             @PathParam("partNumber") String partNumber,
                                             @PathParam("baselineId") String baselineId) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, BaselineNotFoundException, PartMasterNotFoundException {

        PSFilter filter = filterService.getBaselinePSFilter(Integer.valueOf(baselineId));
        PartMaster partMaster = productService.getPartMaster(new PartMasterKey(workspaceId, partNumber));
        List<PartIteration> partIterations = filter.filter(partMaster);
        if(!partIterations.isEmpty()){
            return Response.ok().entity(Tools.mapPartIterationToPartIterationDTO(partIterations.get(0))).build();
        }else{
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @POST
    @ApiOperation(value = "Export custom query", response = Response.class)
    @Path("query-export")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/vnd.ms-excel")
    public Response exportCustomQuery(@Context HttpServletRequest request,
                                      @PathParam("workspaceId") String workspaceId,
                                      @QueryParam("export") String exportType,
                                      @ApiParam(required = true, value = "Query to export") QueryDTO queryDTO)
            throws BaselineNotFoundException, ProductInstanceMasterNotFoundException, EntityConstraintException, WorkspaceNotFoundException, UserNotFoundException, NotAllowedException, PartMasterNotFoundException, ConfigurationItemNotFoundException, UserNotActiveException {

            Query query = mapper.map(queryDTO, Query.class);
            User user = userManager.whoAmI(workspaceId);
            Locale locale = new Locale(user.getLanguage());
            return export(workspaceId, query, request, exportType, locale);
    }

    @GET
    @ApiOperation(value = "Export existing query", response = Response.class)
    @Path("queries/{queryId}/format/{export}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/vnd.ms-excel")
    public Response exportexistingQuery(@Context HttpServletRequest request,
                                      @PathParam("workspaceId") String workspaceId,
                                      @PathParam("queryId") String queryId,
                                      @PathParam("export") String exportType)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, CreationException, QueryAlreadyExistsException, EntityConstraintException, NotAllowedException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale locale = new Locale(user != null ? user.getLanguage() : "en");
        Query query = productService.loadQuery(workspaceId, Integer.valueOf(queryId));
        return export(workspaceId,query,request,exportType,locale);
    }

    private Response export(String workspaceId, Query query, HttpServletRequest request, String exportType, Locale locale)
            throws BaselineNotFoundException, ProductInstanceMasterNotFoundException, EntityConstraintException, WorkspaceNotFoundException, UserNotFoundException, NotAllowedException, PartMasterNotFoundException, ConfigurationItemNotFoundException, UserNotActiveException {
        QueryResult queryResult = getQueryResult(workspaceId, query, exportType);
        String url = request.getRequestURL().toString();
        String baseURL = url.substring(0, url.length() - request.getRequestURI().length()) + request.getContextPath();
        return makeQueryResponse(queryResult, locale, baseURL);
    }

    private QueryResult getQueryResult(String workspaceId, Query query, String pExportType) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, EntityConstraintException, BaselineNotFoundException, ProductInstanceMasterNotFoundException, NotAllowedException, ConfigurationItemNotFoundException, PartMasterNotFoundException {
        List<PartRevision> partRevisions = productService.searchPartRevisions(workspaceId, query);
        QueryResult queryResult = new QueryResult(partRevisions, query);
        if (query.hasContext()) {
            List<QueryResultRow> rows = productService.filterProductBreakdownStructure(workspaceId, query);
            queryResult.mergeRows(rows);
        }
        String exportType = pExportType != null ? pExportType : "JSON";
        queryResult.setExportType(QueryResult.ExportType.valueOf(exportType));
        return queryResult;
    }

    public Response makeQueryResponse(QueryResult queryResult, Locale locale, String baseURL) {
        ExcelGenerator excelGenerator = new ExcelGenerator();
        String contentType = "application/vnd.ms-excel";
        String contentDisposition = "attachment; filename=export_parts.xls";
        Response.ResponseBuilder responseBuilder = Response.ok((Object) excelGenerator.generateXLSResponse(queryResult, locale, baseURL));
        responseBuilder
                .header("Content-Type", contentType)
                .header("Content-Disposition", contentDisposition);

        return responseBuilder.build();
    }

    @DELETE
    @ApiOperation(value = "Delete custom query", response = Response.class)
    @Path("queries/{queryId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteQuery(@PathParam("workspaceId") String workspaceId,
                                @PathParam("queryId") int queryId)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        productService.deleteQuery(workspaceId, queryId);
        return Response.ok().build();
    }

    @GET
    @ApiOperation(value = "Get checked out part revisions", response = PartRevisionDTO.class, responseContainer = "List")
    @Path("checkedout")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCheckedOutPartRevisions(@PathParam("workspaceId") String workspaceId)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        PartRevision[] checkedOutPartRevisions = productService.getCheckedOutPartRevisions(workspaceId);
        List<PartRevisionDTO> partRevisionDTOs = new ArrayList<>();

        for (PartRevision partRevision : checkedOutPartRevisions) {
            PartRevisionDTO partRevisionDTO = Tools.mapPartRevisionToPartDTO(partRevision);

            List<ModificationNotificationDTO> notificationDTOs = getModificationNotificationDTOs(partRevision);
            partRevisionDTO.setNotifications(notificationDTOs);

            partRevisionDTOs.add(partRevisionDTO);
        }

        return Response.ok(new GenericEntity<List<PartRevisionDTO>>((List<PartRevisionDTO>) partRevisionDTOs) {
        }).build();
    }

    @GET
    @ApiOperation(value = "Count checked out part revisions", response = CountDTO.class)
    @Path("countCheckedOut")
    @Produces(MediaType.APPLICATION_JSON)
    public CountDTO getCheckedOutNumberOfItems(@PathParam("workspaceId") String workspaceId)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, AccessRightException, AccountNotFoundException {
        return new CountDTO(productService.getCheckedOutPartRevisions(workspaceId).length);
    }

    @GET
    @ApiOperation(value = "Search part numbers", response = LightPartMasterDTO.class, responseContainer = "List")
    @Path("numbers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchPartNumbers(@PathParam("workspaceId") String workspaceId,
                                      @QueryParam("q") String q)
            throws EntityNotFoundException, AccessRightException {

        String search = "%" + q + "%";
        List<PartMaster> partMasters = productService.findPartMasters(Tools.stripTrailingSlash(workspaceId), search, search, 8);
        List<LightPartMasterDTO> partsMastersDTO = new ArrayList<>();
        for (PartMaster p : partMasters) {
            LightPartMasterDTO lightPartMasterDTO = new LightPartMasterDTO(p.getNumber(), p.getName());
            partsMastersDTO.add(lightPartMasterDTO);
        }

        return Response.ok(new GenericEntity<List<LightPartMasterDTO>>((List<LightPartMasterDTO>) partsMastersDTO) {
        }).build();
    }


    @POST
    @ApiOperation(value = "Create new part", response = PartRevisionDTO.class)
    @Produces(MediaType.APPLICATION_JSON)
    public PartRevisionDTO createNewPart(@PathParam("workspaceId") String workspaceId,
                                         @ApiParam(required = true, value = "Part to create") PartCreationDTO partCreationDTO)
            throws EntityNotFoundException, EntityAlreadyExistsException, CreationException, AccessRightException, NotAllowedException {

        String pWorkflowModelId = partCreationDTO.getWorkflowModelId();
        RoleMappingDTO[] roleMappingDTOs = partCreationDTO.getRoleMapping();

        Map<String, Collection<String>> userRoleMapping = new HashMap<>();
        Map<String, Collection<String>> groupRoleMapping = new HashMap<>();

        if (roleMappingDTOs != null) {
            for (RoleMappingDTO roleMappingDTO : roleMappingDTOs) {
                userRoleMapping.put(roleMappingDTO.getRoleName(), roleMappingDTO.getUserLogins());
                groupRoleMapping.put(roleMappingDTO.getRoleName(), roleMappingDTO.getGroupIds());
            }
        }

        ACLDTO acl = partCreationDTO.getAcl();
        ACLUserEntry[] userEntries = null;
        ACLUserGroupEntry[] userGroupEntries = null;
        if (acl != null) {
            userEntries = new ACLUserEntry[acl.getUserEntries().size()];
            userGroupEntries = new ACLUserGroupEntry[acl.getGroupEntries().size()];
            int i = 0;
            for (Map.Entry<String, ACL.Permission> entry : acl.getUserEntries().entrySet()) {
                userEntries[i] = new ACLUserEntry();
                userEntries[i].setPrincipal(new User(new Workspace(workspaceId), new Account(entry.getKey())));
                userEntries[i++].setPermission(ACL.Permission.valueOf(entry.getValue().name()));
            }
            i = 0;
            for (Map.Entry<String, ACL.Permission> entry : acl.getGroupEntries().entrySet()) {
                userGroupEntries[i] = new ACLUserGroupEntry();
                userGroupEntries[i].setPrincipal(new UserGroup(new Workspace(workspaceId), entry.getKey()));
                userGroupEntries[i++].setPermission(ACL.Permission.valueOf(entry.getValue().name()));
            }
        }

        PartMaster partMaster = productService.createPartMaster(workspaceId, partCreationDTO.getNumber(), partCreationDTO.getName(), partCreationDTO.isStandardPart(), pWorkflowModelId, partCreationDTO.getDescription(), partCreationDTO.getTemplateId(), userEntries, userGroupEntries, userRoleMapping, groupRoleMapping);
        return Tools.mapPartRevisionToPartDTO(partMaster.getLastRevision());
    }

    @GET
    @ApiOperation(value = "Search documents last iteration to link", response = PartIterationDTO.class, responseContainer = "List")
    @Path("parts_last_iter")
    @Produces(MediaType.APPLICATION_JSON)
    public PartIterationDTO[] searchDocumentsLastIterationToLink(@PathParam("workspaceId") String workspaceId, @QueryParam("q") String q, @QueryParam("l") int limit)
            throws EntityNotFoundException, UserNotActiveException {

        int maxResults = limit == 0 ? 15 : limit;
        PartRevision[] partRs = productService.getPartRevisionsWithReferenceOrName(workspaceId, q, maxResults);

        List<PartIterationDTO> partsLastIter = new ArrayList<>();
        for (PartRevision partR : partRs) {
            PartIteration partLastIter = partR.getLastIteration();
            if (partLastIter != null) {
                partsLastIter.add(new PartIterationDTO(partLastIter.getWorkspaceId(), partLastIter.getPartName(), partLastIter.getPartNumber(), partLastIter.getPartVersion(), partLastIter.getIteration()));
            }
        }

        return partsLastIter.toArray(new PartIterationDTO[partsLastIter.size()]);
    }

    @POST
    @ApiOperation(value = "Import part attributes from file", response = Response.class)
    @Path("import")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response importAttributes(@Context HttpServletRequest request,
                                     @PathParam("workspaceId") String workspaceId,
                                     @QueryParam("autoCheckout") boolean autoCheckout,
                                     @QueryParam("autoCheckin") boolean autoCheckin,
                                     @QueryParam("permissiveUpdate") boolean permissiveUpdate,
                                     @QueryParam("revisionNote") String revisionNote)
            throws Exception {

        Collection<Part> parts = request.getParts();

        if (parts.isEmpty() || parts.size() > 1) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        Part part = parts.iterator().next();
        String name = FileIO.getFileNameWithoutExtension(part.getSubmittedFileName());
        String extension = FileIO.getExtension(part.getSubmittedFileName());

        File importFile = Files.createTempFile("part-" + name, "-import.tmp" + (extension == null ? "" : "." + extension)).toFile();
        BinaryResourceUpload.uploadBinary(new BufferedOutputStream(new FileOutputStream(importFile)), part);
        importerService.importIntoParts(workspaceId, importFile, name + "." + extension, revisionNote, autoCheckout, autoCheckin, permissiveUpdate);

        importFile.deleteOnExit();

        return Response.noContent().build();
    }

    @GET
    @ApiOperation(value = "Get current imports", response = ImportDTO.class, responseContainer = "List")
    @Path("imports/{filename}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public List<ImportDTO> getImports(@PathParam("workspaceId") String workspaceId, @PathParam("filename") String filename)
            throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {

        List<Import> imports = productService.getImports(workspaceId, filename);
        List<ImportDTO> importDTOs = new ArrayList<>();
        for (Import i : imports) {
            importDTOs.add(mapper.map(i, ImportDTO.class));
        }
        return importDTOs;
    }

    @GET
    @ApiOperation(value = "Get import", response = ImportDTO.class)
    @Path("import/{importId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public ImportDTO getImport(@PathParam("workspaceId") String workspaceId, @PathParam("importId") String importId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException {
        Import anImport = productService.getImport(workspaceId, importId);
        return mapper.map(anImport, ImportDTO.class);
    }

    @DELETE
    @ApiOperation(value = "Delete import", response = Response.class)
    @Path("import/{importId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteImport(@PathParam("workspaceId") String workspaceId, @PathParam("importId") String importId) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, AccessRightException {
        productService.removeImport(workspaceId, importId);
        return Response.noContent().build();
    }

    @POST
    @Path("importPreview")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public List<LightPartRevisionDTO> getImportPreview(@Context HttpServletRequest request,
                                                     @PathParam("workspaceId") String workspaceId,
                                                     @QueryParam("autoCheckout") boolean autoCheckout,
                                                     @QueryParam("autoCheckin") boolean autoCheckin,
                                                     @QueryParam("permissiveUpdate") boolean permissiveUpdate)
            throws Exception {

        Collection<Part> parts = request.getParts();

        if(parts.isEmpty() || parts.size() > 1){
            return null;
        }

        Part part = parts.iterator().next();
        String name = FileIO.getFileNameWithoutExtension(part.getSubmittedFileName());
        String extension = FileIO.getExtension(part.getSubmittedFileName());

        File importFile = Files.createTempFile("part-" + name, "-import.tmp" +  (extension==null?"":"." + extension)).toFile();
        BinaryResourceUpload.uploadBinary(new BufferedOutputStream(new FileOutputStream(importFile)), part);
        ImportPreview importPreview = importerService.dryRunImportIntoParts(workspaceId,importFile,name+"."+extension,autoCheckout, autoCheckin, permissiveUpdate);

        importFile.deleteOnExit();

        List<LightPartRevisionDTO> result = new ArrayList<>();
        for(PartRevision partRevision : importPreview.getPartRevsToCheckout()){
            result.add(mapper.map(partRevision,LightPartRevisionDTO.class));
        }

        return result;

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

}