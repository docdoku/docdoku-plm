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
package com.docdoku.core.services;

import com.docdoku.core.common.User;
import com.docdoku.core.common.UserGroup;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.workflow.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Florent Garin
 */
public interface IWorkflowManagerLocal {

    void deleteWorkflowModel(WorkflowModelKey pKey) throws WorkspaceNotFoundException, AccessRightException, WorkflowModelNotFoundException, UserNotFoundException, UserNotActiveException, EntityConstraintException;
    WorkflowModel getWorkflowModel(WorkflowModelKey pKey) throws WorkspaceNotFoundException, WorkflowModelNotFoundException, UserNotFoundException, UserNotActiveException;
    WorkflowModel[] getWorkflowModels(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;
    WorkflowModel createWorkflowModel(String pWorkspaceId, String pId, String pFinalLifeCycleState, ActivityModel[] pActivityModels) throws WorkspaceNotFoundException, AccessRightException, WorkflowModelAlreadyExistsException, UserNotFoundException, CreationException, NotAllowedException;
    WorkflowModel updateWorkflowModel(WorkflowModelKey workflowModelKey, String pFinalLifeCycleState, ActivityModel[] pActivityModels) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, AccessRightException, WorkflowModelNotFoundException, NotAllowedException, WorkflowModelAlreadyExistsException, CreationException;

    Role[] getRoles(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;
    Role[] getRolesInUse(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException;
    Role createRole(String roleName, String workspaceId, List<String> userLogins, List<String> userGroupIds) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException, RoleAlreadyExistsException, CreationException, UserGroupNotFoundException;
    Role updateRole(RoleKey roleKey, List<String> userLogins, List<String> userGroupIds) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException, RoleNotFoundException, UserGroupNotFoundException;
    void deleteRole(RoleKey roleKey) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException, RoleNotFoundException, EntityConstraintException;
    void removeUserFromAllRoleMappings(User pUser) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException;
    void removeUserGroupFromAllRoleMappings(UserGroup pUserGroup) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException;

    void removeACLFromWorkflow(String pWorkspaceId, String workflowModelId) throws WorkflowNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkflowModelNotFoundException, AccessRightException;
    WorkflowModel updateACLForWorkflow(String pWorkspaceId, String workflowModelId, Map<String, String> userEntries, Map<String, String> groupEntries) throws WorkflowNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkflowModelNotFoundException, AccessRightException;

    WorkspaceWorkflow instantiateWorkflow(String workspaceId, String id, String workflowModelId, Map<String, Collection<String>> userRoleMapping, Map<String, Collection<String>> groupRoleMapping) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, RoleNotFoundException, WorkflowModelNotFoundException, NotAllowedException, UserGroupNotFoundException;

    Workflow getWorkflow(String workspaceId, int workflowId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException;

    WorkspaceWorkflow getWorkspaceWorkflow(String workspaceId, String workspaceWorkflowId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkflowNotFoundException;

    WorkspaceWorkflow[] getWorkspaceWorkflowList(String workspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;

    Workflow[] getWorkflowAbortedWorkflows(String workspaceId, int workflowId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException;

    void approveTaskOnWorkspaceWorkflow(String workspaceId, TaskKey taskKey, String comment, String signature) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, TaskNotFoundException, AccessRightException, WorkflowNotFoundException, NotAllowedException;

    void rejectTaskOnWorkspaceWorkflow(String workspaceId, TaskKey taskKey, String comment, String signature) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, TaskNotFoundException, AccessRightException, WorkflowNotFoundException, NotAllowedException;

    void deleteWorkspaceWorkflow(String workspaceId, String workspaceWorkflowId) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException;
}
