package com.docdoku.core.query;

import com.docdoku.core.product.PartRevision;

/**
 * Created by morgan on 21/04/15.
 */
public class QueryResultRow {

    private PartRevision partRevision;
    private int depth;
    private double[] results;
    private QueryContext context;


    public QueryResultRow() {
    }

    public QueryResultRow(PartRevision partRevision) {
        this.partRevision = partRevision;
    }

    public QueryResultRow(PartRevision partRevision, int depth, double[] results) {
        this.partRevision = partRevision;
        this.depth = depth;
        this.results = results;
    }

    public PartRevision getPartRevision() {
        return partRevision;
    }

    public void setPartRevision(PartRevision partRevision) {
        this.partRevision = partRevision;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public double[] getResults() {
        return results;
    }

    public void setResults(double[] results) {
        this.results = results;
    }

    public QueryContext getContext() {
        return context;
    }

    public void setContext(QueryContext context) {
        this.context = context;
    }
}
