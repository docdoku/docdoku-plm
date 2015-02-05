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
import com.docdoku.core.document.*;
import com.docdoku.core.gcm.GCMAccount;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SubscriptionDAO {
    private static final Logger LOGGER = Logger.getLogger(SubscriptionDAO.class.getName());
    private final EntityManager em;

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
            LOGGER.log(Level.FINER,null,pNPEx);
        } catch (EntityNotFoundException pENFEx) {
            //not subscribed, no need to unsubscribe
            LOGGER.log(Level.FINER,null,pENFEx);
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
            LOGGER.log(Level.FINER,null,pNPEx);
        } catch (EntityNotFoundException pENFEx) {
            //not subscribed, no need to unsubscribe
            LOGGER.log(Level.FINER,null,pENFEx);
        }
    }

    public void removeAllSubscriptions(DocumentRevision pDocR) {
        Query query = em.createQuery("DELETE FROM StateChangeSubscription s WHERE s.observedDocumentRevision = :docR");
        query.setParameter("docR", pDocR);
        query.executeUpdate();

        Query query2 = em.createQuery("DELETE FROM IterationChangeSubscription s WHERE s.observedDocumentRevision = :docR");
        query2.setParameter("docR", pDocR);
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

    public DocumentRevisionKey[] getIterationChangeEventSubscriptions(User pUser) {
        DocumentRevisionKey[] docRKeys;
        Query query = em.createQuery("SELECT s.observedDocumentRevisionWorkspaceId, s.observedDocumentRevisionId, s.observedDocumentRevisionVersion FROM IterationChangeSubscription s WHERE s.subscriber = :user");
        List listDocRKeys = query.setParameter("user", pUser).getResultList();
        docRKeys = new DocumentRevisionKey[listDocRKeys.size()];
        for (int i = 0; i < listDocRKeys.size(); i++) {
            Object[] values = (Object[]) listDocRKeys.get(i);
            docRKeys[i] = new DocumentRevisionKey((String) values[0], (String) values[1], (String) values[2]);
        }


        return docRKeys;
    }

    public DocumentRevisionKey[] getStateChangeEventSubscriptions(User pUser) {
        DocumentRevisionKey[] docRKeys;
        Query query = em.createQuery("SELECT s.observedDocumentRevisionWorkspaceId, s.observedDocumentRevisionId, s.observedDocumentRevisionVersion FROM StateChangeSubscription s WHERE s.subscriber = :user");
        List listDocRKeys = query.setParameter("user", pUser).getResultList();
        docRKeys = new DocumentRevisionKey[listDocRKeys.size()];
        for (int i = 0; i < listDocRKeys.size(); i++) {
            Object[] values = (Object[]) listDocRKeys.get(i);
            docRKeys[i] = new DocumentRevisionKey((String) values[0], (String) values[1], (String) values[2]);
        }


        return docRKeys;
    }


    public boolean isUserStateChangeEventSubscribedForGivenDocument(User pUser, DocumentRevision docR) {
        return ! em.createNamedQuery("StateChangeSubscription.findSubscriptionByUserAndDocRevision").
                setParameter("user", pUser).setParameter("docR", docR).getResultList().isEmpty();

    }

    public boolean isUserIterationChangeEventSubscribedForGivenDocument(User pUser, DocumentRevision docR) {
        return ! em.createNamedQuery("IterationChangeSubscription.findSubscriptionByUserAndDocRevision").
                setParameter("user", pUser).setParameter("docR", docR).getResultList().isEmpty();
    }



    public User[] getIterationChangeEventSubscribers(DocumentRevision pDocR) {
        User[] users;
        Query query = em.createQuery("SELECT DISTINCT s.subscriber FROM IterationChangeSubscription s WHERE s.observedDocumentRevision = :docR");
        List listUsers = query.setParameter("docR", pDocR).getResultList();
        users = new User[listUsers.size()];
        for (int i = 0; i < listUsers.size(); i++) {
            users[i] = (User) listUsers.get(i);
        }

        return users;
    }

    public User[] getStateChangeEventSubscribers(DocumentRevision pDocR) {
        User[] users;
        Query query = em.createQuery("SELECT DISTINCT s.subscriber FROM StateChangeSubscription s WHERE s.observedDocumentRevision = :docR");
        List listUsers = query.setParameter("docR", pDocR).getResultList();
        users = new User[listUsers.size()];
        for (int i = 0; i < listUsers.size(); i++) {
            users[i] = (User) listUsers.get(i);
        }

        return users;
    }

    public GCMAccount[] getIterationChangeEventSubscribersGCMAccount(DocumentRevision pDocR) {
        GCMAccount[] gcmAccounts;
        Query query = em.createQuery("SELECT DISTINCT gcm FROM GCMAccount gcm, IterationChangeSubscription s WHERE gcm.account.login = s.subscriber.login AND s.observedDocumentRevision = :docR");
        List gcmAccountsList = query.setParameter("docR", pDocR).getResultList();
        gcmAccounts = new GCMAccount[gcmAccountsList.size()];
        for (int i = 0; i < gcmAccountsList.size(); i++) {
            gcmAccounts[i] = (GCMAccount) gcmAccountsList.get(i);
        }

        return gcmAccounts;
    }

    public GCMAccount[] getStateChangeEventSubscribersGCMAccount(DocumentRevision pDocR) {
        GCMAccount[] gcmAccounts;
        Query query = em.createQuery("SELECT DISTINCT gcm FROM GCMAccount gcm, StateChangeSubscription s WHERE gcm.account.login = s.subscriber.login AND s.observedDocumentRevision = :docR");
        List gcmAccountsList = query.setParameter("docR", pDocR).getResultList();
        gcmAccounts = new GCMAccount[gcmAccountsList.size()];
        for (int i = 0; i < gcmAccountsList.size(); i++) {
            gcmAccounts[i] = (GCMAccount) gcmAccountsList.get(i);
        }

        return gcmAccounts;
    }

}
