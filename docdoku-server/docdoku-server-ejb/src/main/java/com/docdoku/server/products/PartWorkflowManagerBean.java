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
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

@DeclareRoles({UserGroupMapping.REGULAR_USER_ROLE_ID,UserGroupMapping.ADMIN_ROLE_ID,UserGroupMapping.GUEST_PROXY_ROLE_ID})
@Local(IPartWorkflowManagerLocal.class)
@Stateless(name = "PartWorkflowManagerBean")
public class PartWorkflowManagerBean implements IPartWorkflowManagerLocal {

    @PersistenceContext
    private EntityManager em;

    @EJB
    private IUserManagerLocal userManager;
    @EJB
    private IProductManagerLocal productManager;
    @EJB
    private IMailerLocal mailer;

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public Workflow getCurentWorkflow(PartRevisionKey partRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartRevisionNotFoundException, AccessRightException{
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
    public Workflow[] getAbortedWorkflow(PartRevisionKey partRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartRevisionNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(partRevisionKey.getPartMaster().getWorkspace());
        if(!productManager.canUserAccess(user, partRevisionKey)) {
            throw new AccessRightException(new Locale(user.getLanguage()), user);
        }

        Locale locale = new Locale(user.getLanguage());
        PartRevision partR = new PartRevisionDAO(locale, em).loadPartR(partRevisionKey);
        List<Workflow> abortedWorkflows= partR.getAbortedWorkflows();

        return abortedWorkflows.toArray(new Workflow[abortedWorkflows.size()]);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartRevision[] getPartRevisionsWithAssignedTasksForGivenUser(String pWorkspaceId, String assignedUserLogin) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        // Todo
        return new PartRevision[0];
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartRevision[] getPartRevisionsWithOpenedTasksForGivenUser(String pWorkspaceId, String assignedUserLogin) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        // Todo
        return new PartRevision[0];
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartRevision approveTaskOnPart(String pWorkspaceId, TaskKey pTaskKey, String pComment, String pSignature) throws WorkspaceNotFoundException, TaskNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException, WorkflowNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);

        Task task = new TaskDAO(new Locale(user.getLanguage()), em).loadTask(pTaskKey);
        Workflow workflow = task.getActivity().getWorkflow();
        PartRevision partRevision = checkTaskAccess(user,task);

        task.approve(pComment, partRevision.getLastIteration().getIteration(), pSignature);

        Collection<Task> runningTasks = workflow.getRunningTasks();
        for (Task runningTask : runningTasks) {
            runningTask.start();
        }
        mailer.sendApproval(runningTasks, partRevision);
        return partRevision;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartRevision rejectTaskOnPart(String pWorkspaceId, TaskKey pTaskKey, String pComment, String pSignature) throws WorkspaceNotFoundException, TaskNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException, WorkflowNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);

        Task task = new TaskDAO(new Locale(user.getLanguage()), em).loadTask(pTaskKey);
        PartRevision partR = checkTaskAccess(user,task);

        task.reject(pComment, partR.getLastIteration().getIteration(), pSignature);

        // Relaunch Workflow ?
        Activity currentActivity = task.getActivity();
        Activity relaunchActivity = currentActivity.getRelaunchActivity();

        if(currentActivity.isStopped() && relaunchActivity != null){
            relaunchWorkflow(partR,relaunchActivity.getStep());

            // Send mails for running tasks
            mailer.sendApproval(partR.getWorkflow().getRunningTasks(), partR);
            // Send notification for relaunch
            mailer.sendPartRevisionWorkflowRelaunchedNotification(partR);
        }
        return partR;
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
        if (!task.getWorker().equals(user)) {
            throw new NotAllowedException(locale, "NotAllowedException14");
        }
        if (!workflow.getRunningTasks().contains(task)) {
            throw new NotAllowedException(locale, "NotAllowedException15");
        }
        if (partR.isCheckedOut()) {
            throw new NotAllowedException(locale, "NotAllowedException16");
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