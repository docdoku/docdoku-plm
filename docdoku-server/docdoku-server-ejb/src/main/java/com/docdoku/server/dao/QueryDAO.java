/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2015 DocDoku SARL
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
 * @author Morgan Guimard on 09/04/15.
 */
public class QueryDAO {

    private EntityManager em;
    private Locale mLocale;

    private CriteriaBuilder cb;
    private CriteriaQuery<PartRevision> cq;
    private Root<PartMaster> pm;
    private Root<PartRevision> pr;
    private Root<PartIteration> pi;


    private static final Logger LOGGER = Logger.getLogger(QueryDAO.class.getName());

    public QueryDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale = pLocale;
        cb = em.getCriteriaBuilder();

        cq = cb.createQuery(PartRevision.class);
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

        if(subQueryRules != null && !subQueryRules.isEmpty()){

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
        List<String> values = queryRule.getValues();
        String type = queryRule.getType();

        if(field.startsWith("pm.")){
            return getPartMasterPredicate(field.substring(3), operator, values , type);
        }

        if(field.startsWith("pr.")){
            return getPartRevisionPredicate(field.substring(3), operator, values, type);
        }

        if(field.startsWith("author.")){
            return getAuthorPredicate(field.substring(7), operator, values, type);
        }

        if(field.startsWith("attr-TEXT.")){
            return getInstanceTextAttributePredicate(field.substring(10), operator, values, type);
        }

        if(field.startsWith("attr-LONG_TEXT.")){
            return getInstanceLongTextAttributePredicate(field.substring(15), operator, values, type);
        }

        if(field.startsWith("attr-DATE.")){
            return getInstanceDateAttributePredicate(field.substring(10), operator, values, type);
        }

        if(field.startsWith("attr-BOOLEAN.")){
            return getInstanceBooleanAttributePredicate(field.substring(13), operator, values, type);
        }

        if(field.startsWith("attr-URL.")){
            return getInstanceURLAttributePredicate(field.substring(9), operator, values, type);
        }

        if(field.startsWith("attr-NUMBER.")){
            return getInstanceNumberAttributePredicate(field.substring(12), operator, values, type);
        }

        if(field.startsWith("attr-LOV.")){
            return getInstanceLovAttributePredicate(field.substring(9), operator, values, type);
        }

        throw new IllegalArgumentException();
    }

    private Predicate getAuthorPredicate(String field, String operator, List<String> values, String type) {
        return getPredicate(pr.get("author").get(field),operator,values,type);
    }

    private Predicate getPartRevisionPredicate(String field, String operator, List<String> values, String type) {
        if("checkInDate".equals(field)){
            Predicate lastIterationPredicate = cb.equal(cb.size(pr.get("partIterations")), pi.get("iteration"));
            return cb.and(lastIterationPredicate, getPredicate(pi.get("checkInDate"), operator, values, type));
        }
        else if("status".equals(field)){
            if (values.size() == 1) {
                return getPredicate(pr.get(field), operator, values, "status");
            }
        }
        else if("tags".equals(field)){
            return getTagsPredicate(values);
        }
        else if("linkedDocuments".equals(field)){
            // should be ignored, returning always true for the moment
            return cb.and();
        }
        return getPredicate(pr.get(field), operator, values, type);
    }

    private Predicate getTagsPredicate(List<String> values) {
        Root<Tag> tag = cq.from(Tag.class);
        Predicate prPredicate = tag.in(pr.get("tags"));
        Predicate valuesPredicate = cb.equal(tag.get("label"),values);
        return cb.and(prPredicate, valuesPredicate);
    }

    private Predicate getPartMasterPredicate(String field, String operator, List<String> values, String type) {
        return getPredicate(pm.get(field), operator, values, type);
    }

    // Instances Attributes
    private Predicate getInstanceURLAttributePredicate(String field, String operator, List<String> values, String type) {
        Root<InstanceURLAttribute> iua = cq.from(InstanceURLAttribute.class);
        Predicate valuesPredicate = getPredicate(iua.get("urlValue"), operator, values, "string");
        Predicate memberPredicate = iua.in(pi.get("instanceAttributes"));
        return cb.and(cb.equal(iua.get("name"), field), valuesPredicate, memberPredicate);
    }

    private Predicate getInstanceBooleanAttributePredicate(String field, String operator, List<String> values, String type) {
        if (values.size() == 1) {
            Root<InstanceBooleanAttribute> iba = cq.from(InstanceBooleanAttribute.class);
            Predicate valuesPredicate = cb.equal(iba.get("booleanValue"), Boolean.parseBoolean(values.get(0)));
            Predicate memberPredicate = iba.in(pi.get("instanceAttributes"));
            switch(operator){
                case "equal":
                    return cb.and(cb.equal(iba.get("name"),field),valuesPredicate,memberPredicate);
                case "not_equal":
                    return cb.and(cb.equal(iba.get("name"),field),valuesPredicate.not(),memberPredicate);
                default:
                    break;
            }
        }

        throw new IllegalArgumentException();
    }

    private Predicate getInstanceNumberAttributePredicate(String field, String operator, List<String> values, String type) {
        Root<InstanceNumberAttribute> ina = cq.from(InstanceNumberAttribute.class);
        Predicate valuesPredicate = getPredicate(ina.get("numberValue"), operator, values, "double");
        Predicate memberPredicate = ina.in(pi.get("instanceAttributes"));
        return cb.and(cb.equal(ina.get("name"),field),valuesPredicate,memberPredicate);
    }

