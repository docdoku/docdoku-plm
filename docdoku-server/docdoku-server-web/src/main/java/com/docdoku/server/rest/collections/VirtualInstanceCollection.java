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

package com.docdoku.server.rest.collections;

import com.docdoku.core.configuration.PSFilter;
import com.docdoku.core.product.PartRevision;

/**
 *
 * @author Morgan Guimard
 */

public class VirtualInstanceCollection {

    private PartRevision rootPart;
    private PSFilter filter;

    public VirtualInstanceCollection() {
    }

    public VirtualInstanceCollection(PartRevision rootPart, PSFilter filter){
        this.rootPart = rootPart;
        this.filter = filter;
    }

    public PartRevision getRootPart() {
        return rootPart;
    }

    public void setRootPart(PartRevision rootPart) {
        this.rootPart = rootPart;
    }

    public PSFilter getFilter() {
        return filter;
    }

    public void setFilter(PSFilter filter) {
        this.filter = filter;
    }
}
