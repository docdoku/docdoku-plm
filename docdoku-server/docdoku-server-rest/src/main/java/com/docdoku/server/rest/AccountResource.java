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
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IAccountManagerLocal;
import com.docdoku.core.services.IContextManagerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.server.jwt.JWTokenFactory;
import com.docdoku.server.rest.dto.AccountDTO;
import com.docdoku.server.rest.dto.GCMAccountDTO;
import com.docdoku.server.rest.dto.WorkspaceDTO;
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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@RequestScoped
@Path("accounts")
@Api(value = "accounts", description = "Operations about accounts")
@DeclareRoles({UserGroupMapping.REGULAR_USER_ROLE_ID,UserGroupMapping.ADMIN_ROLE_ID})
@RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID,UserGroupMapping.ADMIN_ROLE_ID})
public class AccountResource {

    @Inject
    private IAccountManagerLocal accountManager;

    @Inject
    private IUserManagerLocal userManager;

    @Inject
    private IContextManagerLocal contextManager;

    private Mapper mapper;

    public AccountResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @Path("/me")
    @ApiOperation(value = "Get authenticated user's account", response = AccountDTO.class)
    @Produces(MediaType.APPLICATION_JSON)
    public AccountDTO getAccount() throws AccountNotFoundException {
        Account account = accountManager.getMyAccount();
        AccountDTO accountDTO = mapper.map(account, AccountDTO.class);
        if(contextManager.isCallerInRole(UserGroupMapping.ADMIN_ROLE_ID)){
            accountDTO.setAdmin(true);
        }
        return accountDTO;
    }

    @PUT
    @Path("/me")
    @ApiOperation(value = "Update user's account", response = AccountDTO.class)
    @Produces(MediaType.APPLICATION_JSON)
    public AccountDTO updateAccount(@ApiParam(required = true,value = "Updated account") AccountDTO accountDTO) throws AccountNotFoundException {
        Account account = accountManager.updateAccount(accountDTO.getName(), accountDTO.getEmail(), accountDTO.getLanguage(), accountDTO.getNewPassword(), accountDTO.getTimeZone());
        return mapper.map(account,AccountDTO.class);
    }

    @POST
    @Path("/create")
    @ApiOperation(value = "Create user's account", response = AccountDTO.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createAccount(@Context HttpServletRequest request, @ApiParam(required = true,value = "Account to create") AccountDTO accountDTO) throws AccountAlreadyExistsException, CreationException {
        Account account = accountManager.createAccount(accountDTO.getLogin(), accountDTO.getName(), accountDTO.getEmail(), accountDTO.getLanguage(), accountDTO.getNewPassword(), accountDTO.getTimeZone());
        HttpSession session = request.getSession();
        try {
            request.login(accountDTO.getLogin(), accountDTO.getNewPassword());
            return Response.ok()
                    .entity(mapper.map(account, AccountDTO.class))
                    .header("jwt", JWTokenFactory.createToken(account))
                    .build();
        } catch (ServletException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/workspaces")
    @ApiOperation(value = "Get workspaces where authenticated user is active", response = WorkspaceDTO.class, responseContainer = "List")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWorkspaces() {
        Workspace[] workspaces = userManager.getWorkspacesWhereCallerIsActive();

        List<WorkspaceDTO> workspaceDTOs = new ArrayList<>();
        for (Workspace workspace : workspaces) {
            workspaceDTOs.add(mapper.map(workspace, WorkspaceDTO.class));
        }

        return Response.ok(new GenericEntity<List<WorkspaceDTO>>((List<WorkspaceDTO>) workspaceDTOs) {
        }).build();

    }

    @PUT
    @Path("gcm")
    @ApiOperation(value = "Update GCM account for authenticated user", response = Response.class, code = 200)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setGCMAccount(@ApiParam(required = true, value = "GCM account to set") GCMAccountDTO data)
            throws EntityAlreadyExistsException, AccountNotFoundException, CreationException {
        accountManager.setGCMAccount(data.getGcmId());
        return Response.ok().build();
    }


    @DELETE
    @Path("gcm")
    @ApiOperation(value = "Update GCM account for authenticated user", response = Response.class, code = 200)
    public Response deleteGCMAccount() throws EntityNotFoundException {
        accountManager.deleteGCMAccount();
        return Response.ok().build();
    }

}
