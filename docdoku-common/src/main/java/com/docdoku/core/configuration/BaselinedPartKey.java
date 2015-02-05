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

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Identity class of {@link BaselinedPart} objects defined as an embeddable object in order
 * to be used inside the baselined parts map in the {@link PartCollection} class.
 *
 * @author Florent Garin
 */
@Embeddable
public class BaselinedPartKey implements Serializable{
    @Column(name = "PARTCOLLECTION_ID", nullable = false, insertable = false, updatable = false)
    private int partCollectionId;

    @Column(name = "TARGET_WORKSPACE_ID", length=100, nullable = false, insertable = false, updatable = false)
    private String targetPartWorkspaceId="";

    @Column(name = "TARGET_PARTMASTER_PARTNUMBER", length=100, nullable = false, insertable = false, updatable = false)
    private String targetPartNumber="";

    public BaselinedPartKey(){
    }

    public BaselinedPartKey(int partCollectionId, String targetPartWorkspaceId, String targetPartNumber) {
        this.partCollectionId = partCollectionId;
        this.targetPartWorkspaceId = targetPartWorkspaceId;
        this.targetPartNumber = targetPartNumber;
    }

    public int getPartCollectionId() {
        return partCollectionId;
    }

    public void setPartCollectionId(int partCollectionId) {
        this.partCollectionId = partCollectionId;
    }

    public String getTargetPartWorkspaceId() {
        return targetPartWorkspaceId;
    }

    public void setTargetPartWorkspaceId(String targetPartWorkspaceId) {
        this.targetPartWorkspaceId = targetPartWorkspaceId;
    }

    public String getTargetPartNumber() {
        return targetPartNumber;
    }

    public void setTargetPartNumber(String targetPartNumber) {
        this.targetPartNumber = targetPartNumber;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BaselinedPartKey)) {
            return false;
        }

        BaselinedPartKey that = (BaselinedPartKey) o;

        return partCollectionId == that.partCollectionId &&
               targetPartNumber.equals(that.targetPartNumber) &&
               targetPartWorkspaceId.equals(that.targetPartWorkspaceId);

    }

    @Override
    public int hashCode() {
        int result = partCollectionId;
        result = 31 * result + targetPartWorkspaceId.hashCode();
        result = 31 * result + targetPartNumber.hashCode();
        return result;
    }
}