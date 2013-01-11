/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,  
 * but WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
 * GNU General Public License for more details.  
 *  
 * You should have received a copy of the GNU General Public License  
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.  
 */

package com.docdoku.server.rest.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WorkflowDTO implements Serializable {

    private int id;
    private String finalLifeCycleState;
    private List<ActivityDTO> activities;

    public WorkflowDTO() {
        activities = new ArrayList<ActivityDTO>();
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
        return current==null?finalLifeCycleState:current.getLifeCycleState();
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

}
