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

package com.docdoku.server.jsf.actions;

import com.docdoku.core.common.Workspace;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.services.*;
import org.codehaus.jettison.json.JSONObject;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@ManagedBean(name = "superAdminStateBean")
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

    public JSONObject getDiskSpaceUsageStats() throws AccountNotFoundException, UserNotFoundException {

        Map<String, Long> diskUsage = new HashMap<String,Long>();

        Workspace[] allWorkspaces = userManager.getAdministratedWorkspaces();

        for(Workspace workspace:allWorkspaces){
            Long workspaceDiskUsage = workspaceService.getDiskUsageInWorkspace(workspace.getId());
            diskUsage.put(workspace.getId(), workspaceDiskUsage);
        }

        return new JSONObject(diskUsage);

    }

    public JSONObject getUsersStats() throws WorkspaceNotFoundException, UserNotActiveException, UserNotFoundException, AccountNotFoundException {

        Map<String, Integer> userStats = new HashMap<String,Integer>();

        Workspace[] allWorkspaces = userManager.getAdministratedWorkspaces();

        for(Workspace workspace:allWorkspaces){
            int userCount =  documentService.getUsers(workspace.getId()).length;
            userStats.put(workspace.getId(), userCount);
        }

        return new JSONObject(userStats);

    }

    public JSONObject getDocsStats() throws AccountNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {

        Map<String, Integer> docStats = new HashMap<String,Integer>();

        Workspace[] allWorkspaces = userManager.getAdministratedWorkspaces();

        for(Workspace workspace:allWorkspaces){
            int documentsCount = documentService.getDocumentsCountInWorkspace(workspace.getId());
            docStats.put(workspace.getId(), documentsCount);
        }

        return new JSONObject(docStats);

    }

    public JSONObject getProductsStats() throws AccountNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {

        Map<String, Integer> productsStats = new HashMap<String,Integer>();

        Workspace[] allWorkspaces = userManager.getAdministratedWorkspaces();

        for(Workspace workspace:allWorkspaces){
            int productsCount = productService.getConfigurationItems(workspace.getId()).size();
            productsStats.put(workspace.getId(), productsCount);
        }

        return new JSONObject(productsStats);

    }

    public JSONObject getPartsStats() throws AccountNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException, WorkspaceNotFoundException {

        Map<String, Integer> partsStats = new HashMap<String,Integer>();

        Workspace[] allWorkspaces = userManager.getAdministratedWorkspaces();

        for(Workspace workspace:allWorkspaces){
            int productsCount = productService.getPartMastersCount(workspace.getId());
            partsStats.put(workspace.getId(), productsCount);
        }

        return new JSONObject(partsStats);

    }


}
