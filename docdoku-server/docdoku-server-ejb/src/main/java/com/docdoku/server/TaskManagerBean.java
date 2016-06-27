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
package com.docdoku.server;

import com.docdoku.core.common.User;
import com.docdoku.core.workflow.WorkspaceWorkflow;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.*;
import com.docdoku.core.workflow.Task;
import com.docdoku.core.workflow.TaskKey;
import com.docdoku.core.workflow.TaskWrapper;
import com.docdoku.server.dao.DocumentRevisionDAO;
import com.docdoku.server.dao.PartRevisionDAO;
import com.docdoku.server.dao.TaskDAO;
import com.docdoku.server.dao.WorkflowDAO;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author Morgan Guimard
 */
@DeclareRoles({UserGroupMapping.REGULAR_USER_ROLE_ID})
@Local(ITaskManagerLocal.class)
@Stateless(name = "TaskManagerBean")
public class TaskManagerBean implements ITaskManagerLocal {

    @PersistenceContext
    private EntityManager em;

    @Inject
    private IUserManagerLocal userManager;

    @Inject
    private IDocumentWorkflowManagerLocal documentWorkflowService;

    @Inject
    private IPartWorkflowManagerLocal partWorkflowService;

    @Inject
    private IWorkflowManagerLocal workflowService;

    @Override
    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    public TaskWrapper[] getAssignedTasksForGivenUser(String workspaceId, String userLogin) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        TaskDAO taskDAO = new TaskDAO(new Locale(user.getLanguage()), em);
        Task[] assignedTasks = taskDAO.findAssignedTasks(workspaceId, userLogin);
        List<TaskWrapper> taskWrappers = new ArrayList<>();
        for(Task task : assignedTasks){
            taskWrappers.add(wrapTask(task,workspaceId));
        }
        return taskWrappers.toArray(new TaskWrapper[taskWrappers.size()]);
    }

    @Override
    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    public TaskWrapper[] getInProgressTasksForGivenUser(String workspaceId, String userLogin) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        TaskDAO taskDAO = new TaskDAO(new Locale(user.getLanguage()), em);
        Task[] inProgressTasks = taskDAO.findInProgressTasks(workspaceId, userLogin);
        List<TaskWrapper> taskWrappers = new ArrayList<>();
        for(Task task : inProgressTasks){
            taskWrappers.add(wrapTask(task,workspaceId));
        }
        return taskWrappers.toArray(new TaskWrapper[taskWrappers.size()]);
    }

    @Override
    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    public TaskWrapper getTask(String workspaceId, TaskKey taskKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, TaskNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        TaskDAO taskDAO = new TaskDAO(new Locale(user.getLanguage()), em);
        Task task = taskDAO.loadTask(taskKey);
        TaskWrapper taskWrapper = wrapTask(task, workspaceId);
        if(taskWrapper == null){
            throw new AccessRightException(new Locale(user.getLanguage()),user);
        }
        return taskWrapper;
    }

    @Override
    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    public void processTask(String workspaceId, TaskKey taskKey, String action, String comment, String signature) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, TaskNotFoundException, NotAllowedException, WorkflowNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        TaskDAO taskDAO = new TaskDAO(new Locale(user.getLanguage()), em);
        Task task = taskDAO.loadTask(taskKey);
        TaskWrapper taskWrapper = wrapTask(task, workspaceId);
        switch (taskWrapper.getHolderType()){
            case "documents":
                    if("approve".equals(action)){
                        documentWorkflowService.approveTaskOnDocument(workspaceId,taskKey,comment,signature);
                    }
                    else if("reject".equals(action)){
                        documentWorkflowService.rejectTaskOnDocument(workspaceId, taskKey, comment, signature);
                    }
                break;
            case "parts":
                if("approve".equals(action)){
                    partWorkflowService.approveTaskOnPart(workspaceId,taskKey,comment,signature);
                }
                else if("reject".equals(action)){
                    partWorkflowService.rejectTaskOnPart(workspaceId, taskKey, comment, signature);
                }
                break;
            case "workspace-workflows":
                if("approve".equals(action)){
                    workflowService.approveTaskOnWorkspaceWorkflow(workspaceId, taskKey, comment, signature);
                }
                else if("reject".equals(action)){
                    workflowService.rejectTaskOnWorkspaceWorkflow(workspaceId, taskKey, comment, signature);
                }
                break;
            default:
                // should throw
                break;
        }

    }



    private TaskWrapper wrapTask(Task task, String workspaceId){
        TaskWrapper taskWrapper = new TaskWrapper(task, workspaceId);

        DocumentRevisionDAO documentRevisionDAO = new DocumentRevisionDAO(em);
        DocumentRevision documentRevision = documentRevisionDAO.getWorkflowHolder(task.getActivity().getWorkflow());

        if(documentRevision!=null){
            taskWrapper.setHolderType("documents");
            taskWrapper.setHolderReference(documentRevision.getDocumentMasterId());
            taskWrapper.setHolderVersion(documentRevision.getVersion());
            return taskWrapper;
        }
        PartRevisionDAO PartRevisionDAO = new PartRevisionDAO(em);
        PartRevision partRevision = PartRevisionDAO.getWorkflowHolder(task.getActivity().getWorkflow());

        if(partRevision!=null){
            taskWrapper.setHolderType("parts");
            taskWrapper.setHolderReference(partRevision.getPartNumber());
            taskWrapper.setHolderVersion(partRevision.getVersion());
            return taskWrapper;
        }

        WorkspaceWorkflow workspaceWorkflowTarget = new WorkflowDAO(em).getWorkspaceWorkflowTarget(workspaceId,task.getActivity().getWorkflow());
        if(workspaceWorkflowTarget!=null){
            taskWrapper.setHolderType("workspace-workflows");
            taskWrapper.setHolderReference(workspaceWorkflowTarget.getId());
            return taskWrapper;
        }

        return null;
    }

}
