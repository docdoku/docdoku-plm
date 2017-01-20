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
package com.docdoku.server.products;

import com.docdoku.core.common.User;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.product.PartRevisionKey;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IMailerLocal;
import com.docdoku.core.services.IPartWorkflowManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.core.workflow.Activity;
import com.docdoku.core.workflow.Task;
import com.docdoku.core.workflow.TaskKey;
import com.docdoku.core.workflow.Workflow;
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
import java.util.Collection;
import java.util.List;
import java.util.Locale;

@DeclareRoles({UserGroupMapping.REGULAR_USER_ROLE_ID,UserGroupMapping.ADMIN_ROLE_ID})
@Local(IPartWorkflowManagerLocal.class)
@Stateless(name = "PartWorkflowManagerBean")
public class PartWorkflowManagerBean implements IPartWorkflowManagerLocal {

    @PersistenceContext
    private EntityManager em;

    @Inject
    private IUserManagerLocal userManager;

    @Inject
    private IProductManagerLocal productManager;

    @Inject
    private IMailerLocal mailer;

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public Workflow getCurrentWorkflow(PartRevisionKey partRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartRevisionNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(partRevisionKey.getPartMaster().getWorkspace());
        if(!productManager.canUserAccess(user, partRevisionKey)) {
            throw new AccessRightException(new Locale(user.getLanguage()), user);
        }

        Locale locale = new Locale(user.getLanguage());
        PartRevision partR = new PartRevisionDAO(locale, em).loadPartR(partRevisionKey);
        return partR.getWorkflow();
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public Workflow[] getAbortedWorkflow(PartRevisionKey partRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartRevisionNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(partRevisionKey.getPartMaster().getWorkspace());
        if(!productManager.canUserAccess(user, partRevisionKey)) {
            throw new AccessRightException(new Locale(user.getLanguage()), user);
        }

        Locale locale = new Locale(user.getLanguage());
        PartRevision partR = new PartRevisionDAO(locale, em).loadPartR(partRevisionKey);
        List<Workflow> abortedWorkflowList= partR.getAbortedWorkflows();

        return abortedWorkflowList.toArray(new Workflow[abortedWorkflowList.size()]);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartRevision approveTaskOnPart(String pWorkspaceId, TaskKey pTaskKey, String pComment, String pSignature) throws WorkspaceNotFoundException, TaskNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException, WorkflowNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);

        Task task = new TaskDAO(new Locale(user.getLanguage()), em).loadTask(pTaskKey);
        Workflow workflow = task.getActivity().getWorkflow();
        PartRevision partRevision = checkTaskAccess(user,task);
        task = partRevision.getWorkflow().getTasks().stream().filter(pTask -> pTask.getKey().equals(pTaskKey)).findFirst().get();

        task.approve(user,pComment, partRevision.getLastIteration().getIteration(), pSignature);

        Collection<Task> runningTasks = workflow.getRunningTasks();
        for (Task runningTask : runningTasks) {
            runningTask.start();
        }
        em.flush();
        mailer.sendApproval(runningTasks, partRevision);
        return partRevision;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartRevision rejectTaskOnPart(String pWorkspaceId, TaskKey pTaskKey, String pComment, String pSignature) throws WorkspaceNotFoundException, TaskNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException, WorkflowNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);

        Task task = new TaskDAO(new Locale(user.getLanguage()), em).loadTask(pTaskKey);
        PartRevision partRevision = checkTaskAccess(user,task);
        task = partRevision.getWorkflow().getTasks().stream().filter(pTask -> pTask.getKey().equals(pTaskKey)).findFirst().get();

        task.reject(user,pComment, partRevision.getLastIteration().getIteration(), pSignature);

        // Relaunch Workflow ?
        Activity currentActivity = task.getActivity();
        Activity relaunchActivity = currentActivity.getRelaunchActivity();

        if(currentActivity.isStopped() && relaunchActivity != null){
            relaunchWorkflow(partRevision,relaunchActivity.getStep());
            em.flush();
            // Send mails for running tasks
            mailer.sendApproval(partRevision.getWorkflow().getRunningTasks(), partRevision);
            // Send notification for relaunch
            mailer.sendPartRevisionWorkflowRelaunchedNotification(partRevision);
        }
        return partRevision;
    }

    /**
     * Check if a user can approve or reject a task
     * @param user The specific user
     * @param task The specific task
     * @return The part concern by the task
     * @throws WorkflowNotFoundException If no workflow was find for this task
     * @throws NotAllowedException If you can not make this task
     */
    private PartRevision checkTaskAccess(User user,Task task) throws WorkflowNotFoundException, NotAllowedException {
        Locale locale = new Locale(user.getLanguage());
        Workflow workflow = task.getActivity().getWorkflow();
        PartRevision partR = new WorkflowDAO(em).getPartTarget(workflow);
        if(partR == null){
            throw new WorkflowNotFoundException(locale,workflow.getId());
        }
        if(!task.isInProgress()){
            throw new NotAllowedException(locale,"NotAllowedException15");
        }
        if (!task.isPotentialWorker(user)) {
            throw new NotAllowedException(locale, "NotAllowedException14");
        }
        if (!workflow.getRunningTasks().contains(task)) {
            throw new NotAllowedException(locale, "NotAllowedException15");
        }
        if (partR.isCheckedOut()) {
            throw new NotAllowedException(locale, "NotAllowedException17");
        }
        return partR;
    }

    private void relaunchWorkflow(PartRevision partR, int activityStep){
        Workflow workflow = partR.getWorkflow();
        // Clone new workflow
        Workflow relaunchedWorkflow = new WorkflowDAO(em).duplicateWorkflow(workflow);

        // Move aborted workflow in docR list
        workflow.abort();
        partR.addAbortedWorkflows(workflow);
        // Set new workflow on document
        partR.setWorkflow(relaunchedWorkflow);
        // Reset some properties
        relaunchedWorkflow.relaunch(activityStep);
    }
}