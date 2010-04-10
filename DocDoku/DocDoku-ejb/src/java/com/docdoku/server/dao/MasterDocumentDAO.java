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
import com.docdoku.core.entities.Activity;
import com.docdoku.core.entities.InstanceAttribute;
import com.docdoku.core.entities.Tag;
import com.docdoku.core.entities.Task;
import com.docdoku.core.entities.Workflow;
import com.docdoku.core.entities.keys.MasterDocumentKey;
import com.docdoku.core.entities.MasterDocument;
import com.docdoku.core.entities.SearchQuery;
import com.docdoku.core.entities.User;
import java.util.*;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

public class MasterDocumentDAO {

    private EntityManager em;
    private Locale mLocale;
    private final static int MAX_RESULTS = 500;

    public MasterDocumentDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale = pLocale;
    }

    public MasterDocumentDAO(EntityManager pEM) {
        em = pEM;
        mLocale = Locale.getDefault();
    }

    public List<MasterDocument> searchMDocs(String pWorkspaceId, String pMDocID, String pTitle,
            String pVersion, String pAuthor, String pType, java.util.Date pCreationDateFrom,
            java.util.Date pCreationDateTo, Collection<Tag> pTags, Collection<SearchQuery.AbstractAttributeQuery> pAttrs) {
        StringBuilder queryStr = new StringBuilder();
        queryStr.append("SELECT DISTINCT m FROM MasterDocument m ");

        if (pAttrs != null && pAttrs.size() > 0) {
            queryStr.append("JOIN m.documentIterations d ");
            for(int i =0 ;i<pAttrs.size();i++)
                queryStr.append("JOIN d.instanceAttributes attr" + i + " ");
        }

        queryStr.append("WHERE m.workspaceId = :workspaceId ");
        queryStr.append("AND m.id LIKE :id ");
        queryStr.append("AND m.version LIKE :version ");
        queryStr.append("AND m.title LIKE :title ");
        queryStr.append("AND m.type LIKE :type ");
        queryStr.append("AND m.author.login LIKE :author ");

        if (pTags != null && pTags.size() > 0) {
            for(int i =0 ;i<pTags.size();i++)
                queryStr.append("AND :tag" + i + " MEMBER OF m.tags ");
        }

        if (pAttrs != null && pAttrs.size() > 0) {
            queryStr.append("AND d.iteration = (SELECT MAX(d2.iteration) FROM Document d2 WHERE d2=d) ");
            int i=0;
            for(SearchQuery.AbstractAttributeQuery attr:pAttrs){
                if(attr instanceof SearchQuery.DateAttributeQuery){
                    queryStr.append("AND attr" + i + ".attributeValue BETWEEN :attrLValue" + i + " AND :attrUValue" + i + " ");
                }else{
                    queryStr.append("AND attr" + i + ".attributeValue = :attrValue" + i + " ");
                }
                queryStr.append("AND attr" + i + ".name = :attrName" + (i++) + " ");
            }
        }

        queryStr.append("AND m.creationDate BETWEEN :lowerDate AND :upperDate ");
        queryStr.append("ORDER BY m.id, m.version");

        Query query = em.createQuery(queryStr.toString());
        query.setParameter("workspaceId", pWorkspaceId);
        query.setParameter("id", pMDocID == null ? "%" : "%" + pMDocID + "%");
        query.setParameter("version", pVersion == null ? "%" : "%" + pVersion + "%");
        query.setParameter("title", pTitle == null ? "%" : "%" + pTitle + "%");
        query.setParameter("type", pType == null ? "%" : "%" + pType + "%");
        query.setParameter("author", pAuthor == null ? "%" : "%" + pAuthor + "%");

        
        if (pTags != null && pTags.size() > 0) {
            int i=0;
            for(Tag tag:pTags)
                query.setParameter("tag" + (i++), tag);
        }
        
        if (pAttrs != null && pAttrs.size() > 0) {
            int i=0;
            for(SearchQuery.AbstractAttributeQuery attr:pAttrs){              
                if(attr instanceof SearchQuery.TextAttributeQuery){
                    query.setParameter("attrValue" + i, ((SearchQuery.TextAttributeQuery)attr).getTextValue());
                }else if(attr instanceof SearchQuery.URLAttributeQuery){
                    query.setParameter("attrValue" + i, ((SearchQuery.URLAttributeQuery)attr).getUrlValue());
                }else if(attr instanceof SearchQuery.NumberAttributeQuery){
                    query.setParameter("attrValue" + i, ((SearchQuery.NumberAttributeQuery)attr).getNumberValue()+"");
                }else if(attr instanceof SearchQuery.BooleanAttributeQuery){
                    query.setParameter("attrValue" + i, ((SearchQuery.BooleanAttributeQuery)attr).isBooleanValue()+"");
                }else if(attr instanceof SearchQuery.DateAttributeQuery){
                    query.setParameter("attrLValue" + i, ((SearchQuery.DateAttributeQuery)attr).getFromDate().getTime()+"");
                    query.setParameter("attrUValue" + i, ((SearchQuery.DateAttributeQuery)attr).getToDate().getTime()+"");
                }
                query.setParameter("attrName" + (i++), attr.getName());
            }
        }

        query.setParameter("lowerDate", pCreationDateFrom == null ? new Date(0) : pCreationDateFrom);
        query.setParameter("upperDate", pCreationDateTo == null ? new Date() : pCreationDateTo);
        query.setMaxResults(MAX_RESULTS);
        return query.getResultList();

    }

    public String findLatestMDocId(String pWorkspaceId, String pType) {
        String mdocId;
        Query query = em.createQuery("SELECT m.id FROM MasterDocument m "
                + "WHERE m.workspaceId = :workspaceId "
                + "AND m.type = :type "
                + "AND m.creationDate = ("
                + "SELECT MAX(m2.creationDate) FROM MasterDocument m2 "
                + "WHERE m2.workspaceId = :workspaceId "
                + "AND m2.type = :type"
                + ")");
        query.setParameter("workspaceId", pWorkspaceId);
        query.setParameter("type", pType);
        mdocId = (String) query.getSingleResult();
        return mdocId;
    }

    public List<MasterDocument> findMDocsByFolder(String pCompletePath) {
        Query query = em.createQuery("SELECT DISTINCT m FROM MasterDocument m WHERE m.location.completePath = :completePath");
        query.setParameter("completePath", pCompletePath);
        return query.getResultList();
    }

    public List<MasterDocument> findMDocsByTag(Tag pTag) {
        Query query = em.createQuery("SELECT DISTINCT m FROM MasterDocument m WHERE :tag MEMBER OF m.tags");
        query.setParameter("tag", pTag);
        return query.getResultList();
    }

    public List<MasterDocument> findCheckedOutMDocs(User pUser) {
        Query query = em.createQuery("SELECT DISTINCT m FROM MasterDocument m WHERE m.checkOutUser = :user");
        query.setParameter("user", pUser);
        return query.getResultList();
    }

    public MasterDocument loadMDoc(MasterDocumentKey pKey) throws MasterDocumentNotFoundException {
        MasterDocument mdoc = em.find(MasterDocument.class, pKey);
        if (mdoc == null) {
            throw new MasterDocumentNotFoundException(mLocale, pKey);
        } else {
            return mdoc;
        }
    }

    public MasterDocument getMDocRef(MasterDocumentKey pKey) throws MasterDocumentNotFoundException {
        try {
            MasterDocument mdoc = em.getReference(MasterDocument.class, pKey);
            return mdoc;
        } catch (EntityNotFoundException pENFEx) {
            throw new MasterDocumentNotFoundException(mLocale, pKey);
        }
    }

    public void createMDoc(MasterDocument pMasterDocument) throws MasterDocumentAlreadyExistsException, CreationException {
        try {
            //the EntityExistsException is thrown only when flush occurs
            em.persist(pMasterDocument);
            em.flush();
            if (pMasterDocument.hasWorkflow()) {
                //because of flush, the workflowId has been generated
                Workflow wf = pMasterDocument.getWorkflow();
                for (Activity activity : wf.getActivities()) {
                    activity.setWorkflow(wf);
                    for (Task task : activity.getTasks()) {
                        task.setActivity(activity);
                    }
                }
            }
        } catch (EntityExistsException pEEEx) {
            throw new MasterDocumentAlreadyExistsException(mLocale, pMasterDocument);
        } catch (PersistenceException pPEx) {
            //EntityExistsException is case sensitive
            //whereas MySQL is not thus PersistenceException could be
            //thrown instead of EntityExistsException
            throw new CreationException(mLocale);
        }
    }

    public void removeMDoc(MasterDocumentKey pKey) throws MasterDocumentNotFoundException {
        try {
            MasterDocument mdoc = em.getReference(MasterDocument.class, pKey);
            removeMDoc(mdoc);
        } catch (EntityNotFoundException pENFEx) {
            throw new MasterDocumentNotFoundException(mLocale, pKey);
        }
    }

    public void removeMDoc(MasterDocument pMDoc) {
        SubscriptionDAO subscriptionDAO = new SubscriptionDAO(em);
        subscriptionDAO.removeAllSubscriptions(pMDoc);
        em.remove(pMDoc);
    }
}
