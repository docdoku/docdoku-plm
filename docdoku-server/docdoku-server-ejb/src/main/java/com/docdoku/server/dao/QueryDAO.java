package com.docdoku.server.dao;

import com.docdoku.core.product.PartRevision;
import com.docdoku.core.query.Query;
import com.docdoku.core.query.QueryRule;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
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
//        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
//
//        Root<PartMaster> pm = cq.from(PartMaster.class);
//        Root<ProductInstanceMaster> pi = cq.from(ProductInstanceMaster.class);
//
//        Expression<String> number = pm.get("number");
//        Expression<String> type = pm.get("type");
//        Expression<String> name = pm.get("name");
//        Expression<String> serialNumber = pi.get("serialNumber");
//
//        cq.multiselect(number.alias("pm.number"));
//        cq.multiselect(serialNumber.alias("pi.serial"));
//
//        cq.where(getPredicate(query.getQueryRule()));
//
//        cq.orderBy(cb.desc(number));
//
//        TypedQuery<Tuple> tq = em.createQuery(cq);
//        for (Tuple t : tq.getResultList()) {
//            System.out.println(t.get("foo"));
//        }

        return new ArrayList<>();

    }


    public Predicate getPredicate(QueryRule queryRule){

//        String condition = queryRule.getCondition();
//
//        if(condition == "OR"){
//
//        }else if(condition == "AND"){
//
//        }
//
//        Expression<String> number = root.get("number");

        return null;

    }


    public List<Query> loadQueries(String workspaceId) {
        return em.createNamedQuery("Query.findByWorkspace", Query.class)
                .setParameter("workspaceId", workspaceId)
                .getResultList();
    }
}
