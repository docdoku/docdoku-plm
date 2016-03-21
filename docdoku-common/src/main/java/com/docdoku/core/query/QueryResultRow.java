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

import com.docdoku.core.configuration.PathDataIteration;
import com.docdoku.core.product.PartLink;
import com.docdoku.core.product.PartLinkList;
import com.docdoku.core.product.PartRevision;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Morgan Guimard
 */
public class QueryResultRow {

    private PartRevision partRevision;
    private int depth;
    private Map<String, List<PartLinkList>> sources = new HashMap<>();
    private Map<String, List<PartLinkList>> targets = new HashMap<>();
    private PathDataIteration pathDataIteration;
    private double[] results;
    private QueryContext context;
    private double amount;
    private String path;

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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Map<String, List<PartLinkList>> getSources() {
        return sources;
    }

    public void setSources(Map<String, List<PartLinkList>> sources) {
        this.sources = sources;
    }

    public Map<String, List<PartLinkList>> getTargets() {
        return targets;
    }

    public void setTargets(Map<String, List<PartLinkList>> targets) {
        this.targets = targets;
    }

    public void addTarget(String type, List<PartLink> targetPath) {
        List<PartLinkList> paths = targets.get(type) != null ? targets.get(type) : new ArrayList<>();
        paths.add(new PartLinkList(targetPath));
        targets.put(type, paths);
    }

    public void addSource(String type, List<PartLink> sourcePath) {
        List<PartLinkList> paths = sources.get(type) != null ? sources.get(type) : new ArrayList<>();
        paths.add(new PartLinkList(sourcePath));
        sources.put(type, paths);
    }

    public PathDataIteration getPathDataIteration() {
        return pathDataIteration;
    }

    public void setPathDataIteration(PathDataIteration pathDataIteration) {
        this.pathDataIteration = pathDataIteration;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
