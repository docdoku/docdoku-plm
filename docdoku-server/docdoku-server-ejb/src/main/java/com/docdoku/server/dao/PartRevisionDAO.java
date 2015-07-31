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
import com.docdoku.core.exceptions.CreationException;
import com.docdoku.core.exceptions.PartRevisionAlreadyExistsException;
import com.docdoku.core.exceptions.PartRevisionNotFoundException;
import com.docdoku.core.meta.Tag;
import com.docdoku.core.product.*;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Locale;

public class PartRevisionDAO {

    private EntityManager em;
    private Locale mLocale;

    public PartRevisionDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale = pLocale;
    }
    public PartRevisionDAO(EntityManager pEM) {
        em = pEM;
        mLocale = Locale.getDefault();
    }


    public PartRevision loadPartR(PartRevisionKey pKey) throws PartRevisionNotFoundException {
        PartRevision partR = em.find(PartRevision.class, pKey);
        if (partR == null) {
            throw new PartRevisionNotFoundException(mLocale, pKey);
        } else {
            return partR;
        }
    }

    public void updateRevision(PartRevision pPartR) {
        em.merge(pPartR);
    }

    public void removeRevision(PartRevision pPartR) {
        new SharedEntityDAO(em).deleteSharesForPart(pPartR);
        new WorkflowDAO(em).removeWorkflowConstraints(pPartR);
        new ConversionDAO(em).removePartRevisionConversions(pPartR);
        for(PartIteration partIteration:pPartR.getPartIterations()){
            for(PartUsageLink partUsageLink:partIteration.getComponents()){
                em.remove(partUsageLink);
            }
        }
        em.remove(pPartR);
    }

    public List<PartRevision> findAllCheckedOutPartRevisions(String pWorkspaceId) {
        TypedQuery<PartRevision> query = em.createQuery("SELECT DISTINCT p FROM PartRevision p WHERE p.checkOutUser is not null and p.partMaster.workspace.id = :workspaceId", PartRevision.class);
        query.setParameter("workspaceId", pWorkspaceId);
        return query.getResultList();
    }

    public List<PartRevision> findCheckedOutPartRevisionsForUser(String pWorkspaceId, String pUserLogin) {
        TypedQuery<PartRevision> query = em.createQuery("SELECT DISTINCT p FROM PartRevision p WHERE p.checkOutUser is not null and p.partMaster.workspace.id = :workspaceId and p.checkOutUser.login = :userLogin", PartRevision.class);
        query.setParameter("workspaceId", pWorkspaceId);
        query.setParameter("userLogin", pUserLogin);
        return query.getResultList();
    }

    public List<PartRevision> getPartRevisions(String pWorkspaceId, int pStart, int pMaxResults) {
        return em.createNamedQuery("PartRevision.findByWorkspace", PartRevision.class)
                .setParameter("workspaceId", pWorkspaceId)
                .setFirstResult(pStart)
                .setMaxResults(pMaxResults)
                .getResultList();
    }

    public List<PartRevision> getAllPartRevisions(String pWorkspaceId) {
        return em.createNamedQuery("PartRevision.findByWorkspace", PartRevision.class)
                .setParameter("workspaceId", pWorkspaceId)
                .getResultList();
    }

    public int getTotalNumberOfParts(String pWorkspaceId) {
        return ((Number)em.createNamedQuery("PartRevision.countByWorkspace")
                .setParameter("workspaceId", pWorkspaceId)
                .getSingleResult()).intValue();
    }

    public int getPartRevisionCountFiltered(User caller, String workspaceId) {
        return ((Number) em.createNamedQuery("PartRevision.countByWorkspace.filterUserACLEntry")
                .setParameter("workspaceId", workspaceId)
                .setParameter("user", caller)
                .getSingleResult()).intValue();
    }

    public List<PartRevision> getPartRevisionsFiltered(User caller, String pWorkspaceId, int pStart, int pMaxResults) {
        return em.createNamedQuery("PartRevision.findByWorkspace.filterUserACLEntry", PartRevision.class)
                .setParameter("workspaceId", pWorkspaceId)
                .setParameter("user", caller)
                .setFirstResult(pStart)
                .setMaxResults(pMaxResults)
                .getResultList();
    }

    public void createPartR(PartRevision partR) throws PartRevisionAlreadyExistsException, CreationException {

        try {
            if (partR.getWorkflow() != null) {
                WorkflowDAO workflowDAO = new WorkflowDAO(em);
                workflowDAO.createWorkflow(partR.getWorkflow());
            }

            if (partR.getACL() != null) {
                ACLDAO aclDAO = new ACLDAO(em);
                aclDAO.createACL(partR.getACL());
            }

            //the EntityExistsException is thrown only when flush occurs
            em.persist(partR);
            em.flush();
        } catch (EntityExistsException pEEEx) {
            throw new PartRevisionAlreadyExistsException(mLocale, partR);
        } catch (PersistenceException pPEx) {
            //EntityExistsException is case sensitive
            //whereas MySQL is not thus PersistenceException could be
            //thrown instead of EntityExistsException
            throw new CreationException(mLocale);
        }

    }


    public List<PartRevision> findPartsRevisionsWithReferenceOrNameLike(String pWorkspaceId, String reference, int maxResults) {
        return em.createNamedQuery("PartRevision.findByReferenceOrName",PartRevision.class)
                .setParameter("workspaceId", pWorkspaceId)
                .setParameter("partNumber", "%" + reference + "%")
                .setParameter("partName", "%" + reference + "%")
                .setMaxResults(maxResults)
                .getResultList();
    }

    public boolean isCheckedOutIteration(PartIterationKey partIKey) throws PartRevisionNotFoundException {
        PartRevision partR = loadPartR(partIKey.getPartRevision());
        return partR.isCheckedOut() && (partIKey.getIteration() == partR.getLastIterationNumber());
    }

    public List<PartRevision> findPartByTag(Tag tag) {
        TypedQuery<PartRevision> query = em.createQuery("SELECT DISTINCT d FROM PartRevision d WHERE :tag MEMBER OF d.tags", PartRevision.class);
        query.setParameter("tag", tag);
        return query.getResultList();
    }
}