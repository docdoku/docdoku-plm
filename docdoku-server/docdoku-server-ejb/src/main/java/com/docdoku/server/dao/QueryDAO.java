package com.docdoku.server.dao;

import com.docdoku.core.product.PartRevision;
import com.docdoku.core.query.Query;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by morgan on 09/04/15.
 */
public class QueryDAO {


    private EntityManager em;
    private Locale mLocale;

    private static Logger LOGGER = Logger.getLogger(QueryDAO.class.getName());

    public QueryDAO(EntityManager pEM) {
        em = pEM;
    }

    public QueryDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale = pLocale;
    }

    public void createQuery(Query query){
        try {
            em.persist(query);
            em.flush();
        }catch (Exception e){
            LOGGER.log(Level.SEVERE,"Fail to create query",e);
        }
    }

    public Query loadQuery(int id) {
        Query query = em.find(Query.class, id);
        return query;
    }

    public void removeQuery(Query query){
        em.remove(query);
        em.flush();
    }

    public List<PartRevision> runQuery(String workspaceId, Query query) {

//        CriteriaBuilder cb = em.getCriteriaBuilder();
//
//        CriteriaQuery<PartRevision> q = cb.createQuery(PartRevision.class);
//        Root<PartRevision> c = q.from(PartRevision.class);
//        CriteriaQuery<PartRevision> select = q.select(c);

        // TODO create query
        return new ArrayList<>();

    }

    public List<Query> loadQueries(String workspaceId) {
        return em.createNamedQuery("Query.findByWorkspace", Query.class)
                .setParameter("workspaceId", workspaceId)
                .getResultList();
    }
}
