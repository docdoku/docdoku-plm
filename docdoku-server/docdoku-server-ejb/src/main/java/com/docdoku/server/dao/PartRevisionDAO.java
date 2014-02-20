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
import com.docdoku.core.exceptions.CreationException;
import com.docdoku.core.exceptions.PartRevisionAlreadyExistsException;
import com.docdoku.core.exceptions.PartRevisionNotFoundException;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.product.PartRevisionKey;
import com.docdoku.core.query.SearchQuery;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class PartRevisionDAO {

    private EntityManager em;
    private Locale mLocale;
    private final static int MAX_RESULTS = 500;

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
        em.remove(pPartR);
    }

    public List<PartRevision> findAllCheckedOutPartRevisions(String pWorkspaceId) {
        TypedQuery<PartRevision> query = em.createQuery("SELECT DISTINCT p FROM PartRevision p WHERE p.checkOutUser is not null and p.partMaster.workspace.id = :workspaceId", PartRevision.class);
        query.setParameter("workspaceId", pWorkspaceId);
        return query.getResultList();
    }

    public List<PartRevision> getPartRevisions(String pWorkspaceId, int pStart, int pMaxResults) {
        return em.createNamedQuery("PartRevision.findByWorkspace", PartRevision.class)
                .setParameter("workspaceId", pWorkspaceId)
                .setFirstResult(pStart)
                .setMaxResults(pMaxResults)
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

    public List<PartRevision> searchPartRevisions(String pWorkspaceId, String pNumber, String pName, String pVersion, String pAuthor, String pType, Date pCreationDateFrom, Date pCreationDateTo, List<SearchQuery.AbstractAttributeQuery> pAttrs, Boolean standardPart) {

        StringBuilder queryStr = new StringBuilder();
        queryStr.append("SELECT DISTINCT p FROM PartRevision p ");

        if (pAttrs != null && pAttrs.size() > 0) {
            queryStr.append("JOIN p.partIterations i ");
        }

        queryStr.append("WHERE p.partMaster.workspace.id = :workspaceId ");
        if (pAttrs != null && pAttrs.size() > 0) {
            queryStr.append("AND i.iteration = (SELECT MAX(i2.iteration) FROM PartRevision p2 JOIN p2.partIterations i2 WHERE p2=p) ");
            int i = 0;
            for (SearchQuery.AbstractAttributeQuery attr : pAttrs) {
                queryStr.append("AND EXISTS (");
                if (attr instanceof SearchQuery.DateAttributeQuery) {
                    queryStr.append("SELECT attr").append(i).append(" FROM InstanceDateAttribute attr").append(i).append(" ");
                    queryStr.append("WHERE attr").append(i).append(".dateValue BETWEEN :attrLValue").append(i).append(" AND :attrUValue").append(i).append(" ");
                    queryStr.append("AND attr").append(i).append(" MEMBER OF i.instanceAttributes ");
                    queryStr.append("AND attr").append(i).append(".name = :attrName").append(i++);
                } else if (attr instanceof SearchQuery.TextAttributeQuery) {
                    queryStr.append("SELECT attr").append(i).append(" FROM InstanceTextAttribute attr").append(i).append(" ");
                    queryStr.append("WHERE attr").append(i).append(".textValue  = :attrValue").append(i).append(" ");
                    queryStr.append("AND attr").append(i).append(" MEMBER OF i.instanceAttributes ");
                    queryStr.append("AND attr").append(i).append(".name = :attrName").append(i++);
                } else if (attr instanceof SearchQuery.NumberAttributeQuery) {
                    queryStr.append("SELECT attr").append(i).append(" FROM InstanceNumberAttribute attr").append(i).append(" ");
                    queryStr.append("WHERE ABS(attr").append(i).append(".numberValue - :attrValue").append(i).append(" ) < 0.0001");
                    queryStr.append("AND attr").append(i).append(" MEMBER OF i.instanceAttributes ");
                    queryStr.append("AND attr").append(i).append(".name = :attrName").append(i++);
                } else if (attr instanceof SearchQuery.BooleanAttributeQuery) {
                    queryStr.append("SELECT attr").append(i).append(" FROM InstanceBooleanAttribute attr").append(i).append(" ");
                    queryStr.append("WHERE attr").append(i).append(".booleanValue  = :attrValue").append(i).append(" ");
                    queryStr.append("AND attr").append(i).append(" MEMBER OF i.instanceAttributes ");
                    queryStr.append("AND attr").append(i).append(".name = :attrName").append(i++);
                } else if (attr instanceof SearchQuery.URLAttributeQuery) {
                    queryStr.append("SELECT attr").append(i).append(" FROM InstanceURLAttribute attr").append(i).append(" ");
                    queryStr.append("WHERE attr").append(i).append(".urlValue  = :attrValue").append(i).append(" ");
                    queryStr.append("AND attr").append(i).append(" MEMBER OF i.instanceAttributes ");
                    queryStr.append("AND attr").append(i).append(".name = :attrName").append(i++);
                }
                queryStr.append(") ");
            }

        }
        queryStr.append("AND p.partMaster.number LIKE :number ");
        queryStr.append("AND p.version LIKE :version ");
        queryStr.append("AND p.partMaster.name LIKE :name ");

        if(pType != null)
            queryStr.append("AND p.partMaster.type LIKE :type ");

        if (standardPart != null)
            queryStr.append("AND p.partMaster.standardPart = :standardPart ");

        if (pAuthor != null)
            queryStr.append("AND p.author.login = :author ");

        queryStr.append("AND p.creationDate BETWEEN :lowerDate AND :upperDate ");
        queryStr.append("ORDER BY p.partMaster.number, p.version");

        TypedQuery<PartRevision> query = em.createQuery(queryStr.toString(), PartRevision.class);
        query.setParameter("workspaceId", pWorkspaceId);
        query.setParameter("number", pNumber == null ? "%" : "%" + pNumber + "%");
        query.setParameter("version", pVersion == null ? "%" : "%" + pVersion + "%");
        query.setParameter("name", pName == null ? "%" : "%" + pName + "%");

        if(pType != null)
            query.setParameter("type","%" + pType + "%");

        if (standardPart != null)
            query.setParameter("standardPart", standardPart);

        if (pAuthor != null)
            query.setParameter("author", pAuthor);

        if (pAttrs != null && pAttrs.size() > 0) {
            int i = 0;
            for (SearchQuery.AbstractAttributeQuery attr : pAttrs) {
                if (attr instanceof SearchQuery.TextAttributeQuery) {
                    query.setParameter("attrValue" + i, ((SearchQuery.TextAttributeQuery) attr).getTextValue());
                } else if (attr instanceof SearchQuery.URLAttributeQuery) {
                    query.setParameter("attrValue" + i, ((SearchQuery.URLAttributeQuery) attr).getUrlValue());
                } else if (attr instanceof SearchQuery.NumberAttributeQuery) {
                    query.setParameter("attrValue" + i, ((SearchQuery.NumberAttributeQuery) attr).getNumberValue());
                } else if (attr instanceof SearchQuery.BooleanAttributeQuery) {
                    query.setParameter("attrValue" + i, ((SearchQuery.BooleanAttributeQuery) attr).isBooleanValue());
                } else if (attr instanceof SearchQuery.DateAttributeQuery) {
                    query.setParameter("attrLValue" + i, ((SearchQuery.DateAttributeQuery) attr).getFromDate());
                    query.setParameter("attrUValue" + i, ((SearchQuery.DateAttributeQuery) attr).getToDate());
                }
                query.setParameter("attrName" + (i++), attr.getName());
            }
        }

        query.setParameter("lowerDate", pCreationDateFrom == null ? new Date(0) : pCreationDateFrom);
        if (pCreationDateTo != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(pCreationDateTo);
            cal.set(Calendar.HOUR_OF_DAY, 24);
            cal.set(Calendar.MINUTE, 0);

            pCreationDateTo = cal.getTime();
        } else
            pCreationDateTo = new Date();

        query.setParameter("upperDate", pCreationDateTo);

        query.setMaxResults(MAX_RESULTS);
        return query.getResultList();
    }
}