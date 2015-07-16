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
        this.filters = new ArrayList<>();
        this.queries = new ArrayList<>();
    }

    public void add(QueryBuilder query) {
        this.queries.add(query);
    }

    public void add(FilterBuilder filter) {
        this.filters.add(filter);
    }

    public QueryBuilder getFilteredQuery() {
        return QueryBuilders.filteredQuery(getQuery(),getFilter());
    }

    private QueryBuilder getQuery() {
        QueryBuilder query;
        if(this.queries.size() > 0) {
            BoolQueryBuilder bqr = QueryBuilders.boolQuery();
            for(QueryBuilder qr : this.queries) {
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
        if(this.filters.size() == 0) {
            // a FilteredQuery can have no Filter.
            filter = null;
        } else {
            FilterBuilder array[] = this.filters.toArray(new FilterBuilder[this.filters.size()]);
            filter = FilterBuilders.boolFilter().must(array);
        }
        return filter;
    }


}
