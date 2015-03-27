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


package com.docdoku.server.configuration.filter;

import com.docdoku.core.common.User;
import com.docdoku.core.configuration.PSFilter;
import com.docdoku.core.product.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Morgan Guimard
 *
 * Check for cyclic assembly after part iteration update : must check on the wip and on the latest.
 *
 */
public class UpdatePartIterationPSFilter extends PSFilter {

    private User user;
    private PartMasterKey rootKey;
    private PartIteration partIteration;

    public UpdatePartIterationPSFilter() {
    }

    public UpdatePartIterationPSFilter(User user, PartIteration partIteration) {
        this.user = user;
        this.partIteration = partIteration;
        rootKey = partIteration.getKey().getPartRevision().getPartMaster();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public List<PartIteration> filter(PartMaster part) {

        // Return wip on updated part iteration
        if(part.getKey().equals(rootKey)){
            return Arrays.asList(new PartIteration[]{partIteration});
        }

        // Return wip and last
        List<PartIteration> partIterations = new ArrayList<>();
        PartRevision partRevision = part.getLastRevision();
        PartIteration partIteration = partRevision.getLastIteration();
        PartIteration lastCheckedInIteration = partRevision.getLastCheckedInIteration();

        if(partRevision.isCheckedOut() && lastCheckedInIteration != null){
            partIterations.add(lastCheckedInIteration);
        }

        partIterations.add(partIteration);
        return partIterations;
    }

    @Override
    public List<PartLink> filter(List<PartLink> path) {

        List<PartLink> links = new ArrayList<>();
        PartLink link = path.get(path.size()-1);
        links.add(link);

        for(PartSubstituteLink substituteLink: link.getSubstitutes()){
            links.add(substituteLink);
        }

        return links;
    }

}
