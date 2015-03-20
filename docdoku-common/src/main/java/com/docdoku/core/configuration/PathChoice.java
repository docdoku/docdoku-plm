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

package com.docdoku.core.configuration;

import com.docdoku.core.product.PartLink;
import com.docdoku.core.product.PartRevision;

import java.util.List;

/**
 * Created by morgan on 10/03/15.
 */
public class PathChoice {

    private List<PartRevision> partRevisions;
    private List<PartLink> paths;
    private PartLink partUsageLink;

    public PathChoice() {
    }

    public PathChoice(List<PartRevision> partRevisions, List<PartLink> paths) {
        this.partRevisions =  partRevisions;
        this.paths = paths;
        this.partUsageLink =  this.paths.get(this.paths.size()-1);
    }

    public List<PartRevision> getPartRevisions() {
        return partRevisions;
    }

    public void setPartRevisions(List<PartRevision> partRevisions) {
        this.partRevisions = partRevisions;
    }

    public List<PartLink> getPaths() {
        return paths;
    }

    public void setPaths(List<PartLink> paths) {
        this.paths = paths;
    }

    public PartLink getPartUsageLink() {
        return partUsageLink;
    }

    public void setPartUsageLink(PartLink partUsageLink) {
        this.partUsageLink = partUsageLink;
    }

}
