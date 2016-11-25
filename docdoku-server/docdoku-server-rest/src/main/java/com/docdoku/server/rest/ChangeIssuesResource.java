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

import com.docdoku.core.change.ChangeIssue;
import com.docdoku.core.document.DocumentIterationKey;
import com.docdoku.core.exceptions.AccessRightException;
import com.docdoku.core.exceptions.EntityConstraintException;
import com.docdoku.core.exceptions.EntityNotFoundException;
import com.docdoku.core.exceptions.UserNotActiveException;
import com.docdoku.core.meta.Tag;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IChangeManagerLocal;
import com.docdoku.server.rest.dto.*;
import com.docdoku.server.rest.dto.change.ChangeIssueDTO;
import com.docdoku.server.rest.dto.change.ChangeItemDTO;
import io.swagger.annotations.*;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

import javax.annotation.PostConstruct;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

@RequestScoped
@Api(hidden = true, value = "issues", description = "Operations about issues")
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class ChangeIssuesResource {

    @Inject
    private IChangeManagerLocal changeManager;

    private Mapper mapper;

    public ChangeIssuesResource() {

    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @ApiOperation(value = "Get issues for given parameters",
            response = ChangeIssueDTO.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of ChangeIssueDTOs. It can be an empty list."),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response getIssues(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId)
            throws EntityNotFoundException, UserNotActiveException {
        List<ChangeIssue> changeIssues = changeManager.getChangeIssues(workspaceId);
        List<ChangeIssueDTO> changeIssueDTOs = new ArrayList<>();
        for (ChangeIssue issue : changeIssues) {
            ChangeIssueDTO changeIssueDTO = mapper.map(issue, ChangeIssueDTO.class);
            changeIssueDTO.setWritable(changeManager.isChangeItemWritable(issue));
            changeIssueDTOs.add(changeIssueDTO);
        }
        return Response.ok(new GenericEntity<List<ChangeIssueDTO>>((List<ChangeIssueDTO>) changeIssueDTOs) {
        }).build();
    }

    @POST
    @ApiOperation(value = "Create issue",
            response = ChangeIssueDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of created ChangeIssueDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeIssueDTO createIssue(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Change issue to create") ChangeIssueDTO changeIssueDTO)
            throws EntityNotFoundException, AccessRightException {
        ChangeIssue changeIssue = changeManager.createChangeIssue(workspaceId,
                changeIssueDTO.getName(),
                changeIssueDTO.getDescription(),
                changeIssueDTO.getInitiator(),
                changeIssueDTO.getPriority(),
                changeIssueDTO.getAssignee(),
                changeIssueDTO.getCategory());
        ChangeIssueDTO ret = mapper.map(changeIssue, ChangeIssueDTO.class);
        ret.setWritable(true);
        return ret;
    }

    @GET
    @ApiOperation(value = "Search issue with given reference",
            response = ChangeIssueDTO.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of ChangeIssueDTOs. It can be an empty list"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("link")
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeIssueDTO[] searchIssuesToLink(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Query") @QueryParam("q") String q)
            throws EntityNotFoundException, UserNotActiveException {
        int maxResults = 8;
        List<ChangeIssue> issues = changeManager.getIssuesWithReference(workspaceId, q, maxResults);
        List<ChangeIssueDTO> issueDTOs = new ArrayList<>();
        for (ChangeIssue issue : issues) {
            ChangeIssueDTO changeIssueDTO = mapper.map(issue, ChangeIssueDTO.class);
            changeIssueDTO.setWritable(changeManager.isChangeItemWritable(issue));
            issueDTOs.add(changeIssueDTO);
        }
        return issueDTOs.toArray(new ChangeIssueDTO[issueDTOs.size()]);
    }

    @GET
    @ApiOperation(value = "Get one issue",
            response = ChangeIssueDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of ChangeIssueDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{issueId}")
    public ChangeIssueDTO getIssue(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Issue id") @PathParam("issueId") int issueId)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        ChangeIssue changeIssue = changeManager.getChangeIssue(workspaceId, issueId);
        ChangeIssueDTO changeIssueDTO = mapper.map(changeIssue, ChangeIssueDTO.class);
        changeIssueDTO.setWritable(changeManager.isChangeItemWritable(changeIssue));
        return changeIssueDTO;
    }

    @PUT
    @ApiOperation(value = "Update issue",
            response = ChangeIssueDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of updated ChangeIssueDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{issueId}")
    public ChangeIssueDTO updateIssue(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Issue id") @PathParam("issueId") int issueId,
            @ApiParam(required = true, value = "Change issue to update") ChangeIssueDTO pChangeIssueDTO)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        ChangeIssue changeIssue = changeManager.updateChangeIssue(issueId,
                workspaceId,
                pChangeIssueDTO.getDescription(),
                pChangeIssueDTO.getPriority(),
                pChangeIssueDTO.getAssignee(),
                pChangeIssueDTO.getCategory());
        ChangeIssueDTO changeIssueDTO = mapper.map(changeIssue, ChangeIssueDTO.class);
        changeIssueDTO.setWritable(changeManager.isChangeItemWritable(changeIssue));
        return changeIssueDTO;
    }

    @DELETE
    @ApiOperation(value = "Delete issue",
            response = ChangeIssueDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successful deletion"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{issueId}")
    public Response removeIssue(
            @ApiParam(required = true, value = "Issue id") @PathParam("issueId") int issueId)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, EntityConstraintException {
        changeManager.deleteChangeIssue(issueId);
        return Response.noContent().build();
    }

    @PUT
    @ApiOperation(value = "Update tags attached to an issue",
            response = ChangeIssueDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of updated ChangeIssueDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{issueId}/tags")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeItemDTO saveChangeItemTags(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Issue id") @PathParam("issueId") int issueId,
            @ApiParam(required = true, value = "Tag list to add") TagListDTO tagListDTO)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {

        List<TagDTO> tagDTOs = tagListDTO.getTags();
        String[] tagsLabel = new String[tagDTOs.size()];
        for (int i = 0; i < tagDTOs.size(); i++) {
            tagsLabel[i] = tagDTOs.get(i).getLabel();
        }

        ChangeIssue changeIssue = changeManager.saveChangeIssueTags(workspaceId, issueId, tagsLabel);
        ChangeIssueDTO changeIssueDTO = mapper.map(changeIssue, ChangeIssueDTO.class);
        changeIssueDTO.setWritable(changeManager.isChangeItemWritable(changeIssue));
        return changeIssueDTO;
    }

    @POST
    @ApiOperation(value = "Attached a new tag to an issue",
            response = ChangeIssueDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of updated ChangeIssueDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{issueId}/tags")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeIssueDTO addTagToChangeIssue(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Issue id") @PathParam("issueId") int issueId,
            @ApiParam(required = true, value = "Tag list to add") TagListDTO tagListDTO)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        ChangeIssue changeIssue = changeManager.getChangeIssue(workspaceId, issueId);
        Set<Tag> tags = changeIssue.getTags();
        Set<String> tagLabels = new HashSet<>();

        for (TagDTO tagDTO : tagListDTO.getTags()) {
            tagLabels.add(tagDTO.getLabel());
        }

        for (Tag tag : tags) {
            tagLabels.add(tag.getLabel());
        }

        changeIssue = changeManager.saveChangeIssueTags(workspaceId, issueId, tagLabels.toArray(new String[tagLabels.size()]));
        ChangeIssueDTO changeIssueDTO = mapper.map(changeIssue, ChangeIssueDTO.class);
        changeIssueDTO.setWritable(changeManager.isChangeItemWritable(changeIssue));
        return changeIssueDTO;
    }

    @DELETE
    @ApiOperation(value = "Delete a tag attached to an issue",
            response = ChangeIssueDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successful retrieval of updated ChangeIssueDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{issueId}/tags/{tagName}")
    public Response removeTagsFromChangeIssue(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Issue id") @PathParam("issueId") int issueId,
            @ApiParam(required = true, value = "Tag name") @PathParam("tagName") String tagName)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        changeManager.removeChangeIssueTag(workspaceId, issueId, tagName);
        return Response.noContent().build();
    }

    @PUT
    @ApiOperation(value = "Attach a document to an issue",
            response = ChangeIssueDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of updated ChangeIssueDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{issueId}/affected-documents")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeIssueDTO saveChangeIssueAffectedDocuments(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Issue id") @PathParam("issueId") int issueId,
            @ApiParam(required = true, value = "Document list to save as affected") DocumentIterationListDTO documentListDTO)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {

        List<DocumentIterationDTO> documentIterationDTOs = documentListDTO.getDocuments();
        DocumentIterationKey[] links = createDocumentIterationKeys(documentIterationDTOs);

        ChangeIssue changeIssue = changeManager.saveChangeIssueAffectedDocuments(workspaceId, issueId, links);
        ChangeIssueDTO changeIssueDTO = mapper.map(changeIssue, ChangeIssueDTO.class);
        changeIssueDTO.setWritable(changeManager.isChangeItemWritable(changeIssue));
        return changeIssueDTO;
    }

    @PUT
    @ApiOperation(value = "Attach a part to an issue",
            response = ChangeIssueDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of updated ChangeIssueDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{issueId}/affected-parts")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeIssueDTO saveChangeIssueAffectedParts(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Issue id") @PathParam("issueId") int issueId,
            @ApiParam(required = true, value = "Part list to save as affected") PartIterationListDTO partIterationListDTO)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {

        List<PartIterationDTO> partIterationDTOs = partIterationListDTO.getParts();
        PartIterationKey[] links = createPartIterationKeys(partIterationDTOs);

        ChangeIssue changeIssue = changeManager.saveChangeIssueAffectedParts(workspaceId, issueId, links);
        ChangeIssueDTO changeIssueDTO = mapper.map(changeIssue, ChangeIssueDTO.class);
        changeIssueDTO.setWritable(changeManager.isChangeItemWritable(changeIssue));
        return changeIssueDTO;
    }

    @PUT
    @ApiOperation(value = "Update ACL of an issue",
            response = ChangeIssueDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of updated ChangeIssueDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{issueId}/acl")
    @Consumes(MediaType.APPLICATION_JSON)
    public ChangeIssueDTO updateChangeIssueACL(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String pWorkspaceId,
            @ApiParam(required = true, value = "Issue id") @PathParam("issueId") int issueId,
            @ApiParam(required = true, value = "ACL rules to set") ACLDTO acl)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {

        ChangeIssue changeIssue;

        if (!acl.getGroupEntries().isEmpty() || !acl.getUserEntries().isEmpty()) {

            Map<String, String> userEntries = new HashMap<>();
            Map<String, String> groupEntries = new HashMap<>();

            for (ACLEntryDTO entry : acl.getUserEntries()) {
                userEntries.put(entry.getKey(), entry.getValue().name());
            }

            for (ACLEntryDTO entry : acl.getGroupEntries()) {
                groupEntries.put(entry.getKey(), entry.getValue().name());
            }

            changeIssue = changeManager.updateACLForChangeIssue(pWorkspaceId, issueId, userEntries, groupEntries);
        } else {
            changeIssue = changeManager.removeACLFromChangeIssue(pWorkspaceId, issueId);
        }

        ChangeIssueDTO changeIssueDTO = mapper.map(changeIssue, ChangeIssueDTO.class);
        changeIssueDTO.setWritable(changeManager.isChangeItemWritable(changeIssue));

        return changeIssueDTO;
    }


    private DocumentIterationKey[] createDocumentIterationKeys(List<DocumentIterationDTO> dtos) {
        DocumentIterationKey[] data = new DocumentIterationKey[dtos.size()];
        int i = 0;
        for (DocumentIterationDTO dto : dtos) {
            data[i++] = new DocumentIterationKey(dto.getWorkspaceId(), dto.getDocumentMasterId(), dto.getVersion(), dto.getIteration());
        }

        return data;
    }

    private PartIterationKey[] createPartIterationKeys(List<PartIterationDTO> dtos) {
        PartIterationKey[] data = new PartIterationKey[dtos.size()];
        int i = 0;
        for (PartIterationDTO dto : dtos) {
            data[i++] = new PartIterationKey(dto.getWorkspaceId(), dto.getNumber(), dto.getVersion(), dto.getIteration());
        }

        return data;
    }
}