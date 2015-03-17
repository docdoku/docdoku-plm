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
import com.docdoku.core.product.PartUsageLink;

import java.util.List;

/**
 *
 * @author Florent Garin
 */

public class InstanceCollection {
    
    private PartUsageLink rootUsageLink;
    private List<Integer> usageLinkPaths;
    private PSFilter filter;
    
    public InstanceCollection(){
        
    }
    
    public InstanceCollection(PartUsageLink rootUsageLink, List<Integer> usageLinkPaths, PSFilter filter){
        this.rootUsageLink=rootUsageLink;
        this.usageLinkPaths=usageLinkPaths;
        this.filter=filter;
    }

    public PSFilter getFilter() {
        return filter;
    }

    public PartUsageLink getRootUsageLink() {
        return rootUsageLink;
    }

    public List<Integer> getUsageLinkPaths() {
        return usageLinkPaths;
    }
    
    
}
