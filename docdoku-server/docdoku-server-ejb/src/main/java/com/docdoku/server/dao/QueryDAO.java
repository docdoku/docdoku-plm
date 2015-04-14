package com.docdoku.server.dao;

import com.docdoku.core.common.Workspace;
import com.docdoku.core.configuration.ProductInstanceMaster;
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
    private CriteriaBuilder cb;
    private CriteriaQuery cq;
    private Root<PartMaster> pm;
    private Root<PartRevision> pr;
    private Root<ProductInstanceMaster> pi;


    private static Logger LOGGER = Logger.getLogger(QueryDAO.class.getName());

    public QueryDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale = pLocale;
        cb = em.getCriteriaBuilder();

        cq = cb.createQuery();
        pm = cq.from(PartMaster.class);
        pr = cq.from(PartRevision.class);
        pi = cq.from(ProductInstanceMaster.class);

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

    public List<PartRevision> runQuery(Workspace workspace, Query query) {

        // Simple select
        cq.select(pm);

        // Join on pr

        // Restrict search to workspace
        Expression workspaceExp = pm.get("workspace");
        Predicate predicate = getPredicate(query.getQueryRule());
        Predicate workspaceFilter = cb.and(cb.equal(workspaceExp,workspace));
        cq.where(cb.and(new Predicate[]{predicate,workspaceFilter}));

        TypedQuery<PartMaster> tp = em.createQuery(cq);
        List<PartRevision> revisions = new ArrayList<>();

        for(PartMaster part : tp.getResultList()){
            revisions.addAll(part.getPartRevisions());
        }

        return revisions;

    }

    public Predicate getPredicate(QueryRule queryRule){

        String condition = queryRule.getCondition();

        List<QueryRule> subQueryRules = queryRule.getSubQueryRules();

        if(subQueryRules != null){

            Predicate[] predicates = new Predicate[subQueryRules.size()];

            for(int i = 0; i < predicates.length; i++){
                Predicate predicate = getPredicate(subQueryRules.get(i));
                predicates[i] = predicate;
            }

            if("OR".equals(condition)){
                return cb.or(predicates);
            }
            else if("AND".equals(condition)){
                return cb.and(predicates);
            }

            throw new IllegalArgumentException();

        }else{
            return getRulePredicate(queryRule);
        }
    }

    private Predicate getRulePredicate(QueryRule queryRule){

        String field = queryRule.getField();
        String operator = queryRule.getOperator();
        String value = queryRule.getValue();

        if(field.startsWith("pm.")){
            return getPartMasterPredicate(field.substring(3), operator, value);
        }

        if(field.startsWith("pr.")){
            return getPartRevisionPredicate(field.substring(3), operator, value);
        }

        if(field.startsWith("pi.")){
            return getProductInstancePredicate(field.substring(3),operator,value);
        }

        throw new IllegalArgumentException();
    }

    private Predicate getPartRevisionPredicate(String substring, String operator, String value) {
        return null;
    }

    private Predicate getProductInstancePredicate(String field, String operator, String value) {

        return null;
    }

    private Predicate getPartMasterPredicate(String field, String operator, String value) {

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
