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

package com.docdoku.core.query;

import com.docdoku.core.common.User;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Wraps data needed to perform a custom query on database.
 *
 * @author Morgan Guimard
 */
@Table(name="QUERY")
@Entity
@NamedQueries({
        @NamedQuery(name="Query.findByWorkspace",query="SELECT q FROM Query q WHERE q.author.workspaceId = :workspaceId"),
        @NamedQuery(name="Query.findByWorkspaceAndWorkspace",query="SELECT q FROM Query q WHERE q.author.workspaceId = :workspaceId AND q.name = :name")
})
public class Query implements Serializable {

    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Id
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "AUTHOR_LOGIN", referencedColumnName = "LOGIN"),
            @JoinColumn(name = "AUTHOR_WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID")
    })
    private User author;

    private String name;

    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

    @OneToOne(orphanRemoval = true, cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    @JoinColumn(name="QUERYRULE_ID")
    private QueryRule queryRule;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "QUERY_SELECTS",
            joinColumns= {
                    @JoinColumn(name = "QUERY_ID", referencedColumnName = "ID")
            }
    )
    private List<String> selects=new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "QUERY_ORDER_BY",
            joinColumns= {
                    @JoinColumn(name = "QUERY_ID", referencedColumnName = "ID")
            }
    )
    private List<String> orderByList=new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "QUERY_GROUPED_BY",
            joinColumns= {
                    @JoinColumn(name = "QUERY_ID", referencedColumnName = "ID")
            }
    )
    private List<String> groupedByList=new ArrayList<>();


    @OneToMany(mappedBy = "parentQuery", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<QueryContext> contexts =new ArrayList<>();

    public Query() {
    }

    public Query(User author, String name, Date creationDate, QueryRule queryRule, List<String> selects, List<String> orderByList, List<String> groupedByList, List<QueryContext> contexts) {
        this.author = author;
        this.name = name;
        this.creationDate = creationDate;
        this.queryRule = queryRule;
        this.selects = selects;
        this.orderByList = orderByList;
        this.groupedByList = groupedByList;
        this.contexts = contexts;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
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

    public void setQueryRule(QueryRule queryRule) {
        this.queryRule = queryRule;
    }

    public QueryRule getQueryRule() {
        return queryRule;
    }

    public void setRules(QueryRule queryRule) {
        this.queryRule = queryRule;
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

    public List<QueryContext> getContexts() {
        return contexts;
    }

    public void setContexts(List<QueryContext> contexts) {
        this.contexts = contexts;
    }

    public boolean hasContext() {
        return !contexts.isEmpty();
    }
}
