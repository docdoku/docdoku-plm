/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
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

import com.docdoku.core.change.ChangeRequest;
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
import com.docdoku.server.rest.dto.change.ChangeIssueListDTO;
import com.docdoku.server.rest.dto.change.ChangeRequestDTO;
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
@Api(hidden = true, value = "requests", description = "Operations about requests")
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class ChangeRequestsResource {

    @Inject
    private IChangeManagerLocal changeManager;

    private Mapper mapper;

    public ChangeRequestsResource() {

    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @ApiOperation(value = "Get requests for given parameters",
            response = ChangeRequestDTO.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of ChangeRequestDTOs. It can be an empty list."),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRequests(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId)
            throws EntityNotFoundException, UserNotActiveException {
        List<ChangeRequest> changeRequests = changeManager.getChangeRequests(workspaceId);
        List<ChangeRequestDTO> changeRequestDTOs = new ArrayList<>();
        for (ChangeRequest request : changeRequests) {
            ChangeRequestDTO changeRequestDTO = mapper.map(request, ChangeRequestDTO.class);
            changeRequestDTO.setWritable(changeManager.isChangeItemWritable(request));
            changeRequestDTOs.add(changeRequestDTO);
        }
        return Response.ok(new GenericEntity<List<ChangeRequestDTO>>((List<ChangeRequestDTO>) changeRequestDTOs) {
        }).build();
    }

    @POST
    @ApiOperation(value = "Create request",
            response = ChangeRequestDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of created ChangeRequestDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeRequestDTO createRequest(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Change request to create") ChangeRequestDTO changeRequestDTO)
            throws EntityNotFoundException, AccessRightException {
        ChangeRequest changeRequest = changeManager.createChangeRequest(workspaceId,
                changeRequestDTO.getName(),
                changeRequestDTO.getDescription(),
                changeRequestDTO.getMilestoneId(),
                changeRequestDTO.getPriority(),
                changeRequestDTO.getAssignee(),
                changeRequestDTO.getCategory());
        ChangeRequestDTO ret = mapper.map(changeRequest, ChangeRequestDTO.class);
        ret.setWritable(true);
        return ret;
    }

    @GET
    @ApiOperation(value = "Search request for given references",
            response = ChangeRequestDTO.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of ChangeRequestDTOs. It can be an empty list."),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("link")
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeRequestDTO[] searchRequestsToLink(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Query") @QueryParam("q") String q)
            throws EntityNotFoundException, UserNotActiveException {
        int maxResults = 8;
        List<ChangeRequest> requests = changeManager.getRequestsWithReference(workspaceId, q, maxResults);
        List<ChangeRequestDTO> requestDTOs = new ArrayList<>();
        for (ChangeRequest request : requests) {
            ChangeRequestDTO changeRequestDTO = mapper.map(request, ChangeRequestDTO.class);
            changeRequestDTO.setWritable(changeManager.isChangeItemWritable(request));
            requestDTOs.add(changeRequestDTO);
        }
        return requestDTOs.toArray(new ChangeRequestDTO[requestDTOs.size()]);
    }

    @GET
    @ApiOperation(value = "Get request",
            response = ChangeRequestDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of ChangeRequestDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{requestId}")
    public ChangeRequestDTO getRequest(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Request id") @PathParam("requestId") int requestId)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        ChangeRequest changeRequest = changeManager.getChangeRequest(workspaceId, requestId);
        ChangeRequestDTO changeRequestDTO = mapper.map(changeRequest, ChangeRequestDTO.class);
        changeRequestDTO.setWritable(changeManager.isChangeItemWritable(changeRequest));
        return changeRequestDTO;
    }

    @PUT
    @ApiOperation(value = "Update request",
            response = ChangeRequestDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of updated ChangeRequestDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{requestId}")
    public ChangeRequestDTO updateRequest(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Request id") @PathParam("requestId") int requestId,
            @ApiParam(required = true, value = "Request to update") ChangeRequestDTO pChangeRequestDTO)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        ChangeRequest changeRequest = changeManager.updateChangeRequest(requestId,
                workspaceId,
                pChangeRequestDTO.getDescription(),
                pChangeRequestDTO.getMilestoneId(),
                pChangeRequestDTO.getPriority(),
                pChangeRequestDTO.getAssignee(),
                pChangeRequestDTO.getCategory());
        ChangeRequestDTO changeRequestDTO = mapper.map(changeRequest, ChangeRequestDTO.class);
        changeRequestDTO.setWritable(changeManager.isChangeItemWritable(changeRequest));
        return changeRequestDTO;
    }

    @DELETE
    @ApiOperation(value = "Delete request",
            response = ChangeRequestDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successful deletion of ChangeRequestDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{requestId}")
    public Response removeRequest(
            @PathParam("workspaceId") String workspaceId,
            @PathParam("requestId") int requestId)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, EntityConstraintException {
        changeManager.deleteChangeRequest(workspaceId, requestId);
        return Response.noContent().build();
    }


    @PUT
    @ApiOperation(value = "Update tag attached to a request",
            response = ChangeRequestDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of updated ChangeRequestDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{requestId}/tags")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeRequestDTO saveChangeRequestTags(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Request id") @PathParam("requestId") int requestId,
            @ApiParam(required = true, value = "Tag list to add") TagListDTO tagListDTO)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        List<TagDTO> tagDTOs = tagListDTO.getTags();
        String[] tagsLabel = new String[tagDTOs.size()];
        for (int i = 0; i < tagDTOs.size(); i++) {
            tagsLabel[i] = tagDTOs.get(i).getLabel();
        }

        ChangeRequest changeRequest = changeManager.saveChangeRequestTags(workspaceId, requestId, tagsLabel);
        ChangeRequestDTO changeRequestDTO = mapper.map(changeRequest, ChangeRequestDTO.class);
        changeRequestDTO.setWritable(changeManager.isChangeItemWritable(changeRequest));
        return changeRequestDTO;
    }

    @POST
    @ApiOperation(value = "Attach a new tag to a request",
            response = ChangeRequestDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of updated ChangeRequestDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{requestId}/tags")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeRequestDTO addTagsToChangeRequest(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Request id") @PathParam("requestId") int requestId,
            @ApiParam(required = true, value = "Tag list to add") TagListDTO tagListDTO)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        ChangeRequest changeRequest = changeManager.getChangeRequest(workspaceId, requestId);
        Set<Tag> tags = changeRequest.getTags();
        Set<String> tagLabels = new HashSet<>();

        for (TagDTO tagDTO : tagListDTO.getTags()) {
            tagLabels.add(tagDTO.getLabel());
        }

        for (Tag tag : tags) {
            tagLabels.add(tag.getLabel());
        }

        changeRequest = changeManager.saveChangeRequestTags(workspaceId, requestId, tagLabels.toArray(new String[tagLabels.size()]));
        ChangeRequestDTO changeRequestDTO = mapper.map(changeRequest, ChangeRequestDTO.class);
        changeRequestDTO.setWritable(changeManager.isChangeItemWritable(changeRequest));
        return changeRequestDTO;

    }

    @DELETE
    @ApiOperation(value = "Delete tag attached to a request",
            response = ChangeRequestDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of updated ChangeRequestDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{requestId}/tags/{tagName}")
    public ChangeRequestDTO removeTagFromChangeRequest(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Request id") @PathParam("requestId") int requestId,
            @ApiParam(required = true, value = "Tag to remove") @PathParam("tagName") String tagName)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        ChangeRequest changeRequest = changeManager.removeChangeRequestTag(workspaceId, requestId, tagName);
        ChangeRequestDTO changeRequestDTO = mapper.map(changeRequest, ChangeRequestDTO.class);
        changeRequestDTO.setWritable(changeManager.isChangeItemWritable(changeRequest));
        return changeRequestDTO;
    }

    @PUT
    @ApiOperation(value = "Attach document to a request",
            response = ChangeRequestDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of updated ChangeRequestDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{requestId}/affected-documents")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeRequestDTO saveChangeRequestAffectedDocuments(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Request id") @PathParam("requestId") int requestId,
            @ApiParam(required = true, value = "Document list to save as affected") DocumentIterationListDTO documentIterationListDTO)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {

        List<DocumentIterationDTO> documentIterationDTOs = documentIterationListDTO.getDocuments();
        DocumentIterationKey[] links = createDocumentIterationKeys(documentIterationDTOs);

        ChangeRequest changeRequest = changeManager.saveChangeRequestAffectedDocuments(workspaceId, requestId, links);
        ChangeRequestDTO changeRequestDTO = mapper.map(changeRequest, ChangeRequestDTO.class);
        changeRequestDTO.setWritable(changeManager.isChangeItemWritable(changeRequest));
        return changeRequestDTO;
    }

    @PUT
    @ApiOperation(value = "Attach part to a request",
            response = ChangeRequestDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of updated ChangeRequestDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{requestId}/affected-parts")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeRequestDTO saveChangeRequestAffectedParts(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Request id") @PathParam("requestId") int requestId,
            @ApiParam(required = true, value = "Parts to save as affected") PartIterationListDTO partIterationListDTO)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {

        List<PartIterationDTO> partIterationDTOs = partIterationListDTO.getParts();
        PartIterationKey[] links = createPartIterationKeys(partIterationDTOs);

        ChangeRequest changeRequest = changeManager.saveChangeRequestAffectedParts(workspaceId, requestId, links);
        ChangeRequestDTO changeRequestDTO = mapper.map(changeRequest, ChangeRequestDTO.class);
        changeRequestDTO.setWritable(changeManager.isChangeItemWritable(changeRequest));
        return changeRequestDTO;
    }

    @PUT
    @ApiOperation(value = "Attach issue to a request",
            response = ChangeRequestDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of updated ChangeRequestDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{requestId}/affected-issues")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeRequestDTO saveAffectedIssues(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Request id") @PathParam("requestId") int requestId,
            @ApiParam(required = true, value = "Change issues to save as affected") ChangeIssueListDTO changeIssueListDTO)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        int[] links;
        List<ChangeIssueDTO> changeIssueDTOs = changeIssueListDTO.getIssues();
        if (changeIssueDTOs != null) {
            int i = 0;
            links = new int[changeIssueDTOs.size()];
            for (ChangeIssueDTO changeIssueDTO : changeIssueDTOs) {
                links[i++] = changeIssueDTO.getId();
            }
        } else {
            links = new int[0];
        }

        ChangeRequest changeRequest = changeManager.saveChangeRequestAffectedIssues(workspaceId, requestId, links);
        ChangeRequestDTO changeRequestDTO = mapper.map(changeRequest, ChangeRequestDTO.class);
        changeRequestDTO.setWritable(changeManager.isChangeItemWritable(changeRequest));
        return changeRequestDTO;
    }

    @PUT
    @ApiOperation(value = "Update ACL of a request",
            response = ChangeRequestDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of updated ChangeRequestDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{requestId}/acl")
    @Consumes(MediaType.APPLICATION_JSON)
    public ChangeRequestDTO updateChangeRequestACL(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Request id") @PathParam("requestId") int requestId,
            @ApiParam(required = true, value = "ACL rules to set") ACLDTO acl)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {

        ChangeRequest changeRequest;

        if (acl.hasEntries()) {
            changeRequest = changeManager.updateACLForChangeRequest(workspaceId, requestId, acl.getUserEntriesMap(), acl.getUserGroupEntriesMap());
        } else {
            changeRequest = changeManager.removeACLFromChangeRequest(workspaceId, requestId);
        }

        ChangeRequestDTO changeRequestDTO = mapper.map(changeRequest, ChangeRequestDTO.class);
        changeRequestDTO.setWritable(changeManager.isChangeItemWritable(changeRequest));
        return changeRequestDTO;
    }

    private DocumentIterationKey[] createDocumentIterationKeys(List<DocumentIterationDTO> documentIterationDTOList) {
        DocumentIterationKey[] data = new DocumentIterationKey[documentIterationDTOList.size()];
        int i = 0;
        for (DocumentIterationDTO dto : documentIterationDTOList) {
            data[i++] = new DocumentIterationKey(dto.getWorkspaceId(), dto.getDocumentMasterId(), dto.getVersion(), dto.getIteration());
        }

        return data;
    }

    private PartIterationKey[] createPartIterationKeys(List<PartIterationDTO> partIterationDTOs) {
        PartIterationKey[] data = new PartIterationKey[partIterationDTOs.size()];
        int i = 0;
        for (PartIterationDTO dto : partIterationDTOs) {
            data[i++] = new PartIterationKey(dto.getWorkspaceId(), dto.getNumber(), dto.getVersion(), dto.getIteration());
        }

        return data;
    }
}