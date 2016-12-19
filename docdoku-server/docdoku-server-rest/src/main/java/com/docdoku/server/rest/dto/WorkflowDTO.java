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

package com.docdoku.server.rest.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@XmlRootElement
@ApiModel(value = "WorkflowDTO", description = "This class is a representation of a {@link com.docdoku.core.workflow.Workflow} entity")
public class WorkflowDTO implements Serializable, Comparable<WorkflowDTO> {

    @ApiModelProperty(value = "Workflow id")
    private int id;

    @ApiModelProperty(value = "Workflow final lifecycle state")
    private String finalLifeCycleState;

    @ApiModelProperty(value = "Workflow activity list")
    private List<ActivityDTO> activities;

    @ApiModelProperty(value = "Workflow aborted date if aborted")
    private Date abortedDate;

    public WorkflowDTO() {
        activities = new ArrayList<>();
    }

    public List<ActivityDTO> getActivities() {
        return activities;
    }

    public void setActivities(List<ActivityDTO> activities) {
        this.activities = activities;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFinalLifeCycleState() {
        return finalLifeCycleState;
    }

    public void setFinalLifeCycleState(String finalLifeCycleState) {
        this.finalLifeCycleState = finalLifeCycleState;
    }

    public String getLifeCycleState() {
        ActivityDTO current = getCurrentActivity();
        return (current == null) ? finalLifeCycleState : current.getLifeCycleState();
    }

    public Date getAbortedDate() {
        return (abortedDate != null) ? (Date) abortedDate.clone() : null;
    }

    public void setAbortedDate(Date abortedDate) {
        this.abortedDate = (abortedDate != null) ? (Date) abortedDate.clone() : null;
    }

    public ActivityDTO getCurrentActivity() {
        if (getCurrentStep() < activities.size()) {
            return activities.get(getCurrentStep());
        } else {
            return null;
        }
    }

    public int getCurrentStep() {
        int i = 0;
        for (ActivityDTO activity : activities) {
            if (activity.isComplete()) {
                i++;
            } else {
                break;
            }
        }
        return i;
    }

    @Override
    public int compareTo(WorkflowDTO o) {
        return o.getAbortedDate().compareTo(this.getAbortedDate());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WorkflowDTO that = (WorkflowDTO) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (finalLifeCycleState != null ? finalLifeCycleState.hashCode() : 0);
        result = 31 * result + (activities != null ? activities.hashCode() : 0);
        result = 31 * result + (abortedDate != null ? abortedDate.hashCode() : 0);
        return result;
    }
}
