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

package com.docdoku.server.esindexer;

import org.elasticsearch.index.query.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kelto on 09/07/15.
 */
public class ESQueryBuilder {

    private final List<FilterBuilder> filters;
    private final List<QueryBuilder> queries;

    public ESQueryBuilder() {
        filters = new ArrayList<>();
        queries = new ArrayList<>();
    }

    public void add(QueryBuilder query) {
        queries.add(query);
    }

    public void add(FilterBuilder filter) {
        filters.add(filter);
    }

    public QueryBuilder getFilteredQuery() {
        return QueryBuilders.filteredQuery(getQuery(),getFilter());
    }

    private QueryBuilder getQuery() {
        QueryBuilder query;
        if(!queries.isEmpty()) {
            BoolQueryBuilder bqr = QueryBuilders.boolQuery();
            for(QueryBuilder qr : queries) {
                bqr.must(qr);
            }
            query = bqr;
        } else {
            // A FilteredQuery must have a query
            // Therefore, we send a neutral query which will match anything.
            query = QueryBuilders.matchAllQuery();
        }
        return query;
    }

    private FilterBuilder getFilter() {
        FilterBuilder filter;
        if(filters.isEmpty()) {
            // a FilteredQuery can have no Filter.
            filter = null;
        } else {
            FilterBuilder array[] = filters.toArray(new FilterBuilder[filters.size()]);
            filter = FilterBuilders.boolFilter().must(array);
        }
        return filter;
    }


}
