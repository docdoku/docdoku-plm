/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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

import com.docdoku.core.product.*;

import javax.persistence.*;
import java.util.Collections;

@Table(name="BASELINECONFIGSPEC")
@Entity
public class BaselineConfigSpec extends ConfigSpec {

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Baseline baseline;

    public BaselineConfigSpec(){
    }

    public BaselineConfigSpec(Baseline baseline) {
        this.baseline = baseline;
    }

    public Baseline getBaseline() {
        return baseline;
    }

    public void setBaseline(Baseline baseline) {
        this.baseline = baseline;
    }


    @Override
    public PartMaster filterConfigSpec(PartMaster root, int depth, EntityManager em){
        // int baselineId, String targetPartWorkspaceId, String targetPartNumber
        BaselinedPartKey baselinedRootPartKey = new BaselinedPartKey(baseline.getId(),root.getWorkspaceId(),root.getNumber());

        BaselinedPart baselinedRootPart = baseline.getBaselinedPart(baselinedRootPartKey);

        PartRevision partR ;
        PartIteration partI;

        if(baselinedRootPart != null){
            partR = baselinedRootPart.getTargetPart().getPartRevision();
            partI = baselinedRootPart.getTargetPart();
        }else{
            // the part isn't in baseline, choose the latest version-iteration
            partR = root.getLastRevision();
            partI = partR.getLastIteration();
        }

        if (partI != null) {
            if (depth != 0) {
                depth--;
                for (PartUsageLink usageLink : partI.getComponents()) {
                    filterConfigSpec(usageLink.getComponent(), depth, em);

                    for (PartSubstituteLink subLink : usageLink.getSubstitutes()) {
                        filterConfigSpec(subLink.getSubstitute(), 0, em);
                    }
                }
            }
        }

        for (PartAlternateLink alternateLink : root.getAlternates()) {
            filterConfigSpec(alternateLink.getAlternate(), 0, em);
        }

        em.detach(root);
        if (root.getPartRevisions().size() > 1) {
            root.getPartRevisions().retainAll(Collections.singleton(partR));
        }
        if (partR != null && partR.getNumberOfIterations() > 1) {
            partR.getPartIterations().retainAll(Collections.singleton(partI));
        }

        return root;
    }
}
