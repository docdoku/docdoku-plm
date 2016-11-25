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

import com.docdoku.core.change.ChangeItem;
import com.docdoku.core.change.ChangeOrder;
import com.docdoku.core.document.DocumentIterationKey;
import com.docdoku.core.exceptions.AccessRightException;
import com.docdoku.core.exceptions.EntityNotFoundException;
import com.docdoku.core.exceptions.UserNotActiveException;
import com.docdoku.core.meta.Tag;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IChangeManagerLocal;
import com.docdoku.server.rest.dto.*;
import com.docdoku.server.rest.dto.change.ChangeOrderDTO;
import com.docdoku.server.rest.dto.change.ChangeRequestDTO;
import com.docdoku.server.rest.dto.change.ChangeRequestListDTO;
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
@Api(hidden = true, value = "orders", description = "Operations about orders")
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class ChangeOrdersResource {

    @Inject
    private IChangeManagerLocal changeManager;

    private Mapper mapper;

    public ChangeOrdersResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @ApiOperation(value = "Get orders for given parameters",
            response = ChangeOrderDTO.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of ChangeOrderDTOs. It can be an empty list."),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOrders(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId)
            throws EntityNotFoundException, UserNotActiveException {
        List<ChangeOrder> changeOrders = changeManager.getChangeOrders(workspaceId);
        List<ChangeOrderDTO> changeOrderDTOs = new ArrayList<>();
        for (ChangeOrder order : changeOrders) {
            ChangeOrderDTO changeOrderDTO = mapper.map(order, ChangeOrderDTO.class);
            changeOrderDTO.setWritable(changeManager.isChangeItemWritable(order));
            changeOrderDTOs.add(changeOrderDTO);
        }
        return Response.ok(new GenericEntity<List<ChangeOrderDTO>>((List<ChangeOrderDTO>) changeOrderDTOs) {
        }).build();
    }

    @POST
    @ApiOperation(value = "Create order",
            response = ChangeOrderDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of created ChangeOrderDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeOrderDTO createOrder(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Change order to create") ChangeOrderDTO changeOrderDTO)
            throws EntityNotFoundException, AccessRightException {
        ChangeOrder changeOrder = changeManager.createChangeOrder(workspaceId,
                changeOrderDTO.getName(),
                changeOrderDTO.getDescription(),
                changeOrderDTO.getMilestoneId(),
                changeOrderDTO.getPriority(),
                changeOrderDTO.getAssignee(),
                changeOrderDTO.getCategory());
        ChangeOrderDTO ret = mapper.map(changeOrder, ChangeOrderDTO.class);
        ret.setWritable(true);
        return ret;
    }

    @GET
    @ApiOperation(value = "Get order",
            response = ChangeOrderDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of ChangeOrderDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{orderId}")
    public ChangeOrderDTO getOrder(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Order id") @PathParam("orderId") int orderId)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        ChangeOrder changeOrder = changeManager.getChangeOrder(workspaceId, orderId);
        ChangeOrderDTO changeOrderDTO = mapper.map(changeOrder, ChangeOrderDTO.class);
        changeOrderDTO.setWritable(changeManager.isChangeItemWritable(changeOrder));
        return changeOrderDTO;
    }

    @PUT
    @ApiOperation(value = "Update order",
            response = ChangeOrderDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of updated ChangeOrderDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{orderId}")
    public ChangeOrderDTO updateOrder(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Order id") @PathParam("orderId") int orderId,
            @ApiParam(required = true, value = "Change order to update") ChangeOrderDTO pChangeOrderDTO)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        ChangeOrder changeOrder = changeManager.updateChangeOrder(orderId,
                workspaceId,
                pChangeOrderDTO.getDescription(),
                pChangeOrderDTO.getMilestoneId(),
                pChangeOrderDTO.getPriority(),
                pChangeOrderDTO.getAssignee(),
                pChangeOrderDTO.getCategory());
        ChangeOrderDTO changeOrderDTO = mapper.map(changeOrder, ChangeOrderDTO.class);
        changeOrderDTO.setWritable(changeManager.isChangeItemWritable(changeOrder));
        return changeOrderDTO;
    }

    @DELETE
    @ApiOperation(value = "Delete order",
            response = Response.class)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successful deletion of ChangeOrderDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{orderId}")
    public Response removeOrder(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Order id") @PathParam("orderId") int orderId)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        changeManager.deleteChangeOrder(orderId);
        return Response.noContent().build();
    }


    @PUT
    @ApiOperation(value = "Update tag attached to order",
            response = ChangeOrderDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of updated ChangeOrderDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{orderId}/tags")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeOrderDTO saveChangeOrderTags(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Order id") @PathParam("orderId") int orderId,
            @ApiParam(required = true, value = "Tag list to add") TagListDTO tagListDTO)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        List<TagDTO> tagDTOs = tagListDTO.getTags();
        String[] tagsLabel = new String[tagDTOs.size()];
        for (int i = 0; i < tagDTOs.size(); i++) {
            tagsLabel[i] = tagDTOs.get(i).getLabel();
        }

        ChangeOrder changeOrder = changeManager.saveChangeOrderTags(workspaceId, orderId, tagsLabel);
        ChangeOrderDTO changeOrderDTO = mapper.map(changeOrder, ChangeOrderDTO.class);
        changeOrderDTO.setWritable(changeManager.isChangeItemWritable(changeOrder));
        return changeOrderDTO;
    }

    @POST
    @ApiOperation(value = "Add new tag to order",
            response = ChangeOrderDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of updated ChangeOrderDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{orderId}/tags")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeOrderDTO addTagToChangeOrder(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Order id") @PathParam("orderId") int orderId,
            @ApiParam(required = true, value = "Tag list to add") TagListDTO tagListDTO)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        ChangeOrder changeOrder = changeManager.getChangeOrder(workspaceId, orderId);
        Set<Tag> tags = changeOrder.getTags();
        Set<String> tagLabels = new HashSet<>();

        for (TagDTO tagDTO : tagListDTO.getTags()) {
            tagLabels.add(tagDTO.getLabel());
        }

        for (Tag tag : tags) {
            tagLabels.add(tag.getLabel());
        }

        changeOrder = changeManager.saveChangeOrderTags(workspaceId, orderId, tagLabels.toArray(new String[tagLabels.size()]));
        ChangeOrderDTO changeOrderDTO = mapper.map(changeOrder, ChangeOrderDTO.class);
        changeOrderDTO.setWritable(changeManager.isChangeItemWritable(changeOrder));
        return changeOrderDTO;

    }

    @DELETE
    @ApiOperation(value = "Delete tag attached to order",
            response = ChangeOrderDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of updated ChangeOrderDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{orderId}/tags/{tagName}")
    public ChangeOrderDTO removeTagsFromChangeOrder(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Order id") @PathParam("orderId") int orderId,
            @ApiParam(required = true, value = "Tag name") @PathParam("tagName") String tagName)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        ChangeOrder changeOrder = changeManager.removeChangeOrderTag(workspaceId, orderId, tagName);
        ChangeOrderDTO changeOrderDTO = mapper.map(changeOrder, ChangeOrderDTO.class);
        changeOrderDTO.setWritable(changeManager.isChangeItemWritable(changeOrder));
        return changeOrderDTO;
    }

    @PUT
    @ApiOperation(value = "Attach document to order",
            response = ChangeOrderDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of updated ChangeOrderDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{orderId}/affected-documents")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeOrderDTO saveChangeOrderAffectedDocuments(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Order id") @PathParam("orderId") int orderId,
            @ApiParam(required = true, value = "Documents to save as affected") DocumentIterationListDTO documentIterationListDTO)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {

        List<DocumentIterationDTO> documentIterationDTOs = documentIterationListDTO.getDocuments();
        DocumentIterationKey[] links = createDocumentIterationKeys(documentIterationDTOs);

        ChangeOrder changeOrder = changeManager.saveChangeOrderAffectedDocuments(workspaceId, orderId, links);
        ChangeOrderDTO changeOrderDTO = mapper.map(changeOrder, ChangeOrderDTO.class);
        changeOrderDTO.setWritable(changeManager.isChangeItemWritable(changeOrder));
        return changeOrderDTO;
    }

    @PUT
    @ApiOperation(value = "Attach part to order",
            response = ChangeOrderDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of updated ChangeOrderDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{orderId}/affected-parts")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeOrderDTO saveChangeOrderAffectedParts(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Order id") @PathParam("orderId") int orderId,
            @ApiParam(required = true, value = "Parts to save as affected") PartIterationListDTO partIterationListDTO)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {

        List<PartIterationDTO> partIterationDTOs = partIterationListDTO.getParts();
        PartIterationKey[] links = createPartIterationKeys(partIterationDTOs);

        ChangeOrder changeOrder = changeManager.saveChangeOrderAffectedParts(workspaceId, orderId, links);
        ChangeOrderDTO changeOrderDTO = mapper.map(changeOrder, ChangeOrderDTO.class);
        changeOrderDTO.setWritable(changeManager.isChangeItemWritable(changeOrder));
        return changeOrderDTO;
    }

    @PUT
    @ApiOperation(value = "Attach request to order",
            response = ChangeOrderDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of updated ChangeOrderDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{orderId}/affected-requests")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeOrderDTO saveAffectedRequests(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Order id") @PathParam("orderId") int orderId,
            @ApiParam(required = true, value = "Change requests to save as affected") ChangeRequestListDTO changeRequestListDTOs)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        int[] links;
        List<ChangeRequestDTO> changeRequestDTOs = changeRequestListDTOs.getRequests();
        if (changeRequestDTOs != null) {
            int i = 0;
            links = new int[changeRequestDTOs.size()];
            for (ChangeRequestDTO changeRequestDTO : changeRequestDTOs) {
                links[i++] = changeRequestDTO.getId();
            }
        } else {
            links = new int[0];
        }

        ChangeOrder changeOrder = changeManager.saveChangeOrderAffectedRequests(workspaceId, orderId, links);
        ChangeOrderDTO changeOrderDTO = mapper.map(changeOrder, ChangeOrderDTO.class);
        changeOrderDTO.setWritable(changeManager.isChangeItemWritable(changeOrder));
        return changeOrderDTO;
    }

    @PUT
    @ApiOperation(value = "Update ACL of the order",
            response = ChangeOrderDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of updated ChangeOrderDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("{orderId}/acl")
    @Consumes(MediaType.APPLICATION_JSON)
    public ChangeOrderDTO updateChangeOrderACL(
            @ApiParam(required = true, value = "Workspace id") @PathParam("workspaceId") String pWorkspaceId,
            @ApiParam(required = true, value = "Order id") @PathParam("orderId") int orderId,
            @ApiParam(required = true, value = "ACL rules to set") ACLDTO acl)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        ChangeItem changeOrder;
        if (!acl.getGroupEntries().isEmpty() || !acl.getUserEntries().isEmpty()) {

            Map<String, String> userEntries = new HashMap<>();
            Map<String, String> groupEntries = new HashMap<>();

            for (ACLEntryDTO entry : acl.getUserEntries()) {
                userEntries.put(entry.getKey(), entry.getValue().name());
            }

            for (ACLEntryDTO entry : acl.getGroupEntries()) {
                groupEntries.put(entry.getKey(), entry.getValue().name());
            }

            changeOrder = changeManager.updateACLForChangeOrder(pWorkspaceId, orderId, userEntries, groupEntries);
        } else {
            changeOrder = changeManager.removeACLFromChangeOrder(pWorkspaceId, orderId);
        }

        ChangeOrderDTO changeOrderDTO = mapper.map(changeOrder, ChangeOrderDTO.class);
        changeOrderDTO.setWritable(changeManager.isChangeItemWritable(changeOrder));
        return changeOrderDTO;
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