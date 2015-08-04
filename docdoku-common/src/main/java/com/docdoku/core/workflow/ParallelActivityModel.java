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

package com.docdoku.core.workflow;

import com.docdoku.core.common.User;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class is the model used to create instances
 * of {@link ParallelActivity} attached to workflows.
 * 
 * @author Florent Garin
 * @version 1.0, 02/06/08
 * @since   V1.0
 */
@Table(name="PARALLELACTIVITYMODEL")
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
        this(pWorkflowModel, 0,  new LinkedList<>(), pLifeCycleState,pTasksToComplete);
    }

 
    public int getTasksToComplete() {
        return tasksToComplete;
    }

    public void setTasksToComplete(int pTasksToComplete) {
        if (pTasksToComplete < this.getTaskModels().size()) {
            tasksToComplete = pTasksToComplete;
        } else {
            tasksToComplete = this.getTaskModels().size();
        }
    }

    @Override
    public void removeTaskModel(TaskModel pTaskModel) {
        super.removeTaskModel(pTaskModel);
        if (tasksToComplete > taskModels.size()) {
            tasksToComplete--;
        }
    }

    @Override
    public Activity createActivity(Map<Role, User> roleUserMap) {
        Activity activity = new ParallelActivity(step, lifeCycleState, tasksToComplete);
        List<Task> tasks = activity.getTasks();
        for(TaskModel model:taskModels){
            Task task = model.createTask(roleUserMap);
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