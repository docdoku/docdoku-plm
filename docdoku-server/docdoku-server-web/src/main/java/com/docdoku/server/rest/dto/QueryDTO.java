package com.docdoku.server.rest.dto;

import java.util.Date;
import java.util.List;

/**
 * Created by morgan on 09/04/15.
 */
public class QueryDTO {

    private int id;
    private String name;
    private Date creationDate;
    private QueryRuleDTO subRules;
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

    public QueryRuleDTO getSubRules() {
        return subRules;
    }

    public void setSubRules(QueryRuleDTO subRules) {
        this.subRules = subRules;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
}
