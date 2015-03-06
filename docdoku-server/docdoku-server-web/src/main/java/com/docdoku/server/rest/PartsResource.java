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
import com.docdoku.core.common.UserGroup;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.query.PartSearchQuery;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.ACLUserEntry;
import com.docdoku.core.security.ACLUserGroupEntry;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.server.rest.dto.*;
import com.docdoku.server.rest.util.SearchQueryParser;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            partDTOs.add(Tools.mapPartRevisionToPartDTO(partRevision));
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
            partDTOs.add(Tools.mapPartRevisionToPartDTO(partRevision));
        }

        return partDTOs;
    }

    @GET
    @Path("search/{query}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PartDTO> searchPartRevisions(@PathParam("workspaceId") String workspaceId, @PathParam("query") String pStringQuery)
            throws EntityNotFoundException, ESServerException, UserNotActiveException {

        PartSearchQuery partSearchQuery = SearchQueryParser.parsePartStringQuery(workspaceId, pStringQuery);

        List<PartRevision> partRevisions = productService.searchPartRevisions(partSearchQuery);
        List<PartDTO> partDTOs = new ArrayList<>();

        for(PartRevision partRevision : partRevisions){
            partDTOs.add(Tools.mapPartRevisionToPartDTO(partRevision));
        }

        return partDTOs;
    }

    @GET
    @Path("checkedout")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PartDTO> getCheckedOutPartRevisions(@PathParam("workspaceId") String workspaceId) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, AccessRightException, AccountNotFoundException {
        PartRevision[] checkedOutPartRevisions = productService.getCheckedOutPartRevisions(workspaceId);
        List<PartDTO> partDTOs = new ArrayList<>();
        for(PartRevision partRevision : checkedOutPartRevisions){
            partDTOs.add(Tools.mapPartRevisionToPartDTO(partRevision));
        }
        return partDTOs;
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
    public PartIterationDTO[] searchDocumentsLastIterationToLink(@PathParam("workspaceId") String workspaceId,@QueryParam("q") String q)
            throws EntityNotFoundException, UserNotActiveException {

        int maxResults = 8;
        PartRevision[] partRs = productService.getPartRevisionsWithReference(workspaceId, q, maxResults);

        List<PartIterationDTO> partsLastIter = new ArrayList<>();
        for (PartRevision partR : partRs) {
            PartIteration partLastIter = partR.getLastIteration();
            if (partLastIter != null) {
                partsLastIter.add(new PartIterationDTO(partLastIter.getWorkspaceId(), partLastIter.getPartNumber(), partLastIter.getPartVersion(), partLastIter.getIteration()));
            }
        }

        return partsLastIter.toArray(new PartIterationDTO[partsLastIter.size()]);
    }
}