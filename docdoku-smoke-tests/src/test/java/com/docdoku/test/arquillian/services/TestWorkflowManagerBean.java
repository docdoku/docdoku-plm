/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2014 DocDoku SARL
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

package com.docdoku.test.arquillian.services;


import com.docdoku.core.change.ChangeItem;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.services.IWorkflowManagerLocal;
import com.docdoku.core.workflow.ActivityModel;
import com.docdoku.core.workflow.Role;
import com.docdoku.core.workflow.WorkflowModel;
import com.docdoku.core.workflow.WorkflowModelKey;
import com.sun.enterprise.security.ee.auth.login.ProgrammaticLogin;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;


/**
 * @author Asmae CHADID
 */

@LocalBean
@Stateless
public class TestWorkflowManagerBean {

    @EJB
    private IWorkflowManagerLocal workflowManagerLocal;

    private ProgrammaticLogin loginP = new ProgrammaticLogin();
    private String password = "password";


    public WorkflowModel createWorkflowModel(String login, String pWorkspaceId, String pId, String pFinalLifeCycleState, ActivityModel[] pActivityModels) throws UserNotActiveException, WorkspaceNotFoundException, AccessRightException, CreationException, FolderNotFoundException, UserNotFoundException, FolderAlreadyExistsException, NotAllowedException, WorkflowModelAlreadyExistsException {
        loginP.login(login, password.toCharArray());
        WorkflowModel workflowModel = null;
        for (WorkflowModel workflow : workflowManagerLocal.getWorkflowModels(pWorkspaceId)) {
            if (workflow.getKey().equals(new WorkflowModelKey(pWorkspaceId, pId)))
                workflowModel = workflow;
        }
        if (workflowModel == null) {
            workflowModel= workflowManagerLocal.createWorkflowModel(pWorkspaceId, pId, pFinalLifeCycleState, pActivityModels);
        }
        loginP.logout();
        return workflowModel;
    }

    public WorkflowModel[] getWorkflows(String login, String pWorkspaceId) throws UserNotActiveException, WorkspaceNotFoundException, AccessRightException, CreationException, FolderNotFoundException, UserNotFoundException, FolderAlreadyExistsException, NotAllowedException, WorkflowModelAlreadyExistsException {
        loginP.login(login, password.toCharArray());
        WorkflowModel[] workflowModel = workflowManagerLocal.getWorkflowModels(pWorkspaceId);
        loginP.logout();
        return workflowModel;
    }

    public Role[] getWorkspaceRoles(String login, String pWorkspaceId) throws WorkspaceNotFoundException, UserNotActiveException, UserNotFoundException {
        loginP.login(login, password.toCharArray());
        Role[] roles = workflowManagerLocal.getRoles(pWorkspaceId);
        loginP.logout();
        return roles;
    }

    public Role createRole(String login, String roleName, String workspace, String userlogin) throws UserNotActiveException, WorkspaceNotFoundException, CreationException, UserNotFoundException, AccessRightException, RoleNotFoundException, EntityConstraintException, RoleAlreadyExistsException {
        loginP.login(login, password.toCharArray());
        Role roleExist = null, role = null;
        for (Role pRole : workflowManagerLocal.getRoles(workspace)) {
            if (pRole.getName().equals(roleName)) {
                roleExist = pRole;
            }
        }
        if (roleExist == null) {
            role = workflowManagerLocal.createRole(roleName, workspace, userlogin);
        }
        loginP.logout();
        return role;
    }


    public void createRequest(String login, String pWorkspaceId, String name, String description, int milestone, ChangeItem.Priority priority, String assignee, ChangeItem.Category category) {
        loginP.login(login, password.toCharArray());

        loginP.logout();

    }

}