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

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link com.docdoku.core.configuration.ProductConfigSpec} which selects the latest iteration (checkin or not)
 *
 * @author Morgan Guimard
 */
public class WIPPSFilter extends PSFilter {

    private User user;
    private boolean diverge = false;
    private EntityManager em;

    public WIPPSFilter(EntityManager em) {
        this.em = em;
    }

    public WIPPSFilter(User user, EntityManager em) {
        this.user = user;
        this.em = em;
    }
    public WIPPSFilter(User user, boolean diverge, EntityManager em) {
        this.user = user;
        this.diverge = diverge;
        this.em = em;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public List<PartIteration> filter(PartMaster part) {
        List<PartIteration> partIterations = new ArrayList<>();
        if(isCheckoutByAnotherUser(user,part.getLastRevision())) {
            em.detach(part);
            part.getLastRevision().removeLastIteration();
        }
        if(part.getLastRevision().getPartIterations().size() == 0) {
            em.detach(part);
            part.removeLastRevision();
        }
        PartIteration partIteration = part.getLastRevision().getLastIteration();
        partIterations.add(partIteration);
        return partIterations;
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

    //TODO: Duplicate from ProductManagerBean. Need refactor.
    private boolean isCheckoutByAnotherUser(User user, PartRevision partRevision) {
        return partRevision.isCheckedOut() && !partRevision.getCheckOutUser().equals(user);
    }

}