    private Predicate getInstanceLovAttributePredicate(String field, String operator, List<String> values, String type) {
        if (values.size() == 1) {
            Root<InstanceListOfValuesAttribute> ila = cq.from(InstanceListOfValuesAttribute.class);
            Predicate valuesPredicate = cb.equal(ila.get("indexValue"), Integer.parseInt(values.get(0)));
            Predicate memberPredicate = ila.in(pi.get("instanceAttributes"));
            switch(operator) {
                case "equal":
                    return cb.and(cb.equal(ila.get("name"), field), valuesPredicate, memberPredicate);
                case "not_equal":
                    return cb.and(cb.equal(ila.get("name"), field), valuesPredicate.not(), memberPredicate);
                default:
                    break;
            }
        }

        throw new IllegalArgumentException();
    }

    private Predicate getInstanceDateAttributePredicate(String field, String operator, List<String> values, String type) {
        Root<InstanceDateAttribute> ida = cq.from(InstanceDateAttribute.class);
        Predicate valuesPredicate = getPredicate(ida.get("dateValue"), operator, values, "date");
        Predicate memberPredicate = ida.in(pi.get("instanceAttributes"));
        return cb.and(cb.equal(ida.get("name"),field),valuesPredicate,memberPredicate);
    }

    private Predicate getInstanceLongTextAttributePredicate(String field, String operator, List<String> values, String type) {
        Root<InstanceLongTextAttribute> ita = cq.from(InstanceLongTextAttribute.class);
        Predicate valuesPredicate = getPredicate(ita.get("longTextValue"), operator, values, "string");
        Predicate memberPredicate = ita.in(pi.get("instanceAttributes"));
        return cb.and(cb.equal(ita.get("name"),field),valuesPredicate,memberPredicate);
    }

    private Predicate getInstanceTextAttributePredicate(String field, String operator, List<String> values, String type) {
        Root<InstanceTextAttribute> ita = cq.from(InstanceTextAttribute.class);
        Predicate valuesPredicate = getPredicate(ita.get("textValue"), operator, values, "string");
        Predicate memberPredicate = ita.in(pi.get("instanceAttributes"));
        return cb.and(cb.equal(ita.get("name"),field),valuesPredicate,memberPredicate);
    }


    // Rule parsing

    private Predicate getPredicate(Expression fieldExp, String operator, List<String> values, String type){

        List<?> operands;

        switch(type){
            case "string" :
                operands=values;
                break;
            case "date":
                try {
                    //TODO: this formatting is already done by other method, should be refactored.
                    //TODO: the pattern for the date format should be declared somewhere.
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    df.setTimeZone(TimeZone.getTimeZone("UTC"));
                    List<Date> temp = new ArrayList<Date>();
                    for (String string : values) {
                        temp.add(df.parse(string));
                    }
                    operands = temp;
                } catch (ParseException e) {
                    throw new IllegalArgumentException();
                }
                break;
            case "double":
                try {
                    List<Double> temp = new ArrayList<Double>();
                    for (String string : values) {
                        temp.add(Double.parseDouble(string));
                    }
                    operands = temp;
                }catch(NumberFormatException e){
                    throw new IllegalArgumentException();
                }
                break;
            case "status":
                List<PartRevision.RevisionStatus> temp = new ArrayList<>();
                for (String string : values) {
                    temp.add(PartRevision.RevisionStatus.valueOf(string));
                }
                operands = temp;
                break;
            default :
                operands=values;
                break;
        }

        switch (operator){
            case "between" :
                if (operands.size() == 2) {
                    if("date".equals(type)){
                        return cb.between(fieldExp, (Date)operands.get(0), (Date)operands.get(1));

                    } else if("double".equals(type)){
                        return cb.between(fieldExp, (Double) operands.get(0), (Double) operands.get(1));
                    }
                }
                break;
            case "equal" :
                if("date".equals(type)){
                    Date date1 = (Date) operands.get(0);
                    Calendar c = Calendar.getInstance();
                    c.setTime(date1);
                    c.add(Calendar.DATE, 1);
                    Date date2 = c.getTime();
                    return cb.between(fieldExp, date1, date2);

                } else {
                    return cb.equal(fieldExp,operands.get(0));
                }
            case "not_equal" : return cb.equal(fieldExp, operands.get(0)).not();

            case "contains" : return cb.like(fieldExp, "%" + operands.get(0) + "%");
            case "not_contains" : return cb.like(fieldExp, "%"+operands.get(0)+"%").not();

            case "begins_with" : return cb.like(fieldExp, operands.get(0)+"%");
            case "not_begins_with" : return  cb.like(fieldExp, operands.get(0)+"%").not();

            case "ends_with" : return cb.like(fieldExp, "%"+operands.get(0));
            case "not_ends_with" : return cb.like(fieldExp, "%"+operands.get(0)).not();

            case "less":
                if("date".equals(type)){
                    return cb.lessThan(fieldExp,(Date)operands.get(0));
                } else if("double".equals(type)){
                    return cb.lessThan(fieldExp,(Double)operands.get(0));
                }
                break;
            case "less_or_equal":
                if("date".equals(type)){
                    return cb.lessThanOrEqualTo(fieldExp, (Date) operands.get(0));
                } else if("double".equals(type)){
                    return cb.lessThanOrEqualTo(fieldExp, (Double) operands.get(0));
                }
                break;
            case "greater":
                if("date".equals(type)){
                    return cb.greaterThan(fieldExp, (Date) operands.get(0));
                } else if("double".equals(type)){
                    return cb.greaterThan(fieldExp, (Double) operands.get(0));
                }
                break;
            case "greater_or_equal":
                if("date".equals(type)){
                    return cb.greaterThanOrEqualTo(fieldExp, (Date) operands.get(0));
                } else if("double".equals(type)){
                    return cb.greaterThanOrEqualTo(fieldExp, (Double) operands.get(0));
                }
                break;
            default:
                break;
        }

        // Should have return a value
        throw new IllegalArgumentException();
    }
}
