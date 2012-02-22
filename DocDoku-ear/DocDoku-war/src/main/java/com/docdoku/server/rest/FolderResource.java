/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,  
 * but WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
 * GNU General Public License for more details.  
 *  
 * You should have received a copy of the GNU General Public License  
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.  
 */
package com.docdoku.server.rest;

import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.document.DocumentMasterKey;
import com.docdoku.core.document.Folder;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.*;
import com.docdoku.server.rest.dto.DocumentMasterDTO;
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
import org.apache.commons.codec.binary.Base64;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

@Stateless
@Path("workspaces/{workspaceId}/folders")
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class FolderResource {

    @EJB
    private ICommandLocal commandService;
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

    /**
     * Retrieves representation of an instance of FolderResource
     *
     * @param parent folder path
     * @return the array of sub-folders
     */
    
    @GET
    @Produces("application/json;charset=UTF-8")
    public FolderDTO[] getRootFoldersJson(@PathParam("workspaceId") String workspaceId) {
        try {
            
            String completePath = Tools.stripTrailingSlash(workspaceId);
            String[] folderNames = commandService.getFolders(completePath);
            FolderDTO[] folderDtos = new FolderDTO[folderNames.length];
            
            for (int i = 0; i < folderNames.length; i++) {
                
                String completeFolderPath=workspaceId+"/"+folderNames[i];
                String encodedFolderId=Base64.encodeBase64String(completeFolderPath.getBytes()); 
                
                folderDtos[i] = new FolderDTO();
                folderDtos[i].setPath(completePath);
                folderDtos[i].setName(folderNames[i]);
                folderDtos[i].setId(encodedFolderId);

            }

            return folderDtos;
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RESTException(ex.toString(), ex.getMessage());
        }
    }
    
    @GET
    @Path("{completePath}/folders")
    @Produces("application/json;charset=UTF-8")
    public FolderDTO[] getSubFoldersJson(@PathParam("completePath") String folderId) {
        try {
            
            byte[] encodedcompletePath = Base64.decodeBase64(folderId.getBytes());
            String decodedCompletePath = new String(encodedcompletePath);
            
            String completePath = Tools.stripTrailingSlash(decodedCompletePath);
            String[] folderNames = commandService.getFolders(completePath);
            
            FolderDTO[] folderDtos = new FolderDTO[folderNames.length];
            
            for (int i = 0; i < folderNames.length; i++) {
                
                String completeFolderPath=completePath+"/"+folderNames[i];
                String encodedFolderId=Base64.encodeBase64String(completeFolderPath.getBytes());
               
                folderDtos[i] = new FolderDTO();
                folderDtos[i].setPath(completePath);
                folderDtos[i].setName(folderNames[i]);
                folderDtos[i].setId(encodedFolderId);
            }

            return folderDtos;
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RESTException(ex.toString(), ex.getMessage());
        }
    }

    @GET
    @Path("{folderId}/documents/")
    @Produces("application/json;charset=UTF-8")
    public DocumentMasterDTO[] getMasterDocumentsWithSpecifiedTagJson(@PathParam("workspaceId") String workspaceId, @PathParam("folderId") String folderId) {

        try {

            byte[] encodedcompletePath = Base64.decodeBase64(folderId.getBytes());
            String decodedCompletePath = new String(encodedcompletePath);
            
            String pCompletePath = Tools.stripTrailingSlash(decodedCompletePath);
            DocumentMaster[] docM = commandService.findDocumentMastersByFolder(pCompletePath);
            DocumentMasterDTO[] dtos = new DocumentMasterDTO[docM.length];

            for (int i = 0; i < docM.length; i++) {
                dtos[i] = mapper.map(docM[i], DocumentMasterDTO.class);
                dtos[i].setPath(docM[i].getLocation().getCompletePath());
                dtos[i] = Tools.createLightDocumentMasterDTO(dtos[i]);
            }

            return dtos;
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RESTException(ex.toString(), ex.getMessage());
        }

    }

    /**
     * PUT method for updating or creating an instance of FolderResource
     *
     * @param complete path of the folder to create or move and the folder to
     * create or the destination folder in case of a move operation as an entity
     * body (with its completePath attribute)
     *
     */
    @PUT
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    @Path("{folderId}")
    public FolderDTO renameFolderjson(@PathParam("folderId") String folderPath, FolderDTO folder) {
        try {
            
            byte[] encodedcompletePath = Base64.decodeBase64(folderPath.getBytes());
            String decodedCompletePath = new String(encodedcompletePath);
            
            String completePath = Tools.stripTrailingSlash(decodedCompletePath);
            int lastSlash = completePath.lastIndexOf('/');
            String destParentFolder = completePath.substring(0, lastSlash);
            String folderName = folder.getName();
            
            commandService.moveFolder(completePath, destParentFolder, folderName);

            String completeRenamedFolderId=destParentFolder+'/'+folderName;
            String encodedRenamedFolderId=Base64.encodeBase64String(completeRenamedFolderId.getBytes());            
            
            FolderDTO renamedFolderDto = new FolderDTO();
            renamedFolderDto.setPath(destParentFolder);
            renamedFolderDto.setName(folderName);
            renamedFolderDto.setId(encodedRenamedFolderId);
                    
            return renamedFolderDto;

        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RESTException(ex.toString(), ex.getMessage());
        }
    }

    @POST
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    @Path("{parentFolderPath}/folders")
    public FolderDTO createSubFolder(@PathParam("parentFolderPath") String parentFolderPath, FolderDTO folder) {
        try {
            
            byte[] encodedcompletePath = Base64.decodeBase64(parentFolderPath.getBytes());
            String decodedCompletePath = new String(encodedcompletePath);
            
            String folderName = folder.getName(); 
            FolderDTO createdSubFolder =  createFolder(decodedCompletePath, folderName);
            
            return createdSubFolder;
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RESTException(ex.toString(), ex.getMessage());
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
            
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RESTException(ex.toString(), ex.getMessage());
        }
    }
    
    /**
     * DELETE method for deleting an instance of FolderResource
     *
     * @param parent folder path
     * @return the array of the documents that have also been deleted
     */
    @DELETE
    @Path("{folderId}")
    @Produces("application/json;charset=UTF-8")
    public Response deleteRootFolder(@PathParam("folderId") String completePath) {
        try {
            
            deleteFolder(completePath);
            
            return Response.status(Response.Status.OK).build();
            
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RESTException(ex.toString(), ex.getMessage());
        }
    }
    
    private DocumentMasterKey[] deleteFolder(String pCompletePath) throws WorkspaceNotFoundException, NotAllowedException, AccessRightException, UserNotFoundException, UserNotActiveException, FolderNotFoundException{

        byte[] encodedcompletePath = Base64.decodeBase64(pCompletePath.getBytes());
        String decodedCompletePath = new String(encodedcompletePath);

        String completePath = Tools.stripTrailingSlash(decodedCompletePath);

        return commandService.deleteFolder(completePath);      
    }
    
    private FolderDTO createFolder(String pCompletePath, String pFolderName) throws WorkspaceNotFoundException, NotAllowedException, AccessRightException, FolderNotFoundException, FolderAlreadyExistsException, UserNotFoundException, UserNotActiveException, CreationException{
    
        Folder createdFolder= commandService.createFolder(pCompletePath, pFolderName);
                        
        String completeCreatedFolderPath=createdFolder.getCompletePath()+'/'+createdFolder.getShortName();
        String encodedFolderId=Base64.encodeBase64String(completeCreatedFolderPath.getBytes());
               
        FolderDTO createdFolderDtos = new FolderDTO();
        createdFolderDtos.setPath(createdFolder.getCompletePath());
        createdFolderDtos.setName(createdFolder.getShortName());
        createdFolderDtos.setId(encodedFolderId);
        
        return createdFolderDtos;

    }

}

