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
import com.docdoku.core.configuration.*;
import com.docdoku.core.product.*;
import com.docdoku.core.util.Tools;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Morgan Guimard
 */
public class BaselineProductCreationConfigSpec extends ProductConfigSpec {

    private ProductBaseline productBaseline;
    private User user;

    public BaselineProductCreationConfigSpec(){
    }

    public BaselineProductCreationConfigSpec(ProductBaseline productBaseline, User user) {
        this.productBaseline = productBaseline;
        this.user = user;
    }

    public ProductBaseline getProductBaseline() {
        return productBaseline;
    }
    public void setProductBaseline(ProductBaseline productBaseline) {
        this.productBaseline = productBaseline;
    }

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    public int getPartCollectionId(){
        return productBaseline.getPartCollection().getId();
    }

    @Override
    public PartIteration filterPartIteration(PartMaster partMaster) {

        if(productBaseline.getType().equals(ProductBaseline.BaselineType.RELEASED)){

            // Try to identify the version
            PartCollection partCollection = productBaseline==null ? null : productBaseline.getPartCollection();
            if(partCollection != null) {
                BaselinedPartKey baselinedRootPartKey = new BaselinedPartKey(partCollection.getId(), partMaster.getWorkspaceId(), partMaster.getNumber());
                BaselinedPart baselinedRootPart = productBaseline.getBaselinedPart(baselinedRootPartKey);
                if (baselinedRootPart != null) {
                    return baselinedRootPart.getTargetPart();
                }
            }

            // Else, take the latest released
            PartRevision lastReleasedRevision = partMaster.getLastReleasedRevision();
            return lastReleasedRevision == null ? null : lastReleasedRevision.getLastIteration();

        }else if(productBaseline.getType().equals(ProductBaseline.BaselineType.LATEST)){
            PartIteration partIteration = partMaster.getLastRevision().getLastCheckedInIteration();
            return partIteration;
        }

        return null;
    }

    @Override
    public PartLink filterPartLink(List<PartLink> path) {

        // No ambiguities here, must return 1 value
        // Check if optional or substitute, nominal link else

        PartLink nominalLink = path.get(path.size()-1);

        if(nominalLink.isOptional() && productBaseline.isLinkOptional(Tools.getPathAsString(path))){
            return null;
        }

        for(PartSubstituteLink substituteLink:nominalLink.getSubstitutes()){

            List<PartLink> substitutePath = new ArrayList<>(path);
            substitutePath.set(substitutePath.size()-1,substituteLink);

            if(productBaseline.hasSubstituteLink(Tools.getPathAsString(substitutePath))){
                return substituteLink;
            }

        }

        return nominalLink;
    }

}