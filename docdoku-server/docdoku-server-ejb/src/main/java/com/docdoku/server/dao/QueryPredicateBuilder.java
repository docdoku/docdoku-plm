package com.docdoku.server.dao;

import com.docdoku.core.meta.RevisionStatus;
import com.docdoku.core.util.DateUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by morgan on 4/7/17.
 */
public class QueryPredicateBuilder {

    // Rule parsing
    // Safe casts for expressions
    @SuppressWarnings("unchecked")
    public static Predicate getExpressionPredicate(CriteriaBuilder cb, Expression fieldExp, String operator, List<String> values, String type, String timeZone) {

        List<?> operands;

        switch (type) {
            case "string":
                operands = values;
                break;
            case "date":
                try {
                    List<Date> temp = new ArrayList<>();
                    for (String string : values) {
                        temp.add(DateUtils.parse(string, timeZone));
                    }
                    operands = temp;
                } catch (ParseException e) {
                    throw new IllegalArgumentException("Parsing exception for dates " + values + " and operator " + operator);
                }
                break;
            case "double":
                try {
                    List<Double> operandValues = new ArrayList<>();
                    for(String value:values){
                        operandValues.add(Double.parseDouble(value));
                    }
                    operands = operandValues;
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Parsing exception for double " + values + " and operator " + operator);
                }
                break;
            case "status":
                List<RevisionStatus> operandValues = new ArrayList<>();
                for(String value:values){
                    operandValues.add(RevisionStatus.valueOf(value));
                }
                operands = operandValues;
                break;
            default:
                operands = values;
                break;
        }

        switch (operator) {
            case "between":
                if (operands.size() == 2) {
                    if ("date".equals(type)) {
                        return cb.between(fieldExp, (Date) operands.get(0), (Date) operands.get(1));

                    } else if ("double".equals(type)) {
                        return cb.between(fieldExp, (Double) operands.get(0), (Double) operands.get(1));
                    }
                }
                break;
            case "equal":
                if ("date".equals(type)) {
                    Date date1 = (Date) operands.get(0);
                    Calendar c = Calendar.getInstance();
                    c.setTime(date1);
                    c.add(Calendar.DATE, 1);
                    Date date2 = c.getTime();

                    return cb.between(fieldExp, date1, date2);

                } else {
                    return cb.equal(fieldExp, operands.get(0));
                }
            case "not_equal":
                return cb.equal(fieldExp, operands.get(0)).not();

            case "contains":
                return cb.like(fieldExp, "%" + operands.get(0) + "%");
            case "not_contains":
                return cb.like(fieldExp, "%" + operands.get(0) + "%").not();

            case "begins_with":
                return cb.like(fieldExp, operands.get(0) + "%");
            case "not_begins_with":
                return cb.like(fieldExp, operands.get(0) + "%").not();

            case "ends_with":
                return cb.like(fieldExp, "%" + operands.get(0));
            case "not_ends_with":
                return cb.like(fieldExp, "%" + operands.get(0)).not();

            case "less":
                if ("date".equals(type)) {
                    return cb.lessThan(fieldExp, (Date) operands.get(0));
                } else if ("double".equals(type)) {
                    return cb.lessThan(fieldExp, (Double) operands.get(0));
                }
                break;
            case "less_or_equal":
                if ("date".equals(type)) {
                    return cb.lessThanOrEqualTo(fieldExp, (Date) operands.get(0));
                } else if ("double".equals(type)) {
                    return cb.lessThanOrEqualTo(fieldExp, (Double) operands.get(0));
                }
                break;
            case "greater":
                if ("date".equals(type)) {
                    return cb.greaterThan(fieldExp, (Date) operands.get(0));
                } else if ("double".equals(type)) {
                    return cb.greaterThan(fieldExp, (Double) operands.get(0));
                }
                break;
            case "greater_or_equal":
                if ("date".equals(type)) {
                    return cb.greaterThanOrEqualTo(fieldExp, (Date) operands.get(0));
                } else if ("double".equals(type)) {
                    return cb.greaterThanOrEqualTo(fieldExp, (Double) operands.get(0));
                }
                break;
            default:
                break;
        }

        // Should have return a value
        throw new IllegalArgumentException("Parsing exception " + type + " with values" + values + " and operator " + operator);
    }
}
