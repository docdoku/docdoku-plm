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

import com.docdoku.core.change.ChangeOrder;
import com.docdoku.core.document.DocumentIterationKey;
import com.docdoku.core.exceptions.AccessRightException;
import com.docdoku.core.exceptions.EntityNotFoundException;
import com.docdoku.core.exceptions.UserNotActiveException;
import com.docdoku.core.meta.Tag;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IChangeManagerLocal;
import com.docdoku.server.rest.dto.ACLDTO;
import com.docdoku.server.rest.dto.DocumentIterationDTO;
import com.docdoku.server.rest.dto.PartIterationDTO;
import com.docdoku.server.rest.dto.TagDTO;
import com.docdoku.server.rest.dto.change.ChangeItemDTO;
import com.docdoku.server.rest.dto.change.ChangeOrderDTO;
import com.docdoku.server.rest.dto.change.ChangeRequestDTO;
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
public class ChangeOrdersResource {

    @EJB
    private IChangeManagerLocal changeManager;

    private Mapper mapper;

    public ChangeOrdersResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ChangeOrderDTO> getOrders(@PathParam("workspaceId") String workspaceId)
            throws EntityNotFoundException, UserNotActiveException{
        List<ChangeOrder> changeOrders = changeManager.getChangeOrders(workspaceId);
        List<ChangeOrderDTO> changeOrderDTOs = new ArrayList<>();
        for(ChangeOrder order : changeOrders){
            ChangeOrderDTO changeOrderDTO= mapper.map(order, ChangeOrderDTO.class);
            changeOrderDTO.setWritable(changeManager.isChangeItemWritable(order));
            changeOrderDTOs.add(changeOrderDTO);
        }
        return changeOrderDTOs;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeOrderDTO createOrder(@PathParam("workspaceId") String workspaceId, ChangeOrderDTO changeOrderDTO)
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
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{orderId}")
    public ChangeOrderDTO getOrder(@PathParam("workspaceId") String workspaceId, @PathParam("orderId") int orderId)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException{
        ChangeOrder changeOrder = changeManager.getChangeOrder(workspaceId, orderId);
        ChangeOrderDTO changeOrderDTO= mapper.map(changeOrder, ChangeOrderDTO.class);
        changeOrderDTO.setWritable(changeManager.isChangeItemWritable(changeOrder));
        return changeOrderDTO;
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{orderId}")
    public ChangeOrderDTO updateOrder(@PathParam("workspaceId") String workspaceId, @PathParam("orderId") int orderId, ChangeOrderDTO pChangeOrderDTO)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException{
        ChangeOrder changeOrder = changeManager.updateChangeOrder(orderId,
                                                                workspaceId,
                                                                pChangeOrderDTO.getDescription(),
                                                                pChangeOrderDTO.getMilestoneId(),
                                                                pChangeOrderDTO.getPriority(),
                                                                pChangeOrderDTO.getAssignee(),
                                                                pChangeOrderDTO.getCategory());
        ChangeOrderDTO changeOrderDTO= mapper.map(changeOrder, ChangeOrderDTO.class);
        changeOrderDTO.setWritable(changeManager.isChangeItemWritable(changeOrder));
        return changeOrderDTO;
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{orderId}")
    public Response removeOrder(@PathParam("orderId") int orderId)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException{
        changeManager.deleteChangeOrder(orderId);
        return Response.ok().build();
    }


    @PUT
    @Path("{orderId}/tags")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeOrderDTO saveTags(@PathParam("workspaceId") String workspaceId, @PathParam("orderId") int orderId, List<TagDTO> tagDtos)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException{
        String[] tagsLabel = new String[tagDtos.size()];
        for (int i = 0; i < tagDtos.size(); i++) {
            tagsLabel[i] = tagDtos.get(i).getLabel();
        }

        ChangeOrder changeOrder = changeManager.saveChangeOrderTags(workspaceId, orderId, tagsLabel);
        ChangeOrderDTO changeOrderDTO= mapper.map(changeOrder, ChangeOrderDTO.class);
        changeOrderDTO.setWritable(changeManager.isChangeItemWritable(changeOrder));
        return changeOrderDTO;
    }

    @POST
    @Path("{orderId}/tags")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeItemDTO addTag(@PathParam("workspaceId") String workspaceId, @PathParam("orderId") int orderId, List<TagDTO> tagDtos)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException{
        ChangeOrder changeOrder = changeManager.getChangeOrder(workspaceId, orderId);
        Set<Tag> tags = changeOrder.getTags();
        Set<String> tagLabels = new HashSet<>();

        for(TagDTO tagDto:tagDtos){
            tagLabels.add(tagDto.getLabel());
        }

        for(Tag tag : tags){
            tagLabels.add(tag.getLabel());
        }

        changeOrder = changeManager.saveChangeOrderTags(workspaceId, orderId, tagLabels.toArray(new String[tagLabels.size()]));
        ChangeOrderDTO changeOrderDTO= mapper.map(changeOrder, ChangeOrderDTO.class);
        changeOrderDTO.setWritable(changeManager.isChangeItemWritable(changeOrder));
        return changeOrderDTO;

    }

    @DELETE
    @Path("{orderId}/tags/{tagName}")
    public Response removeTags(@PathParam("workspaceId") String workspaceId, @PathParam("orderId")int orderId, @PathParam("tagName") String tagName)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        changeManager.removeChangeOrderTag(workspaceId, orderId, tagName);
        return Response.ok().build();
    }

    @PUT
    @Path("{orderId}/affected-documents")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeItemDTO saveAffectedDocuments(@PathParam("workspaceId") String workspaceId, @PathParam("orderId") int orderId, List<DocumentIterationDTO> documentLinkDtos)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException{
        DocumentIterationKey[] links = null;
        if (documentLinkDtos != null) {
            links = createDocumentIterationKeys(documentLinkDtos);
        }

        ChangeOrder changeOrder = changeManager.saveChangeOrderAffectedDocuments(workspaceId, orderId, links);
        ChangeOrderDTO changeOrderDTO= mapper.map(changeOrder, ChangeOrderDTO.class);
        changeOrderDTO.setWritable(changeManager.isChangeItemWritable(changeOrder));
        return changeOrderDTO;
    }

    @PUT
    @Path("{orderId}/affected-parts")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeItemDTO saveAffectedParts(@PathParam("workspaceId") String workspaceId, @PathParam("orderId") int orderId, List<PartIterationDTO> partLinkDtos)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException{
        PartIterationKey[] links = null;
        if (partLinkDtos != null) {
            links = createPartIterationKeys(partLinkDtos);
        }

        ChangeOrder changeOrder = changeManager.saveChangeOrderAffectedParts(workspaceId, orderId, links);
        ChangeOrderDTO changeOrderDTO= mapper.map(changeOrder, ChangeOrderDTO.class);
        changeOrderDTO.setWritable(changeManager.isChangeItemWritable(changeOrder));
        return changeOrderDTO;
    }

    @PUT
    @Path("{orderId}/affected-requests")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeItemDTO saveAffectedRequests(@PathParam("workspaceId") String workspaceId, @PathParam("orderId") int orderId, List<ChangeRequestDTO> changeRequestDTOs)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException{
        int[] links;
        if (changeRequestDTOs != null) {
            int i = 0;
            links = new int[changeRequestDTOs.size()];
            for(ChangeRequestDTO changeRequestDTO : changeRequestDTOs){
                links[i++] = changeRequestDTO.getId();
            }
        }else{
            links = new int[0];
        }

        ChangeOrder changeOrder = changeManager.saveChangeOrderAffectedRequests(workspaceId, orderId, links);
        ChangeOrderDTO changeOrderDTO= mapper.map(changeOrder, ChangeOrderDTO.class);
        changeOrderDTO.setWritable(changeManager.isChangeItemWritable(changeOrder));
        return changeOrderDTO;
    }

    @PUT
    @Path("{orderId}/acl")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateACL(@PathParam("workspaceId") String pWorkspaceId, @PathParam("orderId") int orderId, ACLDTO acl)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException{
        if (!acl.getGroupEntries().isEmpty() || !acl.getUserEntries().isEmpty()) {

            Map<String,String> userEntries = new HashMap<>();
            Map<String,String> groupEntries = new HashMap<>();

            for (Map.Entry<String, ACL.Permission> entry : acl.getUserEntries().entrySet()) {
                userEntries.put(entry.getKey(), entry.getValue().name());
            }

            for (Map.Entry<String, ACL.Permission> entry : acl.getGroupEntries().entrySet()) {
                groupEntries.put(entry.getKey(), entry.getValue().name());
            }

            changeManager.updateACLForChangeOrder(pWorkspaceId, orderId, userEntries, groupEntries);
        }else{
            changeManager.removeACLFromChangeOrder(pWorkspaceId, orderId);
        }
        return Response.ok().build();
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