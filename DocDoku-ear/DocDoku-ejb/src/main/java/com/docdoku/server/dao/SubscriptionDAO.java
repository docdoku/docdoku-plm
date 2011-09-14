/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011 DocDoku SARL
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

import com.docdoku.core.document.StateChangeSubscription;
import com.docdoku.core.document.IterationChangeSubscription;
import com.docdoku.core.document.MasterDocument;
import com.docdoku.core.common.User;
import com.docdoku.core.document.MasterDocumentKey;
import com.docdoku.core.document.SubscriptionKey;
import java.util.*;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;

public class SubscriptionDAO {

    private EntityManager em;

    public SubscriptionDAO(EntityManager pEM) {
        em = pEM;
    }

    public void createStateChangeSubscription(StateChangeSubscription pSubscription) {
        em.merge(pSubscription);
    }

    public void removeStateChangeSubscription(SubscriptionKey pKey) {
        try {
            StateChangeSubscription subscription = em.getReference(StateChangeSubscription.class, pKey);
            em.remove(subscription);
            em.flush();
        } catch (NullPointerException pNPEx) {
            //em.getReference throws a NullPointerException when entity
            //doesn't exist. It's probably a bug, as a workaround
            //we silently catch this exception
        } catch (EntityNotFoundException pENFEx) {
            //not subscribed, no need to unsubscribe
        }
    }

    public void createIterationChangeSubscription(IterationChangeSubscription pSubscription) {
        em.merge(pSubscription);
    }

    public void removeIterationChangeSubscription(SubscriptionKey pKey) {
        try {
            IterationChangeSubscription subscription = em.getReference(IterationChangeSubscription.class, pKey);
            em.remove(subscription);
            em.flush();
        } catch (NullPointerException pNPEx) {
            //em.getReference throws a NullPointerException when entity
            //doesn't exist. It's probably a bug, as a workaround
            //we silently catch this exception
        } catch (EntityNotFoundException pENFEx) {
            //not subscribed, no need to unsubscribe
        }
    }

    public void removeAllSubscriptions(MasterDocument pMDoc) {
        Query query = em.createQuery("DELETE FROM StateChangeSubscription s WHERE s.observedMasterDocument = :mdoc");
        query.setParameter("mdoc", pMDoc);
        query.executeUpdate();

        Query query2 = em.createQuery("DELETE FROM IterationChangeSubscription s WHERE s.observedMasterDocument = :mdoc");
        query2.setParameter("mdoc", pMDoc);
        query2.executeUpdate();
    }

    public void removeAllSubscriptions(User pUser) {
        Query query = em.createQuery("DELETE FROM StateChangeSubscription s WHERE s.subscriber = :user");
        query.setParameter("user", pUser);
        query.executeUpdate();

        Query query2 = em.createQuery("DELETE FROM IterationChangeSubscription s WHERE s.subscriber = :user");
        query2.setParameter("user", pUser);
        query2.executeUpdate();
    }

    public MasterDocumentKey[] getIterationChangeEventSubscriptions(User pUser) {
        MasterDocumentKey[] mdocKeys;
        Query query = em.createQuery("SELECT s.observedMasterDocumentWorkspaceId, s.observedMasterDocumentId, s.observedMasterDocumentVersion FROM IterationChangeSubscription s WHERE s.subscriber = :user");
        List listMDocKeys = query.setParameter("user", pUser).getResultList();
        mdocKeys = new MasterDocumentKey[listMDocKeys.size()];
        for (int i = 0; i < listMDocKeys.size(); i++) {
            Object[] values = (Object[]) listMDocKeys.get(i);
            mdocKeys[i] = new MasterDocumentKey((String) values[0], (String) values[1], (String) values[2]);
        }


        return mdocKeys;
    }

    public MasterDocumentKey[] getStateChangeEventSubscriptions(User pUser) {
        MasterDocumentKey[] mdocKeys;
        Query query = em.createQuery("SELECT s.observedMasterDocumentWorkspaceId, s.observedMasterDocumentId, s.observedMasterDocumentVersion FROM StateChangeSubscription s WHERE s.subscriber = :user");
        List listMDocKeys = query.setParameter("user", pUser).getResultList();
        mdocKeys = new MasterDocumentKey[listMDocKeys.size()];
        for (int i = 0; i < listMDocKeys.size(); i++) {
            Object[] values = (Object[]) listMDocKeys.get(i);
            mdocKeys[i] = new MasterDocumentKey((String) values[0], (String) values[1], (String) values[2]);
        }


        return mdocKeys;
    }

    public User[] getIterationChangeEventSubscribers(MasterDocument pMDoc) {
        User[] users;
        Query query = em.createQuery("SELECT DISTINCT s.subscriber FROM IterationChangeSubscription s WHERE s.observedMasterDocument = :mdoc");
        List listUsers = query.setParameter("mdoc", pMDoc).getResultList();
        users = new User[listUsers.size()];
        for (int i = 0; i < listUsers.size(); i++) {
            users[i] = (User) listUsers.get(i);
        }

        return users;
    }

    public User[] getStateChangeEventSubscribers(MasterDocument pMDoc) {
        User[] users;
        Query query = em.createQuery("SELECT DISTINCT s.subscriber FROM StateChangeSubscription s WHERE s.observedMasterDocument = :mdoc");
        List listUsers = query.setParameter("mdoc", pMDoc).getResultList();
        users = new User[listUsers.size()];
        for (int i = 0; i < listUsers.size(); i++) {
            users[i] = (User) listUsers.get(i);
        }

        return users;
    }
}
