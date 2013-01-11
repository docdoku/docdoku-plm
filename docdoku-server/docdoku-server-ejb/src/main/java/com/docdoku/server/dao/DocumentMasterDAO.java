/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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

import com.docdoku.core.services.DocumentMasterNotFoundException;
import com.docdoku.core.services.DocumentMasterAlreadyExistsException;
import com.docdoku.core.services.CreationException;
import com.docdoku.core.*;
import com.docdoku.core.workflow.Activity;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.document.Tag;
import com.docdoku.core.workflow.Task;
import com.docdoku.core.workflow.Workflow;
import com.docdoku.core.document.DocumentMasterKey;
import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.document.SearchQuery;
import com.docdoku.core.common.User;
import java.util.*;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

public class DocumentMasterDAO {

    private EntityManager em;
    private Locale mLocale;
    private final static int MAX_RESULTS = 500;

    public DocumentMasterDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale = pLocale;
    }

    public DocumentMasterDAO(EntityManager pEM) {
        em = pEM;
        mLocale = Locale.getDefault();
    }

    public List<DocumentMaster> searchDocumentMasters(String pWorkspaceId, String pDocMId, String pTitle,
            String pVersion, String pAuthor, String pType, java.util.Date pCreationDateFrom,
            java.util.Date pCreationDateTo, Collection<Tag> pTags, Collection<SearchQuery.AbstractAttributeQuery> pAttrs) {
        StringBuilder queryStr = new StringBuilder();
        queryStr.append("SELECT DISTINCT m FROM DocumentMaster m ");

        if (pAttrs != null && pAttrs.size() > 0) {
            queryStr.append("JOIN m.documentIterations d ");
        }

        queryStr.append("WHERE m.workspaceId = :workspaceId ");
        if (pAttrs != null && pAttrs.size() > 0) {
            queryStr.append("AND d.iteration = (SELECT MAX(d2.iteration) FROM DocumentMaster m2 JOIN m2.documentIterations d2 WHERE m2=m) ");
            int i=0;
            for(SearchQuery.AbstractAttributeQuery attr:pAttrs){
                queryStr.append("AND EXISTS (");
                if(attr instanceof SearchQuery.DateAttributeQuery){
                    queryStr.append("SELECT attr").append(i).append(" FROM InstanceDateAttribute attr").append(i).append(" ");
                    queryStr.append("WHERE attr").append(i).append(".dateValue BETWEEN :attrLValue").append(i).append(" AND :attrUValue").append(i).append(" ");
                    queryStr.append("AND attr").append(i).append(" MEMBER OF d.instanceAttributes ");
                    queryStr.append("AND attr").append(i).append(".name = :attrName").append(i++);
                }else if(attr instanceof SearchQuery.TextAttributeQuery){
                    queryStr.append("SELECT attr").append(i).append(" FROM InstanceTextAttribute attr").append(i).append(" ");
                    queryStr.append("WHERE attr").append(i).append(".textValue  = :attrValue").append(i).append(" ");
                    queryStr.append("AND attr").append(i).append(" MEMBER OF d.instanceAttributes ");
                    queryStr.append("AND attr").append(i).append(".name = :attrName").append(i++);
                }else if(attr instanceof SearchQuery.NumberAttributeQuery){
                    queryStr.append("SELECT attr").append(i).append(" FROM InstanceNumberAttribute attr").append(i).append(" ");
                    queryStr.append("WHERE attr").append(i).append(".numberValue  = :attrValue").append(i).append(" ");
                    queryStr.append("AND attr").append(i).append(" MEMBER OF d.instanceAttributes ");
                    queryStr.append("AND attr").append(i).append(".name = :attrName").append(i++);
                }else if(attr instanceof SearchQuery.BooleanAttributeQuery){
                    queryStr.append("SELECT attr").append(i).append(" FROM InstanceBooleanAttribute attr").append(i).append(" ");
                    queryStr.append("WHERE attr").append(i).append(".booleanValue  = :attrValue").append(i).append(" ");
                    queryStr.append("AND attr").append(i).append(" MEMBER OF d.instanceAttributes ");
                    queryStr.append("AND attr").append(i).append(".name = :attrName").append(i++);
                }else if(attr instanceof SearchQuery.URLAttributeQuery){
                    queryStr.append("SELECT attr").append(i).append(" FROM InstanceURLAttribute attr").append(i).append(" ");
                    queryStr.append("WHERE attr").append(i).append(".urlValue  = :attrValue").append(i).append(" ");
                    queryStr.append("AND attr").append(i).append(" MEMBER OF d.instanceAttributes ");
                    queryStr.append("AND attr").append(i).append(".name = :attrName").append(i++);
                }
                queryStr.append(") ");
            }
            
        }
        queryStr.append("AND m.id LIKE :id ");
        queryStr.append("AND m.version LIKE :version ");
        queryStr.append("AND m.title LIKE :title ");
        queryStr.append("AND m.type LIKE :type ");
        if(pAuthor != null)
            queryStr.append("AND m.author.login = :author ");

        if (pTags != null && pTags.size() > 0) {
            for(int i =0 ;i<pTags.size();i++)
                queryStr.append("AND :tag").append(i).append(" MEMBER OF m.tags ");
        }
        queryStr.append("AND m.creationDate BETWEEN :lowerDate AND :upperDate ");
        queryStr.append("ORDER BY m.id, m.version");

        TypedQuery<DocumentMaster> query = em.createQuery(queryStr.toString(), DocumentMaster.class);
        query.setParameter("workspaceId", pWorkspaceId);
        query.setParameter("id", pDocMId == null ? "%" : "%" + pDocMId + "%");
        query.setParameter("version", pVersion == null ? "%" : "%" + pVersion + "%");
        query.setParameter("title", pTitle == null ? "%" : "%" + pTitle + "%");
        query.setParameter("type", pType == null ? "%" : "%" + pType + "%");
        if(pAuthor != null)
            query.setParameter("author", pAuthor);

        
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
                    query.setParameter("attrValue" + i, ((SearchQuery.NumberAttributeQuery)attr).getNumberValue());
                }else if(attr instanceof SearchQuery.BooleanAttributeQuery){
                    query.setParameter("attrValue" + i, ((SearchQuery.BooleanAttributeQuery)attr).isBooleanValue());
                }else if(attr instanceof SearchQuery.DateAttributeQuery){
                    query.setParameter("attrLValue" + i, ((SearchQuery.DateAttributeQuery)attr).getFromDate());
                    query.setParameter("attrUValue" + i, ((SearchQuery.DateAttributeQuery)attr).getToDate());
                }
                query.setParameter("attrName" + (i++), attr.getName());
            }
        }

        query.setParameter("lowerDate", pCreationDateFrom == null ? new Date(0) : pCreationDateFrom);
        if(pCreationDateTo!=null){
            Calendar cal = Calendar.getInstance();
            cal.setTime(pCreationDateTo);
            cal.set(Calendar.HOUR_OF_DAY, 24);
            cal.set(Calendar.MINUTE, 0);

            pCreationDateTo=cal.getTime();
        }else
            pCreationDateTo=new Date();

        query.setParameter("upperDate", pCreationDateTo);

        query.setMaxResults(MAX_RESULTS);
        return query.getResultList();

    }

    public String findLatestDocMId(String pWorkspaceId, String pType) {
        String docMId;
        Query query = em.createQuery("SELECT m.id FROM DocumentMaster m "
                + "WHERE m.workspaceId = :workspaceId "
                + "AND m.type = :type "
                + "AND m.version = :version "
                + "AND m.creationDate = ("
                + "SELECT MAX(m2.creationDate) FROM DocumentMaster m2 "
                + "WHERE m2.workspaceId = :workspaceId "
                + "AND m2.type = :type "
                + "AND m2.version = :version"
                + ")");
        query.setParameter("workspaceId", pWorkspaceId);
        query.setParameter("type", pType);
        query.setParameter("version", "A");
        docMId = (String) query.getSingleResult();
        return docMId;
    }

    public List<DocumentMaster> findDocMsByFolder(String pCompletePath) {
        TypedQuery<DocumentMaster> query = em.createQuery("SELECT DISTINCT m FROM DocumentMaster m WHERE m.location.completePath = :completePath", DocumentMaster.class);
        query.setParameter("completePath", pCompletePath);
        return query.getResultList();
    }

    public List<DocumentMaster> findDocMsByTag(Tag pTag) {
        TypedQuery<DocumentMaster> query = em.createQuery("SELECT DISTINCT m FROM DocumentMaster m WHERE :tag MEMBER OF m.tags", DocumentMaster.class);
        query.setParameter("tag", pTag);
        return query.getResultList();
    }

    public List<DocumentMaster> findCheckedOutDocMs(User pUser) {
        TypedQuery<DocumentMaster> query = em.createQuery("SELECT DISTINCT m FROM DocumentMaster m WHERE m.checkOutUser = :user", DocumentMaster.class);
        query.setParameter("user", pUser);
        return query.getResultList();
    }

    public DocumentMaster loadDocM(DocumentMasterKey pKey) throws DocumentMasterNotFoundException {
        DocumentMaster docM = em.find(DocumentMaster.class, pKey);
        if (docM == null) {
            throw new DocumentMasterNotFoundException(mLocale, pKey);
        } else {
            return docM;
        }
    }

    public DocumentMaster getDocMRef(DocumentMasterKey pKey) throws DocumentMasterNotFoundException {
        try {
            DocumentMaster docM = em.getReference(DocumentMaster.class, pKey);
            return docM;
        } catch (EntityNotFoundException pENFEx) {
            throw new DocumentMasterNotFoundException(mLocale, pKey);
        }
    }

    public void createDocM(DocumentMaster pDocumentMaster) throws DocumentMasterAlreadyExistsException, CreationException {
        try {          
            if(pDocumentMaster.getWorkflow()!=null){
                WorkflowDAO workflowDAO = new WorkflowDAO(em);
                workflowDAO.createWorkflow(pDocumentMaster.getWorkflow());
            }
            //the EntityExistsException is thrown only when flush occurs
            em.persist(pDocumentMaster);
            em.flush();
        } catch (EntityExistsException pEEEx) {
            throw new DocumentMasterAlreadyExistsException(mLocale, pDocumentMaster);
        } catch (PersistenceException pPEx) {
            //EntityExistsException is case sensitive
            //whereas MySQL is not thus PersistenceException could be
            //thrown instead of EntityExistsException
            throw new CreationException(mLocale);
        }
    }

    public void removeDocM(DocumentMaster pDocM) {
        SubscriptionDAO subscriptionDAO = new SubscriptionDAO(em);
        subscriptionDAO.removeAllSubscriptions(pDocM);
        em.remove(pDocM);
    }
}
