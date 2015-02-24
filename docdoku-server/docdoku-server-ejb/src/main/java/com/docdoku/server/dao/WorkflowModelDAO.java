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

import com.docdoku.core.exceptions.CreationException;
import com.docdoku.core.exceptions.WorkflowModelAlreadyExistsException;
import com.docdoku.core.exceptions.WorkflowModelNotFoundException;
import com.docdoku.core.workflow.ActivityModel;
import com.docdoku.core.workflow.WorkflowModel;
import com.docdoku.core.workflow.WorkflowModelKey;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.util.List;
import java.util.Locale;

public class WorkflowModelDAO {

    private EntityManager em;
    private Locale mLocale;

    public WorkflowModelDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale = pLocale;
    }

    public void removeWorkflowModel(WorkflowModelKey pKey) throws WorkflowModelNotFoundException {
        WorkflowModel model = loadWorkflowModel(pKey);
        for(ActivityModel activity:model.getActivityModels()){
            activity.setRelaunchActivity(null);
        }
        em.flush();
        em.remove(model);
    }

    public WorkflowModel[] findAllWorkflowModels(String pWorkspaceId) {
        WorkflowModel[] models;
        Query query = em.createQuery("SELECT DISTINCT w FROM WorkflowModel w WHERE w.workspaceId = :workspaceId");
        List listModels = query.setParameter("workspaceId", pWorkspaceId).getResultList();
        models = new WorkflowModel[listModels.size()];
        for (int i = 0; i < listModels.size(); i++) {
            models[i] = (WorkflowModel) listModels.get(i);
        }

        return models;
    }

    public void createWorkflowModel(WorkflowModel pModel) throws WorkflowModelAlreadyExistsException, CreationException {
        try {

            if(pModel.getAcl()!=null){
                ACLDAO aclDAO = new ACLDAO(em);
                aclDAO.createACL(pModel.getAcl());
            }
            //the EntityExistsException is thrown only when flush occurs
            em.persist(pModel);
            em.flush();
        } catch (EntityExistsException pEEEx) {
            throw new WorkflowModelAlreadyExistsException(mLocale, pModel);
        } catch (PersistenceException pPEx) {
            //EntityExistsException is case sensitive
            //whereas MySQL is not thus PersistenceException could be
            //thrown instead of EntityExistsException
            throw new CreationException(mLocale);
        }
    }

    public WorkflowModel loadWorkflowModel(WorkflowModelKey pKey) throws WorkflowModelNotFoundException {
        WorkflowModel model = em.find(WorkflowModel.class, pKey);
        if (model == null) {
            throw new WorkflowModelNotFoundException(mLocale, pKey.getId());
        } else {
            return model;
        }
    }
}
