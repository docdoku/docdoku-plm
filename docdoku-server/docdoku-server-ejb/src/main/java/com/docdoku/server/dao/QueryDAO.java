package com.docdoku.server.dao;

import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.query.Query;
import com.docdoku.core.query.QueryRule;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
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

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery cq = cb.createQuery();
        Root<PartMaster> pm = cq.from(PartMaster.class);

        Expression<String> number = pm.get("number");
        Expression<String> name = pm.get("name");
        Expression<String> type = pm.get("type");

        cq.multiselect( number.alias("pm.number"),
                        name.alias("pm.name"),
                        type.alias("pm.type."));

        Predicate predicate = getPredicate(query.getQueryRule(), cq);
        cq.where(predicate);
        cq.orderBy(cb.desc(number));
        TypedQuery<PartMaster> tp = em.createQuery(cq);

        List<PartRevision> revisions = new ArrayList<>();

        List<PartMaster> resultList = tp.getResultList();
        for(PartMaster part : resultList){
            revisions.addAll(part.getPartRevisions());
        }

        return revisions;

    }


    public Predicate getPredicate(QueryRule queryRule, CriteriaQuery cq){

        CriteriaBuilder cb = em.getCriteriaBuilder();
        String condition = queryRule.getCondition();

        List<QueryRule> subQueryRules = queryRule.getSubQueryRules();

        if(subQueryRules != null){

            Predicate[] predicates = new Predicate[subQueryRules.size()];

            for(int i = 0; i < predicates.length; i++){
                Predicate predicate = getPredicate(subQueryRules.get(i),cq);
                predicates[i] = predicate;
            }

            if("OR".equals(condition)){
                return cb.or(predicates);
            }
            else if("AND".equals(condition)){
                return cb.and(predicates);
            }
            else{
                return null;
            }

        }else{

            String field = queryRule.getField();
            String operator = queryRule.getOperator();
            String value = queryRule.getValue();

            Root<PartMaster> pm = cq.from(PartMaster.class);

            if("p.number".equals(field)){
                Expression<String> number = pm.get("number");
                return number.in(value);
            }

            return null;
        }


    }


    public List<Query> loadQueries(String workspaceId) {
        return em.createNamedQuery("Query.findByWorkspace", Query.class)
                .setParameter("workspaceId", workspaceId)
                .getResultList();
    }
}
