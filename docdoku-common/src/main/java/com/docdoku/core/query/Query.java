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
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Wraps data needed to perform a custom query on database
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
    private Set<String> selects=new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "QUERY_ORDER_BY",
            joinColumns= {
                    @JoinColumn(name = "QUERY_ID", referencedColumnName = "ID")
            }
    )
    private Set<String> orderByList=new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "QUERY_GROUPED_BY",
            joinColumns= {
                    @JoinColumn(name = "QUERY_ID", referencedColumnName = "ID")
            }
    )
    private Set<String> groupedByList=new HashSet<>();

    public Query() {
    }

    public Query(User author, String name, QueryRule queryRule, Set<String> selects, Set<String> orderByList, Set<String> groupedByList) {
        this.author = author;
        this.name = name;
        this.queryRule = queryRule;
        this.selects = selects;
        this.orderByList = orderByList;
        this.groupedByList = groupedByList;
        this.creationDate = new Date();
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

    public Set<String> getSelects() {
        return selects;
    }

    public void setSelects(Set<String> selects) {
        this.selects = selects;
    }

    public Set<String> getOrderByList() {
        return orderByList;
    }

    public void setOrderByList(Set<String> orderByList) {
        this.orderByList = orderByList;
    }

    public Set<String> getGroupedByList() {
        return groupedByList;
    }

    public void setGroupedByList(Set<String> groupedByList) {
        this.groupedByList = groupedByList;
    }
}
