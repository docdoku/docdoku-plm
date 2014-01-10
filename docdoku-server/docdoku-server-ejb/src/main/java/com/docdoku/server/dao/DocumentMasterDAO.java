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

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.common.User;
import com.docdoku.core.document.*;
import com.docdoku.core.exceptions.CreationException;
import com.docdoku.core.exceptions.DocumentMasterAlreadyExistsException;
import com.docdoku.core.exceptions.DocumentMasterNotFoundException;

import javax.persistence.*;
import java.util.*;

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
            java.util.Date pCreationDateTo, Collection<Tag> pTags, Collection<DocumentSearchQuery.AbstractAttributeQuery> pAttrs) {
        StringBuilder queryStr = new StringBuilder();
        queryStr.append("SELECT DISTINCT m FROM DocumentMaster m ");

        if (pAttrs != null && pAttrs.size() > 0) {
            queryStr.append("JOIN m.documentIterations d ");
        }

        queryStr.append("WHERE m.workspaceId = :workspaceId ");
        if (pAttrs != null && pAttrs.size() > 0) {
            queryStr.append("AND d.iteration = (SELECT MAX(d2.iteration) FROM DocumentMaster m2 JOIN m2.documentIterations d2 WHERE m2=m) ");
            int i=0;
            for(DocumentSearchQuery.AbstractAttributeQuery attr:pAttrs){
                queryStr.append("AND EXISTS (");
                if(attr instanceof DocumentSearchQuery.DateAttributeQuery){
                    queryStr.append("SELECT attr").append(i).append(" FROM InstanceDateAttribute attr").append(i).append(" ");
                    queryStr.append("WHERE attr").append(i).append(".dateValue BETWEEN :attrLValue").append(i).append(" AND :attrUValue").append(i).append(" ");
                    queryStr.append("AND attr").append(i).append(" MEMBER OF d.instanceAttributes ");
                    queryStr.append("AND attr").append(i).append(".name = :attrName").append(i++);
                }else if(attr instanceof DocumentSearchQuery.TextAttributeQuery){
                    queryStr.append("SELECT attr").append(i).append(" FROM InstanceTextAttribute attr").append(i).append(" ");
                    queryStr.append("WHERE attr").append(i).append(".textValue  = :attrValue").append(i).append(" ");
                    queryStr.append("AND attr").append(i).append(" MEMBER OF d.instanceAttributes ");
                    queryStr.append("AND attr").append(i).append(".name = :attrName").append(i++);
                }else if(attr instanceof DocumentSearchQuery.NumberAttributeQuery){
                    queryStr.append("SELECT attr").append(i).append(" FROM InstanceNumberAttribute attr").append(i).append(" ");
                    queryStr.append("WHERE ABS(attr").append(i).append(".numberValue - :attrValue").append(i).append(" ) < 0.0001");
                    queryStr.append("AND attr").append(i).append(" MEMBER OF d.instanceAttributes ");
                    queryStr.append("AND attr").append(i).append(".name = :attrName").append(i++);
                }else if(attr instanceof DocumentSearchQuery.BooleanAttributeQuery){
                    queryStr.append("SELECT attr").append(i).append(" FROM InstanceBooleanAttribute attr").append(i).append(" ");
                    queryStr.append("WHERE attr").append(i).append(".booleanValue  = :attrValue").append(i).append(" ");
                    queryStr.append("AND attr").append(i).append(" MEMBER OF d.instanceAttributes ");
                    queryStr.append("AND attr").append(i).append(".name = :attrName").append(i++);
                }else if(attr instanceof DocumentSearchQuery.URLAttributeQuery){
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
            for(DocumentSearchQuery.AbstractAttributeQuery attr:pAttrs){
                if(attr instanceof DocumentSearchQuery.TextAttributeQuery){
                    query.setParameter("attrValue" + i, ((DocumentSearchQuery.TextAttributeQuery)attr).getTextValue());
                }else if(attr instanceof DocumentSearchQuery.URLAttributeQuery){
                    query.setParameter("attrValue" + i, ((DocumentSearchQuery.URLAttributeQuery)attr).getUrlValue());
                }else if(attr instanceof DocumentSearchQuery.NumberAttributeQuery){
                    query.setParameter("attrValue" + i, ((DocumentSearchQuery.NumberAttributeQuery)attr).getNumberValue());
                }else if(attr instanceof DocumentSearchQuery.BooleanAttributeQuery){
                    query.setParameter("attrValue" + i, ((DocumentSearchQuery.BooleanAttributeQuery)attr).isBooleanValue());
                }else if(attr instanceof DocumentSearchQuery.DateAttributeQuery){
                    query.setParameter("attrLValue" + i, ((DocumentSearchQuery.DateAttributeQuery)attr).getFromDate());
                    query.setParameter("attrUValue" + i, ((DocumentSearchQuery.DateAttributeQuery)attr).getToDate());
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
            return em.getReference(DocumentMaster.class, pKey);
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

            if(pDocumentMaster.getACL()!=null){
                ACLDAO aclDAO = new ACLDAO(em);
                aclDAO.createACL(pDocumentMaster.getACL());
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
        DocumentDAO docDAO = new DocumentDAO(em);
        subscriptionDAO.removeAllSubscriptions(pDocM);

        WorkflowDAO workflowDAO = new WorkflowDAO(em);
        workflowDAO.removeWorkflowConstraints(pDocM);

        for(DocumentIteration doc:pDocM.getDocumentIterations())
            docDAO.removeDoc(doc);

        SharedEntityDAO sharedEntityDAO = new SharedEntityDAO(em);
        sharedEntityDAO.deleteSharesForDocument(pDocM);

        em.remove(pDocM);
    }

    public List<DocumentMaster> findDocWithAssignedTasksForGivenUser(String pWorkspaceId, String assignedUserLogin) {
        return em.createNamedQuery("findDocumentMastersWithAssignedTasksForGivenUser").
                setParameter("workspaceId", pWorkspaceId).setParameter("assignedUserLogin", assignedUserLogin).getResultList();
    }

    public List<DocumentMaster> findDocWithOpenedTasksForGivenUser(String pWorkspaceId, String assignedUserLogin) {
        return em.createNamedQuery("findDocumentMastersWithOpenedTasksForGivenUser").
                setParameter("workspaceId", pWorkspaceId).setParameter("assignedUserLogin", assignedUserLogin).getResultList();
    }

    public List<DocumentMaster> findDocMsWithReferenceLike(String pWorkspaceId, String reference, int maxResults) {
        return em.createNamedQuery("findDocumentMastersWithReference").
                setParameter("workspaceId", pWorkspaceId).setParameter("id", "%" + reference + "%").setMaxResults(maxResults).getResultList();
    }

    public int getDocumentsCountInWorkspace(String pWorkspaceId) {
        return ((Number)em.createNamedQuery("countDocumentMastersInWorkspace")
                .setParameter("workspaceId", pWorkspaceId)
                .getSingleResult()).intValue();
    }

    public Long getDiskUsageForDocumentsInWorkspace(String pWorkspaceId) {
        Number result = ((Number)em.createNamedQuery("BinaryResource.diskUsageInPath")
                .setParameter("path", pWorkspaceId+"/documents/%")
                .getSingleResult());

        return result != null ? result.longValue() : 0L;

    }

    public Long getDiskUsageForDocumentTemplatesInWorkspace(String pWorkspaceId) {
        Number result = ((Number)em.createNamedQuery("BinaryResource.diskUsageInPath")
                .setParameter("path", pWorkspaceId+"/document-templates/%")
                .getSingleResult());

        return result != null ? result.longValue() : 0L;

    }

    public List<DocumentMaster> findAllCheckedOutDocMs(String pWorkspaceId) {
        TypedQuery<DocumentMaster> query = em.createQuery("SELECT DISTINCT m FROM DocumentMaster m WHERE m.checkOutUser is not null and m.workspace.id = :workspaceId", DocumentMaster.class);
        query.setParameter("workspaceId", pWorkspaceId);
        return query.getResultList();
    }

    public DocumentIteration findDocumentIterationByBinaryResource(BinaryResource pBinaryResource) {
        TypedQuery<DocumentIteration> query = em.createNamedQuery("DocumentIteration.findByBinaryResource", DocumentIteration.class);
        query.setParameter("binaryResource", pBinaryResource);
        try{
            return query.getSingleResult();
        }catch(NoResultException ex){
            return null;
        }
    }

    public List<DocumentMaster> getDocumentsMasterFiltered(User user, String workspaceId, int start, int pMaxResults) {

        String excludedFolders = workspaceId + "/~%";

        return em.createNamedQuery("DocumentMaster.findByWorkspace.filterUserACLEntry", DocumentMaster.class)
                .setParameter("workspaceId", workspaceId)
                .setParameter("user", user)
                .setParameter("excludedFolders", excludedFolders)
                .setFirstResult(start)
                .setMaxResults(pMaxResults)
                .getResultList();
    }

    public int getDocumentsMasterCountFiltered(User user, String workspaceId) {

        String excludedFolders = workspaceId + "/~%";

        return ((Number) em.createNamedQuery("DocumentMaster.countByWorkspace.filterUserACLEntry")
                .setParameter("workspaceId", workspaceId)
                .setParameter("user", user)
                .setParameter("excludedFolders", excludedFolders)
                .getSingleResult()).intValue();
    }

    public List<DocumentMaster> getAllByWorkspace(String workspaceId) {
        List<DocumentMaster> documentMasters = em.createNamedQuery("DocumentMaster.findByWorkspace")
                .setParameter("workspaceId",workspaceId)
                .getResultList();
        return documentMasters;
    }
}
