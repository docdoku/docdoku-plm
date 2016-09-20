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

package com.docdoku.api;


import com.docdoku.api.client.ApiException;
import com.docdoku.api.models.*;
import com.docdoku.api.models.utils.LastIterationHelper;
import com.docdoku.api.models.utils.UploadDownloadHelper;
import com.docdoku.api.models.utils.WorkflowHelper;
import com.docdoku.api.services.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
    private PartsApi partsApi = new PartsApi(TestConfig.BASIC_CLIENT);
    private DocumentApi documentApi = new DocumentApi(TestConfig.BASIC_CLIENT);
    private FoldersApi foldersApi = new FoldersApi(TestConfig.BASIC_CLIENT);


    @Test
    public void testWorkflowOnWorkspacesWorkflow() throws ApiException {

        // Create a workflow model
        String workflowModelReference = "W_MODEL-" + UUID.randomUUID().toString().substring(0, 8);
        WorkflowModelDTO workflowModel = createModel(workflowModelReference);
        Assert.assertEquals(workflowModel.getId(), workflowModelReference);

        List<RoleMappingDTO> roleMapping = resolveDefaultRoles(workflowModel);

        // Create a workflow container
        WorkspaceWorkflowCreationDTO workspaceWorkflowCreationDTO = new WorkspaceWorkflowCreationDTO();
        workspaceWorkflowCreationDTO.setId(UUID.randomUUID().toString().substring(0, 8));
        workspaceWorkflowCreationDTO.setRoleMapping(roleMapping);
        workspaceWorkflowCreationDTO.setWorkflowModelId(workflowModel.getId());

        WorkspaceWorkflowDTO workspaceWorkflow = workspaceWorkflowsApi.createWorkspaceWorkflow(TestConfig.WORKSPACE, workspaceWorkflowCreationDTO);
        Assert.assertEquals(workspaceWorkflow.getId(),workspaceWorkflowCreationDTO.getId());
        Assert.assertEquals(workspaceWorkflow.getWorkspaceId(),TestConfig.WORKSPACE);
        runAsserts(workspaceWorkflow.getWorkflow(), workflowModel);
        processTask(workspaceWorkflow.getWorkflow(), workflowModel);

        workspaceWorkflowsApi.deleteWorkspaceWorkflow(TestConfig.WORKSPACE, workspaceWorkflow.getId());
    }

    @Test
    public void testWorkflowOnPart() throws ApiException {

        // Create a workflow model
        String workflowModelReference = "W_MODEL-" + UUID.randomUUID().toString().substring(0, 8);
        WorkflowModelDTO workflowModel = createModel(workflowModelReference);
        Assert.assertEquals(workflowModel.getId(), workflowModelReference);

        List<RoleMappingDTO> roleMapping = resolveDefaultRoles(workflowModel);

        // Create a part

        // Create a part
        PartCreationDTO part = new PartCreationDTO();
        part.setNumber(UUID.randomUUID().toString().substring(0, 8));
        part.setWorkflowModelId(workflowModelReference);
        part.setRoleMapping(roleMapping);
        PartRevisionDTO newPart = partsApi.createNewPart(TestConfig.WORKSPACE, part);
        partsApi.checkIn(TestConfig.WORKSPACE, newPart.getNumber(),newPart.getVersion(), "");

        WorkflowDTO workflow = workflowsApi.getWorkflowInstance(TestConfig.WORKSPACE, newPart.getWorkflow().getId());
        Assert.assertEquals(workflow,newPart.getWorkflow());
        runAsserts(newPart.getWorkflow(), workflowModel);

        processTask(newPart.getWorkflow(), workflowModel);


    }

    @Test
    public void testWorkflowOnDocument() throws ApiException {

        // Create a workflow model
        String workflowModelReference = "W_MODEL-" + UUID.randomUUID().toString().substring(0, 8);
        WorkflowModelDTO workflowModel = createModel(workflowModelReference);
        Assert.assertEquals(workflowModel.getId(), workflowModelReference);

        List<RoleMappingDTO> roleMapping = resolveDefaultRoles(workflowModel);

        // Create a document
        DocumentCreationDTO document = new DocumentCreationDTO();
        document.setReference(UUID.randomUUID().toString().substring(0,8));
        document.setWorkflowModelId(workflowModelReference);
        document.setRoleMapping(roleMapping);

        // Upload file
        DocumentRevisionDTO createdDocument = foldersApi.createDocumentMasterInFolder(TestConfig.WORKSPACE, document, TestConfig.WORKSPACE);
        URL fileURL = WorkflowApiTest.class.getClassLoader().getResource("com/docdoku/api/attached-file.zip");
        File file = new File(fileURL.getPath());

        DocumentIterationDTO lastIteration = LastIterationHelper.getLastIteration(createdDocument);
        UploadDownloadHelper.uploadAttachedFile(lastIteration, TestConfig.BASIC_CLIENT, file);
        DocumentRevisionDTO documentRevision = documentApi.getDocumentRevision(TestConfig.WORKSPACE, createdDocument.getDocumentMasterId(), createdDocument.getVersion(), null);
        lastIteration = LastIterationHelper.getLastIteration(documentRevision);
        UploadDownloadHelper.downloadFile(lastIteration.getAttachedFiles().get(0).getFullName(), TestConfig.BASIC_CLIENT);

        // Check in
        documentApi.checkInDocument(TestConfig.WORKSPACE, createdDocument.getDocumentMasterId(), createdDocument.getVersion(), "");

        WorkflowDTO workflow = workflowsApi.getWorkflowInstance(TestConfig.WORKSPACE, createdDocument.getWorkflow().getId());
        Assert.assertEquals(workflow,createdDocument.getWorkflow());

        runAsserts(createdDocument.getWorkflow(), workflowModel);
        processTask(createdDocument.getWorkflow(), workflowModel);

        documentRevision = documentApi.getDocumentRevision(TestConfig.WORKSPACE, createdDocument.getDocumentMasterId(), createdDocument.getVersion(), null);
        Assert.assertTrue("Task is refreshed on document getter", documentRevision.getWorkflow().getActivities().get(0).getTasks().get(0).getStatus().equals(TaskDTO.StatusEnum.APPROVED));

    }

    private void runAsserts(WorkflowDTO workflowDTO, WorkflowModelDTO workflowModel) {
        Assert.assertNotNull(workflowDTO);
        Assert.assertEquals(workflowDTO.getFinalLifeCycleState(),workflowModel.getFinalLifeCycleState());
        Assert.assertNotNull(workflowDTO.getActivities());
        Assert.assertEquals(workflowDTO.getActivities().size(), workflowModel.getActivityModels().size());
        Assert.assertEquals(workflowDTO.getActivities().get(0).getLifeCycleState(), workflowModel.getActivityModels().get(0).getLifeCycleState());
        Assert.assertEquals(workflowDTO.getActivities().get(0).getTasks().size(), workflowModel.getActivityModels().get(0).getTaskModels().size());
        Assert.assertEquals(workflowDTO.getActivities().get(0).getTasks().get(0).getTitle(), workflowModel.getActivityModels().get(0).getTaskModels().get(0).getTitle());
        Assert.assertEquals(workflowDTO.getActivities().get(0).getTasks().get(1).getTitle(), workflowModel.getActivityModels().get(0).getTaskModels().get(1).getTitle());
    }

    private void processTask(WorkflowDTO createdWorkflow, WorkflowModelDTO createdFrom) throws ApiException {

        UserDTO me = usersApi.whoami(TestConfig.WORKSPACE);

        // Retrieve workflow by it's id
        WorkflowDTO workflow = workflowsApi.getWorkflowInstance(TestConfig.WORKSPACE, createdWorkflow.getId());
        Assert.assertEquals(workflow,createdWorkflow);

        // Get first running task, assert our user is in assigned users
        ActivityDTO currentActivity = WorkflowHelper.getCurrentActivity(workflow);
        List<TaskDTO> runningTasks = WorkflowHelper.getRunningTasks(workflow);
        TaskDTO firstTask = runningTasks.get(0);

        Assert.assertTrue(firstTask.getAssignedUsers().contains(me));
        Assert.assertEquals(firstTask.getStatus(), TaskDTO.StatusEnum.IN_PROGRESS);

        // Approve the first task
        TaskProcessDTO taskProcessDTO = new TaskProcessDTO();
        taskProcessDTO.setComment("Okay");
        taskProcessDTO.setSignature("data:image/gif;base64,R0lGODlhAQABAAAAACw=");
        taskProcessDTO.setAction(TaskProcessDTO.ActionEnum.APPROVE);

        String taskId = workflow.getId() + "-" + currentActivity.getStep() + "-" + firstTask.getNum();

        tasksApi.processTask(TestConfig.WORKSPACE, taskId, taskProcessDTO);

        TaskDTO task = tasksApi.getTask(TestConfig.WORKSPACE, taskId);
        Assert.assertEquals(TaskDTO.StatusEnum.APPROVED, task.getStatus());

        workflow = workflowsApi.getWorkflowInstance(TestConfig.WORKSPACE, workflow.getId());

        currentActivity = WorkflowHelper.getCurrentActivity(workflow);
        TaskDTO nextTask = WorkflowHelper.getRunningTasks(currentActivity).get(0);
        Assert.assertEquals("2nd task must be the next running task", createdFrom.getActivityModels().get(0).getTaskModels().get(1).getTitle(),nextTask.getTitle());

    }

    private List<RoleMappingDTO> resolveDefaultRoles(WorkflowModelDTO workflowModel) {
        Set<RoleDTO> rolesInvolved = WorkflowHelper.getRolesInvolved(workflowModel);
        List<RoleMappingDTO> roleMapping = new ArrayList<>();

        // we need to resolve the roles (use defaults assignments)
        for(RoleDTO role:rolesInvolved){

            RoleMappingDTO roleMappingDTO = new RoleMappingDTO();
            roleMappingDTO.setRoleName(role.getName());

            for(UserGroupDTO group :role.getDefaultAssignedGroups()){
                roleMappingDTO.getGroupIds().add(group.getId());
            }
            for(UserDTO user:role.getDefaultAssignedUsers()){
                roleMappingDTO.getUserLogins().add(user.getLogin());
            }
            roleMapping.add(roleMappingDTO);
        }

        return roleMapping;
    }

    public WorkflowModelDTO createModel(String workflowModelReference) throws ApiException {

        String roleName = "ROLE-" + UUID.randomUUID().toString().substring(0, 8);

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
        TaskModelDTO taskModelDTO_1 = new TaskModelDTO();
        taskModelDTO_1.setTitle("TASK_1");
        taskModelDTO_1.setInstructions("Do something please");
        taskModelDTO_1.setNum(0);
        taskModelDTO_1.setRole(createdRole);

        TaskModelDTO taskModelDTO_2 = new TaskModelDTO();
        taskModelDTO_2.setTitle("TASK_2");
        taskModelDTO_2.setInstructions("Do something else");
        taskModelDTO_2.setNum(1);
        taskModelDTO_2.setRole(createdRole);

        List<TaskModelDTO> tasks = new ArrayList<>();
        tasks.add(taskModelDTO_1);
        tasks.add(taskModelDTO_2);

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
        return workflowModel;
    }

}
