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

import com.docdoku.core.change.ChangeIssue;
import com.docdoku.core.change.ChangeItem;
import com.docdoku.core.change.ChangeOrder;
import com.docdoku.core.change.ChangeRequest;
import com.docdoku.core.exceptions.ChangeIssueNotFoundException;
import com.docdoku.core.exceptions.ChangeOrderNotFoundException;
import com.docdoku.core.exceptions.ChangeRequestNotFoundException;
import com.docdoku.core.exceptions.LayerNotFoundException;
import com.docdoku.core.product.Layer;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Locale;

public class ChangeItemDAO {

    private EntityManager em;
    private Locale mLocale;

    public ChangeItemDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale = pLocale;
    }

    public ChangeItemDAO(EntityManager pEM) {
        em = pEM;
        mLocale = Locale.getDefault();
    }


    public List<ChangeIssue> findAllChangeIssues(String pWorkspaceId) {
        TypedQuery<ChangeIssue> query = em.createNamedQuery("ChangeIssue.findChangeIssuesByWorkspace", ChangeIssue.class);
        query.setParameter("workspaceId", pWorkspaceId);
        return query.getResultList();
    }

    public List<ChangeRequest> findAllChangeRequests(String pWorkspaceId) {
        TypedQuery<ChangeRequest> query = em.createNamedQuery("ChangeRequest.findChangeRequestsByWorkspace", ChangeRequest.class);
        query.setParameter("workspaceId", pWorkspaceId);
        return query.getResultList();
    }

    public List<ChangeOrder> findAllChangeOrders(String pWorkspaceId) {
        TypedQuery<ChangeOrder> query = em.createNamedQuery("ChangeOrder.findChangeOrdersByWorkspace", ChangeOrder.class);
        query.setParameter("workspaceId", pWorkspaceId);
        return query.getResultList();
    }
    
    public ChangeIssue loadChangeIssue(int pId) throws ChangeIssueNotFoundException {
        ChangeIssue change = em.find(ChangeIssue.class, pId);
        if (change == null) {
            throw new ChangeIssueNotFoundException(mLocale, pId);
        } else {
            return change;
        }
    }

    public ChangeOrder loadChangeOrder(int pId) throws ChangeOrderNotFoundException {
        ChangeOrder change = em.find(ChangeOrder.class, pId);
        if (change == null) {
            throw new ChangeOrderNotFoundException(mLocale, pId);
        } else {
            return change;
        }
    }

    public ChangeRequest loadChangeRequest(int pId) throws ChangeRequestNotFoundException {
        ChangeRequest change = em.find(ChangeRequest.class, pId);
        if (change == null) {
            throw new ChangeRequestNotFoundException(mLocale, pId);
        } else {
            return change;
        }
    }

    public void createChangeItem(ChangeItem pChange) {
        em.persist(pChange);
        em.flush();
    }


    public void deleteChangeItem(ChangeItem pChange) {
        em.remove(pChange);
        em.flush();
    }
}
