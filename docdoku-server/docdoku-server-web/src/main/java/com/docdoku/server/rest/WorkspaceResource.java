/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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

import com.docdoku.core.security.UserGroupMapping;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Path;

@Stateless
@Path("workspaces/{workspaceId}")
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class WorkspaceResource {

    @EJB
    private DocumentsResource documents;

    @EJB
    private FolderResource folders;

    @EJB
    private DocumentTemplateResource docTemplates;

    @EJB
    private PartTemplateResource partTemplates;

    @EJB
    private ProductResource products;

    @EJB
    private PartsResource parts;

    @EJB
    private TagResource tags;

    @EJB
    private CheckedOutResource checkedOuts;

    @EJB
    private TaskResource tasks;

    @EJB
    private SearchResource searches;

    @EJB
    private WorkflowResource workflows;

    @EJB
    private UserResource users;

    public WorkspaceResource() {
    }

    @Path("/documents")
    public DocumentsResource documents() {
        return documents;
    }

    @Path("/folders") 
    public FolderResource folders() {
        return folders;
    }

    @Path("/document-templates")
    public DocumentTemplateResource docTemplates() {
        return docTemplates;
    }

    @Path("/part-templates")
    public PartTemplateResource partTemplates() {
        return partTemplates;
    }

    @Path("/products")
    public ProductResource products() {
        return products;
    }

    @Path("/parts")
    public PartsResource parts() {
        return parts;
    }

    @Path("/tags")
    public TagResource tags() {
        return tags;
    }

    @Path("/checkedouts")
    public CheckedOutResource checkedOuts() {
        return checkedOuts;
    }

    @Path("/search")
    public SearchResource search() {
        return searches;
    }

    @Path("/tasks")
    public TaskResource task() {
        return tasks;
    }

    @Path("/workflows")
    public WorkflowResource workflows() {
        return workflows;
    }

    @Path("/users")
    public UserResource users() {
        return users;
    }

}