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

package com.docdoku.core.configuration;


import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.product.PartRevision;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Class link that gathers a part collection and a given part iteration.
 *
 * @author Florent Garin
 * @version 2.0, 15/05/13
 * @since V2.0
 */

@Table(name = "BASELINEDPART")
@Entity
@NamedQueries({
        @NamedQuery(name = "BaselinedPart.existBaselinedPart", query = "SELECT count(b) FROM BaselinedPart b WHERE b.baselinedPartKey.targetPartNumber = :partNumber AND b.baselinedPartKey.targetPartWorkspaceId = :workspaceId"),
        @NamedQuery(name = "BaselinedPart.findByReference", query = "SELECT b FROM BaselinedPart b WHERE b.partCollection.id = :partCollection AND b.baselinedPartKey.targetPartNumber LIKE :id")
})
public class BaselinedPart implements Serializable {

    @EmbeddedId
    private BaselinedPartKey baselinedPartKey;

    //@MapsId("partCollectionId")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "PARTCOLLECTION_ID", referencedColumnName = "ID")
    private PartCollection partCollection;


    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "TARGET_ITERATION", referencedColumnName = "ITERATION"),
            @JoinColumn(name = "TARGET_PARTMASTER_PARTNUMBER", referencedColumnName = "PARTMASTER_PARTNUMBER"),
            @JoinColumn(name = "TARGET_PARTREVISION_VERSION", referencedColumnName = "PARTREVISION_VERSION"),
            @JoinColumn(name = "TARGET_WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID")
    })
    private PartIteration targetPart;


    @Column(name = "TARGET_ITERATION", nullable = false, insertable = false, updatable = false)
    private int targetPartIteration;

    @Column(name = "TARGET_PARTREVISION_VERSION", length = 10, nullable = false, insertable = false, updatable = false)
    private String targetPartVersion = "";

    public BaselinedPart() {
    }

    public BaselinedPart(PartCollection partCollection, PartIteration targetPart) {
        this.partCollection = partCollection;
        this.targetPart = targetPart;
        this.baselinedPartKey = new BaselinedPartKey(partCollection.getId(), targetPart.getWorkspaceId(), targetPart.getPartNumber());
        this.targetPartIteration = targetPart.getIteration();
        this.targetPartVersion = targetPart.getVersion();
    }


    @XmlTransient
    public PartCollection getPartCollection() {
        return partCollection;
    }

    public BaselinedPartKey getBaselinedPartKey() {
        return baselinedPartKey;
    }

    public PartIteration getTargetPart() {
        return targetPart;
    }

    public String getTargetPartVersion() {
        return targetPartVersion;
    }

    public void setTargetPartVersion(String targetPartVersion) {
        this.targetPartVersion = targetPartVersion;
    }

    public String getTargetPartNumber() {
        return targetPart.getPartNumber();
    }

    public int getTargetPartIteration() {
        return targetPartIteration;
    }

    public void setTargetPartIteration(int targetPartIteration) {
        this.targetPartIteration = targetPartIteration;
    }

    public List<PartIterationKey> getReleasedIterations() {
        List<PartIterationKey> partIterationKeyList = new ArrayList<>();
        for (PartRevision partRevision : targetPart.getPartRevision().getPartMaster().getPartRevisions()) {
            if (partRevision.isReleased()) {
                PartIterationKey partIterationKey = new PartIterationKey(partRevision.getWorkspaceId(), partRevision.getPartNumber(), partRevision.getVersion(), partRevision.getLastIteration().getIteration());
                partIterationKeyList.add(partIterationKey);
            }
        }
        return partIterationKeyList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BaselinedPart that = (BaselinedPart) o;

        return !(baselinedPartKey != null ? !baselinedPartKey.equals(that.baselinedPartKey) : that.baselinedPartKey != null);
    }

    @Override
    public int hashCode() {
        return baselinedPartKey != null ? baselinedPartKey.hashCode() : 0;
    }
}
