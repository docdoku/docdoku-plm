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

import com.docdoku.core.document.DocumentMasterKey;
import com.docdoku.core.document.Folder;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.*;
import com.docdoku.server.rest.dto.FolderDTO;

import javax.annotation.PostConstruct;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

@Stateless
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class FolderResource {

    @EJB
    private IDocumentManagerLocal documentService;

    @EJB
    private DocumentsResource documentsResource;

    @EJB
    private IUserManagerLocal userManager;

    @Context
    private UriInfo context;

    private Mapper mapper;

    public FolderResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @Path("{folderId}/documents/")
    public DocumentsResource getDocumentsResource() {
        return documentsResource;
    }

    /**
     * Retrieves representation of folders located at the root of the given workspace
     *
     * @param workspaceId
     * @return the array of folders
     */
    @GET
    @Produces("application/json;charset=UTF-8")
    public FolderDTO[] getRootFoldersJson(@PathParam("workspaceId") String workspaceId) {
        try {
            
            String completePath = Tools.stripTrailingSlash(workspaceId);
            String[] folderNames = documentService.getFolders(completePath);
            FolderDTO[] folderDtos = new FolderDTO[folderNames.length];
            
            for (int i = 0; i < folderNames.length; i++) {
                
                String completeFolderPath=workspaceId+"/"+folderNames[i];
                String encodedFolderId=Tools.replaceSlashWithColon(completeFolderPath);
                
                folderDtos[i] = new FolderDTO();
                folderDtos[i].setPath(completePath);
                folderDtos[i].setName(folderNames[i]);
                folderDtos[i].setId(encodedFolderId);

            }

            return folderDtos;
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }
    
    @GET
    @Path("{completePath}/folders")
    @Produces("application/json;charset=UTF-8")
    public FolderDTO[] getSubFoldersJson(@PathParam("completePath") String folderId) {
        try {
            
            String decodedCompletePath = Tools.replaceColonWithSlash(folderId);
            
            String completePath = Tools.stripTrailingSlash(decodedCompletePath);
            String[] folderNames = documentService.getFolders(completePath);
            
            FolderDTO[] folderDtos = new FolderDTO[folderNames.length];
            
            for (int i = 0; i < folderNames.length; i++) {
                
                String completeFolderPath=completePath+"/"+folderNames[i];
                String encodedFolderId=Tools.replaceSlashWithColon(completeFolderPath);
               
                folderDtos[i] = new FolderDTO();
                folderDtos[i].setPath(completePath);
                folderDtos[i].setName(folderNames[i]);
                folderDtos[i].setId(encodedFolderId);
            }

            return folderDtos;
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    /**
     * PUT method for updating or creating an instance of FolderResource
     */
    @PUT
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    @Path("{folderId}")
    public FolderDTO renameFolderjson(@PathParam("folderId") String folderPath, FolderDTO folder) {
        try {
            
            String decodedCompletePath = Tools.replaceColonWithSlash(folderPath);
            
            String completePath = Tools.stripTrailingSlash(decodedCompletePath);
            int lastSlash = completePath.lastIndexOf('/');
            String destParentFolder = completePath.substring(0, lastSlash);
            String folderName = folder.getName();
            
            documentService.moveFolder(completePath, destParentFolder, folderName);

            String completeRenamedFolderId=destParentFolder+'/'+folderName;
            String encodedRenamedFolderId=Tools.replaceSlashWithColon(completeRenamedFolderId);            
            
            FolderDTO renamedFolderDto = new FolderDTO();
            renamedFolderDto.setPath(destParentFolder);
            renamedFolderDto.setName(folderName);
            renamedFolderDto.setId(encodedRenamedFolderId);
                    
            return renamedFolderDto;

        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @POST
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    @Path("{parentFolderPath}/folders")
    public FolderDTO createSubFolder(@PathParam("parentFolderPath") String parentFolderPath, FolderDTO folder) {
        try {
            
            String decodedCompletePath = Tools.replaceColonWithSlash(parentFolderPath);  
            
            String folderName = folder.getName(); 
            FolderDTO createdSubFolder =  createFolder(decodedCompletePath, folderName);
            
            return createdSubFolder;
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @POST
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    public FolderDTO createRootFolder(@PathParam("workspaceId") String workspaceId, FolderDTO folder) {
        try {

            String folderName = folder.getName();  
            FolderDTO createdRootFolder = createFolder(workspaceId, folderName);
            
            return createdRootFolder;
            
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }
    
    /**
     * DELETE method for deleting an instance of FolderResource
     *
     * @param completePath the folder path
     * @return the array of the documents that have also been deleted
     */
    @DELETE
    @Path("{folderId}")
    @Produces("application/json;charset=UTF-8")
    public Response deleteRootFolder(@PathParam("folderId") String completePath) {
        try {
            
            deleteFolder(completePath);
            
            return Response.status(Response.Status.OK).build();
            
        } catch (ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }
    
    private DocumentMasterKey[] deleteFolder(String pCompletePath) throws WorkspaceNotFoundException, NotAllowedException, AccessRightException, UserNotFoundException, UserNotActiveException, FolderNotFoundException {

        String decodedCompletePath = Tools.replaceColonWithSlash(pCompletePath);

        String completePath = Tools.stripTrailingSlash(decodedCompletePath);

        return documentService.deleteFolder(completePath);      
    }
    
    private FolderDTO createFolder(String pCompletePath, String pFolderName) throws WorkspaceNotFoundException, NotAllowedException, AccessRightException, FolderNotFoundException, FolderAlreadyExistsException, UserNotFoundException, UserNotActiveException, CreationException {
    
        Folder createdFolder= documentService.createFolder(pCompletePath, pFolderName);
                        
        String completeCreatedFolderPath=createdFolder.getCompletePath()+'/'+createdFolder.getShortName();
        String encodedFolderId=Tools.replaceSlashWithColon(completeCreatedFolderPath); 

        FolderDTO createdFolderDtos = new FolderDTO();
        createdFolderDtos.setPath(createdFolder.getCompletePath());
        createdFolderDtos.setName(createdFolder.getShortName());
        createdFolderDtos.setId(encodedFolderId);
        
        return createdFolderDtos;

    }

}

