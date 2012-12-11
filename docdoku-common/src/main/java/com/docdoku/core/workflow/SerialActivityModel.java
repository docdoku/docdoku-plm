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
import java.util.List;
import javax.persistence.*;

/**
 * This class is the model used to create instances
 * of <a href="SerialActivity.html">SerialActivity</a> attached to
 * workflows.
 * 
 * @author Florent Garin
 * @version 1.0, 02/06/08
 * @since   V1.0
 */
@Table(name="SERIALACTIVITYMODEL")
@Entity
public class SerialActivityModel extends ActivityModel {


    
    public SerialActivityModel() {
    }

    public SerialActivityModel(WorkflowModel pWorkflowModel, int pStep, List<TaskModel> pTaskModels, String pLifeCycleState) {
        super(pWorkflowModel, pStep, pTaskModels, pLifeCycleState);      
    }

    public SerialActivityModel(WorkflowModel pWorkflowModel, String pLifeCycleState) {
        this(pWorkflowModel, 0,  new ArrayList<TaskModel>(), pLifeCycleState);      
    }
        
    @Override
    public Activity createActivity() {
        Activity activity = new SerialActivity(step, lifeCycleState);
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
        return taskModels.toString();
    }

    public void moveUpTaskModel(int pSelectedIndex) {
        if (pSelectedIndex > 0) {
            TaskModel taskModel = taskModels.remove(pSelectedIndex);
            int newIndex=pSelectedIndex-1;
            taskModels.get(newIndex).setNum(pSelectedIndex);
            taskModels.add(newIndex, taskModel);
            taskModel.setNum(newIndex);
        }
    }

    public void moveDownTaskModel(int pSelectedIndex) {
        if (pSelectedIndex < taskModels.size() - 1) {
            TaskModel taskModel = taskModels.remove(pSelectedIndex);
            int newIndex=pSelectedIndex+1;
            taskModels.get(pSelectedIndex).setNum(pSelectedIndex);
            taskModels.add(newIndex, taskModel);
            taskModel.setNum(newIndex);
        }
    }

    @Override
    public SerialActivityModel clone() {
        return (SerialActivityModel) super.clone();
    }
}
