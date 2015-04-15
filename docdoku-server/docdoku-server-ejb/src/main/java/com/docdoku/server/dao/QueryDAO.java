package com.docdoku.server.dao;

import com.docdoku.core.common.Workspace;
import com.docdoku.core.configuration.ProductInstanceMaster;
import com.docdoku.core.exceptions.CreationException;
import com.docdoku.core.exceptions.QueryAlreadyExistsException;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.query.Query;
import com.docdoku.core.query.QueryRule;

import javax.persistence.*;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.Metamodel;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by morgan on 09/04/15.
 */
public class QueryDAO {


    private EntityManager em;
    private Locale mLocale;
    private CriteriaBuilder cb;
    private Metamodel metamodel;
    private CriteriaQuery cq;
    private Root<PartMaster> pm;
    private Root<PartRevision> pr;
    private Root<ProductInstanceMaster> pi;


    private static Logger LOGGER = Logger.getLogger(QueryDAO.class.getName());

    public QueryDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale = pLocale;
        cb = em.getCriteriaBuilder();
        metamodel = em.getMetamodel();

        cq = cb.createQuery();
        pm = cq.from(PartMaster.class);
        pr = cq.from(PartRevision.class);
        pi = cq.from(ProductInstanceMaster.class);

    }

    public void createQuery(Query query) throws CreationException, QueryAlreadyExistsException {
        try {
            persistQueryRules(query.getQueryRule());
            em.persist(query);
            em.flush();
        }catch (EntityExistsException pEEEx) {
            LOGGER.log(Level.FINEST,null,pEEEx);
            throw new QueryAlreadyExistsException(mLocale, query);
        } catch (PersistenceException pPEx) {
            LOGGER.log(Level.FINEST,null,pPEx);
            throw new CreationException(mLocale);
        }
    }

    private void persistQueryRules(QueryRule queryRule) {

        em.persist(queryRule);
        em.flush();

        if(!queryRule.hasSubRules()){
            return;
        }

        for(QueryRule subRule : queryRule.getSubQueryRules()){
            subRule.setParentQueryRule(queryRule);
            persistQueryRules(subRule);
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

        // Join on pr and pi

        // Restrict search to workspace
        Expression workspaceExp = pm.get("workspace");
        Predicate rulesPredicate = getPredicate(query.getQueryRule());
        Predicate workspacePredicate = cb.and(cb.equal(workspaceExp,workspace));

        Join<PartRevision,PartMaster> prJoin = pr.join("partMaster");
        Predicate prJoinPredicate = prJoin.on(cb.equal(pm.get("number"), pr.get("partMasterNumber"))).getOn();

        cq.where(cb.and(new Predicate[]{
            rulesPredicate,
            workspacePredicate,
            prJoinPredicate
        }));

        TypedQuery<PartMaster> tp = em.createQuery(cq);
        Set<PartRevision> revisions = new HashSet<>();

        for(PartMaster part : tp.getResultList()){
            revisions.addAll(part.getPartRevisions());
        }

        return new ArrayList<>(revisions);
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

        if(field.startsWith("author.")){
            return getPartRevisionAuthorPredicate(field.substring(7),operator, value);
        }

        throw new IllegalArgumentException();
    }

    private Predicate getPartRevisionAuthorPredicate(String field, String operator, String value) {

        Expression fieldExp = pr.get("author").get(field);

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

    private Predicate getPartRevisionPredicate(String field, String operator, String value) {

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

    public Query findQueryByName(String workspaceId, String name) {
        try {
            return em.createNamedQuery("Query.findByWorkspaceAndWorkspace", Query.class)
                    .setParameter("workspaceId", workspaceId)
                    .setParameter("name", name)
                    .getSingleResult();
        }catch (NoResultException e){
            return null;
        }
    }
}
