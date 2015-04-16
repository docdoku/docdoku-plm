package com.docdoku.server.dao;

import com.docdoku.core.common.Workspace;
import com.docdoku.core.exceptions.CreationException;
import com.docdoku.core.exceptions.QueryAlreadyExistsException;
import com.docdoku.core.meta.*;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.query.Query;
import com.docdoku.core.query.QueryRule;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    private CriteriaQuery cq;
    private Root<PartMaster> pm;
    private Root<PartRevision> pr;
    private Root<PartIteration> pi;

    private Root<InstanceBooleanAttribute> iba;
    private Root<InstanceNumberAttribute> ina;
    private Root<InstanceDateAttribute> ida;
    private Root<InstanceListOfValuesAttribute> ila;
    private Root<InstanceTextAttribute> ita;
    private Root<InstanceURLAttribute> iua;

    private static Logger LOGGER = Logger.getLogger(QueryDAO.class.getName());

    public QueryDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale = pLocale;
        cb = em.getCriteriaBuilder();

        cq = cb.createQuery();
        pm = cq.from(PartMaster.class);
        pr = cq.from(PartRevision.class);
        pi = cq.from(PartIteration.class);

        iba = cq.from(InstanceBooleanAttribute.class);
        ina = cq.from(InstanceNumberAttribute.class);
        ida = cq.from(InstanceDateAttribute.class);
        ila = cq.from(InstanceListOfValuesAttribute.class);
        ita = cq.from(InstanceTextAttribute.class);
        iua = cq.from(InstanceURLAttribute.class);

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

        // Restrict search to workspace
        Expression workspaceExp = pm.get("workspace");
        Predicate rulesPredicate = getPredicate(query.getQueryRule());
        Predicate workspacePredicate = cb.and(cb.equal(workspaceExp,workspace));

        // Join PartMaster
        Join<PartRevision,PartMaster> pmJoin = pr.join("partMaster");
        Predicate prJoinPredicate = pmJoin.on(cb.equal(pm.get("number"), pr.get("partMasterNumber"))).getOn();

        // Join PartIteration
        Join<PartIteration,PartRevision> piJoin = pi.join("partRevision");
        Predicate piJoinPredicate = piJoin.on(cb.equal(pi.get("partRevision").get("partMasterNumber"), pr.get("partMasterNumber"))).getOn();

//        Join<PartIteration,InstanceAttribute> iaJoin = pi.join("instanceAttributes");
//        Predicate iaJoinPredicate = iaJoin.on(????).getOn();

        cq.where(cb.and(new Predicate[]{
            rulesPredicate,
            workspacePredicate,
            prJoinPredicate,
            piJoinPredicate

        }));

        TypedQuery<PartMaster> tp = em.createQuery(cq);
        Set<PartRevision> revisions = new HashSet<>();

        for(PartMaster part : tp.getResultList()){
            revisions.addAll(part.getPartRevisions());
        }

        return new ArrayList<>(revisions);
    }

    private Predicate getPredicate(QueryRule queryRule){

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
        String type = queryRule.getType();

        if(field.startsWith("pm.")){
            return getPartMasterPredicate(field.substring(3), operator, value , type);
        }

        if(field.startsWith("pr.")){
            return getPartRevisionPredicate(field.substring(3), operator, value, type);
        }

//        if(field.startsWith("pi.")){
//            return getPartIterationPredicate(field.substring(3),operator,value, type);
//        }

        if(field.startsWith("author.")){
            return getAuthorPredicate(field.substring(7), operator, value, type);
        }

        if(field.startsWith("attr-TEXT.")){
            return getInstanceTextAttributePredicate(field.substring(10), operator, value, type);
        }

        if(field.startsWith("attr-DATE.")){
            return getInstanceDateAttributePredicate(field.substring(10), operator, value, type);
        }

        if(field.startsWith("attr-BOOLEAN.")){
            return getInstanceBooleanAttributePredicate(field.substring(13), operator, value, type);
        }

        if(field.startsWith("attr-URL.")){
            return getInstanceURLAttributePredicate(field.substring(9), operator, value, type);
        }

        if(field.startsWith("attr-NUMBER.")){
            return getInstanceNumberAttributePredicate(field.substring(10), operator, value, type);
        }

        if(field.startsWith("attr-LOV.")){
            return getInstanceLovAttributePredicate(field.substring(8), operator, value, type);
        }


        throw new IllegalArgumentException();
    }


    private Predicate getAuthorPredicate(String field, String operator, String value, String type) {
        return getPredicate(pr.get("author").get(field),operator,value,type);
    }

    private Predicate getPartRevisionPredicate(String field, String operator, String value, String type) {
        return getPredicate(pm.get(field),operator,value,type);
    }
    private Predicate getPartMasterPredicate(String field, String operator, String value, String type) {
        return getPredicate(pm.get(field),operator,value,type);
    }

    private Predicate getInstanceURLAttributePredicate(String field, String operator, String value, String type) {
        return null;
    }

    private Predicate getInstanceBooleanAttributePredicate(String field, String operator, String value, String type) {
        return null;
    }

    private Predicate getInstanceNumberAttributePredicate(String field, String operator, String value, String type) {
        return null;
    }

    private Predicate getInstanceLovAttributePredicate(String field, String operator, String value, String type) {
        return null;
    }

    private Predicate getInstanceDateAttributePredicate(String field, String operator, String value, String type) {
        return null;
    }

    private Predicate getInstanceTextAttributePredicate(String field, String operator, String value, String type) {
        Predicate namePredicate = getPredicate(ita.get("name"), operator, field, "string");
        Predicate valuePredicate = getPredicate(ita.get("textValue"), operator, value, "string");
        return cb.and(namePredicate,valuePredicate);
    }


    private Predicate getPredicate(Expression fieldExp, String operator, String value, String type){
        Object o;

        switch(type){
            case "string" :
                o=value;
                break;

            case "date":
                try {
                    o = new SimpleDateFormat("yyyy-MM-dd").parse(value);
                } catch (ParseException e) {
                    throw new IllegalArgumentException();                }
                break;

            default :
                o=value;
                break;
        }

        switch (operator){

            case "equal" : return cb.equal(fieldExp,o);
            case "not_equal" : return cb.equal(fieldExp, o).not();

            case "contains" : return cb.like(fieldExp, "%" + o + "%");
            case "not_contains" : return cb.like(fieldExp, "%"+o+"%").not();

            case "begins_with" : return cb.like(fieldExp, o+"%");
            case "not_begins_with" : return  cb.like(fieldExp, o+"%").not();

            case "ends_with" : return cb.like(fieldExp, "%"+o);
            case "not_ends_with" : return cb.like(fieldExp, "%"+o).not();


            case "less": return cb.lessThan(fieldExp,(Date)o);
            case "less_or_equal": return cb.lessThanOrEqualTo(fieldExp,(Date)o);

            case "greater": return cb.greaterThan(fieldExp,(Date)o);
            case "greater_or_equal": return cb.greaterThanOrEqualTo(fieldExp,(Date)o);

            default:
                throw new IllegalArgumentException();
        }

    }
}
