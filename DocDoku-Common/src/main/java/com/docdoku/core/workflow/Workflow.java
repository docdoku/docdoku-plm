/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010 DocDoku SARL
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

package com.docdoku.core.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.persistence.*;
import javax.persistence.Id;

/**
 * Workflows organize tasks around documents on which they're applied to.  
 * 
 * @author Florent GARIN
 * @version 1.0, 02/06/08
 * @since   V1.0
 */
@javax.persistence.Entity
public class Workflow implements Serializable, Cloneable {


    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Id
    private int id;
    
    @OneToMany(mappedBy = "workflow", cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    @OrderBy("step ASC")
    private List<Activity> activities=new LinkedList<Activity>();

    private String finalLifeCycleState;
    
    
    public Workflow(List<Activity> pActivities, String pFinalLifeCycleState) {
        activities=pActivities;
        finalLifeCycleState=pFinalLifeCycleState;
    }
    
    public Workflow() {

    }

    
    public Collection<Task> getRunningTasks(){
        
        Activity current = getCurrentActivity();
        if(current!=null){
            return current.getOpenTasks();
        }else
            return new ArrayList<Task>();
    }
    

    public int getCurrentStep() {
        int i = 0;
        for (Activity activity : activities) {
            if (activity.isComplete())
                i++;
            else
                break;
        }
        return i;
    }

    public Activity getCurrentActivity() {
        if (getCurrentStep() < activities.size())
            return getActivity(getCurrentStep());
        else
            return null;
    }

    public String getFinalLifeCycleState() {
        return finalLifeCycleState;
    }

    public int getId() {
        return id;
    }

    public java.util.List<Activity> getActivities() {
        return activities;
    }

    public void setActivities(java.util.List<Activity> activities) {
        this.activities = activities;
    }

    public void setId(int id) {
        this.id = id;
    }
    

    public void setFinalLifeCycleState(String finalLifeCycleState) {
        this.finalLifeCycleState = finalLifeCycleState;
    }

    public int numberOfSteps(){
        return activities.size();
    }
    
    public List<String> getLifeCycle(){
        List<String> lc=new LinkedList<String>();
        for(Activity activity:activities)
            lc.add(activity.getLifeCycleState());
        
        return lc;
    }
    
    public String getLifeCycleState() {
        Activity current = getCurrentActivity();
        return current==null?finalLifeCycleState:current.getLifeCycleState();
    }

    public Activity getActivity(int pIndex) {
        return activities.get(pIndex);
    }
    
    /**
     * perform a deep clone operation
     */
    @Override
    public Workflow clone() {
        Workflow clone = null;
        try {
            clone = (Workflow) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
        //perform a deep copy
        List<Activity> clonedActivities = new LinkedList<Activity>();
        for (Activity activity : activities) {
            Activity clonedActivity=activity.clone();
            clonedActivity.setWorkflow(clone);
            clonedActivities.add(clonedActivity);
        }
        clone.activities = clonedActivities;
        return clone;
    }
    
}
