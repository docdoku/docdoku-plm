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

import com.docdoku.core.common.Workspace;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.core.services.IWorkspaceManagerLocal;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.Serializable;


@RequestScoped
@Api(value = "admin", description = "Admin resources")
@Path("admin")
@DeclareRoles(UserGroupMapping.ADMIN_ROLE_ID)
@RolesAllowed(UserGroupMapping.ADMIN_ROLE_ID)
public class AdminResource implements Serializable {

    @Inject
    private IWorkspaceManagerLocal workspaceService;

    @Inject
    private IDocumentManagerLocal documentService;

    @Inject
    private IProductManagerLocal productService;

    @Inject
    private IUserManagerLocal userManager;

    @Inject
    private IWorkspaceManagerLocal workspaceManager;

    public AdminResource(){

    }

    @GET
    @Path("disk-usage-stats")
    @ApiOperation(value = "Get disk usage stats", response = JsonObject.class)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getDiskSpaceUsageStats() throws AccountNotFoundException {

        JsonObjectBuilder diskUsage = Json.createObjectBuilder();

        Workspace[] allWorkspaces = userManager.getAdministratedWorkspaces();

        for(Workspace workspace:allWorkspaces){
            long workspaceDiskUsage = workspaceService.getDiskUsageInWorkspace(workspace.getId());
            diskUsage.add(workspace.getId(), workspaceDiskUsage);
        }

        return diskUsage.build();

    }
    @GET
    @Path("users-stats")
    @ApiOperation(value = "Get users stats", response = JsonObject.class)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getUsersStats() throws AccountNotFoundException, WorkspaceNotFoundException, AccessRightException, UserNotFoundException, UserNotActiveException {

        JsonObjectBuilder userStats = Json.createObjectBuilder();

        Workspace[] allWorkspaces = userManager.getAdministratedWorkspaces();

        for(Workspace workspace:allWorkspaces){
            int userCount =  userManager.getUsers(workspace.getId()).length;
            userStats.add(workspace.getId(), userCount);
        }

        return userStats.build();

    }
    @GET
    @Path("documents-stats")
    @ApiOperation(value = "Get documents stats", response = JsonObject.class)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getDocumentsStats() throws AccountNotFoundException, WorkspaceNotFoundException, AccessRightException {

        JsonObjectBuilder docStats = Json.createObjectBuilder();

        Workspace[] allWorkspaces = userManager.getAdministratedWorkspaces();

        for(Workspace workspace:allWorkspaces){
            int documentsCount = documentService.getTotalNumberOfDocuments(workspace.getId());
            docStats.add(workspace.getId(), documentsCount);
        }

        return docStats.build();

    }
    @GET
    @Path("products-stats")
    @ApiOperation(value = "Get products stats", response = JsonObject.class)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getProductsStats() throws AccountNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {

        JsonObjectBuilder productsStats = Json.createObjectBuilder();

        Workspace[] allWorkspaces = userManager.getAdministratedWorkspaces();

        for(Workspace workspace:allWorkspaces){
            int productsCount = productService.getConfigurationItems(workspace.getId()).size();
            productsStats.add(workspace.getId(), productsCount);
        }

        return productsStats.build();

    }

    @GET
    @Path("parts-stats")
    @ApiOperation(value = "Get parts stats", response = JsonObject.class)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getPartsStats() throws AccountNotFoundException, AccessRightException, WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {

        JsonObjectBuilder partsStats = Json.createObjectBuilder();

        Workspace[] allWorkspaces = userManager.getAdministratedWorkspaces();

        for(Workspace workspace:allWorkspaces){
            int productsCount = productService.getTotalNumberOfParts(workspace.getId());
            partsStats.add(workspace.getId(), productsCount);
        }

        return partsStats.build();
    }


    @PUT
    @ApiOperation(value = "Synchronize index for workspace")
    @Path("index/{workspaceId}")
    public Response indexWorkspace(@PathParam("workspaceId") String workspaceId){
        workspaceManager.synchronizeIndexer(workspaceId);
        return Response.ok().build();

    }

    @PUT
    @ApiOperation(value = "Synchronize index for all workspaces")
    @Path("index-all")
    public Response indexAllWorkspaces() throws AccountNotFoundException {
        Workspace[] administratedWorkspaces = userManager.getAdministratedWorkspaces();
        for(Workspace workspace : administratedWorkspaces){
            workspaceManager.synchronizeIndexer(workspace.getId());
        }
        return Response.ok().build();
    }
}
