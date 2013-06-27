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
import java.util.List;


public class ActivityDTO implements Serializable {

    private int step;
    private Integer relaunchStep;
    private List<TaskDTO> tasks;
    private String lifeCycleState;
    private Type type;
    private Integer tasksToComplete;
    private boolean complete;
    private boolean stopped;
    public enum Type {
        SERIAL, PARALLEL;
    }

    public ActivityDTO(int step, List<TaskDTO> tasks, String lifeCycleState, Type type, Integer tasksToComplete, boolean complete, boolean stopped, Integer relaunchStep) {
        this.step = step;
        this.relaunchStep = relaunchStep;
        this.tasks = tasks;
        this.lifeCycleState = lifeCycleState;
        this.type = type;
        this.tasksToComplete = tasksToComplete;
        this.complete = complete;
        this.stopped = stopped;
    }

    public ActivityDTO() {
        tasks = new ArrayList<TaskDTO>();
    }

    public Integer getTasksToComplete() {
        return tasksToComplete;
    }

    public void setTasksToComplete(Integer tasksToComplete) {
        this.tasksToComplete = tasksToComplete;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public List<TaskDTO> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskDTO> tasks) {
        this.tasks = tasks;
    }

    public boolean isStopped() {
        return stopped;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public String getLifeCycleState() {
        return lifeCycleState;
    }

    public void setLifeCycleState(String lifeCycleState) {
        this.lifeCycleState = lifeCycleState;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }

    public Integer getRelaunchStep() {
        return relaunchStep;
    }

    public void setRelaunchStep(Integer relaunchStep) {
        this.relaunchStep = relaunchStep;
    }
}
