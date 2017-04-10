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

import com.docdoku.core.configuration.PathDataIteration;
import com.docdoku.core.configuration.PathDataMaster;
import com.docdoku.core.configuration.ProductInstanceIteration;
import com.docdoku.core.meta.*;
import com.docdoku.core.product.*;
import com.docdoku.core.query.Query;
import com.docdoku.core.query.QueryRule;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Morgan Guimard on 09/04/15.
 */
public class PathDataQueryDAO {

    private EntityManager em;
    private Locale mLocale;

    private CriteriaBuilder cb;

    private CriteriaQuery<PathDataMaster> cq;
    private Root<PathDataMaster> pdm;
    private Root<PathDataIteration> pdi;

    private Root<InstanceURLAttribute> iua;
    private Root<InstanceBooleanAttribute> iba;
    private Root<InstanceNumberAttribute> ina;
    private Root<InstanceListOfValuesAttribute> ila;
    private Root<InstanceDateAttribute> ida;
    private Root<InstanceTextAttribute> ita;
    private Root<InstanceLongTextAttribute> ilta;
    private Root<InstancePartNumberAttribute> ipna;

    private static final Logger LOGGER = Logger.getLogger(PathDataQueryDAO.class.getName());

    public PathDataQueryDAO(Locale pLocale, EntityManager pEM) {

        em = pEM;
        mLocale = pLocale;
        cb = em.getCriteriaBuilder();
        cq = cb.createQuery(PathDataMaster.class);

        pdm = cq.from(PathDataMaster.class);
        pdi = cq.from(PathDataIteration.class);

        iua = cq.from(InstanceURLAttribute.class);
        iba = cq.from(InstanceBooleanAttribute.class);
        ina = cq.from(InstanceNumberAttribute.class);
        ila = cq.from(InstanceListOfValuesAttribute.class);
        ida = cq.from(InstanceDateAttribute.class);
        ilta = cq.from(InstanceLongTextAttribute.class);
        ipna = cq.from(InstancePartNumberAttribute.class);
        ita = cq.from(InstanceTextAttribute.class);

    }

    public List<String> runQuery(ProductInstanceIteration productInstanceIteration, Query query) {

        cq.select(pdm);

        List<PathDataMaster> pathDataMasterList = productInstanceIteration.getPathDataMasterList();

        Set<Integer> pathIds =
                pathDataMasterList.stream()
                        .map(PathDataMaster::getId)
                        .collect(Collectors.toSet());

        Predicate pathFilter = cb.and(pdm.get("id").in(pathIds), cb.equal(pdi.get("pathDataMaster"), pdm));

        Predicate rulesPredicate = getPredicate(query.getPathDataQueryRule());

        cq.where(cb.and(pathFilter, rulesPredicate));

        TypedQuery<PathDataMaster> tp = em.createQuery(cq);

        Set<String> pathList = tp.getResultList().stream()
                .map(PathDataMaster::getPath)
                .collect(Collectors.toSet());

        return new ArrayList<>(pathList);
    }

    private Predicate getPredicate(QueryRule queryRule) {

        String condition = queryRule.getCondition();

        List<QueryRule> subQueryRules = queryRule.getSubQueryRules();

        if (subQueryRules != null && !subQueryRules.isEmpty()) {

            Predicate[] predicates = new Predicate[subQueryRules.size()];

            for (int i = 0; i < predicates.length; i++) {
                Predicate predicate = getPredicate(subQueryRules.get(i));
                predicates[i] = predicate;
            }

            if ("OR".equals(condition)) {
                return cb.or(predicates);
            } else if ("AND".equals(condition)) {
                return cb.and(predicates);
            }

            throw new IllegalArgumentException("Cannot parse rule or sub rule condition: " + condition + " ");

        } else {
            return getRulePredicate(queryRule);
        }
    }

    private Predicate getRulePredicate(QueryRule queryRule) {

        String field = queryRule.getField();

        if (field == null) {
            return cb.and();
        }

        String operator = queryRule.getOperator();
        List<String> values = queryRule.getValues();
        String type = queryRule.getType();

        if (field.startsWith("pd-attr-TEXT.")) {
            return getInstanceTextAttributePredicate(field.substring(13), operator, values);
        }

        if (field.startsWith("pd-attr-LONG_TEXT.")) {
            return getInstanceLongTextAttributePredicate(field.substring(18), operator, values);
        }

        if (field.startsWith("pd-attr-DATE.")) {
            return getInstanceDateAttributePredicate(field.substring(13), operator, values);
        }

        if (field.startsWith("pd-attr-BOOLEAN.")) {
            return getInstanceBooleanAttributePredicate(field.substring(16), operator, values);
        }

        if (field.startsWith("pd-attr-URL.")) {
            return getInstanceURLAttributePredicate(field.substring(12), operator, values);
        }

        if (field.startsWith("pd-attr-NUMBER.")) {
            return getInstanceNumberAttributePredicate(field.substring(15), operator, values);
        }

        if (field.startsWith("pd-attr-LOV.")) {
            return getInstanceLovAttributePredicate(field.substring(12), operator, values);
        }

        if (field.startsWith("pd-attr-PART_NUMBER.")) {
            return getInstancePartNumberAttributePredicate(field.substring(20), operator, values);
        }

        throw new IllegalArgumentException("Unhandled attribute: [" + field + ", " + operator + ", " + values + "]");
    }


