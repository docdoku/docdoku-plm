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


package com.docdoku.server.configuration.filter;

import com.docdoku.core.common.User;
import com.docdoku.core.configuration.ProductStructureFilter;
import com.docdoku.core.product.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A {@link com.docdoku.core.configuration.ProductStructureFilter} implementation
 * which selects the last iteration of the latest released revision.
 *
 * @author Taylor LABEJOF
 * @version 2.0, 29/08/14
 * @since   V2.0
 */

public class LatestReleasedPSFilter implements ProductStructureFilter, Serializable {

    private User user;
    private boolean diverge = false;

    public LatestReleasedPSFilter() {
    }

    public LatestReleasedPSFilter(User user) {
        this.user = user;
    }
    public LatestReleasedPSFilter(User user, boolean diverge) {
        this.user = user;
        this.diverge = diverge;
    }

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public List<PartIteration> filter(PartMaster part) {
        PartRevision partRevision = part.getLastReleasedRevision();
        if(partRevision != null){
            return Collections.singletonList(partRevision.getLastIteration());
        }
        return new ArrayList<>();
    }

    @Override
    public List<PartLink> filter(List<PartLink> path) {

        List<PartLink> links = new ArrayList<>();

        PartLink link = path.get(path.size()-1);
        links.add(link);

        if(diverge){
            for(PartSubstituteLink substituteLink: link.getSubstitutes()){
                links.add(substituteLink);
            }
        }

        return links;
    }

}