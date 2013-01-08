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
    private DocumentTemplateResource templates;

    @EJB
    private ProductResource products;

    @EJB
    private TagResource tags;

    @EJB
    private WorkflowResource workflows;

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

    @Path("/templates")
    public DocumentTemplateResource templates() {
        return templates;
    }

    @Path("/products")
    public ProductResource products() {
        return products;
    }

    @Path("/tags")
    public TagResource tags() {
        return tags;
    }

    @Path("/workflows")
    public WorkflowResource workflows() {
        return workflows;
    }

}