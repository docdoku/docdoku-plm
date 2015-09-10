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
    private CheckedOutResource checkedOuts;

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
    @Path("/more")
    public Response getDetailedWorkspacesForConnectedUser() throws EntityNotFoundException {
        List<WorkspaceDetailsDTO> workspaceListDTO = new ArrayList<>();

        for (Workspace workspace : userManager.getWorkspacesWhereCallerIsActive()) {
            workspaceListDTO.add(mapper.map(workspace, WorkspaceDetailsDTO.class));
        }
        return Response.ok(new GenericEntity<List<WorkspaceDetailsDTO>>((List<WorkspaceDetailsDTO>) workspaceListDTO) {
        }).build();
    }

    @Path("/{workspaceId}/documents")
    public DocumentsResource documents() {
        return documents;
    }

    @Path("/{workspaceId}/folders")
    public FolderResource folders() {
        return folders;
    }

    @Path("/{workspaceId}/document-templates")
    public DocumentTemplateResource docTemplates() {
        return docTemplates;
    }

    @Path("/{workspaceId}/part-templates")
    public PartTemplateResource partTemplates() {
        return partTemplates;
    }

    @Path("/{workspaceId}/products")
    public ProductResource products() {
        return products;
    }

    @Path("/{workspaceId}/parts")
    public PartsResource parts() {
        return parts;
    }

    @Path("/{workspaceId}/tags")
    public TagResource tags() {
        return tags;
    }

    @Path("/{workspaceId}/checkedouts")
    public CheckedOutResource checkedOuts() {
        return checkedOuts;
    }

    @Path("/{workspaceId}/search")
    public SearchResource search() {
        return searches;
    }

    @Path("/{workspaceId}/tasks")
    public TaskResource tasks() {
        return tasks;
    }

    @Path("/{workspaceId}/notifications")
    public ModificationNotificationResource notifications() {
        return notifications;
    }

    @Path("/{workspaceId}/workflows")
    public WorkflowResource workflows() {
        return workflows;
    }

    @Path("/{workspaceId}/users")
    public UserResource users() {
        return users;
    }

    @Path("/{workspaceId}/roles")
    public RoleResource roles() {
        return roles;
    }

    @Path("/{workspaceId}/memberships")
    public WorkspaceMembershipResource workspaceMemberships() {
        return workspaceMemberships;
    }

    @Path("/{workspaceId}/changes")
    public ChangeItemsResource changeItems() {
        return changeItems;
    }

    @Path("/{workspaceId}/document-baselines")
    public DocumentBaselinesResource documentBaselines() {
        return documentBaselines;
    }

    @Path("/{workspaceId}/lov")
    public LOVResource lov() {
        return lov;
    }

    @Path("/{workspaceId}/attributes")
    public AttributesResource attributes() {
        return attributes;
    }
}