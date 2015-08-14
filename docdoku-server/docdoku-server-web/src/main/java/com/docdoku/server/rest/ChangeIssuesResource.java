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
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IChangeManagerLocal;
import com.docdoku.server.rest.dto.ACLDTO;
import com.docdoku.server.rest.dto.DocumentIterationDTO;
import com.docdoku.server.rest.dto.PartIterationDTO;
import com.docdoku.server.rest.dto.TagDTO;
import com.docdoku.server.rest.dto.change.ChangeIssueDTO;
import com.docdoku.server.rest.dto.change.ChangeItemDTO;
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
public class ChangeIssuesResource {

    @EJB
    private IChangeManagerLocal changeManager;

    private Mapper mapper;

    public ChangeIssuesResource() {

    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ChangeIssueDTO> getIssues(@PathParam("workspaceId") String workspaceId)
            throws EntityNotFoundException, UserNotActiveException{
        List<ChangeIssue> changeIssues = changeManager.getChangeIssues(workspaceId);
        List<ChangeIssueDTO> changeIssueDTOs = new ArrayList<>();
        for(ChangeIssue issue : changeIssues){
            ChangeIssueDTO changeIssueDTO= mapper.map(issue, ChangeIssueDTO.class);
            changeIssueDTO.setWritable(changeManager.isChangeItemWritable(issue));
            changeIssueDTOs.add(changeIssueDTO);
        }
        return changeIssueDTOs;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeItemDTO createIssue(@PathParam("workspaceId") String workspaceId, ChangeIssueDTO changeIssueDTO)
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
    @Path("link")
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeIssueDTO[] searchIssuesToLink(@PathParam("workspaceId") String workspaceId,@QueryParam("q") String q)
            throws EntityNotFoundException, UserNotActiveException{
        int maxResults = 8;
        List<ChangeIssue> issues = changeManager.getIssuesWithReference(workspaceId, q, maxResults);
        List<ChangeIssueDTO> issueDTOs = new ArrayList<>();
        for(ChangeIssue issue : issues){
            ChangeIssueDTO changeIssueDTO= mapper.map(issue, ChangeIssueDTO.class);
            changeIssueDTO.setWritable(changeManager.isChangeItemWritable(issue));
            issueDTOs.add(changeIssueDTO);
        }
        return issueDTOs.toArray(new ChangeIssueDTO[issueDTOs.size()]);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{issueId}")
    public ChangeItemDTO getIssue(@PathParam("workspaceId") String workspaceId, @PathParam("issueId") int issueId)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        ChangeIssue changeIssue = changeManager.getChangeIssue(workspaceId, issueId);
        ChangeIssueDTO changeIssueDTO = mapper.map(changeIssue, ChangeIssueDTO.class);
        changeIssueDTO.setWritable(changeManager.isChangeItemWritable(changeIssue));
        return changeIssueDTO;
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{issueId}")
    public ChangeItemDTO updateIssue(@PathParam("workspaceId") String workspaceId, @PathParam("issueId") int issueId, ChangeIssueDTO pChangeIssueDTO)
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
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{issueId}")
    public Response removeIssue(@PathParam("issueId") int issueId)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException, EntityConstraintException {
        changeManager.deleteChangeIssue(issueId);
        return Response.ok().build();
    }

    @PUT
    @Path("{issueId}/tags")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeItemDTO saveTags(@PathParam("workspaceId") String workspaceId, @PathParam("issueId") int issueId, List<TagDTO> tagDtos)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        String[] tagsLabel = new String[tagDtos.size()];
        for (int i = 0; i < tagDtos.size(); i++) {
            tagsLabel[i] = tagDtos.get(i).getLabel();
        }

        ChangeIssue changeIssue = changeManager.saveChangeIssueTags(workspaceId, issueId, tagsLabel);
        ChangeIssueDTO changeIssueDTO = mapper.map(changeIssue, ChangeIssueDTO.class);
        changeIssueDTO.setWritable(changeManager.isChangeItemWritable(changeIssue));
        return changeIssueDTO;
    }

    @POST
    @Path("{issueId}/tags")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeItemDTO addTag(@PathParam("workspaceId") String workspaceId, @PathParam("issueId") int issueId, List<TagDTO> tagDtos)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        ChangeIssue changeIssue = changeManager.getChangeIssue(workspaceId,issueId);
        Set<Tag> tags = changeIssue.getTags();
        Set<String> tagLabels = new HashSet<>();

        for(TagDTO tagDto:tagDtos){
            tagLabels.add(tagDto.getLabel());
        }

        for(Tag tag : tags){
            tagLabels.add(tag.getLabel());
        }

        changeIssue = changeManager.saveChangeIssueTags(workspaceId, issueId, tagLabels.toArray(new String[tagLabels.size()]));
        ChangeIssueDTO changeIssueDTO = mapper.map(changeIssue, ChangeIssueDTO.class);
        changeIssueDTO.setWritable(changeManager.isChangeItemWritable(changeIssue));
        return changeIssueDTO;
    }

    @DELETE
    @Path("{issueId}/tags/{tagName}")
    public Response removeTags(@PathParam("workspaceId") String workspaceId, @PathParam("issueId")int issueId, @PathParam("tagName") String tagName)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        changeManager.removeChangeIssueTag(workspaceId, issueId, tagName);
        return Response.ok().build();
    }

