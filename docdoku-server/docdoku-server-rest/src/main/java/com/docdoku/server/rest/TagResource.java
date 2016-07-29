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

import com.docdoku.core.common.Account;
import com.docdoku.core.common.User;
import com.docdoku.core.common.UserGroup;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.meta.TagKey;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.ACLUserEntry;
import com.docdoku.core.security.ACLUserGroupEntry;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.server.rest.dto.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Yassine Belouad
 */
@RequestScoped
@Api(hidden = true, value = "tags", description = "Operations about tags")
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class TagResource {

    private final static Logger LOGGER = Logger.getLogger(TagResource.class.getName());
    @Inject
    private IDocumentManagerLocal documentService;
    private Mapper mapper;

    public TagResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @ApiOperation(value = "Get tags in workspace", response = TagDTO.class, responseContainer = "List")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTagsInWorkspace(@PathParam("workspaceId") String workspaceId)
            throws EntityNotFoundException, UserNotActiveException {

        String[] tagsName = documentService.getTags(workspaceId);
        List<TagDTO> tagsDTO = new ArrayList<>();
        for (String tagName : tagsName) {
            tagsDTO.add(new TagDTO(tagName, workspaceId));
        }
        return Response.ok(new GenericEntity<List<TagDTO>>((List<TagDTO>) tagsDTO) {
        }).build();
    }

    @POST
    @ApiOperation(value = "Create tag in workspace", response = TagDTO.class)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TagDTO createTag(@PathParam("workspaceId") String workspaceId,
                            @ApiParam(value = "Tag to create", required = true) TagDTO tag)
            throws EntityNotFoundException, EntityAlreadyExistsException, UserNotActiveException, AccessRightException, CreationException {

        documentService.createTag(workspaceId, tag.getLabel());
        return new TagDTO(tag.getLabel());
    }

    @POST
    @ApiOperation(value = "Create tags in workspace", response = Response.class)
    @Path("/multiple")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createTags(@PathParam("workspaceId") String workspaceId,
                               @ApiParam(value = "Tag list to create", required = true) TagListDTO tagList)
            throws EntityNotFoundException, EntityAlreadyExistsException, UserNotActiveException, AccessRightException, CreationException {

        for (TagDTO tagDTO : tagList.getTags()) {
            documentService.createTag(workspaceId, tagDTO.getLabel());
        }
        return Response.ok().build();
    }

    @DELETE
    @ApiOperation(value = "Delete tag in workspace", response = Response.class)
    @Path("{tagId}")
    public Response deleteTag(@PathParam("workspaceId") String workspaceId,
                              @PathParam("tagId") String tagId)
            throws EntityNotFoundException, AccessRightException {

        documentService.deleteTag(new TagKey(workspaceId, tagId));
        return Response.ok().build();
    }

    @GET
    @ApiOperation(value = "Get documents from given tag id", response = DocumentRevisionDTO.class, responseContainer = "List")
    @Path("{tagId}/documents/")
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO[] getDocumentsWithGivenTagIdAndWorkspaceId(
            @PathParam("workspaceId") String workspaceId,
            @PathParam("tagId") String tagId)
            throws EntityNotFoundException, UserNotActiveException {

        TagKey tagKey = new TagKey(workspaceId, tagId);
        DocumentRevision[] docRs = documentService.findDocumentRevisionsByTag(tagKey);
        DocumentRevisionDTO[] docRsDTOs = new DocumentRevisionDTO[docRs.length];

        for (int i = 0; i < docRs.length; i++) {
            docRsDTOs[i] = mapper.map(docRs[i], DocumentRevisionDTO.class);
            docRsDTOs[i].setPath(docRs[i].getLocation().getCompletePath());
            docRsDTOs[i] = Tools.createLightDocumentRevisionDTO(docRsDTOs[i]);
            docRsDTOs[i].setLifeCycleState(docRs[i].getLifeCycleState());
            docRsDTOs[i].setIterationSubscription(documentService.isUserIterationChangeEventSubscribedForGivenDocument(workspaceId, docRs[i]));
            docRsDTOs[i].setStateSubscription(documentService.isUserStateChangeEventSubscribedForGivenDocument(workspaceId, docRs[i]));
        }

        return docRsDTOs;
    }

    @POST
    @Path("{tagId}/documents/")
    @ApiOperation(value = "Create document", response = Response.class)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createDocumentMasterInRootFolderWithTag(
            @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Document to create") DocumentCreationDTO docCreationDTO,
            @PathParam("tagId") String tagId,
            @QueryParam("configSpec") String configSpecType)
            throws CreationException, FileAlreadyExistsException, DocumentRevisionAlreadyExistsException, WorkspaceNotFoundException, UserNotFoundException, NotAllowedException, DocumentMasterAlreadyExistsException, RoleNotFoundException, FolderNotFoundException, WorkflowModelNotFoundException, AccessRightException, DocumentMasterTemplateNotFoundException, DocumentRevisionNotFoundException, UserNotActiveException, ESServerException, UserGroupNotFoundException {

        String pDocMID = docCreationDTO.getReference();
        String pTitle = docCreationDTO.getTitle();
        String pDescription = docCreationDTO.getDescription();

        String decodedCompletePath = getPathFromUrlParams(workspaceId, workspaceId);

        String pWorkflowModelId = docCreationDTO.getWorkflowModelId();
        RoleMappingDTO[] roleMappingDTOs = docCreationDTO.getRoleMapping();
        String pDocMTemplateId = docCreationDTO.getTemplateId();

        ACLDTO acl = docCreationDTO.getAcl();

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

        Map<String, Collection<String>> userRoleMapping = new HashMap<>();
        Map<String, Collection<String>> groupRoleMapping = new HashMap<>();

        if (roleMappingDTOs != null) {
            for (RoleMappingDTO roleMappingDTO : roleMappingDTOs) {
                userRoleMapping.put(roleMappingDTO.getRoleName(), roleMappingDTO.getUserLogins());
                groupRoleMapping.put(roleMappingDTO.getRoleName(), roleMappingDTO.getGroupIds());
            }
        }


        DocumentRevision createdDocRs = documentService.createDocumentMaster(decodedCompletePath, pDocMID, pTitle, pDescription, pDocMTemplateId, pWorkflowModelId, userEntries, userGroupEntries, userRoleMapping, groupRoleMapping);
        documentService.saveTags(createdDocRs.getKey(), new String[]{tagId});

        DocumentRevisionDTO docRsDTO = mapper.map(createdDocRs, DocumentRevisionDTO.class);
        docRsDTO.setPath(createdDocRs.getLocation().getCompletePath());
        docRsDTO.setLifeCycleState(createdDocRs.getLifeCycleState());

        try {
            return Response.created(URI.create(URLEncoder.encode(pDocMID + "-" + createdDocRs.getVersion(), "UTF-8"))).entity(docRsDTO).build();
        } catch (UnsupportedEncodingException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            return Response.ok().build();
        }
    }

    private String getPathFromUrlParams(String workspaceId, String folderId) {
        return folderId == null ? Tools.stripTrailingSlash(workspaceId) : Tools.stripTrailingSlash(FolderDTO.replaceColonWithSlash(folderId));
    }

}