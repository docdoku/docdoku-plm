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

package com.docdoku.server.rest.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WorkflowDTO implements Serializable, Comparable<WorkflowDTO> {

    private int id;
    private String finalLifeCycleState;
    private List<ActivityDTO> activities;
    private Date abortedDate;

    public WorkflowDTO() {
        activities = new ArrayList<>();
    }

    public List<ActivityDTO> getActivities() {
        return activities;
    }

    public int getId() {
        return id;
    }

    public void setActivities(List<ActivityDTO> activities) {
        this.activities = activities;
    }

    public String getFinalLifeCycleState() {
        return finalLifeCycleState;
    }

    public void setFinalLifeCycleState(String finalLifeCycleState) {
        this.finalLifeCycleState = finalLifeCycleState;
    }

    public String getLifeCycleState() {
        ActivityDTO current = getCurrentActivity();
        return (current==null)?finalLifeCycleState:current.getLifeCycleState();
    }

    public Date getAbortedDate() {
        return abortedDate;
    }

    public void setAbortedDate(Date abortedDate) {
        this.abortedDate = abortedDate;
    }

    public ActivityDTO getCurrentActivity() {
        if (getCurrentStep() < activities.size())
            return activities.get(getCurrentStep());
        else
            return null;
    }
    
    public void setId(int id) {
        this.id = id;
    }

    public int getCurrentStep() {
        int i = 0;
        for (ActivityDTO activity : activities) {
            if (activity.isComplete())
                i++;
            else
                break;
        }
        return i;
    }

    @Override
    public int compareTo(WorkflowDTO o) {
        return o.getAbortedDate().compareTo(this.getAbortedDate());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkflowDTO that = (WorkflowDTO) o;
        return id == that.id
                && !(abortedDate != null ? !abortedDate.equals(that.abortedDate) : that.abortedDate != null)
                && !(activities != null ? !activities.equals(that.activities) : that.activities != null)
                && !(finalLifeCycleState != null ? !finalLifeCycleState.equals(that.finalLifeCycleState) : that.finalLifeCycleState != null);

    }
}
