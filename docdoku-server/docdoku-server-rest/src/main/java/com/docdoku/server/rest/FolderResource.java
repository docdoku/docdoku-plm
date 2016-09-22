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
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.document.Folder;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.exceptions.NotAllowedException;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@RequestScoped
@Api(hidden = true, value = "folders", description = "Operations about folders")
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class FolderResource {

    private static final Logger LOGGER = Logger.getLogger(FolderResource.class.getName());
    @Inject
    private IDocumentManagerLocal documentService;
    private Mapper mapper;

    public FolderResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @Path("{folderId}/documents/")
    @ApiOperation(value = "Get documents in folder", response = DocumentRevisionDTO.class, responseContainer = "List")
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentRevisionDTO[] getDocumentsWithGivenFolderIdAndWorkspaceId(
            @PathParam("workspaceId") String workspaceId,
            @PathParam("folderId") String folderId)
            throws EntityNotFoundException, UserNotActiveException {

        String decodedCompletePath = getPathFromUrlParams(workspaceId, folderId);
        DocumentRevision[] docRs = documentService.findDocumentRevisionsByFolder(decodedCompletePath);
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
    @Path("{folderId}/documents/")
    @ApiOperation(value = "Create document", response = DocumentRevisionDTO.class)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createDocumentMasterInFolder(
            @PathParam("workspaceId") String workspaceId,
            @ApiParam(required = true, value = "Document to create") DocumentCreationDTO docCreationDTO,
            @PathParam("folderId") String folderId)
            throws EntityNotFoundException, EntityAlreadyExistsException, NotAllowedException, CreationException, AccessRightException {

        String pDocMID = docCreationDTO.getReference();
        String pTitle = docCreationDTO.getTitle();
        String pDescription = docCreationDTO.getDescription();

        String decodedCompletePath = getPathFromUrlParams(workspaceId, folderId);

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

    /**
     * Retrieves representation of folders located at the root of the given workspace
     *
     * @param workspaceId The current workspace id
     * @return The array of folders
     */
    @GET
    @ApiOperation(value = "Get root folders", response = FolderDTO.class, responseContainer = "List")
    @Produces(MediaType.APPLICATION_JSON)
    public FolderDTO[] getRootFolders(@PathParam("workspaceId") String workspaceId,
                                      @QueryParam("configSpec") String configSpecType)
            throws EntityNotFoundException, UserNotActiveException {
        String completePath = Tools.stripTrailingSlash(workspaceId);
        return getFolders(workspaceId, completePath, true, configSpecType);
    }

    @GET
    @ApiOperation(value = "Get sub folders", response = FolderDTO.class, responseContainer = "List")
    @Path("{completePath}/folders")
    @Produces(MediaType.APPLICATION_JSON)
    public FolderDTO[] getSubFolders(@PathParam("workspaceId") String workspaceId,
                                     @PathParam("completePath") String folderId,
                                     @QueryParam("configSpec") String configSpecType)
            throws EntityNotFoundException, UserNotActiveException {
        String decodedCompletePath = FolderDTO.replaceColonWithSlash(folderId);
        String completePath = Tools.stripTrailingSlash(decodedCompletePath);
        return getFolders(workspaceId, completePath, false, configSpecType);
    }

    private FolderDTO[] getFolders(String workspaceId, String completePath, boolean rootFolder, String configSpecType)
            throws EntityNotFoundException, UserNotActiveException {
        String[] folderNames = documentService.getFolders(completePath);
        FolderDTO[] folderDTOs = new FolderDTO[folderNames.length];

        for (int i = 0; i < folderNames.length; i++) {
            String completeFolderPath;
            if (rootFolder) {
                completeFolderPath = workspaceId + "/" + folderNames[i];
            } else {
                completeFolderPath = completePath + "/" + folderNames[i];
            }

            String encodedFolderId = FolderDTO.replaceSlashWithColon(completeFolderPath);

            folderDTOs[i] = new FolderDTO();
            folderDTOs[i].setPath(completePath);
            folderDTOs[i].setName(folderNames[i]);
            folderDTOs[i].setId(encodedFolderId);

        }

        return folderDTOs;
    }

    /**
     * PUT method for updating or creating an instance of FolderResource
     */
    @PUT
    @ApiOperation(value = "Rename a folder", response = FolderDTO.class)
    @Path("{folderId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public FolderDTO renameFolder(@PathParam("workspaceId") String workspaceId,
                                  @PathParam("folderId") String folderPath,
                                  @ApiParam(value = "Folder with new name", required = true) FolderDTO folderDTO)
            throws EntityNotFoundException, EntityAlreadyExistsException, NotAllowedException, AccessRightException, CreationException {

        String decodedCompletePath = FolderDTO.replaceColonWithSlash(folderPath);
        String completePath = Tools.stripTrailingSlash(decodedCompletePath);
        String destParentFolder = FolderDTO.extractParentFolder(completePath);
        String folderName = folderDTO.getName();

        documentService.moveFolder(completePath, destParentFolder, folderName);

        String completeRenamedFolderId = destParentFolder + '/' + folderName;
        String encodedRenamedFolderId = FolderDTO.replaceSlashWithColon(completeRenamedFolderId);

        FolderDTO renamedFolderDTO = new FolderDTO();
        renamedFolderDTO.setPath(destParentFolder);
        renamedFolderDTO.setName(folderName);
        renamedFolderDTO.setId(encodedRenamedFolderId);

        return renamedFolderDTO;
    }

    /**
     * PUT method for moving folder into an other
     */
    @PUT
    @ApiOperation(value = "Move a folder", response = FolderDTO.class)
    @Path("{folderId}/move")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public FolderDTO moveFolder(@PathParam("workspaceId") String workspaceId,
                                @PathParam("folderId") String folderPath,
                                @ApiParam(required = true, value = "Folder to move") FolderDTO folderDTO)
            throws EntityNotFoundException, EntityAlreadyExistsException, NotAllowedException, AccessRightException, CreationException {

        String decodedCompletePath = FolderDTO.replaceColonWithSlash(folderPath);
        String completePath = Tools.stripTrailingSlash(decodedCompletePath);

        String destParentFolder = FolderDTO.replaceColonWithSlash(folderDTO.getId());
        String folderName = Tools.stripLeadingSlash(FolderDTO.extractName(completePath));

        documentService.moveFolder(completePath, destParentFolder, folderName);

        String completeRenamedFolderId = destParentFolder + '/' + folderName;
        String encodedRenamedFolderId = FolderDTO.replaceSlashWithColon(completeRenamedFolderId);

        FolderDTO renamedFolderDTO = new FolderDTO();
        renamedFolderDTO.setPath(destParentFolder);
        renamedFolderDTO.setName(folderName);
        renamedFolderDTO.setId(encodedRenamedFolderId);

        return renamedFolderDTO;
    }

    @POST
    @ApiOperation(value = "Create a sub folder", response = FolderDTO.class)
    @Path("{parentFolderPath}/folders")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public FolderDTO createSubFolder(@PathParam("workspaceId") String workspaceId,
                                     @PathParam("parentFolderPath") String parentFolderPath,
                                     @ApiParam(value = "Folder to create", required = true) FolderDTO folder)
            throws EntityNotFoundException, EntityAlreadyExistsException, NotAllowedException, AccessRightException, UserNotActiveException, CreationException {

        String decodedCompletePath = FolderDTO.replaceColonWithSlash(parentFolderPath);

        String folderName = folder.getName();
        return createFolder(decodedCompletePath, folderName);
    }

    @POST
    @ApiOperation(value = "Create root folder", response = FolderDTO.class)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public FolderDTO createRootFolder(@PathParam("workspaceId") String workspaceId,
                                      @ApiParam(required = true, value = "Folder to create") FolderDTO folder)
            throws EntityNotFoundException, EntityAlreadyExistsException, NotAllowedException, AccessRightException, UserNotActiveException, CreationException {

        String folderName = folder.getName();
        return createFolder(workspaceId, folderName);
    }

    /**
     * DELETE method for deleting an instance of FolderResource
     *
     * @param completePath the folder path
     * @return the array of the documents that have also been deleted
     */
    @DELETE
    @ApiOperation(value = "Delete root folder", response = Response.class)
    @Path("{folderId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteRootFolder(@PathParam("workspaceId") String workspaceId,
                                     @PathParam("folderId") String completePath)
            throws EntityNotFoundException, NotAllowedException, AccessRightException, UserNotActiveException, ESServerException, EntityConstraintException {

        deleteFolder(completePath);
        return Response.status(Response.Status.OK).build();
    }

    private DocumentRevisionKey[] deleteFolder(String pCompletePath)
            throws EntityNotFoundException, ESServerException, AccessRightException, NotAllowedException, EntityConstraintException, UserNotActiveException {

        String decodedCompletePath = FolderDTO.replaceColonWithSlash(pCompletePath);
        String completePath = Tools.stripTrailingSlash(decodedCompletePath);
        return documentService.deleteFolder(completePath);
    }

    private FolderDTO createFolder(String pCompletePath, String pFolderName)
            throws EntityNotFoundException, EntityAlreadyExistsException, CreationException, AccessRightException, NotAllowedException {
        Folder createdFolder = documentService.createFolder(pCompletePath, pFolderName);

        String completeCreatedFolderPath = createdFolder.getCompletePath() + '/' + createdFolder.getShortName();
        String encodedFolderId = FolderDTO.replaceSlashWithColon(completeCreatedFolderPath);

        FolderDTO createdFolderDTOs = new FolderDTO();
        createdFolderDTOs.setPath(createdFolder.getCompletePath());
        createdFolderDTOs.setName(createdFolder.getShortName());
        createdFolderDTOs.setId(encodedFolderId);

        return createdFolderDTOs;
    }

}