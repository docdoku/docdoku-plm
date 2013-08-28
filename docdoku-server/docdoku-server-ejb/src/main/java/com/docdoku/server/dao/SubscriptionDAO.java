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

import com.docdoku.core.common.User;
import com.docdoku.core.document.*;
import com.docdoku.core.gcm.GCMAccount;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;
import java.util.List;

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

    public void removeAllSubscriptions(DocumentMaster pDocM) {
        Query query = em.createQuery("DELETE FROM StateChangeSubscription s WHERE s.observedDocumentMaster = :docM");
        query.setParameter("docM", pDocM);
        query.executeUpdate();

        Query query2 = em.createQuery("DELETE FROM IterationChangeSubscription s WHERE s.observedDocumentMaster = :docM");
        query2.setParameter("docM", pDocM);
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

    public DocumentMasterKey[] getIterationChangeEventSubscriptions(User pUser) {
        DocumentMasterKey[] docMKeys;
        Query query = em.createQuery("SELECT s.observedDocumentMasterWorkspaceId, s.observedDocumentMasterId, s.observedDocumentMasterVersion FROM IterationChangeSubscription s WHERE s.subscriber = :user");
        List listDocMKeys = query.setParameter("user", pUser).getResultList();
        docMKeys = new DocumentMasterKey[listDocMKeys.size()];
        for (int i = 0; i < listDocMKeys.size(); i++) {
            Object[] values = (Object[]) listDocMKeys.get(i);
            docMKeys[i] = new DocumentMasterKey((String) values[0], (String) values[1], (String) values[2]);
        }


        return docMKeys;
    }

    public DocumentMasterKey[] getStateChangeEventSubscriptions(User pUser) {
        DocumentMasterKey[] docMKeys;
        Query query = em.createQuery("SELECT s.observedDocumentMasterWorkspaceId, s.observedDocumentMasterId, s.observedDocumentMasterVersion FROM StateChangeSubscription s WHERE s.subscriber = :user");
        List listDocMKeys = query.setParameter("user", pUser).getResultList();
        docMKeys = new DocumentMasterKey[listDocMKeys.size()];
        for (int i = 0; i < listDocMKeys.size(); i++) {
            Object[] values = (Object[]) listDocMKeys.get(i);
            docMKeys[i] = new DocumentMasterKey((String) values[0], (String) values[1], (String) values[2]);
        }


        return docMKeys;
    }


    public boolean isUserStateChangeEventSubscribedForGivenDocument(User pUser, DocumentMaster docM) {
        return ! em.createNamedQuery("findStateChangeSubscriptionWithGivenUserAndGivenDocMaster").
                setParameter("user", pUser).setParameter("docM", docM).getResultList().isEmpty();

    }

    public boolean isUserIterationChangeEventSubscribedForGivenDocument(User pUser, DocumentMaster docM) {
        return ! em.createNamedQuery("findIterationChangeSubscriptionWithGivenUserAndGivenDocMaster").
                setParameter("user", pUser).setParameter("docM", docM).getResultList().isEmpty();
    }



    public User[] getIterationChangeEventSubscribers(DocumentMaster pDocM) {
        User[] users;
        Query query = em.createQuery("SELECT DISTINCT s.subscriber FROM IterationChangeSubscription s WHERE s.observedDocumentMaster = :docM");
        List listUsers = query.setParameter("docM", pDocM).getResultList();
        users = new User[listUsers.size()];
        for (int i = 0; i < listUsers.size(); i++) {
            users[i] = (User) listUsers.get(i);
        }

        return users;
    }

    public User[] getStateChangeEventSubscribers(DocumentMaster pDocM) {
        User[] users;
        Query query = em.createQuery("SELECT DISTINCT s.subscriber FROM StateChangeSubscription s WHERE s.observedDocumentMaster = :docM");
        List listUsers = query.setParameter("docM", pDocM).getResultList();
        users = new User[listUsers.size()];
        for (int i = 0; i < listUsers.size(); i++) {
            users[i] = (User) listUsers.get(i);
        }

        return users;
    }

    public GCMAccount[] getIterationChangeEventSubscribersGCMAccount(DocumentMaster pDocM) {
        GCMAccount[] gcmAccounts;
        Query query = em.createQuery("SELECT DISTINCT gcm FROM GCMAccount gcm, IterationChangeSubscription s WHERE gcm.account.login = s.subscriber.login AND s.observedDocumentMaster = :docM");
        List gcmAccountsList = query.setParameter("docM", pDocM).getResultList();
        gcmAccounts = new GCMAccount[gcmAccountsList.size()];
        for (int i = 0; i < gcmAccountsList.size(); i++) {
            gcmAccounts[i] = (GCMAccount) gcmAccountsList.get(i);
        }

        return gcmAccounts;
    }

    public GCMAccount[] getStateChangeEventSubscribersGCMAccount(DocumentMaster pDocM) {
        GCMAccount[] gcmAccounts;
        Query query = em.createQuery("SELECT DISTINCT gcm FROM GCMAccount gcm, StateChangeSubscription s WHERE gcm.account.login = s.subscriber.login AND s.observedDocumentMaster = :docM");
        List gcmAccountsList = query.setParameter("docM", pDocM).getResultList();
        gcmAccounts = new GCMAccount[gcmAccountsList.size()];
        for (int i = 0; i < gcmAccountsList.size(); i++) {
            gcmAccounts[i] = (GCMAccount) gcmAccountsList.get(i);
        }

        return gcmAccounts;
    }

}
