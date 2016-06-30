package com.docdoku.api;


import com.docdoku.api.client.ApiException;
import com.docdoku.api.models.*;
import com.docdoku.api.services.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RunWith(JUnit4.class)
public class WorkflowApiTest {

    private WorkspacesApi workspacesApi = new WorkspacesApi(TestConfig.BASIC_CLIENT);
    private RolesApi rolesApi = new RolesApi(TestConfig.BASIC_CLIENT);
    private UsersApi usersApi = new UsersApi(TestConfig.BASIC_CLIENT);
    private WorkflowModelsApi workflowModelsApi = new WorkflowModelsApi(TestConfig.BASIC_CLIENT);
    private WorkspaceWorkflowsApi workspaceWorkflowsApi = new WorkspaceWorkflowsApi(TestConfig.BASIC_CLIENT);
    private WorkflowsApi workflowsApi = new WorkflowsApi(TestConfig.BASIC_CLIENT);
    private TasksApi tasksApi = new TasksApi(TestConfig.BASIC_CLIENT);

    @Test
    public void tests() throws ApiException {

        String workflowModelReference = "W_MODEL-" + UUID.randomUUID().toString().substring(0, 6);
        String roleName = "ROLE-" + UUID.randomUUID().toString().substring(0, 6);

        // Get current user to create a role with default assignee
        UserDTO user = usersApi.whoami(TestConfig.WORKSPACE);
        List<UserGroupDTO> userGroups = workspacesApi.getUserGroups(TestConfig.WORKSPACE);

        RoleDTO role = new RoleDTO();
        role.setWorkspaceId(TestConfig.WORKSPACE);
        role.setName(roleName);

        // Assign default assigned users
        role.getDefaultAssignedUsers().add(user);

        // Assign default assigned groups
        if(!userGroups.isEmpty()) {
            role.getDefaultAssignedGroups().add(userGroups.get(0));
        }

        RoleDTO createdRole = rolesApi.createRole(TestConfig.WORKSPACE, role);

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


        WorkflowModelDTO workflowModel = workflowModelsApi.createWorkflowModel(TestConfig.WORKSPACE, workflowModelDTO);
        Assert.assertEquals(workflowModel.getId(), workflowModelReference);

        // Use this model to create a workflow

        // We need to assigned users and or groups
        List<RoleMappingDTO> roleMapping = new ArrayList<>();
        RoleMappingDTO roleMappingDTO = new RoleMappingDTO();
        roleMappingDTO.setRoleName(role.getName());
        roleMappingDTO.getUserLogins().add(TestConfig.LOGIN);
        roleMapping.add(roleMappingDTO);

        // Create a workflow container
        WorkspaceWorkflowCreationDTO workspaceWorkflowCreationDTO = new WorkspaceWorkflowCreationDTO();
        workspaceWorkflowCreationDTO.setId(UUID.randomUUID().toString().substring(0, 6));
        workspaceWorkflowCreationDTO.setRoleMapping(roleMapping);
        workspaceWorkflowCreationDTO.setWorkflowModelId(workflowModel.getId());


        WorkspaceWorkflowDTO workspaceWorkflow = workspaceWorkflowsApi.createWorkspaceWorkflow(TestConfig.WORKSPACE, workspaceWorkflowCreationDTO);
        Assert.assertEquals(workspaceWorkflow.getId(),workspaceWorkflowCreationDTO.getId());
        Assert.assertEquals(workspaceWorkflow.getWorkspaceId(),TestConfig.WORKSPACE);
        Assert.assertNotNull(workspaceWorkflow.getWorkflow());

        // Get the workflow instance generated id (int) Farm star should use this id in its model
        Integer id = workspaceWorkflow.getWorkflow().getId();

        Assert.assertEquals(workspaceWorkflow.getWorkflow().getFinalLifeCycleState(),"FINAL_STATE");
        Assert.assertNotNull(workspaceWorkflow.getWorkflow().getActivities());
        Assert.assertEquals(workspaceWorkflow.getWorkflow().getActivities().size(), 1);
        Assert.assertEquals(workspaceWorkflow.getWorkflow().getActivities().get(0).getLifeCycleState(), "ACTIVITY_1");
        Assert.assertEquals(workspaceWorkflow.getWorkflow().getActivities().get(0).getTasks().size(), 1);
        Assert.assertEquals(workspaceWorkflow.getWorkflow().getActivities().get(0).getTasks().get(0).getTitle(), "TASK_1");

        // Retrieve workflow by it's id
        WorkflowDTO workflowInstance = workflowsApi.getWorkflowInstance(TestConfig.WORKSPACE, id);
        Assert.assertEquals(workflowInstance,workspaceWorkflow.getWorkflow());

        // Get first task, assert our user is in assigned users
        int activityStep = 0; int taskNum = 0;
        TaskDTO firstTask = workflowInstance.getActivities().get(0).getTasks().get(0);
        List<UserDTO> assignedUsers = firstTask.getAssignedUsers();
        Assert.assertTrue(assignedUsers.contains(user));
        Assert.assertEquals(firstTask.getStatus(), TaskDTO.StatusEnum.IN_PROGRESS);

        // Approve the first task
        TaskProcessDTO taskProcessDTO = new TaskProcessDTO();
        taskProcessDTO.setComment("Okay");
        taskProcessDTO.setSignature("data:image/gif;base64,R0lGODlhAQABAAAAACw=");
        taskProcessDTO.setAction(TaskProcessDTO.ActionEnum.APPROVE);

        String taskId = workflowInstance.getId() + "-" + activityStep + "-" + taskNum;
        tasksApi.processTask(TestConfig.WORKSPACE, taskId, taskProcessDTO);


    }


}
