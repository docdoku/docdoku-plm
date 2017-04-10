/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
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

import com.docdoku.core.exceptions.CreationException;
import com.docdoku.core.exceptions.QueryAlreadyExistsException;
import com.docdoku.core.query.Query;
import com.docdoku.core.query.QueryContext;
import com.docdoku.core.query.QueryRule;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Morgan Guimard on 09/04/15.
 */
public class QueryDAO {

    private EntityManager em;
    private Locale mLocale;


    private static final Logger LOGGER = Logger.getLogger(QueryDAO.class.getName());

    public QueryDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale = pLocale;

    }

    public void createQuery(Query query) throws CreationException, QueryAlreadyExistsException {
        try {

            QueryRule queryRule = query.getQueryRule();

            if(queryRule != null){
                persistQueryRules(queryRule);
            }

            QueryRule pathDataQueryRule = query.getPathDataQueryRule();

            if (pathDataQueryRule != null) {
                persistQueryRules(pathDataQueryRule);
            }

            em.persist(query);
            em.flush();
            persistContexts(query, query.getContexts());
        } catch (EntityExistsException pEEEx) {
            LOGGER.log(Level.FINEST, null, pEEEx);
            throw new QueryAlreadyExistsException(mLocale, query);
        } catch (PersistenceException pPEx) {
            LOGGER.log(Level.FINEST, null, pPEx);
            throw new CreationException(mLocale);
        }
    }

    private void persistContexts(Query query, List<QueryContext> contexts) {
        for (QueryContext context : contexts) {
            context.setParentQuery(query);
            em.persist(context);
        }
        em.flush();
    }

    private void persistQueryRules(QueryRule queryRule) {

        em.persist(queryRule);
        em.flush();

        if (!queryRule.hasSubRules()) {
            return;
        }

        for (QueryRule subRule : queryRule.getSubQueryRules()) {
            subRule.setParentQueryRule(queryRule);
            persistQueryRules(subRule);
        }

    }

    public List<Query> loadQueries(String workspaceId) {
        return em.createNamedQuery("Query.findByWorkspace", Query.class)
                .setParameter("workspaceId", workspaceId)
                .getResultList();
    }

    public Query findQueryByName(String workspaceId, String name) {
        try {
            return em.createNamedQuery("Query.findByWorkspaceAndName", Query.class)
                    .setParameter("workspaceId", workspaceId)
                    .setParameter("name", name)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Query loadQuery(int id) {
        return em.find(Query.class, id);
    }

    public void removeQuery(Query query) {
        em.remove(query);
        em.flush();
    }
}
