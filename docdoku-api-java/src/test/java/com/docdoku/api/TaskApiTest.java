/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
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

package com.docdoku.api;


import com.docdoku.api.client.ApiException;
import com.docdoku.api.models.*;
import com.docdoku.api.models.utils.WorkflowHelper;
import com.docdoku.api.services.*;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;

@RunWith(JUnit4.class)
public class TaskApiTest {

    private WorkspacesApi workspacesApi = new WorkspacesApi(TestConfig.REGULAR_USER_CLIENT);
    private RolesApi rolesApi = new RolesApi(TestConfig.REGULAR_USER_CLIENT);
    private UsersApi usersApi = new UsersApi(TestConfig.REGULAR_USER_CLIENT);
    private WorkflowModelsApi workflowModelsApi = new WorkflowModelsApi(TestConfig.REGULAR_USER_CLIENT);
    private WorkspaceWorkflowsApi workspaceWorkflowsApi = new WorkspaceWorkflowsApi(TestConfig.REGULAR_USER_CLIENT);
    private TasksApi tasksApi = new TasksApi(TestConfig.REGULAR_USER_CLIENT);
    private static WorkspaceDTO workspace;

    @BeforeClass
    public static void initWorkspace() throws ApiException {
        workspace = TestUtils.createWorkspace();
    }
    @AfterClass
    public static void deleteWorkspace() throws ApiException {
        TestUtils.deleteWorkspace(workspace);
    }

    @Test
    public void tests() throws ApiException {

        String workflowModelReference = "W_MODEL-" + TestUtils.randomString();
        String roleName = "ROLE-" + TestUtils.randomString();
        String groupId = "GROUP-" + TestUtils.randomString();

        // Get current user to create a role with default assignee
        UserDTO user = usersApi.whoAmI(workspace.getId());


        UserGroupDTO userGroup = new UserGroupDTO();
        userGroup.setWorkspaceId(workspace.getId());
        userGroup.setId(groupId);

        workspacesApi.createGroup(workspace.getId(),userGroup);
        workspacesApi.addUser(workspace.getId(),user, userGroup.getId());

        RoleDTO role = new RoleDTO();
        role.setWorkspaceId(workspace.getId());
        role.setName(roleName);
        // Assign default assigned users
        role.getDefaultAssignedUsers().add(user);


        RoleDTO createdRole = rolesApi.createRole(workspace.getId(), role);

        // Create a task model
        TaskModelDTO taskModelDTO = new TaskModelDTO();
        taskModelDTO.setTitle("TASK_1");
        taskModelDTO.setInstructions("Do something please");
        taskModelDTO.setNum(0);
        taskModelDTO.setRole(createdRole);

        List<TaskModelDTO> tasks = new ArrayList<>();
        tasks.add(taskModelDTO);

        // Create an activity model
        ActivityModelDTO activityModelDTO = new ActivityModelDTO();
        activityModelDTO.setLifeCycleState("ACTIVITY_1");
        activityModelDTO.setStep(0);
        activityModelDTO.setType(ActivityModelDTO.TypeEnum.SEQUENTIAL);
        activityModelDTO.setTaskModels(tasks);

        List<ActivityModelDTO> activityModels = new ArrayList<>();
        activityModels.add(activityModelDTO);

        // Create a workflow model
        WorkflowModelDTO workflowModelDTO = new WorkflowModelDTO();
        workflowModelDTO.setReference(workflowModelReference);
        workflowModelDTO.setFinalLifeCycleState("FINAL_STATE");
        workflowModelDTO.setActivityModels(activityModels);


        WorkflowModelDTO workflowModel = workflowModelsApi.createWorkflowModel(workspace.getId(), workflowModelDTO);
        Assert.assertEquals(workflowModel.getId(), workflowModelReference);

        // Use this model to create a workflow

        // We need to assigned users and or groups
        List<RoleMappingDTO> roleMapping = new ArrayList<>();
        RoleMappingDTO roleMappingDTO = new RoleMappingDTO();
        roleMappingDTO.setRoleName(role.getName());
        roleMappingDTO.getGroupIds().add(groupId);
        roleMapping.add(roleMappingDTO);

        // Create a workflow container
        WorkspaceWorkflowCreationDTO workspaceWorkflowCreationDTO = new WorkspaceWorkflowCreationDTO();
        workspaceWorkflowCreationDTO.setId(TestUtils.randomString());
        workspaceWorkflowCreationDTO.setRoleMapping(roleMapping);
        workspaceWorkflowCreationDTO.setWorkflowModelId(workflowModel.getId());

        WorkspaceWorkflowDTO workspaceWorkflow = workspaceWorkflowsApi.createWorkspaceWorkflow(workspace.getId(), workspaceWorkflowCreationDTO);
        List<TaskDTO> assignedTasks = tasksApi.getAssignedTasksForGivenUser(workspace.getId(), TestConfig.LOGIN);
        Assert.assertEquals(1,
                assignedTasks.stream()
                .filter(taskDTO -> taskDTO.getWorkflowId() != null &&
                        taskDTO.getWorkflowId().equals(workspaceWorkflow.getWorkflow().getId()))
                .count());

        ActivityDTO currentActivity = WorkflowHelper.getCurrentActivity(workspaceWorkflow.getWorkflow());
        List<TaskDTO> runningTasks = WorkflowHelper.getRunningTasks(currentActivity);
        TaskDTO firstRunningTasks = runningTasks.get(0);
        Assert.assertNotNull(firstRunningTasks);

        String taskId = workspaceWorkflow.getWorkflow().getId() + "-" + currentActivity.getStep() + "-" + firstRunningTasks.getNum();


        TaskProcessDTO taskProcessDTO = new TaskProcessDTO();
        taskProcessDTO.setAction(TaskProcessDTO.ActionEnum.APPROVE);
        taskProcessDTO.setComment("Test are passing !");
        tasksApi.processTask(workspace.getId(), taskId, taskProcessDTO);


    }


}
