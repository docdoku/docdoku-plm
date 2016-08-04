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

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Morgan Guimard
 */
@Table(name="QUERYRULE")
@Entity
public class QueryRule implements Serializable {

    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Id
    private int qid;

    @Column(name="COND")
    private String condition;
    private String id;
    private String field;
    private String type;
    private String operator;

    @Column(name="VALUE")
    @OrderColumn(name="VALUE_ORDER")
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "QUERYRULE_VALUES",
            joinColumns= {
                    @JoinColumn(name = "QUERYRULE_ID", referencedColumnName = "QID")
            }
    )
    private List<String> values=new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "PARENT_QUERY_RULE")
    private QueryRule parentQueryRule;

    @OneToMany(mappedBy = "parentQueryRule", cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    @OrderBy("qid ASC")
    private List<QueryRule> subQueryRules;

    public QueryRule() {
    }

    public int getQid() {
        return qid;
    }

    public void setQid(int qid) {
        this.qid = qid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public QueryRule getParentQueryRule() {
        return parentQueryRule;
    }

    public void setParentQueryRule(QueryRule parentQueryRule) {
        this.parentQueryRule = parentQueryRule;
    }

    public List<QueryRule> getSubQueryRules() {
        return subQueryRules;
    }

    public void setSubQueryRules(List<QueryRule> subQueryRules) {
        this.subQueryRules = subQueryRules;
    }

    public boolean hasSubRules() {
        return getSubQueryRules() != null && !getSubQueryRules().isEmpty();
    }
}
