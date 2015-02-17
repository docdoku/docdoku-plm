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

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * SerialActivity is an activity where
 * all tasks are launched subsequently in a specific order.
 * For the workflow to proceed to the next step, all tasks of 
 * SerialActivity should have been completed.
 * 
 * @author Florent Garin
 * @version 1.0, 02/06/08
 * @since   V1.0
 */
@Table(name="SERIALACTIVITY")
@Entity
public class SerialActivity extends Activity {
    public SerialActivity() {

    }

    public SerialActivity(int pStep, String pLifeCycleState) {
        super(pStep, pLifeCycleState);
    }
    
    @Override
    public boolean isStopped() {
        for(Task task:tasks)
            if(task.isRejected()) {
                return true;
            }
        
        return false;
    }

    @Override
    public Collection<Task> getOpenTasks() {
        List<Task> runningTasks = new ArrayList<>();
        if (!isComplete() && !isStopped()) {
            for(Task task:tasks){
                if (task.isInProgress() || task.isNotStarted()) {
                    runningTasks.add(task);
                    break;
                }
            }
        }
        return runningTasks;
    }
    
    @Override
    public boolean isComplete() {
        for(Task task:tasks) {
            if (!(task.isApproved() || task.isNotToBeDone())) {
                return false;
            }
        }
        
        return true;
    }

    @Override
    public void relaunch(){
        tasks.get(0).start();
    }
}