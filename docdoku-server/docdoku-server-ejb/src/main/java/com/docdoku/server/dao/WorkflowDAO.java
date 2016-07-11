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
package com.docdoku.server.dao;

import com.docdoku.core.common.User;
import com.docdoku.core.common.UserGroup;
import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.workflow.*;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class WorkflowDAO {

    private EntityManager em;

    public WorkflowDAO(EntityManager pEM) {
        this.em = pEM;
    }

    public void createWorkflow(Workflow pWf) {
        //Hack to prevent a bug inside the JPA implementation (Eclipse Link)
        List<Activity> activities = pWf.getActivities();
        pWf.setActivities(new ArrayList<Activity>());
        em.persist(pWf);
        em.flush();
        pWf.setActivities(activities);
    }

    public Workflow createWorkflow(WorkflowModel workflowModel,Map<Role, Collection<User>> roleUserMap, Map<Role, Collection<UserGroup>> roleGroupMap){
        Workflow workflow = new Workflow(workflowModel.getFinalLifeCycleState());
        em.persist(workflow);
        em.flush();

        List<Activity> activities = new ArrayList<>();
        for(ActivityModel model:workflowModel.getActivityModels()){
            Activity activity = model.createActivity(roleUserMap, roleGroupMap);
            activity.setWorkflow(workflow);
            Activity relaunchActivity = activity.getRelaunchActivity();
            if(relaunchActivity!=null){
                activity.setRelaunchActivity(activities.get(relaunchActivity.getStep()));
            }
            activities.add(activity);
        }
        workflow.setActivities(activities);
        return workflow;
    }

    public Workflow duplicateWorkflow(Workflow workflow){
        Workflow duplicatedWF = new Workflow(workflow.getFinalLifeCycleState());
        em.persist(duplicatedWF);
        em.flush();

        List<Activity> rwActivities = new ArrayList<>();
        for (Activity activity : workflow.getActivities()){
            Activity clonedActivity = activity.clone();
            clonedActivity.setWorkflow(duplicatedWF);
            Activity relaunchActivity = activity.getRelaunchActivity();
            if(relaunchActivity!=null){
                Activity clonedRelaunchActivity = rwActivities.get(relaunchActivity.getStep());
                clonedActivity.setRelaunchActivity(clonedRelaunchActivity);
            }
            rwActivities.add(clonedActivity);
        }

        duplicatedWF.setActivities(rwActivities);
        return duplicatedWF;

    }

    public DocumentRevision getDocumentTarget(Workflow pWorkflow) {
        TypedQuery<DocumentRevision> query = em.createNamedQuery("DocumentRevision.findByWorkflow", DocumentRevision.class);
        try {
            return query.setParameter("workflow", pWorkflow).getSingleResult();
        }catch(NoResultException e){
            return null;
        }
    }

    public PartRevision getPartTarget(Workflow pWorkflow) {
        TypedQuery<PartRevision> query = em.createNamedQuery("PartRevision.findByWorkflow", PartRevision.class);
        try {
            return query.setParameter("workflow", pWorkflow).getSingleResult();
        }catch(NoResultException e){
            return null;
        }
    }

    public void removeWorkflowConstraints(WorkspaceWorkflow ww) {
        List<Workflow> workflows = ww.getAbortedWorkflows();
        Workflow workflow = ww.getWorkflow();
        removeWorkflowConstraints(workflows,workflow);
    }

    public void removeWorkflowConstraints(DocumentMaster pDocM) {
        for(DocumentRevision documentRevision : pDocM.getDocumentRevisions()){
            removeWorkflowConstraints(documentRevision);
        }
    }

    public void removeWorkflowConstraints(PartMaster pPartM) {
        for(PartRevision partRevision : pPartM.getPartRevisions()){
            removeWorkflowConstraints(partRevision);
        }
    }

    public void removeWorkflowConstraints(DocumentRevision pDocR) {
        List<Workflow> workflows = pDocR.getAbortedWorkflows();
        Workflow workflow = pDocR.getWorkflow();
        removeWorkflowConstraints(workflows,workflow);
    }

    public void removeWorkflowConstraints(PartRevision pPartR) {
        List<Workflow> workflows = pPartR.getAbortedWorkflows();
        Workflow workflow = pPartR.getWorkflow();
        removeWorkflowConstraints(workflows,workflow);
    }

    private void removeWorkflowConstraints(List<Workflow> pWorkflows, Workflow pWorkflow){
        if(pWorkflows != null){
            for(Workflow workflow:pWorkflows){
                for(Activity activity:workflow.getActivities()){
                    activity.setRelaunchActivity(null);
                }
            }
        }
        if(pWorkflow != null){
            for(Activity activity:pWorkflow.getActivities()){
                activity.setRelaunchActivity(null);
            }
        }
        em.flush();
    }

    public Workflow getWorkflow(int workflowId) {
        return em.find(Workflow.class, workflowId);
    }

    public void createWorkspaceWorkflow(WorkspaceWorkflow workspaceWorkflow) {
        em.persist(workspaceWorkflow);
        em.flush();
    }

    public WorkspaceWorkflow getWorkspaceWorkflowTarget(String workspaceId, Workflow workflow) {
        TypedQuery<WorkspaceWorkflow> query = em.createQuery("SELECT w FROM WorkspaceWorkflow w WHERE w.workflow = :workflow AND w.workspace.id = :workspaceId", WorkspaceWorkflow.class);
        try{
            return query.setParameter("workflow", workflow)
                .setParameter("workspaceId", workspaceId)
                .getSingleResult();
        }catch(NoResultException e){
            return null;
        }
    }

    public WorkspaceWorkflow getWorkspaceWorkflow(String workspaceId, String workspaceWorkflowId) {
        return em.find(WorkspaceWorkflow.class, new WorkspaceWorkflowKey(workspaceId, workspaceWorkflowId));
    }

    public List<WorkspaceWorkflow> getWorkspaceWorkflowList(String workspaceId) {
        TypedQuery<WorkspaceWorkflow> query = em.createQuery("SELECT w FROM WorkspaceWorkflow w WHERE w.workspace.id = :workspaceId", WorkspaceWorkflow.class);
        try{
            return query.setParameter("workspaceId", workspaceId).getResultList();
        }catch(NoResultException e){
            return null;
        }
    }
}