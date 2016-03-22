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
import com.docdoku.core.exceptions.EntityNotFoundException;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.server.rest.dto.WorkspaceDTO;
import com.docdoku.server.rest.dto.WorkspaceDetailsDTO;
import com.docdoku.server.rest.dto.WorkspaceListDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

import javax.annotation.PostConstruct;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
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
    private IUserManagerLocal userManager;

    @Inject
    private LOVResource lov;

    @Inject
    private AttributesResource attributes;

    private Mapper mapper;

    public WorkspaceResource() {
    }

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @ApiOperation(value = "Get workspace list for authenticated user", response = WorkspaceListDTO.class)
    @Path("/")
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
    public Response getDetailedWorkspacesForConnectedUser() throws EntityNotFoundException {
        List<WorkspaceDetailsDTO> workspaceListDTO = new ArrayList<>();

        for (Workspace workspace : userManager.getWorkspacesWhereCallerIsActive()) {
            workspaceListDTO.add(mapper.map(workspace, WorkspaceDetailsDTO.class));
        }
        return Response.ok(new GenericEntity<List<WorkspaceDetailsDTO>>((List<WorkspaceDetailsDTO>) workspaceListDTO) {
        }).build();
    }

    @ApiOperation(value = "SubResource : DocumentsResource")
    @Path("/{workspaceId}/documents")
    public DocumentsResource documents() {
        return documents;
    }

    @ApiOperation(value = "SubResource : FolderResource")
    @Path("/{workspaceId}/folders")
    public FolderResource folders() {
        return folders;
    }

    @ApiOperation(value = "SubResource : DocumentTemplateResource")
    @Path("/{workspaceId}/document-templates")
    public DocumentTemplateResource docTemplates() {
        return docTemplates;
    }

    @ApiOperation(value = "SubResource : PartTemplateResource")
    @Path("/{workspaceId}/part-templates")
    public PartTemplateResource partTemplates() {
        return partTemplates;
    }

    @ApiOperation(value = "SubResource : ProductResource")
    @Path("/{workspaceId}/products")
    public ProductResource products() {
        return products;
    }

    @ApiOperation(value = "SubResource : PartsResource")
    @Path("/{workspaceId}/parts")
    public PartsResource parts() {
        return parts;
    }

    @ApiOperation(value = "SubResource : TagResource")
    @Path("/{workspaceId}/tags")
    public TagResource tags() {
        return tags;
    }

    @ApiOperation(value = "SubResource : CheckedOutResource")
    @Path("/{workspaceId}/checkedouts")
    public CheckedOutDocumentResource checkedOuts() {
        return checkedOutDocuments;
    }

    @ApiOperation(value = "SubResource : SearchResource")
    @Path("/{workspaceId}/search")
    public SearchResource search() {
        return searches;
    }

    @ApiOperation(value = "SubResource : TaskResource")
    @Path("/{workspaceId}/tasks")
    public TaskResource tasks() {
        return tasks;
    }

    @ApiOperation(value = "SubResource : ModificationNotificationResource")
    @Path("/{workspaceId}/notifications")
    public ModificationNotificationResource notifications() {
        return notifications;
    }

    @ApiOperation(value = "SubResource : WorkflowResource")
    @Path("/{workspaceId}/workflows")
    public WorkflowResource workflows() {
        return workflows;
    }

    @ApiOperation(value = "SubResource : UserResource")
    @Path("/{workspaceId}/users")
    public UserResource users() {
        return users;
    }

    @ApiOperation(value = "SubResource : RoleResource")
    @Path("/{workspaceId}/roles")
    public RoleResource roles() {
        return roles;
    }

    @ApiOperation(value = "SubResource : WorkspaceMembershipResource")
    @Path("/{workspaceId}/memberships")
    public WorkspaceMembershipResource workspaceMemberships() {
        return workspaceMemberships;
    }

    //@ApiOperation(value = "SubResource : ChangeItemsResource")
    @Path("/{workspaceId}/changes")
    public ChangeItemsResource changeItems() {
        return changeItems;
    }

    //@ApiOperation(value = "SubResource : DocumentBaselinesResource")
    @Path("/{workspaceId}/document-baselines")
    public DocumentBaselinesResource documentBaselines() {
        return documentBaselines;
    }

    //@ApiOperation(value = "SubResource : LOVResource")
    @Path("/{workspaceId}/lov")
    public LOVResource lov() {
        return lov;
    }

    @ApiOperation(hidden = true, value = "SubResource : AttributesResource")
    @Path("/{workspaceId}/attributes")
    public AttributesResource attributes() {
        return attributes;
    }
}