/*
 * ActivityModelModel.java
 * 
 * Copyright (c) 2009 Docdoku. All rights reserved.
 * 
 * This file is part of Docdoku.
 * 
 * Docdoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Docdoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Docdoku.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.docdoku.gwt.explorer.client.ui.workflow.editor.model;

import com.docdoku.gwt.explorer.common.AbstractActivityModelDTO;
import com.docdoku.gwt.explorer.common.TaskModelDTO;
import com.google.gwt.event.shared.HandlerRegistration;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public abstract class ActivityModelModel extends ModelObject {

    private String workspaceId;
    protected List<TaskModelModel> taskModels;
    protected AbstractActivityModelDTO model;
    

    public ActivityModelModel(AbstractActivityModelDTO model, String workspaceId) {
        this.model = model;
        this.workspaceId = workspaceId;
        taskModels = new ArrayList<TaskModelModel>();

        // we have at least 1 task
        if (model.getTasks().isEmpty()) {
            TaskModelModel tmpModel = new TaskModelModel(workspaceId);
            model.addTask(tmpModel.getData());
            taskModels.add(tmpModel);
        } else {
            for (TaskModelDTO taskModelDTO : model.getTasks()) {
                TaskModelModel tmpModel = new TaskModelModel(taskModelDTO);
                taskModels.add(tmpModel);
            }
        }
    }

    public void addTask() {
        TaskModelModel tmpModel = new TaskModelModel(workspaceId);
        model.addTask(tmpModel.getData());
        taskModels.add(tmpModel);
        ActivityEvent.fire(this, model.getTasks().size(), ActivityEvent.EventType.ADD_TASK);
       

    }

    public void removeTask(int position) {
        taskModels.get(position).removeAllListeners();
        taskModels.remove(position);
        model.getTasks().remove(position);
        ActivityEvent.fire(this, model.getTasks().size(), ActivityEvent.EventType.DELETE_TASK);

    }

    public List<TaskModelModel> getTasks() {
        return taskModels;
    }

    public void setLifeCycleState(String state) {
        model.setLifeCycleState(state);
    }

    public String getLifeCycleState() {
        return model.getLifeCycleState();
    }


    public AbstractActivityModelDTO getData() {
        return model;
    }

    public HandlerRegistration addActivityModelHandler(ActivityModelHandler handler){
        return addHandler(handler, ActivityEvent.TYPE);
    }
}
