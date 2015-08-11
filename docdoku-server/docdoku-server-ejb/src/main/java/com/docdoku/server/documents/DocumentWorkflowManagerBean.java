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
package com.docdoku.server.documents;

import com.docdoku.core.common.User;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.gcm.GCMAccount;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.*;
import com.docdoku.core.workflow.Activity;
import com.docdoku.core.workflow.Task;
import com.docdoku.core.workflow.TaskKey;
import com.docdoku.core.workflow.Workflow;
import com.docdoku.server.CheckActivity;
import com.docdoku.server.dao.DocumentRevisionDAO;
import com.docdoku.server.dao.SubscriptionDAO;
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
@Local(IDocumentWorkflowManagerLocal.class)
@Stateless(name = "DocumentWorkflowManagerBean")
public class DocumentWorkflowManagerBean implements IDocumentWorkflowManagerLocal {

    @PersistenceContext
    private EntityManager em;

    @EJB
    private IUserManagerLocal userManager;
    @EJB
    private IDocumentManagerLocal documentManager;
    @EJB
    private IMailerLocal mailer;
    @EJB
    private IGCMSenderLocal gcmNotifier;

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public Workflow getCurrentWorkflow(DocumentRevisionKey documentRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, DocumentRevisionNotFoundException, AccessRightException, WorkflowNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(documentRevisionKey.getDocumentMaster().getWorkspace());
        if(!documentManager.canUserAccess(user, documentRevisionKey)) {
            throw new AccessRightException(new Locale(user.getLanguage()), user);
        }

        Locale locale = new Locale(user.getLanguage());
        DocumentRevision docR = new DocumentRevisionDAO(locale, em).loadDocR(documentRevisionKey);
        return docR.getWorkflow();
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public Workflow[] getAbortedWorkflow(DocumentRevisionKey documentRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, DocumentRevisionNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(documentRevisionKey.getDocumentMaster().getWorkspace());
        if(!documentManager.canUserAccess(user, documentRevisionKey)) {
            throw new AccessRightException(new Locale(user.getLanguage()), user);
        }

        Locale locale = new Locale(user.getLanguage());
        DocumentRevision docR = new DocumentRevisionDAO(locale, em).loadDocR(documentRevisionKey);
        List<Workflow> abortedWorkflows= docR.getAbortedWorkflows();
        return abortedWorkflows.toArray(new Workflow[abortedWorkflows.size()]);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @CheckActivity
    @Override
    public DocumentRevision approveTaskOnDocument(String pWorkspaceId, TaskKey pTaskKey, String pComment, String pSignature) throws WorkspaceNotFoundException, TaskNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException, WorkflowNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);

        Task task = new TaskDAO(new Locale(user.getLanguage()), em).loadTask(pTaskKey);
        Workflow workflow = task.getActivity().getWorkflow();
        DocumentRevision docR = checkTaskAccess(user,task);

        int previousStep = workflow.getCurrentStep();
        task.approve(pComment, docR.getLastIteration().getIteration(), pSignature);
        int currentStep = workflow.getCurrentStep();

        if (previousStep != currentStep){
            SubscriptionDAO subscriptionDAO = new SubscriptionDAO(em);

            User[] subscribers = subscriptionDAO.getStateChangeEventSubscribers(docR);
            if (subscribers.length != 0) {
                mailer.sendStateNotification(subscribers, docR);
            }

            GCMAccount[] gcmAccounts = subscriptionDAO.getStateChangeEventSubscribersGCMAccount(docR);
            if (gcmAccounts.length != 0) {
                gcmNotifier.sendStateNotification(gcmAccounts, docR);
            }
        }

        Collection<Task> runningTasks = workflow.getRunningTasks();
        for (Task runningTask : runningTasks) {
            runningTask.start();
        }
        mailer.sendApproval(runningTasks, docR);
        return docR;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @CheckActivity
    @Override
    public DocumentRevision rejectTaskOnDocument(String pWorkspaceId, TaskKey pTaskKey, String pComment, String pSignature) throws WorkspaceNotFoundException, TaskNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException, WorkflowNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);

        Task task = new TaskDAO(new Locale(user.getLanguage()), em).loadTask(pTaskKey);
        DocumentRevision docR = checkTaskAccess(user,task);

        task.reject(pComment, docR.getLastIteration().getIteration(), pSignature);

        // Relaunch Workflow ?
        Activity currentActivity = task.getActivity();
        Activity relaunchActivity = currentActivity.getRelaunchActivity();

        if(currentActivity.isStopped() && relaunchActivity != null){
            relaunchWorkflow(docR,relaunchActivity.getStep());

            // Send mails for running tasks
            mailer.sendApproval(docR.getWorkflow().getRunningTasks(), docR);
            // Send notification for relaunch
            mailer.sendDocumentRevisionWorkflowRelaunchedNotification(docR);
        }
        return docR;
    }

    /**
     * Check if a user can approve or reject a task
     * @param user The specific user
     * @param task The specific task
     * @return The document concern by the task
     * @throws WorkflowNotFoundException If no workflow was find for this task
     * @throws NotAllowedException If you can not make this task
     */
    private DocumentRevision checkTaskAccess(User user,Task task) throws WorkflowNotFoundException, NotAllowedException {
        Locale locale = new Locale(user.getLanguage());
        Workflow workflow = task.getActivity().getWorkflow();
        DocumentRevision docR = new WorkflowDAO(em).getDocumentTarget(workflow);
        if(docR == null){
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
        if (docR.isCheckedOut()) {
            throw new NotAllowedException(locale, "NotAllowedException16");
        }
        return  docR;
    }

    private void relaunchWorkflow(DocumentRevision docR, int activityStep){
        Workflow workflow = docR.getWorkflow();
        // Clone new workflow
        Workflow relaunchedWorkflow = new WorkflowDAO(em).duplicateWorkflow(workflow);

        // Move aborted workflow in docR list
        workflow.abort();
        docR.addAbortedWorkflows(workflow);
        // Set new workflow on document
        docR.setWorkflow(relaunchedWorkflow);
        // Reset some properties
        relaunchedWorkflow.relaunch(activityStep);
    }
}