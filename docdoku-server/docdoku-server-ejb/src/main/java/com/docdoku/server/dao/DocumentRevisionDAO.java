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
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.meta.Tag;
import com.docdoku.core.exceptions.CreationException;
import com.docdoku.core.exceptions.DocumentRevisionAlreadyExistsException;
import com.docdoku.core.exceptions.DocumentRevisionNotFoundException;

import javax.persistence.*;
import java.util.List;
import java.util.Locale;

public class DocumentRevisionDAO {

    private EntityManager em;
    private Locale mLocale;
    private final static int MAX_RESULTS = 500;

    public DocumentRevisionDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale = pLocale;
    }

    public DocumentRevisionDAO(EntityManager pEM) {
        em = pEM;
        mLocale = Locale.getDefault();
    }
/*
    public List<DocumentRevision> searchDocumentRevisions(String pWorkspaceId, String pDocMId, String pTitle,
                                                          String pVersion, String pAuthor, String pType, java.util.Date pCreationDateFrom,
                                                          java.util.Date pCreationDateTo, Collection<Tag> pTags, Collection<DocumentSearchQuery.AbstractAttributeQuery> pAttrs) {
        StringBuilder queryStr = new StringBuilder();
        queryStr.append("SELECT DISTINCT r FROM DocumentRevision r ");

        if (pAttrs != null && pAttrs.size() > 0) {
            queryStr.append("JOIN r.documentIterations d ");
        }

        queryStr.append("WHERE r.documentMasterWorkspaceId = :workspaceId ");
        if (pAttrs != null && pAttrs.size() > 0) {
            queryStr.append("AND d.iteration = (SELECT MAX(d2.iteration) FROM DocumentRevision r2 JOIN r2.documentIterations d2 WHERE r2=r) ");
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
        queryStr.append("AND r.documentMasterId LIKE :id ");
        queryStr.append("AND r.version LIKE :version ");
        queryStr.append("AND r.title LIKE :title ");
        queryStr.append("AND r.documentMaster.type LIKE :type ");
        if(pAuthor != null)
            queryStr.append("AND r.author.login = :author ");

        if (pTags != null && pTags.size() > 0) {
            for(int i =0 ;i<pTags.size();i++)
                queryStr.append("AND :tag").append(i).append(" MEMBER OF r.tags ");
        }
        queryStr.append("AND r.creationDate BETWEEN :lowerDate AND :upperDate ");
        queryStr.append("ORDER BY r.documentMasterId, r.version");

        TypedQuery<DocumentRevision> query = em.createQuery(queryStr.toString(), DocumentRevision.class);
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
*/
    public String findLatestDocMId(String pWorkspaceId, String pType) {
        String docMId;
        Query query = em.createQuery("SELECT m.id FROM DocumentMaster m "
                + "WHERE m.workspace.id = :workspaceId "
                + "AND m.type = :type "
                + "AND m.creationDate = ("
                + "SELECT MAX(m2.creationDate) FROM DocumentMaster m2 "
                + "WHERE m2.workspace.id = :workspaceId "
                + "AND m2.type = :type"
                + ")");
        query.setParameter("workspaceId", pWorkspaceId);
        query.setParameter("type", pType);
        docMId = (String) query.getSingleResult();
        return docMId;
    }

    public List<DocumentRevision> findDocRsByFolder(String pCompletePath) {
        TypedQuery<DocumentRevision> query = em.createQuery("SELECT DISTINCT d FROM DocumentRevision d WHERE d.location.completePath = :completePath", DocumentRevision.class);
        query.setParameter("completePath", pCompletePath);
        return query.getResultList();
    }

    public List<DocumentRevision> findDocRsByTag(Tag pTag) {
        TypedQuery<DocumentRevision> query = em.createQuery("SELECT DISTINCT d FROM DocumentRevision d WHERE :tag MEMBER OF d.tags", DocumentRevision.class);
        query.setParameter("tag", pTag);
        return query.getResultList();
    }

    public List<DocumentRevision> findCheckedOutDocRs(User pUser) {
        TypedQuery<DocumentRevision> query = em.createQuery("SELECT DISTINCT d FROM DocumentRevision d WHERE d.checkOutUser = :user", DocumentRevision.class);
        query.setParameter("user", pUser);
        return query.getResultList();
    }

    public DocumentRevision loadDocR(DocumentRevisionKey pKey) throws DocumentRevisionNotFoundException {
        DocumentRevision docR = em.find(DocumentRevision.class, pKey);
        if (docR == null) {
            throw new DocumentRevisionNotFoundException(mLocale, pKey);
        } else {
            return docR;
        }
    }

    public DocumentRevision getDocRRef(DocumentRevisionKey pKey) throws DocumentRevisionNotFoundException {
        try {
            DocumentRevision docR = em.getReference(DocumentRevision.class, pKey);
            return docR;
        } catch (EntityNotFoundException pENFEx) {
            throw new DocumentRevisionNotFoundException(mLocale, pKey);
        }
    }

    public void createDocR(DocumentRevision pDocumentRevision) throws DocumentRevisionAlreadyExistsException, CreationException {
        try {          
            if(pDocumentRevision.getWorkflow()!=null){
                WorkflowDAO workflowDAO = new WorkflowDAO(em);
                workflowDAO.createWorkflow(pDocumentRevision.getWorkflow());
            }

            if(pDocumentRevision.getACL()!=null){
                ACLDAO aclDAO = new ACLDAO(em);
                aclDAO.createACL(pDocumentRevision.getACL());
            }

            //the EntityExistsException is thrown only when flush occurs
            em.persist(pDocumentRevision);
            em.flush();
        } catch (EntityExistsException pEEEx) {
            throw new DocumentRevisionAlreadyExistsException(mLocale, pDocumentRevision);
        } catch (PersistenceException pPEx) {
            //EntityExistsException is case sensitive
            //whereas MySQL is not thus PersistenceException could be
            //thrown instead of EntityExistsException
            throw new CreationException(mLocale);
        }
    }

    public void removeRevision(DocumentRevision pDocR) {
        SubscriptionDAO subscriptionDAO = new SubscriptionDAO(em);
        DocumentDAO docDAO = new DocumentDAO(em);
        subscriptionDAO.removeAllSubscriptions(pDocR);

        WorkflowDAO workflowDAO = new WorkflowDAO(em);
        workflowDAO.removeWorkflowConstraints(pDocR);

        for(DocumentIteration doc:pDocR.getDocumentIterations())
            docDAO.removeDoc(doc);

        SharedEntityDAO sharedEntityDAO = new SharedEntityDAO(em);
        sharedEntityDAO.deleteSharesForDocument(pDocR);

        em.remove(pDocR);
        em.flush();
    }

    public List<DocumentRevision> findDocsWithAssignedTasksForGivenUser(String pWorkspaceId, String assignedUserLogin) {
        return em.createNamedQuery("DocumentRevision.findWithAssignedTasksForUser",DocumentRevision.class).
                setParameter("workspaceId", pWorkspaceId).setParameter("assignedUserLogin", assignedUserLogin).getResultList();
    }

    public List<DocumentRevision> findDocsWithOpenedTasksForGivenUser(String pWorkspaceId, String assignedUserLogin) {
        return em.createNamedQuery("DocumentRevision.findWithOpenedTasksForUser",DocumentRevision.class).
                setParameter("workspaceId", pWorkspaceId).setParameter("assignedUserLogin", assignedUserLogin).getResultList();
    }

    public List<DocumentRevision> findDocsRevisionsWithReferenceLike(String pWorkspaceId, String reference, int maxResults) {
        return em.createNamedQuery("DocumentRevision.findByReference",DocumentRevision.class).
                setParameter("workspaceId", pWorkspaceId).setParameter("id", "%" + reference + "%").setMaxResults(maxResults).getResultList();
    }

    public int getTotalNumberOfDocuments(String pWorkspaceId) {
        return ((Number)em.createNamedQuery("DocumentRevision.countByWorkspace")
                .setParameter("workspaceId", pWorkspaceId)
                .getSingleResult()).intValue();
    }

    public long getDiskUsageForDocumentsInWorkspace(String pWorkspaceId) {
        Number result = ((Number)em.createNamedQuery("BinaryResource.diskUsageInPath")
                .setParameter("path", pWorkspaceId+"/documents/%")
                .getSingleResult());

        return result != null ? result.longValue() : 0L;

    }

    public long getDiskUsageForDocumentTemplatesInWorkspace(String pWorkspaceId) {
        Number result = ((Number)em.createNamedQuery("BinaryResource.diskUsageInPath")
                .setParameter("path", pWorkspaceId+"/document-templates/%")
                .getSingleResult());

        return result != null ? result.longValue() : 0L;

    }

    public List<DocumentRevision> findAllCheckedOutDocRevisions(String pWorkspaceId) {
        TypedQuery<DocumentRevision> query = em.createQuery("SELECT DISTINCT d FROM DocumentRevision d WHERE d.checkOutUser is not null and d.documentMaster.workspace.id = :workspaceId", DocumentRevision.class);
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

    public List<DocumentRevision> getDocumentRevisionsFiltered(User user, String workspaceId, int start, int pMaxResults) {

        String excludedFolders = workspaceId + "/~%";

        return em.createNamedQuery("DocumentRevision.findByWorkspace.filterUserACLEntry", DocumentRevision.class)
                .setParameter("workspaceId", workspaceId)
                .setParameter("user", user)
                .setParameter("excludedFolders", excludedFolders)
                .setFirstResult(start)
                .setMaxResults(pMaxResults)
                .getResultList();
    }

    public int getDocumentRevisionsCountFiltered(User user, String workspaceId) {

        String excludedFolders = workspaceId + "/~%";

        return ((Number) em.createNamedQuery("DocumentRevision.countByWorkspace.filterUserACLEntry")
                .setParameter("workspaceId", workspaceId)
                .setParameter("user", user)
                .setParameter("excludedFolders", excludedFolders)
                .getSingleResult()).intValue();
    }
}
