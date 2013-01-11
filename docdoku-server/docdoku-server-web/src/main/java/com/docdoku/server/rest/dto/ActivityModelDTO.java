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

import javax.xml.bind.annotation.XmlSeeAlso;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ActivityModelDTO implements Serializable {

    private int step;
    private List<TaskModelDTO> taskModels;
    private String lifeCycleState;
    private Type type;
    public enum Type {
        SERIAL, PARALLEL
    }

    private Integer tasksToComplete;

    public ActivityModelDTO() {
        this.taskModels = new ArrayList<TaskModelDTO>();
    }

    public ActivityModelDTO(int step, List<TaskModelDTO> taskModels, String lifeCycleState, Type type, Integer tasksToComplete) {
        this.step = step;
        this.taskModels = taskModels;
        this.lifeCycleState = lifeCycleState;
        this.type = type;
        this.tasksToComplete = tasksToComplete;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public void addTaskModel(TaskModelDTO m) {
        this.taskModels.add(m);
    }

    public void removeTaskModel(TaskModelDTO m) {
        this.taskModels.remove(m);
    }

    public List<TaskModelDTO> getTaskModels() {
        return this.taskModels;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Integer getTasksToComplete() {
        return tasksToComplete;
    }

    public void setTasksToComplete(Integer tasksToComplete) {
        this.tasksToComplete = tasksToComplete;
    }

    public void setLifeCycleState(String lifeCycleState) {
        this.lifeCycleState = lifeCycleState;
    }

    public String getLifeCycleState() {
        return lifeCycleState;
    }
}
