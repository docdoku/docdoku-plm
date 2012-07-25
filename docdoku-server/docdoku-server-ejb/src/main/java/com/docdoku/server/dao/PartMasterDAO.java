/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
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


import com.docdoku.core.services.CreationException;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartMasterKey;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.services.PartMasterAlreadyExistsException;
import com.docdoku.core.services.PartMasterNotFoundException;
import java.util.*;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceException;

public class PartMasterDAO {

    private EntityManager em;
    private Locale mLocale;

    public PartMasterDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale = pLocale;
    }

    public PartMasterDAO(EntityManager pEM) {
        em = pEM;
        mLocale = Locale.getDefault();
    }


    public PartMaster loadPartM(PartMasterKey pKey) throws PartMasterNotFoundException {
        PartMaster partM = em.find(PartMaster.class, pKey);
        if (partM == null) {
            throw new PartMasterNotFoundException(mLocale, pKey.getNumber());
        } else {
            return partM;
        }
    }

    public PartMaster getPartMRef(PartMasterKey pKey) throws PartMasterNotFoundException {
        try {
            PartMaster partM = em.getReference(PartMaster.class, pKey);
            return partM;
        } catch (EntityNotFoundException pENFEx) {
            throw new PartMasterNotFoundException(mLocale, pKey.getNumber());
        }
    }

    public void createPartM(PartMaster pPartM) throws PartMasterAlreadyExistsException, CreationException {
        try {
            PartRevision firstRev = pPartM.getLastRevision();
            if(firstRev!=null && firstRev.getWorkflow()!=null){
                WorkflowDAO workflowDAO = new WorkflowDAO(em);
                workflowDAO.createWorkflow(firstRev.getWorkflow());
            }
            //the EntityExistsException is thrown only when flush occurs
            em.persist(pPartM);
            em.flush();
        } catch (EntityExistsException pEEEx) {
            throw new PartMasterAlreadyExistsException(mLocale, pPartM);
        } catch (PersistenceException pPEx) {
            //EntityExistsException is case sensitive
            //whereas MySQL is not thus PersistenceException could be
            //thrown instead of EntityExistsException
            throw new CreationException(mLocale);
        }
    }

    public void removePartM(PartMaster pPartM) {
        em.remove(pPartM);
    }
}
