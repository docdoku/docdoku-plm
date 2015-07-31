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

import com.docdoku.core.change.ChangeRequest;
import com.docdoku.core.document.DocumentIterationKey;
import com.docdoku.core.exceptions.AccessRightException;
import com.docdoku.core.exceptions.EntityConstraintException;
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
import com.docdoku.server.rest.dto.change.ChangeIssueDTO;
import com.docdoku.server.rest.dto.change.ChangeItemDTO;
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
public class ChangeRequestsResource {

    @EJB
    private IChangeManagerLocal changeManager;

    private Mapper mapper;

    public ChangeRequestsResource() {

    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ChangeRequestDTO> getRequests(@PathParam("workspaceId") String workspaceId)
            throws EntityNotFoundException, UserNotActiveException{
        List<ChangeRequest> changeRequests = changeManager.getChangeRequests(workspaceId);
        List<ChangeRequestDTO> changeRequestDTOs = new ArrayList<>();
        for(ChangeRequest request : changeRequests){
            ChangeRequestDTO changeRequestDTO = mapper.map(request, ChangeRequestDTO.class);
            changeRequestDTO.setWritable(changeManager.isChangeItemWritable(request));
            changeRequestDTOs.add(changeRequestDTO);
        }
        return changeRequestDTOs;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeRequestDTO createRequest(@PathParam("workspaceId") String workspaceId, ChangeRequestDTO changeRequestDTO)
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
    @Path("link")
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeRequestDTO[] searchRequestsToLink(@PathParam("workspaceId") String workspaceId,@QueryParam("q") String q)
            throws EntityNotFoundException, UserNotActiveException{
        int maxResults = 8;
        List<ChangeRequest> requests = changeManager.getRequestsWithReference(workspaceId, q, maxResults);
        List<ChangeRequestDTO> requestDTOs = new ArrayList<>();
        for(ChangeRequest request : requests){
            ChangeRequestDTO changeRequestDTO = mapper.map(request, ChangeRequestDTO.class);
            changeRequestDTO.setWritable(changeManager.isChangeItemWritable(request));
            requestDTOs.add(changeRequestDTO);
        }
        return requestDTOs.toArray(new ChangeRequestDTO[requestDTOs.size()]);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{requestId}")
    public ChangeRequestDTO getRequest(@PathParam("workspaceId") String workspaceId, @PathParam("requestId") int requestId)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException{
        ChangeRequest changeRequest = changeManager.getChangeRequest(workspaceId, requestId);
        ChangeRequestDTO changeRequestDTO = mapper.map(changeRequest, ChangeRequestDTO.class);
        changeRequestDTO.setWritable(changeManager.isChangeItemWritable(changeRequest));
        return changeRequestDTO;
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{requestId}")
    public ChangeRequestDTO updateRequest(@PathParam("workspaceId") String workspaceId, @PathParam("requestId") int requestId, ChangeRequestDTO pChangeRequestDTO)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException{
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
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{requestId}")
    public Response removeRequest(@PathParam("workspaceId") String workspaceId, @PathParam("requestId") int requestId)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, EntityConstraintException {
        changeManager.deleteChangeRequest(workspaceId,requestId);
        return Response.ok().build();
    }


    @PUT
    @Path("{requestId}/tags")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeRequestDTO saveTags(@PathParam("workspaceId") String workspaceId, @PathParam("requestId") int requestId, List<TagDTO> tagDtos)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException{
        String[] tagsLabel = new String[tagDtos.size()];
        for (int i = 0; i < tagDtos.size(); i++) {
            tagsLabel[i] = tagDtos.get(i).getLabel();
        }

        ChangeRequest changeRequest = changeManager.saveChangeRequestTags(workspaceId, requestId, tagsLabel);
        ChangeRequestDTO changeRequestDTO = mapper.map(changeRequest, ChangeRequestDTO.class);
        changeRequestDTO.setWritable(changeManager.isChangeItemWritable(changeRequest));
        return changeRequestDTO;
    }

    @POST
    @Path("{requestId}/tags")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeItemDTO addTag(@PathParam("workspaceId") String workspaceId, @PathParam("requestId") int requestId, List<TagDTO> tagDtos)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException{
        ChangeRequest changeRequest = changeManager.getChangeRequest(workspaceId, requestId);
        Set<Tag> tags = changeRequest.getTags();
        Set<String> tagLabels = new HashSet<>();

        for(TagDTO tagDto:tagDtos){
            tagLabels.add(tagDto.getLabel());
        }

        for(Tag tag : tags){
            tagLabels.add(tag.getLabel());
        }

        changeRequest = changeManager.saveChangeRequestTags(workspaceId, requestId, tagLabels.toArray(new String[tagLabels.size()]));
        ChangeRequestDTO changeRequestDTO = mapper.map(changeRequest, ChangeRequestDTO.class);
        changeRequestDTO.setWritable(changeManager.isChangeItemWritable(changeRequest));
        return changeRequestDTO;

    }

    @DELETE
    @Path("{requestId}/tags/{tagName}")
    public Response removeTags(@PathParam("workspaceId") String workspaceId, @PathParam("requestId")int requestId, @PathParam("tagName") String tagName)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        changeManager.removeChangeRequestTag(workspaceId, requestId, tagName);
        return Response.ok().build();
    }

    @PUT
    @Path("{requestId}/affected-documents")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeItemDTO saveAffectedDocuments(@PathParam("workspaceId") String workspaceId, @PathParam("requestId") int requestId, List<DocumentIterationDTO> documentLinkDtos)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException{
        DocumentIterationKey[] links = null;
        if (documentLinkDtos != null) {
            links = createDocumentIterationKeys(documentLinkDtos);
        }

        ChangeRequest changeRequest = changeManager.saveChangeRequestAffectedDocuments(workspaceId, requestId, links);
        ChangeRequestDTO changeRequestDTO = mapper.map(changeRequest, ChangeRequestDTO.class);
        changeRequestDTO.setWritable(changeManager.isChangeItemWritable(changeRequest));
        return changeRequestDTO;
    }

    @PUT
    @Path("{requestId}/affected-parts")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeItemDTO saveAffectedParts(@PathParam("workspaceId") String workspaceId, @PathParam("requestId") int requestId, List<PartIterationDTO> partLinkDtos)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException{
        PartIterationKey[] links = null;
        if (partLinkDtos != null) {
            links = createPartIterationKeys(partLinkDtos);
        }

        ChangeRequest changeRequest = changeManager.saveChangeRequestAffectedParts(workspaceId, requestId, links);
        ChangeRequestDTO changeRequestDTO = mapper.map(changeRequest, ChangeRequestDTO.class);
        changeRequestDTO.setWritable(changeManager.isChangeItemWritable(changeRequest));
        return changeRequestDTO;
    }

    @PUT
    @Path("{requestId}/affected-issues")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeItemDTO saveAffectedIssues(@PathParam("workspaceId") String workspaceId, @PathParam("requestId") int requestId, List<ChangeIssueDTO> changeIssueDTOs)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException{
        int[] links;
        if (changeIssueDTOs != null) {
            int i = 0;
            links = new int[changeIssueDTOs.size()];
            for(ChangeIssueDTO changeIssueDTO : changeIssueDTOs){
                links[i++] = changeIssueDTO.getId();
            }
        }else{
            links = new int[0];
        }

        ChangeRequest changeRequest = changeManager.saveChangeRequestAffectedIssues(workspaceId, requestId, links);
        ChangeRequestDTO changeRequestDTO = mapper.map(changeRequest, ChangeRequestDTO.class);
        changeRequestDTO.setWritable(changeManager.isChangeItemWritable(changeRequest));
        return changeRequestDTO;
    }

    @PUT
    @Path("{requestId}/acl")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateACL(@PathParam("workspaceId") String pWorkspaceId, @PathParam("requestId") int requestId, ACLDTO acl)
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

            changeManager.updateACLForChangeRequest(pWorkspaceId, requestId, userEntries, groupEntries);
        }else{
            changeManager.removeACLFromChangeRequest(pWorkspaceId, requestId);
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