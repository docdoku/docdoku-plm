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

import com.docdoku.core.configuration.DocumentConfigSpec;
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.document.Folder;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IDocumentConfigSpecManagerLocal;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.server.rest.dto.FolderDTO;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Stateless
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class FolderResource {

    @EJB
    private IDocumentManagerLocal documentService;
    @EJB
    private IDocumentConfigSpecManagerLocal documentConfigSpecService;
    @EJB
    private DocumentsResource documentsResource;

    @Context
    private UriInfo context;

    public FolderResource() {
    }

    @Path("{folderId}/documents/")
    public DocumentsResource getDocumentsResource() {
        return documentsResource;
    }

    /**
     * Retrieves representation of folders located at the root of the given workspace
     *
     * @param workspaceId The current workspace id
     * @return The array of folders
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public FolderDTO[] getRootFolders(@PathParam("workspaceId") String workspaceId, @QueryParam("configSpec") String configSpecType)
            throws EntityNotFoundException, UserNotActiveException {
        String completePath = Tools.stripTrailingSlash(workspaceId);
        return getFolders(workspaceId, completePath, true, configSpecType);
    }
    
    @GET
    @Path("{completePath}/folders")
    @Produces(MediaType.APPLICATION_JSON)
    public FolderDTO[] getSubFolders(@PathParam("workspaceId") String workspaceId, @PathParam("completePath") String folderId, @QueryParam("configSpec") String configSpecType)
            throws EntityNotFoundException, UserNotActiveException {
        String decodedCompletePath = FolderDTO.replaceColonWithSlash(folderId);
        String completePath = Tools.stripTrailingSlash(decodedCompletePath);
        return getFolders(workspaceId, completePath, false, configSpecType);
    }

    private FolderDTO[] getFolders(String workspaceId, String completePath, boolean rootFolder, String configSpecType)
            throws EntityNotFoundException, UserNotActiveException {
        String[] folderNames;
        if(configSpecType==null || "latest".equals(configSpecType)){
            folderNames = documentService.getFolders(completePath);
        }else{
            DocumentConfigSpec cs = getConfigSpec(workspaceId, configSpecType);
            folderNames = documentConfigSpecService.getFilteredFolders(workspaceId,cs,completePath);
        }

        FolderDTO[] folderDtos = new FolderDTO[folderNames.length];

        for (int i = 0; i < folderNames.length; i++) {
            String completeFolderPath;
            if(rootFolder){
                completeFolderPath = workspaceId+"/"+folderNames[i];
            } else {
                completeFolderPath=completePath+"/"+folderNames[i];
            }

            String encodedFolderId=FolderDTO.replaceSlashWithColon(completeFolderPath);

            folderDtos[i] = new FolderDTO();
            folderDtos[i].setPath(completePath);
            folderDtos[i].setName(folderNames[i]);
            folderDtos[i].setId(encodedFolderId);

        }

        return folderDtos;
    }

    /**
     * PUT method for updating or creating an instance of FolderResource
     */
    @PUT
    @Path("{folderId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public FolderDTO renameFolder(@PathParam("folderId") String folderPath, FolderDTO folderDTO)
            throws EntityNotFoundException, EntityAlreadyExistsException, NotAllowedException, AccessRightException, CreationException {

        String decodedCompletePath = FolderDTO.replaceColonWithSlash(folderPath);
        String completePath = Tools.stripTrailingSlash(decodedCompletePath);
        String destParentFolder = FolderDTO.extractParentFolder(completePath);
        String folderName = folderDTO.getName();

        documentService.moveFolder(completePath, destParentFolder, folderName);

        String completeRenamedFolderId=destParentFolder+'/'+folderName;
        String encodedRenamedFolderId=FolderDTO.replaceSlashWithColon(completeRenamedFolderId);

        FolderDTO renamedFolderDto = new FolderDTO();
        renamedFolderDto.setPath(destParentFolder);
        renamedFolderDto.setName(folderName);
        renamedFolderDto.setId(encodedRenamedFolderId);

        return renamedFolderDto;
    }
    
    /**
     * PUT method for moving folder into an other
     */
    @PUT
    @Path("{folderId}/move")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public FolderDTO moveFolder(@PathParam("folderId") String folderPath, FolderDTO folderDTO)
            throws EntityNotFoundException, EntityAlreadyExistsException, NotAllowedException, AccessRightException, CreationException {

        String decodedCompletePath = FolderDTO.replaceColonWithSlash(folderPath);
        String completePath = Tools.stripTrailingSlash(decodedCompletePath);
        
        String destParentFolder = FolderDTO.replaceColonWithSlash(folderDTO.getId());
        String folderName = Tools.stripLeadingSlash(FolderDTO.extractName(completePath));

        documentService.moveFolder(completePath, destParentFolder, folderName);
        
        String completeRenamedFolderId=destParentFolder+'/'+folderName;
        String encodedRenamedFolderId=FolderDTO.replaceSlashWithColon(completeRenamedFolderId);

        FolderDTO renamedFolderDto = new FolderDTO();
        renamedFolderDto.setPath(destParentFolder);
        renamedFolderDto.setName(folderName);
        renamedFolderDto.setId(encodedRenamedFolderId);

        return renamedFolderDto;
    }
    
    @POST
    @Path("{parentFolderPath}/folders")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public FolderDTO createSubFolder(@PathParam("parentFolderPath") String parentFolderPath, FolderDTO folder)
            throws EntityNotFoundException, EntityAlreadyExistsException, NotAllowedException, AccessRightException, UserNotActiveException, CreationException {

        String decodedCompletePath = FolderDTO.replaceColonWithSlash(parentFolderPath);

        String folderName = folder.getName();
        return createFolder(decodedCompletePath, folderName);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public FolderDTO createRootFolder(@PathParam("workspaceId") String workspaceId, FolderDTO folder)
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
    @Path("{folderId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteRootFolder(@PathParam("folderId") String completePath)
            throws EntityNotFoundException, NotAllowedException, AccessRightException, UserNotActiveException, ESServerException, EntityConstraintException {

        deleteFolder(completePath);
        return Response.status(Response.Status.OK).build();
    }
    
    private DocumentRevisionKey[] deleteFolder(String pCompletePath)
            throws EntityNotFoundException, ESServerException, AccessRightException, NotAllowedException, EntityConstraintException {

        String decodedCompletePath = FolderDTO.replaceColonWithSlash(pCompletePath);
        String completePath = Tools.stripTrailingSlash(decodedCompletePath);
        return documentService.deleteFolder(completePath);
    }
    
    private FolderDTO createFolder(String pCompletePath, String pFolderName)
            throws EntityNotFoundException, EntityAlreadyExistsException, CreationException, AccessRightException, NotAllowedException {
        Folder createdFolder= documentService.createFolder(pCompletePath, pFolderName);
                        
        String completeCreatedFolderPath=createdFolder.getCompletePath()+'/'+createdFolder.getShortName();
        String encodedFolderId=FolderDTO.replaceSlashWithColon(completeCreatedFolderPath);

        FolderDTO createdFolderDtos = new FolderDTO();
        createdFolderDtos.setPath(createdFolder.getCompletePath());
        createdFolderDtos.setName(createdFolder.getShortName());
        createdFolderDtos.setId(encodedFolderId);
        
        return createdFolderDtos;
    }

    /**
     * Get a configuration specification
     * @param workspaceId The current workspace
     * @param configSpecType The configuration specification type
     * @return A configuration specification
     * @throws UserNotFoundException If the user login-workspace doesn't exist
     * @throws UserNotActiveException If the user is disabled
     * @throws WorkspaceNotFoundException If the workspace doesn't exist
     * @throws BaselineNotFoundException If the baseline doesn't exist
     */
    private DocumentConfigSpec getConfigSpec(String workspaceId, String configSpecType) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, BaselineNotFoundException {
        DocumentConfigSpec cs;
        switch (configSpecType) {
            case "latest":
            case "undefined":
                cs = documentConfigSpecService.getLatestConfigSpec(workspaceId);
                break;
            default:
                cs = documentConfigSpecService.getConfigSpecForBaseline(Integer.parseInt(configSpecType));
                break;
        }
        return cs;
    }
}