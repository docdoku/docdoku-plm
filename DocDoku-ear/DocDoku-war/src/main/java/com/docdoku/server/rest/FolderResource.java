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

import com.docdoku.core.document.DocumentMasterKey;
import com.docdoku.core.document.Folder;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.*;
import com.docdoku.server.rest.dto.FolderDTO;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

@Stateless
@Path("folders")
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
    @Path("{completePath:.*}")
    @Produces("application/json;charset=UTF-8")
    public FolderDTO[] getJson(@PathParam("completePath") String completePath) {
        try {
            completePath = Tools.stripTrailingSlash(completePath);
            String[] folderNames = commandService.getFolders(completePath);
            FolderDTO[] folderDtos = new FolderDTO[folderNames.length];
            for (int i = 0; i < folderNames.length; i++) {
                folderDtos[i] = new FolderDTO(completePath, folderNames[i]);
            }

            return folderDtos;
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RESTException(ex.toString(), ex.getMessage());
        }
    }

    /**
     * PUT method for updating or creating an instance of FolderResource
     *
     * @param parent folder path and name of the folder to create
     */
    @PUT
    @Path("{completePath:.*}")
    public void putJson(@PathParam("completePath") String completePath) {
        try {
            int lastSlash = completePath.lastIndexOf('/');
            String parentFolder = completePath.substring(0, lastSlash);
            String folderName = completePath.substring(lastSlash + 1, completePath.length());
            commandService.createFolder(parentFolder, folderName);
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RESTException(ex.toString(), ex.getMessage());
        }
    }

    
    /* JSON WS to rename a folder */
    
//    @POST
//    @Path("{completePath:.*}")
//    @Consumes("application/json;charset=UTF-8")
//    public Response renameFolder(@PathParam("completePath") String completePath, FolderDTO folderDTO) {
//        try {
//            
//            completePath = Tools.stripTrailingSlash(completePath);
//            int lastSlash = completePath.lastIndexOf('/');
//            String newFolderCompletePath = folderDTO.getCompletePath();
//            String newFolderName = newFolderCompletePath.substring(lastSlash + 1, newFolderCompletePath.length());
//
//            //TODO
//            commandService.renameFolder(completePath, newFolderName);
//
//            return Response.status(Response.Status.OK).build();
//
//        } catch (WorkspaceNotFoundException ex) {
//            return Response.status(Response.Status.NOT_FOUND).build();
//        } catch (FolderNotFoundException ex) {
//            return Response.status(Response.Status.NOT_FOUND).build();
//        } catch (UserNotFoundException ex) {
//            return Response.status(Response.Status.NOT_FOUND).build();
//        } catch (UserNotActiveException ex) {
//            return Response.status(Response.Status.NOT_FOUND).build();
//        }
//    }

    /**
     * DELETE method for deleting an instance of FolderResource
     *
     * @param parent folder path
     * @return the array of the documents that have also been deleted
     */
    @DELETE
    @Path("{completePath:.*}")
    @Produces("application/json;charset=UTF-8")
    public DocumentMasterKey[] deleteJson(@PathParam("completePath") String completePath) {
        try {
            completePath = Tools.stripTrailingSlash(completePath);
            return commandService.deleteFolder(completePath);
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new RESTException(ex.toString(), ex.getMessage());
        }
    }
}
