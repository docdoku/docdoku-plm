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

package com.docdoku.server.rest.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * @author Florent Garin
 */
@XmlRootElement
@ApiModel(value="TagSubscriptionDTO", description="This is a representation of a {@link com.docdoku.core.meta.TagUserSubscription} or {@link com.docdoku.core.meta.TagUserGroupSubscription} entity")
public class TagSubscriptionDTO implements Serializable {

    @ApiModelProperty(value = "Tag name")
    private String tag;

    @ApiModelProperty(value = "Iteration change flag")
    private boolean onIterationChange;

    @ApiModelProperty(value = "State change flag")
    private boolean onStateChange;

    public TagSubscriptionDTO() {

    }

    public TagSubscriptionDTO(String tag, boolean onIterationChange, boolean onStateChange) {
        this.tag = tag;
        this.onIterationChange = onIterationChange;
        this.onStateChange = onStateChange;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public boolean isOnIterationChange() {
        return onIterationChange;
    }

    public void setOnIterationChange(boolean onIterationChange) {
        this.onIterationChange = onIterationChange;
    }

    public boolean isOnStateChange() {
        return onStateChange;
    }

    public void setOnStateChange(boolean onStateChange) {
        this.onStateChange = onStateChange;
    }
}
