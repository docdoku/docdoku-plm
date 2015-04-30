package com.docdoku.server.dao;

import com.docdoku.core.common.Workspace;
import com.docdoku.core.exceptions.CreationException;
import com.docdoku.core.exceptions.QueryAlreadyExistsException;
import com.docdoku.core.meta.*;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.query.Query;
import com.docdoku.core.query.QueryContext;
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


    private static Logger LOGGER = Logger.getLogger(QueryDAO.class.getName());

    public QueryDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale = pLocale;
        cb = em.getCriteriaBuilder();

        cq = cb.createQuery();
        pm = cq.from(PartMaster.class);
        pr = cq.from(PartRevision.class);
        pi = cq.from(PartIteration.class);
    }

    public void createQuery(Query query) throws CreationException, QueryAlreadyExistsException {
        try {
            persistQueryRules(query.getQueryRule());
            em.persist(query);
            em.flush();
            persistContexts(query,query.getContexts());
        }catch (EntityExistsException pEEEx) {
            LOGGER.log(Level.FINEST,null,pEEEx);
            throw new QueryAlreadyExistsException(mLocale, query);
        } catch (PersistenceException pPEx) {
            LOGGER.log(Level.FINEST,null,pPEx);
            throw new CreationException(mLocale);
        }
    }

    private void persistContexts(Query query, List<QueryContext> contexts) {
        for(QueryContext context:contexts){
            context.setParentQuery(query);
            em.persist(context);
        }
        em.flush();
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
        cq.select(pr);

        // Restrict search to workspace
        Expression workspaceExp = pm.get("workspace");
        Predicate rulesPredicate = null;
        String firstCondition = query.getQueryRule().getCondition();
        if ( firstCondition != null) {
             rulesPredicate = getPredicate(query.getQueryRule());
        }
        Predicate workspacePredicate = cb.and(cb.equal(workspaceExp, workspace));

        // Join PartMaster
        Predicate prJoinPredicate = cb.and(cb.equal(pm.get("number"), pr.get("partMasterNumber")), cb.equal(pm.get("workspace"), workspace));

        // Join PartIteration
        Join<PartIteration,PartRevision> piJoin = pi.join("partRevision");
        Predicate piJoinPredicate = piJoin.on(cb.and(cb.equal(pi.get("partRevision").get("partMasterNumber"), pr.get("partMasterNumber")), cb.equal(pr.get("partMaster").get("workspace"), workspace))).getOn();


        if ( firstCondition != null) {
            cq.where(cb.and(
                    rulesPredicate,
                    workspacePredicate,
                    prJoinPredicate,
                    piJoinPredicate
            ));
        }else{
            cq.where(cb.and(
                    workspacePredicate,
                    prJoinPredicate,
                    piJoinPredicate
            ));
        }


        TypedQuery<PartRevision> tp = em.createQuery(cq);
        Set<PartRevision> revisions = new HashSet<>();

        for(PartRevision part : tp.getResultList()){
            if(part.getLastCheckedInIteration() != null) {
                revisions.add(part);
            }
        }

        return new ArrayList<>(revisions);
    }

    private Predicate getPredicate(QueryRule queryRule){

        String condition = queryRule.getCondition();

        List<QueryRule> subQueryRules = queryRule.getSubQueryRules();

        if(subQueryRules != null && subQueryRules.size() > 0){

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
            return getInstanceNumberAttributePredicate(field.substring(12), operator, value, type);
        }

        if(field.startsWith("attr-LOV.")){
            return getInstanceLovAttributePredicate(field.substring(9), operator, value, type);
        }

        throw new IllegalArgumentException();
    }


    private Predicate getAuthorPredicate(String field, String operator, String value, String type) {
        return getPredicate(pr.get("author").get(field),operator,value,type);
    }

    private Predicate getPartRevisionPredicate(String field, String operator, String value, String type) {
        if(field.equals("status")){
            return getPredicate(pr.get(field),operator,PartRevision.RevisionStatus.valueOf(value),"");
        } else if(field.equals("tags")){
            return getTagsPredicate(value);
        }
        return getPredicate(pr.get(field), operator, value, type);
    }

    private Predicate getTagsPredicate(String value) {
        Root<Tag> tag = cq.from(Tag.class);
        Predicate prPredicate = tag.in(pr.get("tags"));
        Predicate valuePredicate = cb.equal(tag.get("label"),value);
        return cb.and(prPredicate, valuePredicate);
    }

    private Predicate getPartMasterPredicate(String field, String operator, String value, String type) {
        return getPredicate(pm.get(field), operator, value, type);
    }

    // Instances Attributes
    private Predicate getInstanceURLAttributePredicate(String field, String operator, String value, String type) {
        Root<InstanceURLAttribute> iua = cq.from(InstanceURLAttribute.class);
        Predicate valuePredicate = getPredicate(iua.get("urlValue"), operator, value, "string");
        Predicate memberPredicate = iua.in(pi.get("instanceAttributes"));
        return cb.and(cb.equal(iua.get("name"), field), valuePredicate, memberPredicate);
    }

    private Predicate getInstanceBooleanAttributePredicate(String field, String operator, String value, String type) {
        Root<InstanceBooleanAttribute> iba = cq.from(InstanceBooleanAttribute.class);
        Predicate memberPredicate = iba.in(pi.get("instanceAttributes"));
        return cb.and(cb.equal(iba.get("name"),field),cb.equal(iba.get("booleanValue"),Boolean.parseBoolean(value)),memberPredicate);
    }

    private Predicate getInstanceNumberAttributePredicate(String field, String operator, String value, String type) {
        Root<InstanceNumberAttribute> ina = cq.from(InstanceNumberAttribute.class);
        Predicate valuePredicate = getPredicate(ina.get("numberValue"), operator, value, "double");
        Predicate memberPredicate = ina.in(pi.get("instanceAttributes"));
        return cb.and(cb.equal(ina.get("name"),field),valuePredicate,memberPredicate);
    }

    private Predicate getInstanceLovAttributePredicate(String field, String operator, String value, String type) {
        Root<InstanceListOfValuesAttribute> ila = cq.from(InstanceListOfValuesAttribute.class);
        Predicate valuePredicate = cb.equal(ila.get("indexValue"), Integer.parseInt(value));
        Predicate memberPredicate = ila.in(pi.get("instanceAttributes"));
        return cb.and(cb.equal(ila.get("name"),field),valuePredicate, memberPredicate);
    }

    private Predicate getInstanceDateAttributePredicate(String field, String operator, String value, String type) {
        Root<InstanceDateAttribute> ida = cq.from(InstanceDateAttribute.class);
        Predicate valuePredicate = getPredicate(ida.get("dateValue"), operator, value, "date");
        Predicate memberPredicate = ida.in(pi.get("instanceAttributes"));
        return cb.and(cb.equal(ida.get("name"),field),valuePredicate,memberPredicate);
    }

    private Predicate getInstanceTextAttributePredicate(String field, String operator, String value, String type) {
        Root<InstanceTextAttribute> ita = cq.from(InstanceTextAttribute.class);
        Predicate valuePredicate = getPredicate(ita.get("textValue"), operator, value, "string");
        Predicate memberPredicate = ita.in(pi.get("instanceAttributes"));
        return cb.and(cb.equal(ita.get("name"),field),valuePredicate,memberPredicate);
    }


    // Rule parsing

    private Predicate getPredicate(Expression fieldExp, String operator, Object value, String type){

        Object o;

        switch(type){
            case "string" :
                o=value;
                break;

            case "date":
                try {
                    o = new SimpleDateFormat("yyyy-MM-dd").parse((String) value);
                } catch (ParseException e) {
                    throw new IllegalArgumentException();
                }
                break;
            case "double":
                try {
                    if (value!= null){
                        o = Double.parseDouble((String) value);
                    }else{
                        o = "";
                    }
                }catch(NumberFormatException e){
                    throw new IllegalArgumentException();
                }
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

            case "less":
                if(type.equals("date")){
                    return cb.lessThan(fieldExp,(Date)o);
                } else if(type.equals("double")){
                    return cb.lessThan(fieldExp,(Double)o);
                }
                break;
            case "less_or_equal":
                if(type.equals("date")){
                    return cb.lessThanOrEqualTo(fieldExp, (Date) o);
                } else if(type.equals("double")){
                    return cb.lessThanOrEqualTo(fieldExp, (Double) o);
                }
                break;
            case "greater":
                if(type.equals("date")){
                    return cb.greaterThan(fieldExp, (Date) o);
                } else if(type.equals("double")){
                    return cb.greaterThan(fieldExp, (Double) o);
                }
                break;
            case "greater_or_equal":
                if(type.equals("date")){
                    return cb.greaterThanOrEqualTo(fieldExp, (Date) o);
                } else if(type.equals("double")){
                    return cb.greaterThanOrEqualTo(fieldExp, (Double) o);
                }
                break;
            default:
                break;
        }

        // Should have return a value
        throw new IllegalArgumentException();
    }
}
