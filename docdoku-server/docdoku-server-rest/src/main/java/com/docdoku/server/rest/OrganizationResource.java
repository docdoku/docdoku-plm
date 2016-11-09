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
import com.docdoku.core.common.Organization;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IAccountManagerLocal;
import com.docdoku.core.services.IOrganizationManagerLocal;
import com.docdoku.server.rest.dto.AccountDTO;
import com.docdoku.server.rest.dto.OrganizationDTO;
import com.docdoku.server.rest.dto.UserDTO;
import io.swagger.annotations.*;
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
import java.util.Collections;
import java.util.List;

@RequestScoped
@Api(value = "organizations", description = "Operations about organizations")
@Path("organizations")
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class OrganizationResource {

    @Inject
    private IOrganizationManagerLocal organizationManager;

    @Inject
    private IAccountManagerLocal accountManager;

    private Mapper mapper;

    public OrganizationResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }


    @GET
    @ApiOperation(value = "Get organization for authenticated user",
            response = OrganizationDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of OrganizationDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public OrganizationDTO getOrganization()
            throws AccountNotFoundException {

        Organization organization = getOrganizationOfCurrentUser();

        // TODO : send 404 or a null value if no organization found, never send empty DTO
        if (organization == null)
            return new OrganizationDTO();

        return mapper.map(organization, OrganizationDTO.class);
    }

    @POST
    @ApiOperation(value = "Create authenticated user's organization",
            response = OrganizationDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of created OrganizationDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public OrganizationDTO createOrganization(
            @ApiParam(required = true, value = "Organization to create") OrganizationDTO organizationDTO)
            throws NotAllowedException, AccountNotFoundException, OrganizationAlreadyExistsException, CreationException {

        Organization organization = organizationManager.createOrganization(organizationDTO.getName(), organizationDTO.getDescription());
        return mapper.map(organization, OrganizationDTO.class);
    }

    @PUT
    @ApiOperation(value = "Update authenticated user's organization",
            response = OrganizationDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of updated OrganizationDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public OrganizationDTO updateOrganization(
            @ApiParam(required = true, value = "Updated organization") OrganizationDTO organizationDTO)
            throws AccountNotFoundException, AccessRightException, OrganizationNotFoundException {

        Organization organization = getOrganizationOfCurrentUser();
        organization.setDescription(organizationDTO.getDescription());
        organizationManager.updateOrganization(organization);
        return organizationDTO;
    }

    @DELETE
    @ApiOperation(value = "Delete authenticated user's organization",
            response = Response.class)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successful deletion of OrganizationDTO"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteOrganization()
            throws AccountNotFoundException, OrganizationNotFoundException, AccessRightException {

        Organization organization = getOrganizationOfCurrentUser();
        organizationManager.deleteOrganization(organization.getName());
        return Response.noContent().build();
    }

    @GET
    @Path("members")
    @ApiOperation(value = "Get members of the authenticated user's organization",
            response = AccountDTO.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of AccountDTOs. It can be an empty list."),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public AccountDTO[] getMembers()
            throws AccountNotFoundException, OrganizationNotFoundException, AccessRightException {

        Organization organization = getOrganizationOfCurrentUser();
        List<Account> accounts = organization.getMembers();
        AccountDTO[] dtos = new AccountDTO[accounts.size()];

        for (int i = 0; i < accounts.size(); i++) {
            dtos[i] = mapper.map(accounts.get(i), AccountDTO.class);
        }

        return dtos;
    }

    @PUT
    // TODO : change this path, don't use two static paths parts.
    @Path("members/add-user")
    @ApiOperation(value = "Add a member to the authenticated user's organization",
            response = Response.class)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successful member add operation"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response addMember(
            @ApiParam(required = true, value = "User to add") UserDTO userDTO)
            throws AccountNotFoundException, OrganizationNotFoundException, AccessRightException {

        Organization organization = getOrganizationOfCurrentUser();
        Account member = accountManager.getAccount(userDTO.getLogin());
        organization.addMember(member);
        organizationManager.updateOrganization(organization);
        return Response.noContent().build();
    }

    @PUT
    // TODO : change this path, don't use two static paths parts.
    @Path("members/remove-user")
    @ApiOperation(value = "Remove a member to the authenticated user's organization",
            response = Response.class)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successful member removal operation"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeMember(
            @ApiParam(required = true, value = "User to remove") UserDTO userDTO) {

        try {
            Organization organization = getOrganizationOfCurrentUser();
            Account member = accountManager.getAccount(userDTO.getLogin());
            organization.removeMember(member);
            organizationManager.updateOrganization(organization);
            return Response.noContent().build();

            // TODO remove catch blocks and let exception mappers do the job
        } catch (AccessRightException e) {
            return Response.status(Response.Status.FORBIDDEN).build();
        } catch (AccountNotFoundException e) {
            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (OrganizationNotFoundException e) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
    }

    @PUT
    // TODO : change this path, don't use two static paths parts.
    @Path("members/move-up")
    @ApiOperation(value = "Move a member up in the authenticated user's organization",
            response = Response.class)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successful member moved operation"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response moveMemberUp(@ApiParam(required = true, value = "User to move up") UserDTO userDTO) throws AccountNotFoundException, OrganizationNotFoundException, AccessRightException {
        Organization organization = getOrganizationOfCurrentUser();
        Account member = accountManager.getAccount(userDTO.getLogin());
        List<Account> members = organization.getMembers();
        int i = members.indexOf(member);
        if (i > 0) {
            Collections.swap(members, i - 1, i);
            organizationManager.updateOrganization(organization);
        }
        return Response.noContent().build();
    }

    //TODO : refactor to one method with the one above, with query param "up/down"
    @PUT
    // TODO : change this path, don't use two static paths parts.
    @Path("members/move-down")
    @ApiOperation(value = "Move a member down in the authenticated user's organization",
            response = Response.class)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successful member moved operation"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response moveMemberDown(
            @ApiParam(required = true, value = "User to move down") UserDTO userDTO)
            throws AccountNotFoundException, OrganizationNotFoundException, AccessRightException {

        Organization organization = getOrganizationOfCurrentUser();
        Account member = accountManager.getAccount(userDTO.getLogin());
        List<Account> members = organization.getMembers();
        int i = members.indexOf(member);
        if (i > -1 && i < members.size() - 1) {
            Collections.swap(members, i + 1, i);
            organizationManager.updateOrganization(organization);
        }
        return Response.noContent().build();
    }


    private Organization getOrganizationOfCurrentUser() throws AccountNotFoundException {
        Account account = accountManager.getMyAccount();
        return organizationManager.getOrganizationOfAccount(account.getLogin());
    }

}
