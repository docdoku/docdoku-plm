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
import com.docdoku.core.exceptions.PartMasterAlreadyExistsException;
import com.docdoku.core.exceptions.PartMasterNotFoundException;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartMasterKey;
import com.docdoku.core.product.PartRevision;

import javax.persistence.*;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PartMasterDAO {

    private EntityManager em;
    private Locale mLocale;
    private static final Logger LOGGER = Logger.getLogger(PartMasterDAO.class.getName());

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
            return em.getReference(PartMaster.class, pKey);
        } catch (EntityNotFoundException pENFEx) {
            LOGGER.log(Level.FINEST,null,pENFEx);
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
            LOGGER.log(Level.FINEST,null,pEEEx);
            throw new PartMasterAlreadyExistsException(mLocale, pPartM);
        } catch (PersistenceException pPEx) {
            //EntityExistsException is case sensitive
            //whereas MySQL is not thus PersistenceException could be
            //thrown instead of EntityExistsException
            LOGGER.log(Level.FINEST,null,pPEx);
            throw new CreationException(mLocale);
        }
    }

    public void removePartM(PartMaster pPartM) {
        PartRevisionDAO partRevisionDAO = new PartRevisionDAO(mLocale, em);
        for(PartRevision partRevision:pPartM.getPartRevisions()){
            partRevisionDAO.removeRevision(partRevision);
        }
        em.remove(pPartM);
    }

    public List<PartMaster> findPartMasters(String workspaceId, String partNumber, String partName, int maxResults){
        return em.createNamedQuery("PartMaster.findByNameOrNumber", PartMaster.class)
            .setParameter("partNumber", partNumber)
            .setParameter("partName", partNumber)
            .setParameter("workspaceId", workspaceId)
            .setMaxResults(maxResults)
            .getResultList();
    }

    public String findLatestPartMId(String pWorkspaceId, String pType) {
        String partMId;
        Query query = em.createQuery("SELECT m.number FROM PartMaster m "
                + "WHERE m.workspace.id = :workspaceId "
                + "AND m.type = :type "
                + "AND m.creationDate = ("
                + "SELECT MAX(m2.creationDate) FROM PartMaster m2 "
                + "WHERE m2.workspace.id = :workspaceId "
                + "AND m2.type = :type "
                + ")");
        query.setParameter("workspaceId", pWorkspaceId);
        query.setParameter("type", pType);
        partMId = (String) query.getSingleResult();
        return partMId;
    }

    public List<PartMaster> getPartMasters(String pWorkspaceId, int pStart, int pMaxResults) {
        return em.createNamedQuery("PartMaster.findByWorkspace", PartMaster.class)
                .setParameter("workspaceId", pWorkspaceId)
                .setFirstResult(pStart)
                .setMaxResults(pMaxResults)
                .getResultList();
    }

    public long getDiskUsageForPartsInWorkspace(String pWorkspaceId) {
        Number result = (Number)em.createNamedQuery("BinaryResource.diskUsageInPath")
                .setParameter("path", pWorkspaceId+"/parts/%")
                .getSingleResult();

        return result != null ? result.longValue() : 0L;
    }

    public long getDiskUsageForPartTemplatesInWorkspace(String pWorkspaceId) {
        Number result = (Number)em.createNamedQuery("BinaryResource.diskUsageInPath")
                .setParameter("path", pWorkspaceId+"/part-templates/%")
                .getSingleResult();

        return result != null ? result.longValue() : 0L;
    }

    public List<PartMaster> getAllByWorkspace(String workspaceId) {
        return em.createNamedQuery("PartMaster.findByWorkspace")
                .setParameter("workspaceId",workspaceId)
                .getResultList();
    }
}