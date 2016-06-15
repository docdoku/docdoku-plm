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
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.exceptions.TaskNotFoundException;
import com.docdoku.core.exceptions.UserNotActiveException;
import com.docdoku.core.exceptions.UserNotFoundException;
import com.docdoku.core.exceptions.WorkspaceNotFoundException;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.ITaskManagerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.core.workflow.Task;
import com.docdoku.core.workflow.TaskKey;
import com.docdoku.core.workflow.TaskWrapper;
import com.docdoku.server.dao.DocumentRevisionDAO;
import com.docdoku.server.dao.PartRevisionDAO;
import com.docdoku.server.dao.TaskDAO;

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
    public TaskWrapper getTask(String workspaceId, TaskKey taskKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, TaskNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        TaskDAO taskDAO = new TaskDAO(new Locale(user.getLanguage()), em);
        Task task = taskDAO.loadTask(taskKey);
        return wrapTask(task,workspaceId);
    }
    
    private TaskWrapper wrapTask(Task task, String workspaceId){
        TaskWrapper taskWrapper = new TaskWrapper(task, workspaceId);

        DocumentRevisionDAO documentRevisionDAO = new DocumentRevisionDAO(em);
        DocumentRevision documentRevision = documentRevisionDAO.getTaskHolder(task);

        if(documentRevision!=null){
            taskWrapper.setHolderType("document");
            taskWrapper.setHolderReference(documentRevision.getDocumentMasterId());
            taskWrapper.setHolderVersion(documentRevision.getVersion());
            return taskWrapper;
        }
        PartRevisionDAO PartRevisionDAO = new PartRevisionDAO(em);
        PartRevision partRevision = PartRevisionDAO.getTaskHolder(task);

        if(partRevision!=null){
            taskWrapper.setHolderType("part");
            taskWrapper.setHolderReference(partRevision.getPartNumber());
            taskWrapper.setHolderVersion(partRevision.getVersion());
            return taskWrapper;
        }

        return taskWrapper;
    }

}
