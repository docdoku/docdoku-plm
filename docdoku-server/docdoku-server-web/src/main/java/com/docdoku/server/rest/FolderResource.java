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

import com.docdoku.core.common.User;
import com.docdoku.core.common.UserGroup;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.document.DocumentMasterKey;
import com.docdoku.core.document.Folder;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.ACLUserEntry;
import com.docdoku.core.security.ACLUserGroupEntry;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.*;
import com.docdoku.server.rest.dto.ACLDTO;
import com.docdoku.server.rest.dto.DocumentCreationDTO;
import com.docdoku.server.rest.dto.DocumentMasterDTO;
import com.docdoku.server.rest.dto.FolderDTO;
import java.util.Map;
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
@Path("workspaces/{workspaceId}/folders")
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class FolderResource {

    @EJB
    private IDocumentManagerLocal documentService;
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
        } catch (com.docdoku.core.services.ApplicationException ex) {
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
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }

    @GET
    @Path("{folderId}/documents/")
    @Produces("application/json;charset=UTF-8")
    public DocumentMasterDTO[] getMasterDocumentsWithSpecifiedTagJson(@PathParam("workspaceId") String workspaceId, @PathParam("folderId") String folderId) {

        try {

            String decodedCompletePath = Tools.replaceColonWithSlash(folderId);
            
            String pCompletePath = Tools.stripTrailingSlash(decodedCompletePath);
            DocumentMaster[] docM = documentService.findDocumentMastersByFolder(pCompletePath);
            DocumentMasterDTO[] dtos = new DocumentMasterDTO[docM.length];

            for (int i = 0; i < docM.length; i++) {
                dtos[i] = mapper.map(docM[i], DocumentMasterDTO.class);
                dtos[i].setPath(docM[i].getLocation().getCompletePath());
                dtos[i].setLifeCycleState(docM[i].getLifeCycleState());
                dtos[i] = Tools.createLightDocumentMasterDTO(dtos[i]);
            }

            return dtos;
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
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

        } catch (com.docdoku.core.services.ApplicationException ex) {
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
        } catch (com.docdoku.core.services.ApplicationException ex) {
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
            
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }
    
    @POST
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    @Path("{folderId}/documents/")
    public DocumentMasterDTO createDocumentMasterInFolder(@PathParam("workspaceId") String workspaceId, DocumentCreationDTO docCreationDTO,@PathParam("folderId") String folderId) {

        String pDocMID = docCreationDTO.getReference();
        String pTitle = docCreationDTO.getTitle();
        String pDescription = docCreationDTO.getDescription();

        String decodedCompletePath = Tools.replaceColonWithSlash(folderId);             
        String pParentFolder = Tools.stripTrailingSlash(decodedCompletePath);
        
        String pWorkflowModelId = docCreationDTO.getWorkflowModelId();
        String pDocMTemplateId = docCreationDTO.getTemplateId();


        /*
         * Null value for test purpose only
         */
        ACLDTO acl = null;


        try {
            ACLUserEntry[] userEntries = null;
            ACLUserGroupEntry[] userGroupEntries = null;
            if (acl != null) {
                userEntries = new ACLUserEntry[acl.getUserEntries().size()];
                userGroupEntries = new ACLUserGroupEntry[acl.getGroupEntries().size()];
                int i = 0;
                for (Map.Entry<String, ACLDTO.Permission> entry : acl.getUserEntries().entrySet()) {
                    userEntries[i] = new ACLUserEntry();
                    userEntries[i].setPrincipal(new User(new Workspace(workspaceId), entry.getKey()));
                    userEntries[i++].setPermission(ACL.Permission.valueOf(entry.getValue().name()));
                }
                i = 0;
                for (Map.Entry<String, ACLDTO.Permission> entry : acl.getGroupEntries().entrySet()) {
                    userGroupEntries[i] = new ACLUserGroupEntry();
                    userGroupEntries[i].setPrincipal(new UserGroup(new Workspace(workspaceId), entry.getKey()));
                    userGroupEntries[i++].setPermission(ACL.Permission.valueOf(entry.getValue().name()));
                }
            }

           DocumentMaster createdDocMs =  documentService.createDocumentMaster(pParentFolder, pDocMID, pTitle, pDescription, pDocMTemplateId, pWorkflowModelId, userEntries, userGroupEntries);
           DocumentMasterDTO docMsDTO = mapper.map(createdDocMs, DocumentMasterDTO.class);
           docMsDTO.setPath(createdDocMs.getLocation().getCompletePath());
           docMsDTO.setLifeCycleState(createdDocMs.getLifeCycleState());
           
            return docMsDTO;
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RestApiException(ex.toString(), ex.getMessage());
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
            throw new RestApiException(ex.toString(), ex.getMessage());
        }
    }
    
    private DocumentMasterKey[] deleteFolder(String pCompletePath) throws WorkspaceNotFoundException, NotAllowedException, AccessRightException, UserNotFoundException, UserNotActiveException, FolderNotFoundException{

        String decodedCompletePath = Tools.replaceColonWithSlash(pCompletePath);

        String completePath = Tools.stripTrailingSlash(decodedCompletePath);

        return documentService.deleteFolder(completePath);      
    }
    
    private FolderDTO createFolder(String pCompletePath, String pFolderName) throws WorkspaceNotFoundException, NotAllowedException, AccessRightException, FolderNotFoundException, FolderAlreadyExistsException, UserNotFoundException, UserNotActiveException, CreationException{
    
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

