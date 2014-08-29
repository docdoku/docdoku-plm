/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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
import com.docdoku.core.exceptions.ApplicationException;
import com.docdoku.core.meta.Tag;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IChangeManagerLocal;
import com.docdoku.server.rest.dto.*;
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
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class ChangeIssuesResource {

    @EJB
    private IChangeManagerLocal changeManager;

    private final static Logger LOGGER = Logger.getLogger(ChangeIssuesResource.class.getName());
    private Mapper mapper;

    public ChangeIssuesResource() {

    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ChangeIssueDTO> getIssues(@PathParam("workspaceId") String workspaceId){
        try{
            List<ChangeIssue> changeIssues = changeManager.getChangeIssues(workspaceId);
            List<ChangeIssueDTO> changeIssueDTOs = new ArrayList<>();
            for(ChangeIssue issue : changeIssues){
                ChangeIssueDTO changeIssueDTO= mapper.map(issue, ChangeIssueDTO.class);
                changeIssueDTO.setWritable(changeManager.isChangeItemWritable(issue));
                changeIssueDTOs.add(changeIssueDTO);
            }
            return changeIssueDTOs;
        } catch (ApplicationException ex) {
            LOGGER.log(Level.WARNING,null,ex);
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeItemDTO createIssue(@PathParam("workspaceId") String workspaceId, ChangeIssueDTO changeIssueDTO){
        try{
            ChangeIssue changeIssue = changeManager.createChangeIssue(workspaceId, changeIssueDTO.getName(), changeIssueDTO.getDescription(), changeIssueDTO.getInitiator(), changeIssueDTO.getPriority(), changeIssueDTO.getAssignee(), changeIssueDTO.getCategory());
            return mapper.map(changeIssue, ChangeIssueDTO.class);
        } catch (ApplicationException ex) {
            LOGGER.log(Level.WARNING,null,ex);
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @GET
    @Path("link")
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeIssueDTO[] searchIssuesToLink(@PathParam("workspaceId") String workspaceId,@QueryParam("q") String q) {
        try {
            int maxResults = 8;
            List<ChangeIssue> issues = changeManager.getIssuesWithReference(workspaceId, q, maxResults);
            List<ChangeIssueDTO> issueDTOs = new ArrayList<>();
            for(ChangeIssue issue : issues){
                ChangeIssueDTO changeIssueDTO= mapper.map(issue, ChangeIssueDTO.class);
                changeIssueDTO.setWritable(changeManager.isChangeItemWritable(issue));
                issueDTOs.add(changeIssueDTO);
            }
            return issueDTOs.toArray(new ChangeIssueDTO[issueDTOs.size()]);
        } catch (ApplicationException ex) {
            LOGGER.log(Level.WARNING,null,ex);
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{issueId}")
    public ChangeItemDTO getIssue(@PathParam("workspaceId") String workspaceId, @PathParam("issueId") int issueId){
        try{
            ChangeIssue changeIssue = changeManager.getChangeIssue(workspaceId, issueId);
            ChangeIssueDTO changeIssueDTO = mapper.map(changeIssue, ChangeIssueDTO.class);
            changeIssueDTO.setWritable(changeManager.isChangeItemWritable(changeIssue));
            return changeIssueDTO;
        } catch (ApplicationException ex) {
            LOGGER.log(Level.WARNING,null,ex);
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{issueId}")
    public ChangeItemDTO updateIssue(@PathParam("workspaceId") String workspaceId, @PathParam("issueId") int issueId, ChangeIssueDTO pChangeIssueDTO){
        try{
            ChangeIssue changeIssue = changeManager.updateChangeIssue(issueId, workspaceId, pChangeIssueDTO.getDescription(), pChangeIssueDTO.getPriority(), pChangeIssueDTO.getAssignee(), pChangeIssueDTO.getCategory());
            ChangeIssueDTO changeIssueDTO = mapper.map(changeIssue, ChangeIssueDTO.class);
            changeIssueDTO.setWritable(changeManager.isChangeItemWritable(changeIssue));
            return changeIssueDTO;
        } catch (ApplicationException ex) {
            LOGGER.log(Level.WARNING,null,ex);
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{issueId}")
    public Response removeIssue(@PathParam("issueId") int issueId){
        try{
            changeManager.deleteChangeIssue(issueId);
            return Response.ok().build();
        } catch (ApplicationException ex) {
            LOGGER.log(Level.WARNING,null,ex);
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @PUT
    @Path("{issueId}/tags")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeItemDTO saveTags(@PathParam("workspaceId") String workspaceId, @PathParam("issueId") int issueId, List<TagDTO> tagDtos) {
        String[] tagsLabel = new String[tagDtos.size()];
        for (int i = 0; i < tagDtos.size(); i++) {
            tagsLabel[i] = tagDtos.get(i).getLabel();
        }

        try {
            ChangeIssue changeIssue = changeManager.saveChangeIssueTags(workspaceId, issueId, tagsLabel);
            ChangeIssueDTO changeIssueDTO = mapper.map(changeIssue, ChangeIssueDTO.class);
            changeIssueDTO.setWritable(changeManager.isChangeItemWritable(changeIssue));
            return changeIssueDTO;
        } catch (ApplicationException ex) {
            LOGGER.log(Level.WARNING,null,ex);
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @POST
    @Path("{issueId}/tags")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeItemDTO addTag(@PathParam("workspaceId") String workspaceId, @PathParam("issueId") int issueId, List<TagDTO> tagDtos) {
        try {
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

        } catch (ApplicationException ex) {
            LOGGER.log(Level.WARNING,null,ex);
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @DELETE
    @Path("{issueId}/tags/{tagName}")
    public Response removeTags(@PathParam("workspaceId") String workspaceId, @PathParam("issueId")int issueId, @PathParam("tagName") String tagName) {
        try {
            changeManager.removeChangeIssueTag(workspaceId, issueId, tagName);
            return Response.ok().build();
        } catch (ApplicationException ex) {
            LOGGER.log(Level.WARNING,null,ex);
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @PUT
    @Path("{issueId}/affectedDocuments")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeItemDTO saveAffectedDocuments(@PathParam("workspaceId") String workspaceId, @PathParam("issueId") int issueId, List<DocumentIterationDTO> documentLinkDtos) {
        DocumentIterationKey[] links = null;
        if (documentLinkDtos != null) {
            links = createDocumentIterationKeys(documentLinkDtos);
        }

        try {
            ChangeIssue changeIssue = changeManager.saveChangeIssueAffectedDocuments(workspaceId, issueId, links);
            ChangeIssueDTO changeIssueDTO = mapper.map(changeIssue, ChangeIssueDTO.class);
            changeIssueDTO.setWritable(changeManager.isChangeItemWritable(changeIssue));
            return changeIssueDTO;
        } catch (ApplicationException ex) {
            LOGGER.log(Level.WARNING,null,ex);
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @PUT
    @Path("{issueId}/affectedParts")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeItemDTO saveAffectedParts(@PathParam("workspaceId") String workspaceId, @PathParam("issueId") int issueId, List<PartIterationDTO> partLinkDtos) {
        PartIterationKey[] links = null;
        if (partLinkDtos != null) {
            links = createPartIterationKeys(partLinkDtos);
        }
        try {
            ChangeIssue changeIssue = changeManager.saveChangeIssueAffectedParts(workspaceId, issueId, links);
            ChangeIssueDTO changeIssueDTO = mapper.map(changeIssue, ChangeIssueDTO.class);
            changeIssueDTO.setWritable(changeManager.isChangeItemWritable(changeIssue));
            return changeIssueDTO;
        } catch (ApplicationException ex) {
            LOGGER.log(Level.WARNING,null,ex);
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @PUT
    @Path("{issueId}/acl")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateACL(@PathParam("workspaceId") String pWorkspaceId, @PathParam("issueId") int issueId, ACLDTO acl) {
        try {
            if (acl.getGroupEntries().size() > 0 || acl.getUserEntries().size() > 0) {

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
        } catch (ApplicationException ex) {
            LOGGER.log(Level.WARNING,null,ex);
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }


    private DocumentIterationKey[] createDocumentIterationKeys(List<DocumentIterationDTO> dtos) {
        DocumentIterationKey[] data = new DocumentIterationKey[dtos.size()];
        int i = 0;
        for (DocumentIterationDTO dto : dtos) {
            data[i++] = new DocumentIterationKey(dto.getWorkspaceId(), dto.getDocumentMasterId(), dto.getDocumentRevisionVersion(), dto.getIteration());
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