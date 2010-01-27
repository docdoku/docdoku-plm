/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.server.dao;

import com.docdoku.core.*;
import com.docdoku.core.WorkflowModelAlreadyExistsException;
import com.docdoku.core.entities.*;
import com.docdoku.core.entities.keys.BasicElementKey;

import java.util.LinkedList;
import java.util.List;
import java.util.Date;
import java.util.Locale;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

public class WorkflowModelDAO {
    
    private EntityManager em;
    private Locale mLocale;
    
    public WorkflowModelDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale=pLocale;
    }
    
    
    public void removeWorkflowModel(BasicElementKey pKey) throws WorkflowModelNotFoundException{
        try{
            WorkflowModel model = em.getReference(WorkflowModel.class,pKey);
            em.remove(model);
        }catch(EntityNotFoundException pENFEx){
            throw new WorkflowModelNotFoundException(mLocale, pKey.getId());
        }
    }
      
    public WorkflowModel[] findAllWorkflowModels(String pWorkspaceId){
        WorkflowModel[] models;
        Query query = em.createQuery("SELECT DISTINCT w FROM WorkflowModel w WHERE w.workspaceId = :workspaceId");
        List listModels = query.setParameter("workspaceId",pWorkspaceId).getResultList();
        models = new WorkflowModel[listModels.size()];
        for(int i=0;i<listModels.size();i++)
            models[i]=(WorkflowModel) listModels.get(i);
        
        return models;
    }
    
    public void createWorkflowModel(WorkflowModel pModel) throws WorkflowModelAlreadyExistsException, CreationException{
        try{
            //the EntityExistsException is thrown only when flush occurs
            em.persist(pModel);
            em.flush();
        }catch(EntityExistsException pEEEx){
            throw new WorkflowModelAlreadyExistsException(mLocale, pModel);
        }catch(PersistenceException pPEx){
            //EntityExistsException is case sensitive
            //whereas MySQL is not thus PersistenceException could be
            //thrown instead of EntityExistsException
            throw new CreationException(mLocale);
        }
    }
    
    public WorkflowModel loadWorkflowModel(BasicElementKey pKey) throws WorkflowModelNotFoundException {
        WorkflowModel model = em.find(WorkflowModel.class,pKey);
        if (model == null)
            throw new WorkflowModelNotFoundException(mLocale, pKey.getId());
        else
            return model;
    }
    
    
}
