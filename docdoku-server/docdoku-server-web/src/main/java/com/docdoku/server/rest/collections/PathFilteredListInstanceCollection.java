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

import java.util.List;

/**
 *
 * @author Morgan Guimard
 */

public class PathFilteredListInstanceCollection {

    private List<InstanceCollection> instanceCollections;

    private PSFilter filter;

    public PathFilteredListInstanceCollection(){

    }

    public PathFilteredListInstanceCollection(List<InstanceCollection> instanceCollections, PSFilter filter) {
        this.instanceCollections = instanceCollections;
        this.filter = filter;
    }

    public List<InstanceCollection> getInstanceCollections() {
        return instanceCollections;
    }

    public void setInstanceCollections(List<InstanceCollection> instanceCollections) {
        this.instanceCollections = instanceCollections;
    }

    public PSFilter getFilter() {
        return filter;
    }

    public void setFilter(PSFilter filter) {
        this.filter = filter;
    }
}
