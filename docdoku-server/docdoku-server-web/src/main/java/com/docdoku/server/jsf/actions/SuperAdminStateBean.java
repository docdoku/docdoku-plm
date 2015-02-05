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

package com.docdoku.server.jsf.actions;

import com.docdoku.core.common.Workspace;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.core.services.IWorkspaceManagerLocal;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.Serializable;

@Named("superAdminStateBean")
@SessionScoped
public class SuperAdminStateBean implements Serializable {

    @EJB
    private IWorkspaceManagerLocal workspaceService;
    @EJB
    private IDocumentManagerLocal documentService;
    @EJB
    private IProductManagerLocal productService;
    @EJB
    private IUserManagerLocal userManager;

    public SuperAdminStateBean(){
    }

    public JsonObject getDiskSpaceUsageStats() throws AccountNotFoundException {

        JsonObjectBuilder diskUsage = Json.createObjectBuilder();

        Workspace[] allWorkspaces = userManager.getAdministratedWorkspaces();

        for(Workspace workspace:allWorkspaces){
            long workspaceDiskUsage = workspaceService.getDiskUsageInWorkspace(workspace.getId());
            diskUsage.add(workspace.getId(), workspaceDiskUsage);
        }

        return diskUsage.build();

    }

    public JsonObject getUsersStats() throws AccountNotFoundException, WorkspaceNotFoundException, AccessRightException, UserNotFoundException, UserNotActiveException {

        JsonObjectBuilder userStats = Json.createObjectBuilder();

        Workspace[] allWorkspaces = userManager.getAdministratedWorkspaces();

        for(Workspace workspace:allWorkspaces){
            int userCount =  documentService.getUsers(workspace.getId()).length;
            userStats.add(workspace.getId(), userCount);
        }

        return userStats.build();

    }

    public JsonObject getDocsStats() throws AccountNotFoundException, WorkspaceNotFoundException, AccessRightException {

        JsonObjectBuilder docStats = Json.createObjectBuilder();

        Workspace[] allWorkspaces = userManager.getAdministratedWorkspaces();

        for(Workspace workspace:allWorkspaces){
            int documentsCount = documentService.getTotalNumberOfDocuments(workspace.getId());
            docStats.add(workspace.getId(), documentsCount);
        }

        return docStats.build();

    }

    public JsonObject getProductsStats() throws AccountNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {

        JsonObjectBuilder productsStats = Json.createObjectBuilder();

        Workspace[] allWorkspaces = userManager.getAdministratedWorkspaces();

        for(Workspace workspace:allWorkspaces){
            int productsCount = productService.getConfigurationItems(workspace.getId()).size();
            productsStats.add(workspace.getId(), productsCount);
        }

        return productsStats.build();

    }

    public JsonObject getPartsStats() throws AccountNotFoundException, AccessRightException, WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {

        JsonObjectBuilder partsStats = Json.createObjectBuilder();

        Workspace[] allWorkspaces = userManager.getAdministratedWorkspaces();

        for(Workspace workspace:allWorkspaces){
            int productsCount = productService.getTotalNumberOfParts(workspace.getId());
            partsStats.add(workspace.getId(), productsCount);
        }

        return partsStats.build();

    }


}
