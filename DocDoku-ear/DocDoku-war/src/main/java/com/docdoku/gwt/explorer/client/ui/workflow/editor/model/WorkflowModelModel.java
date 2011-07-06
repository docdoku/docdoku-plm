/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011 DocDoku SARL
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

import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.shared.ActivityModelDTO;
import com.docdoku.gwt.explorer.shared.ParallelActivityModelDTO;
import com.docdoku.gwt.explorer.shared.SerialActivityModelDTO;
import com.docdoku.gwt.explorer.shared.WorkflowModelDTO;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class WorkflowModelModel {

    private List<WorkflowModelListener> observers;

    private List<ActivityModelModel> activities ;
    private WorkflowModelDTO model ;
    private String workspaceId ;

    public WorkflowModelModel(WorkflowModelDTO model, String workspaceId) {
        observers = new ArrayList<WorkflowModelListener>();
        this.workspaceId = workspaceId;
        activities = new ArrayList<ActivityModelModel>();
        if (model != null){
            this.model = model;

            for (ActivityModelDTO activity : model.getActivityModels()) {
                ActivityModelModel tmpModel = null ;
                if(activity instanceof SerialActivityModelDTO){
                    tmpModel =new SerialActivityModelModel((SerialActivityModelDTO) activity,workspaceId);
                }else{
                    ParallelActivityModelModel tmp = new ParallelActivityModelModel((ParallelActivityModelDTO) activity,workspaceId);
                    tmp.setTasksToComplete(((ParallelActivityModelDTO)activity).getTasksToComplete());
                    tmpModel = tmp ;
                }
                activities.add(tmpModel);
            }

        }else{
            this.model = new WorkflowModelDTO() ;
            this.model.setWorkspaceId(workspaceId);
            this.model.setFinalLifeCycleState(ServiceLocator.getInstance().getExplorerI18NConstants().stateName());
            this.model.setCreationDate(new Date());
        }
    }

    public WorkflowModelModel(String workspaceId) {
        this(null, workspaceId);
    }


    public void addSerialActivity(int position){
        SerialActivityModelDTO tmpDto = new SerialActivityModelDTO() ;
        tmpDto.setLifeCycleState(ServiceLocator.getInstance().getExplorerI18NConstants().stateName());
        SerialActivityModelModel tmp = new SerialActivityModelModel(tmpDto, workspaceId);

        model.getActivityModels().add(position,tmpDto);
        activities.add(position, tmp);

        fireActivityAdded(position) ;
    }

    public void addParallelActivity(int position){
        ParallelActivityModelDTO tmpDto = new ParallelActivityModelDTO();
        tmpDto.setLifeCycleState(ServiceLocator.getInstance().getExplorerI18NConstants().stateName());
        tmpDto.setTasksToComplete(1) ;
        ParallelActivityModelModel tmp = new ParallelActivityModelModel(tmpDto, workspaceId);

        model.getActivityModels().add(position, tmpDto);
        activities.add(position,tmp);

        fireActivityAdded(position) ;
    }

    public void setStateName(int position, String newName){
        model.getActivityModels().get(position).setLifeCycleState(newName);
    }

    public void setFinalStateName(String newName){
        model.setFinalLifeCycleState(newName);
    }

    public String getFinalStateName(){
        return model.getFinalLifeCycleState();
    }

    public void removeActivity (int position){
        model.getActivityModels().remove(position);
        activities.remove(position);

        WorkflowModelEvent event = new WorkflowModelEvent(this, position, WorkflowModelEvent.ActivityOperation.ACTIVITY_DELETE);
        for (WorkflowModelListener listener : observers) {
            listener.onWorkflowModelChanged(event);
        }
    }

    public void addListener (WorkflowModelListener l){
        observers.add(l);
    }

    public void removeListener( WorkflowModelListener l){
        observers.remove(l);
    }

    public List<ActivityModelModel> getActivities(){
        return activities ;
    }

    public WorkflowModelDTO getData() {
        return model ;
    }

    private void fireActivityAdded(int position){
        WorkflowModelEvent event = new WorkflowModelEvent(this, position, WorkflowModelEvent.ActivityOperation.ACTIVITY_ADD);
        for (WorkflowModelListener listener : observers) {
            listener.onWorkflowModelChanged(event);
        }
    }

}
