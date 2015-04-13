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

        cq.select(pm);

        Predicate predicate = getPredicate(query.getQueryRule(), pm,cb);
        cq.where(predicate);
        //cq.orderBy();
        TypedQuery<PartMaster> tp = em.createQuery(cq);

        List<PartRevision> revisions = new ArrayList<>();

        List<PartMaster> resultList = tp.getResultList();

        for(PartMaster part : resultList){
            if(part.getWorkspaceId().equals(workspaceId)){
                revisions.addAll(part.getPartRevisions());
            }
        }

        return revisions;

    }

    public Predicate getPredicate(QueryRule queryRule, Root<PartMaster> pm, CriteriaBuilder cb){

        String condition = queryRule.getCondition();

        List<QueryRule> subQueryRules = queryRule.getSubQueryRules();

        if(subQueryRules != null){

            Predicate[] predicates = new Predicate[subQueryRules.size()];

            for(int i = 0; i < predicates.length; i++){
                Predicate predicate = getPredicate(subQueryRules.get(i),pm,cb);
                predicates[i] = predicate;
            }

            if("OR".equals(condition)){
                return cb.or(predicates);
            }
            else if("AND".equals(condition)){
                return cb.and(predicates);
            }
            else{
                // WTF ?
                return null;
            }

        }else{
            return getRulePredicate(queryRule,pm,cb);
        }
    }

    private Predicate getRulePredicate(QueryRule queryRule, Root<PartMaster> pm, CriteriaBuilder cb){
        String field = queryRule.getField();
        String operator = queryRule.getOperator();
        String value = queryRule.getValue();


        if(field.startsWith("p.")){
            return getPartMasterPredicate(field.substring(2), operator, value, pm, cb);
        }

        if(field.startsWith("pi.")){
            return getProductInstancePredicate(field.substring(3),operator,value, pm, cb);
        }

        throw new IllegalArgumentException();
    }

    private Predicate getProductInstancePredicate(String field, String operator, String value, Root<PartMaster> pm, CriteriaBuilder cb) {

        return null;
    }

    private Predicate getPartMasterPredicate(String field, String operator, String value, Root<PartMaster> pm, CriteriaBuilder cb) {

        // var stringDefaultOps =
        // ['equal', 'not_equal', 'contains', 'not_contains', 'begins_with', 'not_begins_with', 'ends_with', 'not_ends_with'];
        // var dateOperators =
        // ['equal', 'not_equal', 'less', 'less_or_equal', 'greater', 'greater_or_equal', 'between'];

        Expression fieldExp = pm.get(field);

        switch (operator){

            case "equal" : return cb.equal(fieldExp,value);
            case "not_equal" : return cb.equal(fieldExp, value).not();

            case "contains" : return cb.like(fieldExp, "%" + value + "%");
            case "not_contains" : return cb.like(fieldExp, "%"+value+"%").not();

            case "begins_with" : return cb.like(fieldExp, value+"%");
            case "not_begins_with" : return  cb.like(fieldExp, value+"%").not();

            case "ends_with" : return cb.like(fieldExp, "%"+value);
            case "not_ends_with" : return cb.like(fieldExp, "%"+value).not();

            default:
                throw new IllegalArgumentException();
        }


    }

    public List<Query> loadQueries(String workspaceId) {
        return em.createNamedQuery("Query.findByWorkspace", Query.class)
                .setParameter("workspaceId", workspaceId)
                .getResultList();
    }
}
