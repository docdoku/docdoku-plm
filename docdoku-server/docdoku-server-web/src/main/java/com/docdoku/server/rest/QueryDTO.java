package com.docdoku.server.rest;

import com.docdoku.server.rest.dto.RuleDTO;

import java.util.List;

/**
 * Created by morgan on 09/04/15.
 */
public class QueryDTO {

    private int id;
    private String label;
    private RuleDTO rules;
    private List<String> selects;
    private List<String> orderByList;
    private List<String> groupedByList;

    public QueryDTO() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public RuleDTO getRules() {
        return rules;
    }

    public void setRules(RuleDTO rules) {
        this.rules = rules;
    }

    public List<String> getSelects() {
        return selects;
    }

    public void setSelects(List<String> selects) {
        this.selects = selects;
    }

    public List<String> getOrderByList() {
        return orderByList;
    }

    public void setOrderByList(List<String> orderByList) {
        this.orderByList = orderByList;
    }

    public List<String> getGroupedByList() {
        return groupedByList;
    }

    public void setGroupedByList(List<String> groupedByList) {
        this.groupedByList = groupedByList;
    }
}
