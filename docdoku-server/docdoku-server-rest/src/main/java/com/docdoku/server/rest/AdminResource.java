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
import com.docdoku.core.common.Workspace;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.*;
import com.docdoku.server.rest.dto.AccountDTO;
import com.docdoku.server.rest.dto.PlatformOptionsDTO;
import com.docdoku.server.rest.dto.WorkspaceDTO;
import io.swagger.annotations.*;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

import javax.annotation.PostConstruct;
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
import java.util.ArrayList;
import java.util.List;


/**
 * @author Morgan Guimard
 */
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

    @Inject
    private IAccountManagerLocal accountManager;

    @Inject
    private IPlatformOptionsManagerLocal platformOptionsManager;

    private Mapper mapper;

    public AdminResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @Path("disk-usage-stats")
    @ApiOperation(value = "Get disk usage stats",
            response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of disk usage statistics"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getDiskSpaceUsageStats()
            throws AccountNotFoundException {

        JsonObjectBuilder diskUsage = Json.createObjectBuilder();

        Workspace[] allWorkspaces = userManager.getAdministratedWorkspaces();

        for (Workspace workspace : allWorkspaces) {
            long workspaceDiskUsage = workspaceService.getDiskUsageInWorkspace(workspace.getId());
            diskUsage.add(workspace.getId(), workspaceDiskUsage);
        }

        return diskUsage.build();

    }

    @GET
    @Path("users-stats")
    @ApiOperation(value = "Get users stats",
            response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of user statistics"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getUsersStats()
            throws AccountNotFoundException, WorkspaceNotFoundException, AccessRightException, UserNotFoundException,
            UserNotActiveException, WorkspaceNotEnabledException {

        JsonObjectBuilder userStats = Json.createObjectBuilder();

        Workspace[] allWorkspaces = userManager.getAdministratedWorkspaces();

        for (Workspace workspace : allWorkspaces) {
            int userCount = userManager.getUsers(workspace.getId()).length;
            userStats.add(workspace.getId(), userCount);
        }

        return userStats.build();

    }

    @GET
    @Path("documents-stats")
    @ApiOperation(value = "Get documents stats",
            response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of documents statistics"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getDocumentsStats()
            throws AccountNotFoundException, WorkspaceNotFoundException, AccessRightException {

        JsonObjectBuilder docStats = Json.createObjectBuilder();

        Workspace[] allWorkspaces = userManager.getAdministratedWorkspaces();

        for (Workspace workspace : allWorkspaces) {
            int documentsCount = documentService.getTotalNumberOfDocuments(workspace.getId());
            docStats.add(workspace.getId(), documentsCount);
        }

        return docStats.build();

    }

    @GET
    @Path("products-stats")
    @ApiOperation(value = "Get products stats",
            response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of products statistics"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getProductsStats()
            throws AccountNotFoundException, UserNotFoundException, UserNotActiveException,
            WorkspaceNotFoundException, WorkspaceNotEnabledException {

        JsonObjectBuilder productsStats = Json.createObjectBuilder();

        Workspace[] allWorkspaces = userManager.getAdministratedWorkspaces();

        for (Workspace workspace : allWorkspaces) {
            int productsCount = productService.getConfigurationItems(workspace.getId()).size();
            productsStats.add(workspace.getId(), productsCount);
        }

        return productsStats.build();

    }

    @GET
    @Path("parts-stats")
    @ApiOperation(value = "Get parts stats",
            response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of parts statistics"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getPartsStats()
            throws AccountNotFoundException, AccessRightException, WorkspaceNotFoundException, UserNotFoundException,
            UserNotActiveException, WorkspaceNotEnabledException {

        JsonObjectBuilder partsStats = Json.createObjectBuilder();

        Workspace[] allWorkspaces = userManager.getAdministratedWorkspaces();

        for (Workspace workspace : allWorkspaces) {
            int productsCount = productService.getTotalNumberOfParts(workspace.getId());
            partsStats.add(workspace.getId(), productsCount);
        }

        return partsStats.build();
    }


    @PUT
    @ApiOperation(value = "Synchronize index for workspace",
            response = Response.class)
    @ApiResponses(value = {
            @ApiResponse(code = 202, message = "Accepted delete operation (asynchronous method)"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("index/{workspaceId}")
    public Response indexWorkspace(
            @ApiParam(value = "Workspace id", required = true) @PathParam("workspaceId") String workspaceId,
            @ApiParam(value = "Put content", name = "body") String body) {
        workspaceManager.synchronizeIndexer(workspaceId);
        return Response.status(Response.Status.ACCEPTED).build();

    }

    @PUT
    @ApiOperation(value = "Synchronize index for all workspaces",
            response = Response.class)
    @ApiResponses(value = {
            @ApiResponse(code = 202, message = "Accepted delete operation (asynchronous method)"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("index-all")
    public Response indexAllWorkspaces(
            @ApiParam(name = "body", defaultValue = "") String body)
            throws AccountNotFoundException {
        Workspace[] administratedWorkspaces = userManager.getAdministratedWorkspaces();
        for (Workspace workspace : administratedWorkspaces) {
            workspaceManager.synchronizeIndexer(workspace.getId());
        }
        return Response.status(Response.Status.ACCEPTED).build();
    }

    @GET
    @Path("platform-options")
    @ApiOperation(value = "Get platform options",
            response = PlatformOptionsDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of PlatformOptions"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public PlatformOptionsDTO getPlatformOptions() {
        return mapper.map(platformOptionsManager.getPlatformOptions(), PlatformOptionsDTO.class);
    }

    @PUT
    @Path("platform-options")
    @ApiOperation(value = "Set platform options",
            response = Response.class)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successful update of PlatformOptions"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setPlatformOptions(
            @ApiParam("Options to set") PlatformOptionsDTO platformOptionsDTO) {
        platformOptionsManager.setRegistrationStrategy(platformOptionsDTO.getRegistrationStrategy());
        platformOptionsManager.setWorkspaceCreationStrategy(platformOptionsDTO.getWorkspaceCreationStrategy());
        return Response.noContent().build();
    }

    @PUT
    @ApiOperation(value = "Enable or disable workspace",
            response = WorkspaceDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of updated Workspace"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("workspace/{workspaceId}/enable")
    @Produces(MediaType.APPLICATION_JSON)
    public WorkspaceDTO enableWorkspace(
            @ApiParam(value = "Workspace id", required = true) @PathParam("workspaceId") String workspaceId,
            @ApiParam(value = "Enabled", required = true) @QueryParam("enabled") boolean enabled,
            @ApiParam(value = "Put content", name = "body") String body)
            throws WorkspaceNotFoundException {
        Workspace workspace = workspaceManager.enableWorkspace(workspaceId, enabled);
        return mapper.map(workspace, WorkspaceDTO.class);
    }

    @PUT
    @ApiOperation(value = "Enable or disable account",
            response = AccountDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of updated Account"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("accounts/{login}/enable")
    @Produces(MediaType.APPLICATION_JSON)
    public AccountDTO enableAccount(
            @ApiParam(value = "Workspace id", required = true) @PathParam("login") String login,
            @ApiParam(value = "Enabled", required = true) @QueryParam("enabled") boolean enabled,
            @ApiParam(value = "Put content", name = "body") String body)
            throws WorkspaceNotFoundException, AccountNotFoundException, NotAllowedException {
        Account account = accountManager.enableAccount(login, enabled);
        return mapper.map(account, AccountDTO.class);
    }


    @GET
    @Path("accounts")
    @ApiOperation(value = "Get accounts ",
            response = AccountDTO.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of Accounts"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public List<AccountDTO> getAccounts() {
        List<Account> accounts = accountManager.getAccounts();
        List<AccountDTO> accountsDTO = new ArrayList<>();
        for (Account account : accounts) {
            accountsDTO.add(mapper.map(account, AccountDTO.class));
        }
        return accountsDTO;
    }

    @PUT
    @Path("accounts")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of updated Account"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @ApiOperation(value = "Update account",
            response = AccountDTO.class)
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public AccountDTO updateAccount(
            @ApiParam(required = true, value = "Updated account") AccountDTO accountDTO)
            throws AccountNotFoundException, NotAllowedException {

        Account account = accountManager.updateAccount(
                accountDTO.getLogin(),
                accountDTO.getName(),
                accountDTO.getEmail(),
                accountDTO.getLanguage(),
                accountDTO.getNewPassword(),
                accountDTO.getTimeZone());

        return mapper.map(account, AccountDTO.class);
    }
}
