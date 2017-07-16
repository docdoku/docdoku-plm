/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
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
import com.docdoku.core.product.PartIteration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * This class represents a potential link {@code partUsageLink} that should
 * (otherwise why use this class?) probably have
 * one or more substitution links.
 * {@code resolvedPath} is the path that leads to the choice.
 * This path is resolved in the sens that for each step the right
 * {@link PartIteration} is identified.
 *
 * Instances of this class are not persisted.
 *
 * @author Morgan Guimard
 * @version 2.0, 08/28/16
 * @since 2.0
 */
public class PathChoice implements Serializable{

    private List<ResolvedPartLink> resolvedPath = new ArrayList<>();
    private PartLink partUsageLink;

    public PathChoice() {
    }

    public PathChoice(List<ResolvedPartLink> resolvedPath, PartLink partUsageLink) {
        this.resolvedPath = resolvedPath;
        this.partUsageLink = partUsageLink;
    }

    public List<ResolvedPartLink> getResolvedPath() {
        return resolvedPath;
    }

    public void setResolvedPath(List<ResolvedPartLink> resolvedPath) {
        this.resolvedPath = resolvedPath;
    }

    public PartLink getPartUsageLink() {
        return partUsageLink;
    }

    public void setPartUsageLink(PartLink partUsageLink) {
        this.partUsageLink = partUsageLink;
    }

}