    @PUT
    @Path("{issueId}/affected-documents")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeItemDTO saveAffectedDocuments(@PathParam("workspaceId") String workspaceId, @PathParam("issueId") int issueId, List<DocumentIterationDTO> documentLinkDtos)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        DocumentIterationKey[] links = null;
        if (documentLinkDtos != null) {
            links = createDocumentIterationKeys(documentLinkDtos);
        }

        ChangeIssue changeIssue = changeManager.saveChangeIssueAffectedDocuments(workspaceId, issueId, links);
        ChangeIssueDTO changeIssueDTO = mapper.map(changeIssue, ChangeIssueDTO.class);
        changeIssueDTO.setWritable(changeManager.isChangeItemWritable(changeIssue));
        return changeIssueDTO;
    }

    @PUT
    @Path("{issueId}/affected-parts")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeItemDTO saveAffectedParts(@PathParam("workspaceId") String workspaceId, @PathParam("issueId") int issueId, List<PartIterationDTO> partLinkDtos)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        PartIterationKey[] links = null;
        if (partLinkDtos != null) {
            links = createPartIterationKeys(partLinkDtos);
        }

        ChangeIssue changeIssue = changeManager.saveChangeIssueAffectedParts(workspaceId, issueId, links);
        ChangeIssueDTO changeIssueDTO = mapper.map(changeIssue, ChangeIssueDTO.class);
        changeIssueDTO.setWritable(changeManager.isChangeItemWritable(changeIssue));
        return changeIssueDTO;
    }

    @PUT
    @Path("{issueId}/acl")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateACL(@PathParam("workspaceId") String pWorkspaceId, @PathParam("issueId") int issueId, ACLDTO acl)
            throws EntityNotFoundException, UserNotActiveException, AccessRightException {
        if (!acl.getGroupEntries().isEmpty() || !acl.getUserEntries().isEmpty()) {

            Map<String,String> userEntries = new HashMap<>();
            Map<String,String> groupEntries = new HashMap<>();

            for (Map.Entry<String, ACL.Permission> entry : acl.getUserEntries().entrySet()) {
                userEntries.put(entry.getKey(), entry.getValue().name());
            }

            for (Map.Entry<String, ACL.Permission> entry : acl.getGroupEntries().entrySet()) {
                groupEntries.put(entry.getKey(), entry.getValue().name());
            }

            changeManager.updateACLForChangeIssue(pWorkspaceId, issueId, userEntries, groupEntries);
        }else{
            changeManager.removeACLFromChangeIssue(pWorkspaceId, issueId);
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