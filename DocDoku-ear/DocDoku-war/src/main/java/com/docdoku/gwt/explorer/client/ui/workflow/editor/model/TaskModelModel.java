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

package com.docdoku.gwt.explorer.client.ui.workflow.editor.model;

import com.docdoku.gwt.explorer.client.data.ExplorerConstants;
import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.shared.TaskModelDTO;
import com.docdoku.server.rest.dto.UserDTO;
import java.util.ArrayList;
import java.util.List;

/**
 * TaskModelModel is part of the new new layer for model role in WorkflowEditor
 * TaskModelModel provides access to data contained in a TaskModelDTO
 * Whenever 
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class TaskModelModel {

    private TaskModelDTO data ;
    private List<TaskModelModelListener> observers ;


    /**
     * Build a TaskModelModel using data as sources
     * @param data
     */
    public TaskModelModel(TaskModelDTO data) {
        this.data = data;
        observers = new ArrayList<TaskModelModelListener>() ;
    }

    /**
     * Build a TaskModelModel creating a new data source
     * 
     */
    public TaskModelModel(String workspaceId){
        data = new TaskModelDTO() ;
        data.setInstructions("");
        data.setTitle(ServiceLocator.getInstance().getExplorerI18NConstants().taskName());
        observers = new ArrayList<TaskModelModelListener>();
        data.setWorker(ExplorerConstants.getInstance().getUser());
    }

    public TaskModelDTO getData() {
        return data;

    }

    public void setData(TaskModelDTO data) {
        this.data = data;
        fireChange();
    }

    public void setTaskName(String taskName) {
        data.setTitle(taskName);
        fireChange();
    }

    public void setResponsible(UserDTO responsible) {
        data.setWorker(responsible);
        fireChange();
    }

    public void setInstructions(String instructions) {
        data.setInstructions(instructions);
        fireChange();
    }



    public String getTaskName() {
        return data.getTitle();
    }

    public UserDTO getResponsible() {
        return data.getWorker();
    }

    public String getInstructions() {
        return data.getInstructions();
    }

    public void addListener (TaskModelModelListener l){
        observers.add(l);
    }

    public void removeListener(TaskModelModelListener l){
        observers.remove(l);
    }

    public void removeAllListeners(){
        observers.clear();
    }

    private void fireChange(){
        TaskModelEvent event = new TaskModelEvent(this);
        for (TaskModelModelListener listener : observers) {
            listener.onTaskModelModelChanged(event);
        }
    }
}