    // Instances Attributes
    private Predicate getInstanceURLAttributePredicate(String field, String operator, List<String> values) {
        Predicate valuesPredicate = QueryPredicateBuilder.getExpressionPredicate(cb, iua.get("urlValue"), operator, values, "string");
        Predicate memberPredicate = iua.in(pdi.get("instanceAttributes"));
        return cb.and(cb.equal(iua.get("name"), field), valuesPredicate, memberPredicate);
    }

    private Predicate getInstanceBooleanAttributePredicate(String field, String operator, List<String> values) {
        if (values.size() == 1) {
            Predicate valuesPredicate = cb.equal(iba.get("booleanValue"), Boolean.parseBoolean(values.get(0)));
            Predicate memberPredicate = iba.in(pdi.get("instanceAttributes"));
            switch (operator) {
                case "equal":
                    return cb.and(cb.equal(iba.get("name"), field), valuesPredicate, memberPredicate);
                case "not_equal":
                    return cb.and(cb.equal(iba.get("name"), field), valuesPredicate.not(), memberPredicate);
                default:
                    break;
            }
        }

        throw new IllegalArgumentException("Cannot handle such operator [" + operator + "] on field " + field + "]");
    }

    private Predicate getInstanceNumberAttributePredicate(String field, String operator, List<String> values) {
        Predicate valuesPredicate = QueryPredicateBuilder.getExpressionPredicate(cb, ina.get("numberValue"), operator, values, "double");
        Predicate memberPredicate = ina.in(pdi.get("instanceAttributes"));
        return cb.and(cb.equal(ina.get("name"), field), valuesPredicate, memberPredicate);
    }

    private Predicate getInstanceLovAttributePredicate(String field, String operator, List<String> values) {
        if (values.size() == 1) {
            Predicate valuesPredicate = cb.equal(ila.get("indexValue"), Integer.parseInt(values.get(0)));
            Predicate memberPredicate = ila.in(pdi.get("instanceAttributes"));
            switch (operator) {
                case "equal":
                    return cb.and(cb.equal(ila.get("name"), field), valuesPredicate, memberPredicate);
                case "not_equal":
                    return cb.and(cb.equal(ila.get("name"), field), valuesPredicate.not(), memberPredicate);
                default:
                    break;
            }
        }

        throw new IllegalArgumentException("Cannot handle such operator [" + operator + "] on field " + field + "]");
    }

    private Predicate getInstanceDateAttributePredicate(String field, String operator, List<String> values) {
        Predicate valuesPredicate = QueryPredicateBuilder.getExpressionPredicate(cb, ida.get("dateValue"), operator, values, "date");
        Predicate memberPredicate = ida.in(pdi.get("instanceAttributes"));
        return cb.and(cb.equal(ida.get("name"), field), valuesPredicate, memberPredicate);
    }

    private Predicate getInstanceLongTextAttributePredicate(String field, String operator, List<String> values) {
        Predicate valuesPredicate = QueryPredicateBuilder.getExpressionPredicate(cb, ilta.get("longTextValue"), operator, values, "string");
        Predicate memberPredicate = ita.in(pdi.get("instanceAttributes"));
        return cb.and(cb.equal(ita.get("name"), field), valuesPredicate, memberPredicate);
    }

    private Predicate getInstancePartNumberAttributePredicate(String field, String operator, List<String> values) {
        Predicate valuesPredicate = QueryPredicateBuilder.getExpressionPredicate(cb, ipna.get("partMasterValue").get("number"), operator, values, "string");
        Predicate memberPredicate = ita.in(pdi.get("instanceAttributes"));
        return cb.and(cb.equal(ita.get("name"), field), valuesPredicate, memberPredicate);
    }

    private Predicate getInstanceTextAttributePredicate(String field, String operator, List<String> values) {
        Predicate valuesPredicate = QueryPredicateBuilder.getExpressionPredicate(cb, ita.get("textValue"), operator, values, "string");
        Predicate memberPredicate = ita.in(pdi.get("instanceAttributes"));
        return cb.and(cb.equal(ita.get("name"), field), valuesPredicate, memberPredicate);
    }
}
