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

import com.docdoku.core.common.User;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartRevision;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Table(name="BASELINECONFIGSPEC")
@Entity
public class BaselineConfigSpec extends ConfigSpec {

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Baseline baseline;
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private User user;

    public BaselineConfigSpec(){
    }

    public BaselineConfigSpec(Baseline baseline, User user) {
        this.baseline = baseline;
        this.user = user;
    }

    public Baseline getBaseline() {return baseline;}
    public void setBaseline(Baseline baseline) {this.baseline = baseline;}

    public User getUser() {return user;}
    public void setUser(User user) {this.user = user;}

    @Override
    public PartIteration filterConfigSpec(PartMaster part) {
        BaselinedPartKey baselinedRootPartKey = new BaselinedPartKey(baseline.getPartCollection().getId(),part.getWorkspaceId(),part.getNumber());
        BaselinedPart baselinedRootPart = baseline.getBaselinedPart(baselinedRootPartKey);
        if(baselinedRootPart != null){
            return baselinedRootPart.getTargetPart();
        }else{
            // the part isn't in baseline, choose the latest version-iteration uncheckouted
            PartIteration partI = part.getLastRevision().getLastIteration();
            PartRevision partRevision = partI.getPartRevision();
            if(partRevision.isCheckedOut() && !partRevision.getCheckOutUser().equals(user)){
                partI = partRevision.getLastUncheckoutedIteration();
            }
            return partI;
        }
    }
}
