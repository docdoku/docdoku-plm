/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.persistence.*;

/**
 * This class is the model used to create instances
 * of <a href="ParallelActivity.html">ParallelActivity</a> attached to
 * workflows.
 * 
 * @author Florent Garin
 * @version 1.0, 02/06/08
 * @since   V1.0
 */
@Entity
public class ParallelActivityModel extends ActivityModel {

        
    private int tasksToComplete;

    public ParallelActivityModel() {
    }
    
    public ParallelActivityModel(WorkflowModel pWorkflowModel, int pStep, List<TaskModel> pTaskModels, String pLifeCycleState, int pTasksToComplete) {
        super(pWorkflowModel, pStep, pTaskModels, pLifeCycleState);
        tasksToComplete=pTasksToComplete;       
    }
    
    public ParallelActivityModel(WorkflowModel pWorkflowModel, String pLifeCycleState, int pTasksToComplete) {
        this(pWorkflowModel, 0,  new ArrayList<TaskModel>(), pLifeCycleState,pTasksToComplete);      
    }

 
    public int getTasksToComplete() {
        return tasksToComplete;
    }

    public void setTasksToComplete(int pTasksToComplete) {
        tasksToComplete = pTasksToComplete;
    }

    @Override
    public void removeTaskModel(TaskModel pTaskModel) {
        super.removeTaskModel(pTaskModel);
        if (tasksToComplete > taskModels.size())
            tasksToComplete--;
    }

    @Override
    public Activity createActivity() {
        Activity activity = new ParallelActivity(step, lifeCycleState, tasksToComplete);
        List<Task> tasks = activity.getTasks();
        for(TaskModel model:taskModels){
            Task task = model.createTask();
            task.setActivity(activity);
            tasks.add(task);
        }
        return activity;
    }

    @Override
    public String toString() {
        return taskModels + " (" + getTasksToComplete() + "/" + taskModels.size() + ")";
    }
    
    @Override
    public ParallelActivityModel clone() {
        return (ParallelActivityModel) super.clone();
    }
}
