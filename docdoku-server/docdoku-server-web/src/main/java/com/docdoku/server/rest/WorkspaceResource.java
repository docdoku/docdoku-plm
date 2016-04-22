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
import com.docdoku.core.common.UserGroup;
import com.docdoku.core.common.UserGroupKey;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.security.WorkspaceUserGroupMembership;
import com.docdoku.core.security.WorkspaceUserMembership;
import com.docdoku.core.services.*;
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
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequestScoped
@Api(value = "workspaces", description = "Operations about workspaces")
@Path("workspaces")
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class WorkspaceResource {

    @Inject
    private DocumentsResource documents;

    @Inject
    private DocumentBaselinesResource documentBaselines;

    @Inject
    private FolderResource folders;

    @Inject
    private DocumentTemplateResource docTemplates;

    @Inject
    private PartTemplateResource partTemplates;

    @Inject
    private ProductResource products;

    @Inject
    private PartsResource parts;

    @Inject
    private TagResource tags;

    @Inject
    private CheckedOutDocumentResource checkedOutDocuments;

    @Inject
    private TaskResource tasks;

    @Inject
    private SearchResource searches;

    @Inject
    private WorkflowResource workflows;

    @Inject
    private ChangeItemsResource changeItems;

    @Inject
    private UserResource users;

    @Inject
    private RoleResource roles;

    @Inject
    private ModificationNotificationResource notifications;

    @Inject
    private WorkspaceMembershipResource workspaceMemberships;

    @Inject
    private IDocumentManagerLocal documentService;

    @Inject
    private IProductManagerLocal productService;

    @Inject
    private IUserManagerLocal userManager;

    @Inject
    private LOVResource lov;

    @Inject
    private AttributesResource attributes;

    @Inject
    private IWorkspaceManagerLocal workspaceManager;

    private Mapper mapper;

    @Inject
    private IAccountManagerLocal accountManager;

    @Inject
    private IContextManagerLocal contextManager;

    public WorkspaceResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @ApiOperation(value = "Get workspace list for authenticated user", response = WorkspaceListDTO.class)
    @Produces(MediaType.APPLICATION_JSON)
    public WorkspaceListDTO getWorkspacesForConnectedUser() throws EntityNotFoundException {

        WorkspaceListDTO workspaceListDTO = new WorkspaceListDTO();

        Workspace[] administratedWorkspaces = userManager.getAdministratedWorkspaces();
        Workspace[] allWorkspaces = userManager.getWorkspacesWhereCallerIsActive();

        for (Workspace workspace : administratedWorkspaces) {
            workspaceListDTO.addAdministratedWorkspaces(mapper.map(workspace, WorkspaceDTO.class));
        }
        for (Workspace workspace : allWorkspaces) {
            workspaceListDTO.addAllWorkspaces(mapper.map(workspace, WorkspaceDTO.class));
        }
        return workspaceListDTO;
    }

    @GET
    @ApiOperation(value = "Get detailed workspace list for authenticated user", response = WorkspaceDetailsDTO.class, responseContainer = "List")
    @Path("/more")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDetailedWorkspacesForConnectedUser() throws EntityNotFoundException {
        List<WorkspaceDetailsDTO> workspaceListDTO = new ArrayList<>();

        for (Workspace workspace : userManager.getWorkspacesWhereCallerIsActive()) {
            workspaceListDTO.add(mapper.map(workspace, WorkspaceDetailsDTO.class));
        }
        return Response.ok(new GenericEntity<List<WorkspaceDetailsDTO>>((List<WorkspaceDetailsDTO>) workspaceListDTO) {
        }).build();
    }

    @PUT
    @ApiOperation(value = "Update workspace", response = WorkspaceDTO.class)
    @Path("/{workspaceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateWorkspace(@PathParam("workspaceId") String workspaceId,
                                    @ApiParam(required = true, value = "Workspace values to update") WorkspaceDTO workspaceDTO)
            throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, AccountNotFoundException, AccessRightException {
        Workspace workspace = userManager.updateWorkspace(workspaceId, workspaceDTO.getDescription(), workspaceDTO.isFolderLocked());
        return Response.ok(mapper.map(workspace, WorkspaceDTO.class)).build();
    }

    @PUT
    @Path("/{workspaceId}/index")
    @ApiOperation(value = "Index the workspace", response = Response.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response synchronizeIndexer(@PathParam("workspaceId") String workspaceId)
            throws AccessRightException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        workspaceManager.synchronizeIndexer(workspaceId);
        return Response.ok().build();
    }

    @DELETE
    @ApiOperation(value = "Delete workspace", response = Response.class)
    @Path("/{workspaceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteWorkspace(@PathParam("workspaceId") String workspaceId) {
        workspaceManager.deleteWorkspace(workspaceId);
        return Response.ok().build();
    }

    @POST
    @ApiOperation(value = "Create user group", response = UserGroupDTO.class)
    @Path("/{workspaceId}/user-group")
    @Produces(MediaType.APPLICATION_JSON)
    public UserGroupDTO createGroup(@PathParam("workspaceId") String workspaceId,
                                    @ApiParam(required = true, value = "UserGroup to create") UserGroupDTO userGroupDTO)
            throws UserGroupAlreadyExistsException, AccessRightException, AccountNotFoundException, CreationException, WorkspaceNotFoundException {
        UserGroup userGroup = userManager.createUserGroup(userGroupDTO.getId(), workspaceId);
        return mapper.map(userGroup, UserGroupDTO.class);
    }

    @PUT
    @ApiOperation(value = "Add user to workspace", response = Response.class)
    @Path("/{workspaceId}/add-user")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addUser(@PathParam("workspaceId") String workspaceId,
                            @QueryParam("group") String group,
                            @ApiParam(required = true, value = "User to add") UserDTO userDTO) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException, UserGroupNotFoundException, com.docdoku.core.exceptions.NotAllowedException, AccountNotFoundException, UserAlreadyExistsException, FolderAlreadyExistsException, CreationException {

        if (group != null && !group.isEmpty()) {
            userManager.addUserInGroup(new UserGroupKey(workspaceId, group), userDTO.getLogin());
        } else {
            userManager.addUserInWorkspace(workspaceId, userDTO.getLogin());
        }

        return Response.ok().build();
    }

    @PUT
    @ApiOperation(value = "Set a new admin", response = WorkspaceDTO.class)
    @Path("/{workspaceId}/admin")
    @Produces(MediaType.APPLICATION_JSON)
    public Response setNewAdmin(@PathParam("workspaceId") String workspaceId,
                                @ApiParam(required = true, value = "New admin user") UserDTO userDTO) throws AccountNotFoundException, AccessRightException, WorkspaceNotFoundException {
        Workspace workspace = workspaceManager.changeAdmin(workspaceId, userDTO.getLogin());

        return Response.ok(mapper.map(workspace, WorkspaceDTO.class)).build();
    }

    @POST
    @ApiOperation(value = "Create workspace", response = WorkspaceDTO.class)
    @Produces(MediaType.APPLICATION_JSON)
    public WorkspaceDTO createWorkspace(@QueryParam("userLogin") String userLogin,
                                        @ApiParam(value = "Workspace to create", required = true) WorkspaceDTO workspaceDTO) throws FolderAlreadyExistsException, UserAlreadyExistsException, WorkspaceAlreadyExistsException, CreationException, NotAllowedException, AccountNotFoundException, ESIndexNamingException, IOException, com.docdoku.core.exceptions.NotAllowedException {
        Account account;
        if (contextManager.isCallerInRole(UserGroupMapping.ADMIN_ROLE_ID)) {
            account = accountManager.getAccount(userLogin);
        } else {
            account = accountManager.getMyAccount();
        }
        Workspace workspace = userManager.createWorkspace(workspaceDTO.getId(), account, workspaceDTO.getDescription(), workspaceDTO.isFolderLocked());

        return mapper.map(workspace, WorkspaceDTO.class);
    }

    @PUT
    @ApiOperation(value = "Set user access in workspace", response = Response.class)
    @Path("/{workspaceId}/user-access")
    @Produces(MediaType.APPLICATION_JSON)
    public Response setUserAccess(@PathParam("workspaceId") String workspaceId,
                                  @ApiParam(value = "User to grant access in workspace", required = true) UserDTO userDTO) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException {
        if (userDTO.getMembership() == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        WorkspaceUserMembership workspaceUserMembership = userManager.grantUserAccess(workspaceId, userDTO.getLogin(), userDTO.getMembership() == WorkspaceMembership.READ_ONLY ? true : false);
        return Response.ok(mapper.map(workspaceUserMembership.getMember(), UserDTO.class)).build();
    }

    @PUT
    @ApiOperation(value = "Set group access in workspace", response = Response.class)
    @Path("/{workspaceId}/group-access")
    @Produces(MediaType.APPLICATION_JSON)
    public Response setGroupAccess(@PathParam("workspaceId") String workspaceId,
                                   @ApiParam(value = "User to grant access in group", required = true) WorkspaceUserGroupMemberShipDTO workspaceUserGroupMemberShipDTO)
            throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException {

        WorkspaceUserGroupMembership membership = userManager.grantGroupAccess(workspaceId, workspaceUserGroupMemberShipDTO.getMemberId(), workspaceUserGroupMemberShipDTO.isReadOnly());
        return Response.ok(mapper.map(membership, WorkspaceUserGroupMemberShipDTO.class)).build();
    }

    @PUT
    @ApiOperation(value = "Remove user from group", response = UserGroupDTO.class)
    @Path("/{workspaceId}/remove-from-group/{groupId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeUserFromGroup(@PathParam("workspaceId") String workspaceId,
                                        @PathParam("groupId") String groupId,
                                        @ApiParam(value = "User to remove from group", required = true) UserDTO userDTO)
            throws AccessRightException, UserGroupNotFoundException, AccountNotFoundException, WorkspaceNotFoundException {
        UserGroup userGroup = userManager.removeUserFromGroup(new UserGroupKey(workspaceId, groupId), userDTO.getLogin());
        return Response.ok(mapper.map(userGroup, UserGroupDTO.class)).build();
    }

    @PUT
    @ApiOperation(value = "Remove user from workspace", response = WorkspaceDTO.class)
    @Path("/{workspaceId}/remove-from-workspace")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeUserFromWorkspace(@PathParam("workspaceId") String workspaceId,
                                            @ApiParam(value = "User to remove from workspace", required = true) UserDTO userDTO)
            throws UserGroupNotFoundException, AccessRightException, UserNotFoundException, NotAllowedException, AccountNotFoundException, WorkspaceNotFoundException, FolderNotFoundException, ESServerException, EntityConstraintException, DocumentRevisionNotFoundException, UserNotActiveException, com.docdoku.core.exceptions.NotAllowedException {
        Workspace workspace = userManager.removeUser(workspaceId, userDTO.getLogin());
        return Response.ok(mapper.map(workspace, WorkspaceDTO.class)).build();
    }

    @PUT
    @ApiOperation(value = "Enable user", response = Response.class)
    @Path("/{workspaceId}/enable-user")
    @Produces(MediaType.APPLICATION_JSON)
    public Response enableUser(@PathParam("workspaceId") String workspaceId,
                               @ApiParam(value = "User to enable", required = true) UserDTO userDTO)
            throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException {
        userManager.activateUser(workspaceId, userDTO.getLogin());
        return Response.ok().build();
    }

    @PUT
    @ApiOperation(value = "Disable user", response = Response.class)
    @Path("/{workspaceId}/disable-user")
    @Produces(MediaType.APPLICATION_JSON)
    public Response disableUser(@PathParam("workspaceId") String workspaceId,
                                @ApiParam(value = "User to disable", required = true) UserDTO userDTO)
            throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException {
        userManager.passivateUser(workspaceId, userDTO.getLogin());
        return Response.ok().build();
    }

    @PUT
    @ApiOperation(value = "Enable group", response = Response.class)
    @Path("/{workspaceId}/enable-group")
    @Produces(MediaType.APPLICATION_JSON)
    public Response enableGroup(@PathParam("workspaceId") String workspaceId,
                                @ApiParam(value = "Group to enable", required = true) UserGroupDTO userGroupDTO)
            throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException {
        userManager.activateUserGroup(workspaceId, userGroupDTO.getId());
        return Response.ok().build();
    }

    @PUT
    @ApiOperation(value = "Disable group", response = Response.class)
    @Path("/{workspaceId}/disable-group")
    @Produces(MediaType.APPLICATION_JSON)
    public Response disableGroup(@PathParam("workspaceId") String workspaceId,
                                 @ApiParam(value = "Group to disable", required = true) UserGroupDTO userGroupDTO)
            throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException {
        userManager.passivateUserGroup(workspaceId, userGroupDTO.getId());
        return Response.ok().build();
    }

    @GET
    @ApiOperation(value = "Get stats overview for workspace", response = StatsOverviewDTO.class)
    @Path("/{workspaceId}/stats-overview")
    @Produces(MediaType.APPLICATION_JSON)
    public StatsOverviewDTO getStatsOverview(@PathParam("workspaceId") String workspaceId)
            throws WorkspaceNotFoundException, AccountNotFoundException, AccessRightException, UserNotFoundException, UserNotActiveException {

        StatsOverviewDTO statsOverviewDTO = new StatsOverviewDTO();
        statsOverviewDTO.setDocuments(documentService.getTotalNumberOfDocuments(workspaceId));
        statsOverviewDTO.setParts(productService.getTotalNumberOfParts(workspaceId));
        statsOverviewDTO.setUsers(userManager.getUsers(workspaceId).length);
        return new StatsOverviewDTO();
    }



    // Sub resources

    @ApiOperation(value = "WorkspaceDocuments")
    @Path("/{workspaceId}/documents")
    public DocumentsResource documents() {
        return documents;
    }

    @ApiOperation(value = "WorkspaceFolders")
    @Path("/{workspaceId}/folders")
    public FolderResource folders() {
        return folders;
    }

    @ApiOperation(value = "WorkspaceDocumentTemplates")
    @Path("/{workspaceId}/document-templates")
    public DocumentTemplateResource docTemplates() {
        return docTemplates;
    }

    @ApiOperation(value = "WorkspacePartTemplates")
    @Path("/{workspaceId}/part-templates")
    public PartTemplateResource partTemplates() {
        return partTemplates;
    }

    @ApiOperation(value = "WorkspaceProducts")
    @Path("/{workspaceId}/products")
    public ProductResource products() {
        return products;
    }

    @ApiOperation(value = "WorkspaceParts")
    @Path("/{workspaceId}/parts")
    public PartsResource parts() {
        return parts;
    }

    @ApiOperation(value = "WorkspaceTags")
    @Path("/{workspaceId}/tags")
    public TagResource tags() {
        return tags;
    }

    @ApiOperation(value = "WorkspaceCheckedOut")
    @Path("/{workspaceId}/checkedouts")
    public CheckedOutDocumentResource checkedOuts() {
        return checkedOutDocuments;
    }

    @ApiOperation(value = "WorkspaceSearch")
    @Path("/{workspaceId}/search")
    public SearchResource search() {
        return searches;
    }

    @ApiOperation(value = "WorkspaceTasks")
    @Path("/{workspaceId}/tasks")
    public TaskResource tasks() {
        return tasks;
    }

    @ApiOperation(value = "WorkspaceModificationNotifications")
    @Path("/{workspaceId}/notifications")
    public ModificationNotificationResource notifications() {
        return notifications;
    }

    @ApiOperation(value = "WorkspaceWorkflows")
    @Path("/{workspaceId}/workflows")
    public WorkflowResource workflows() {
        return workflows;
    }

    @ApiOperation(value = "WorkspaceUsers")
    @Path("/{workspaceId}/users")
    public UserResource users() {
        return users;
    }

    @ApiOperation(value = "WorkspaceRoles")
    @Path("/{workspaceId}/roles")
    public RoleResource roles() {
        return roles;
    }

    @ApiOperation(value = "WorkspaceWorkspaceMemberships")
    @Path("/{workspaceId}/memberships")
    public WorkspaceMembershipResource workspaceMemberships() {
        return workspaceMemberships;
    }

    @ApiOperation(value = "WorkspaceChangeItems")
    @Path("/{workspaceId}/changes")
    public ChangeItemsResource changeItems() {
        return changeItems;
    }

    @ApiOperation(value = "WorkspaceDocumentBaselines")
    @Path("/{workspaceId}/document-baselines")
    public DocumentBaselinesResource documentBaselines() {
        return documentBaselines;
    }

    @ApiOperation(value = "WorkspaceLOVs")
    @Path("/{workspaceId}/lov")
    public LOVResource lov() {
        return lov;
    }

    @ApiOperation(hidden = true, value = "WorkspaceAttributes")
    @Path("/{workspaceId}/attributes")
    public AttributesResource attributes() {
        return attributes;
    }

}