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
package com.docdoku.server.configuration.spec;

import com.docdoku.core.common.User;
import com.docdoku.core.configuration.ProductBaseline;
import com.docdoku.core.configuration.ProductConfigSpec;
import com.docdoku.core.product.*;
import com.docdoku.core.util.Tools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Morgan Guimard
 */
public class BaselineProductCreationConfigSpec extends ProductConfigSpec {

    private List<PartIteration> partIterations;
    private List<String> substituteLinks;
    private List<String> optionalUsageLinks;

    private Set<PartIteration> retainedPartIterations = new HashSet<>();
    private Set<String> retainedSubstituteLinks = new HashSet<>();
    private Set<String> retainedOptionalUsageLinks = new HashSet<>();

    private ProductBaseline.BaselineType type;

    private User user;

    public BaselineProductCreationConfigSpec(){
    }

    public BaselineProductCreationConfigSpec(User user, ProductBaseline.BaselineType type ,List<PartIteration> partIterations, List<String> substituteLinks, List<String> optionalUsageLinks) {
        this.user = user;
        this.partIterations = partIterations;
        this.substituteLinks = substituteLinks;
        this.optionalUsageLinks = optionalUsageLinks;
        this.type = type;
    }

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public PartIteration filterPartIteration(PartMaster partMaster) {

        if(type.equals(ProductBaseline.BaselineType.RELEASED)){

            for(PartIteration pi : partIterations){
                if(pi.getPartRevision().getPartMaster().getKey().equals(partMaster.getKey())){
                    retainedPartIterations.add(pi);
                    return pi;
                }
            }
            // Else, take the latest released
            PartRevision lastReleasedRevision = partMaster.getLastReleasedRevision();
            if(lastReleasedRevision != null){
                PartIteration pi = lastReleasedRevision.getLastIteration();
                retainedPartIterations.add(pi);
                return pi;
            }

        }else if(type.equals(ProductBaseline.BaselineType.LATEST)){

            PartIteration pi = partMaster.getLastRevision().getLastCheckedInIteration();

            if(pi!=null){
                retainedPartIterations.add(pi);
                return pi;
            }
        }

        return null;
    }

    @Override
    public PartLink filterPartLink(List<PartLink> path) {

        // No ambiguities here, must return 1 value
        // Check if optional or substitute, nominal link else

        PartLink nominalLink = path.get(path.size()-1);

        if(nominalLink.isOptional() && optionalUsageLinks.contains(Tools.getPathAsString(path))){
            retainedOptionalUsageLinks.add(Tools.getPathAsString(path));
            return null;
        }

        for(PartSubstituteLink substituteLink:nominalLink.getSubstitutes()){

            List<PartLink> substitutePath = new ArrayList<>(path);
            substitutePath.set(substitutePath.size()-1,substituteLink);

            if(substituteLinks.contains(Tools.getPathAsString(substitutePath))){
                retainedSubstituteLinks.add(Tools.getPathAsString(substitutePath));
                return substituteLink;
            }

        }

        return nominalLink;
    }


    public Set<PartIteration> getRetainedPartIterations() {
        return retainedPartIterations;
    }

    public Set<String> getRetainedSubstituteLinks() {
        return retainedSubstituteLinks;
    }

    public Set<String> getRetainedOptionalUsageLinks() {
        return retainedOptionalUsageLinks;
    }
}