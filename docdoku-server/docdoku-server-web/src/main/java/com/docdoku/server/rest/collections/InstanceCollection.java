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
import com.docdoku.core.product.ConfigurationItemKey;
import com.docdoku.core.product.PartLink;
import com.docdoku.core.util.Tools;

import java.util.List;

/**
 *
 * @author Florent Garin
 */

public class InstanceCollection {

    // Used for services call
    private ConfigurationItemKey ciKey;

    // Used to walk the structure
    private PSFilter filter;

    // All instances under these paths
    private List<List<PartLink>> paths;

    public InstanceCollection(ConfigurationItemKey ciKey, PSFilter filter, List<List<PartLink>> paths){
        this.ciKey = ciKey;
        this.filter = filter;
        this.paths = paths;
    }


    public PSFilter getFilter() {
        return filter;
    }

    public ConfigurationItemKey getCiKey() {
        return ciKey;
    }

    public List<List<PartLink>> getPaths() {
        return paths;
    }

    public boolean isFiltered(List<PartLink> currentPath) {
        for(List<PartLink> path : paths){
            if(filter(path,currentPath)){
                return true;
            }
        }
        return false;
    }

    private boolean filter(List<PartLink> path, List<PartLink> currentPath){
        return Tools.getPathAsString(currentPath).startsWith(Tools.getPathAsString(path));
    }
}
