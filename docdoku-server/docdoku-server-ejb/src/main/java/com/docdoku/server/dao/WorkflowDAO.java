/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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

import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.workflow.Activity;
import com.docdoku.core.workflow.Workflow;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

public class WorkflowDAO {

    private EntityManager em;

    public WorkflowDAO(EntityManager pEM) {
        em = pEM;
    }

    public void createWorkflow(Workflow pWf) {
        //Hack to prevent a bug inside the JPA implementation (Eclipse Link)
        List<Activity> activities = pWf.getActivities();
        pWf.setActivities(null);
        em.persist(pWf);
        em.flush();
        pWf.setActivities(activities);
    }

    public DocumentRevision getDocumentTarget(Workflow pWorkflow) {
        TypedQuery<DocumentRevision> query = em.createQuery("SELECT d FROM DocumentRevision d WHERE d.workflow = :workflow", DocumentRevision.class);
        try{
            return query.setParameter("workflow", pWorkflow).getSingleResult();
        }catch(NoResultException e){
            return null;
        }
    }

    public PartRevision getPartTarget(Workflow pWorkflow) {
        TypedQuery<PartRevision> query = em.createQuery("SELECT p FROM PartRevision p WHERE p.workflow = :workflow", PartRevision.class);
        try{
            return query.setParameter("workflow", pWorkflow).getSingleResult();
        }catch(NoResultException e){
            return null;
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
}