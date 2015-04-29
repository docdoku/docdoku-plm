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
import com.docdoku.core.common.UserGroup;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.query.PartSearchQuery;
import com.docdoku.core.query.Query;
import com.docdoku.core.query.QueryResultRow;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.ACLUserEntry;
import com.docdoku.core.security.ACLUserGroupEntry;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.server.export.ExcelGenerator;
import com.docdoku.server.rest.collections.QueryResult;
import com.docdoku.server.rest.dto.*;
import com.docdoku.server.rest.util.SearchQueryParser;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

import javax.annotation.PostConstruct;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

@Stateless
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class PartsResource {

    @EJB
    private IProductManagerLocal productService;

    @EJB
    private IUserManagerLocal userManager;

    @EJB
    private PartResource part;

    public PartsResource() {
    }
    private Mapper mapper;

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @Path("{partNumber: [^/].*}-{partVersion:[A-Z]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public PartResource getPart() {
        return part;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<PartDTO> getPartRevisions(@PathParam("workspaceId") String workspaceId, @QueryParam("start") int start)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException {

        int maxResults = 20;
        List<PartRevision> partRevisions = productService.getPartRevisions(Tools.stripTrailingSlash(workspaceId), start, maxResults);
        List<PartDTO> partDTOs = new ArrayList<>();

        for(PartRevision partRevision : partRevisions){
            PartDTO partDTO = Tools.mapPartRevisionToPartDTO(partRevision);

            List<ModificationNotificationDTO> notificationDTOs = getModificationNotificationDTOs(partRevision);
            partDTO.setNotifications(notificationDTOs);

            partDTOs.add(partDTO);
        }

        return partDTOs;
    }

    @GET
    @Path("count")
    @Produces(MediaType.APPLICATION_JSON)
    public CountDTO getTotalNumberOfParts(@PathParam("workspaceId") String workspaceId)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException {

        return new CountDTO(productService.getTotalNumberOfParts(Tools.stripTrailingSlash(workspaceId)));
    }

    @GET
    @Path("tags/{tagId}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PartDTO> getPartRevisions(@PathParam("workspaceId") String workspaceId,@PathParam("tagId") String tagId)
            throws EntityNotFoundException, AccessRightException, UserNotActiveException {

        PartRevision[] partRevisions = productService.findPartRevisionsByTag(Tools.stripTrailingSlash(workspaceId), tagId);
        List<PartDTO> partDTOs = new ArrayList<>();

        for(PartRevision partRevision : partRevisions){
            PartDTO partDTO = Tools.mapPartRevisionToPartDTO(partRevision);

            List<ModificationNotificationDTO> notificationDTOs = getModificationNotificationDTOs(partRevision);
            partDTO.setNotifications(notificationDTOs);

            partDTOs.add(partDTO);
        }

        return partDTOs;
    }

    @GET
    @Path("search/{query}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PartDTO> searchPartRevisions(@PathParam("workspaceId") String workspaceId, @PathParam("query") String pStringQuery)
            throws EntityNotFoundException, ESServerException, UserNotActiveException, AccessRightException {

        PartSearchQuery partSearchQuery = SearchQueryParser.parsePartStringQuery(workspaceId, pStringQuery);

        List<PartRevision> partRevisions = productService.searchPartRevisions(partSearchQuery);
        List<PartDTO> partDTOs = new ArrayList<>();

        for(PartRevision partRevision : partRevisions){
            PartDTO partDTO = Tools.mapPartRevisionToPartDTO(partRevision);

            List<ModificationNotificationDTO> notificationDTOs = getModificationNotificationDTOs(partRevision);
            partDTO.setNotifications(notificationDTOs);

            partDTOs.add(partDTO);
        }

        return partDTOs;
    }

    @GET
    @Path("queries")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<QueryDTO> getCustomQueries(@PathParam("workspaceId") String workspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        List<Query> queries = productService.getQueries(workspaceId);
        List<QueryDTO> queryDTOs = new ArrayList<>();
        for(Query query : queries){
            queryDTOs.add(mapper.map(query,QueryDTO.class));
        }
        return queryDTOs;
    }

    @POST
    @Path("queries")
    @Consumes(MediaType.APPLICATION_JSON)
    public void runCustomQuery(@PathParam("workspaceId") String workspaceId, @QueryParam("save") boolean save,  @QueryParam("export") String exportType, QueryDTO queryDTO) throws EntityNotFoundException, UserNotActiveException, AccessRightException, CreationException, QueryAlreadyExistsException, EntityConstraintException, NotAllowedException {
        Query query = mapper.map(queryDTO, Query.class);
        QueryResult queryResult = getQueryResult(workspaceId, query, exportType);
        if(save){
            productService.createQuery(workspaceId,query);
        }
        //return makeQueryResponse(queryResult);
    }
    @GET
    @Path("queries/{queryId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/vnd.ms-excel")
    public Response exportCustomQuery(@PathParam("workspaceId") String workspaceId,@PathParam("queryId") int queryId) throws EntityNotFoundException, UserNotActiveException, AccessRightException, CreationException, QueryAlreadyExistsException, EntityConstraintException, NotAllowedException {
        Query query = productService.loadQuery(workspaceId,queryId);
        List<PartRevision> partRevisions = productService.searchPartRevisions(workspaceId, query);
        QueryResult queryResult = new QueryResult(partRevisions,query);
        return makeQueryResponse(queryResult);
    }


    private QueryResult getQueryResult(String workspaceId, Query query, String pExportType) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, EntityConstraintException, BaselineNotFoundException, ProductInstanceMasterNotFoundException, NotAllowedException, ConfigurationItemNotFoundException, PartMasterNotFoundException {
        List<PartRevision> partRevisions = productService.searchPartRevisions(workspaceId, query);
        QueryResult queryResult = new QueryResult(partRevisions,query);
        if(query.hasContext()){
            List<QueryResultRow> rows = productService.filterProductBreakdownStructure(workspaceId,query);
            queryResult.mergeRows(rows);
        }
        String exportType = pExportType != null ? pExportType : "JSON";
        queryResult.setExportType(QueryResult.ExportType.valueOf(exportType));
        return queryResult;
    }

    public Response makeQueryResponse(QueryResult queryResult) {
         ExcelGenerator excelGenerator = new ExcelGenerator();

        String contentType = "application/vnd.ms-excel";
        String contentDisposition =  "attachment; filename=export_parts.xls";
        Response.ResponseBuilder responseBuilder = Response.ok((Object)
                excelGenerator.generateXLSResponse(queryResult));
         responseBuilder
                .header("Content-Type", contentType)
                .header("Content-Disposition", contentDisposition)
                .entity(queryResult).build();
        return responseBuilder.build();
    }

    @DELETE
    @Path("queries/{queryId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteQuery(@PathParam("workspaceId") String workspaceId, @PathParam("queryId") int queryId) throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        productService.deleteQuery(workspaceId,queryId);
        return Response.ok().build();
    }

    @GET
    @Path("checkedout")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PartDTO> getCheckedOutPartRevisions(@PathParam("workspaceId") String workspaceId)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        PartRevision[] checkedOutPartRevisions = productService.getCheckedOutPartRevisions(workspaceId);
        List<PartDTO> partDTOs = new ArrayList<>();

        for(PartRevision partRevision : checkedOutPartRevisions){
            PartDTO partDTO = Tools.mapPartRevisionToPartDTO(partRevision);

            List<ModificationNotificationDTO> notificationDTOs = getModificationNotificationDTOs(partRevision);
            partDTO.setNotifications(notificationDTOs);

            partDTOs.add(partDTO);
        }
        return partDTOs;
    }

    @GET
    @Path("countCheckedOut")
    @Produces(MediaType.APPLICATION_JSON)
    public CountDTO getCheckedOutNumberOfItems(@PathParam("workspaceId") String workspaceId) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, AccessRightException, AccountNotFoundException {
        return new CountDTO(productService.getCheckedOutPartRevisions(workspaceId).length);
    }

    @GET
    @Path("numbers")
    @Produces(MediaType.APPLICATION_JSON)
    public List<LightPartMasterDTO> searchPartNumbers(@PathParam("workspaceId") String workspaceId, @QueryParam("q") String q)
            throws EntityNotFoundException, AccessRightException {

        List<PartMaster> partMasters = productService.findPartMasters(Tools.stripTrailingSlash(workspaceId), "%" + q + "%", 8);
        List<LightPartMasterDTO> partsMastersDTO = new ArrayList<>();
        for(PartMaster p : partMasters){
            LightPartMasterDTO lightPartMasterDTO= new LightPartMasterDTO(p.getNumber(),p.getName());
            partsMastersDTO.add(lightPartMasterDTO);
        }
        return partsMastersDTO;
    }


    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public PartDTO createNewPart(@PathParam("workspaceId") String workspaceId, PartCreationDTO partCreationDTO)
            throws EntityNotFoundException, EntityAlreadyExistsException, CreationException, AccessRightException, NotAllowedException {

        String pWorkflowModelId = partCreationDTO.getWorkflowModelId();
        RoleMappingDTO[] rolesMappingDTO = partCreationDTO.getRoleMapping();

        Map<String, String> roleMappings = new HashMap<>();

        if(rolesMappingDTO != null){
            for(RoleMappingDTO roleMappingDTO : rolesMappingDTO){
                roleMappings.put(roleMappingDTO.getRoleName(),roleMappingDTO.getUserLogin());
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
                userEntries[i].setPrincipal(new User(new Workspace(workspaceId), entry.getKey()));
                userEntries[i++].setPermission(ACL.Permission.valueOf(entry.getValue().name()));
            }
            i = 0;
            for (Map.Entry<String, ACL.Permission> entry : acl.getGroupEntries().entrySet()) {
                userGroupEntries[i] = new ACLUserGroupEntry();
                userGroupEntries[i].setPrincipal(new UserGroup(new Workspace(workspaceId), entry.getKey()));
                userGroupEntries[i++].setPermission(ACL.Permission.valueOf(entry.getValue().name()));
            }
        }

        PartMaster partMaster = productService.createPartMaster(workspaceId, partCreationDTO.getNumber(), partCreationDTO.getName(), partCreationDTO.isStandardPart(), pWorkflowModelId, partCreationDTO.getDescription(), partCreationDTO.getTemplateId(), roleMappings, userEntries, userGroupEntries);
        return Tools.mapPartRevisionToPartDTO(partMaster.getLastRevision());
    }

    @GET
    @Path("parts_last_iter")
    @Produces(MediaType.APPLICATION_JSON)
    public PartIterationDTO[] searchDocumentsLastIterationToLink(@PathParam("workspaceId") String workspaceId, @QueryParam("q") String q, @QueryParam("l") int limit)
            throws EntityNotFoundException, UserNotActiveException {

        int maxResults = limit==0 ? 15 : limit;
        PartRevision[] partRs = productService.getPartRevisionsWithReferenceOrName(workspaceId, q, maxResults);

        List<PartIterationDTO> partsLastIter = new ArrayList<>();
        for (PartRevision partR : partRs) {
            PartIteration partLastIter = partR.getLastIteration();
            if (partLastIter != null) {
                partsLastIter.add(new PartIterationDTO(partLastIter.getWorkspaceId(),partLastIter.getPartName(), partLastIter.getPartNumber(), partLastIter.getPartVersion(), partLastIter.getIteration()));
            }
        }

        return partsLastIter.toArray(new PartIterationDTO[partsLastIter.size()]);
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