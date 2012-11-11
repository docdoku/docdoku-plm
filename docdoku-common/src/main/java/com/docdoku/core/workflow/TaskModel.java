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

import com.docdoku.core.common.User;
import java.io.Serializable;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;

/**
 * This is the model for creating instances of <a href="Task.html">Task</a>
 * that belong to instances of <a href="Activity.html">Activity</a> themselves
 * attached to instances of <a href="Workflow.html">Workflow</a>.
 * 
 * @author Florent Garin
 * @version 1.0, 02/06/08
 * @since   V1.0
 */
@Table(name="TASKMODEL")
@javax.persistence.IdClass(com.docdoku.core.workflow.TaskModelKey.class)
@Entity
public class TaskModel implements Serializable, Cloneable {
    
    @ManyToOne(optional=false, fetch=FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name="ACTIVITYMODEL_STEP", referencedColumnName="STEP"),
        @JoinColumn(name="WORKFLOWMODEL_ID", referencedColumnName="WORKFLOWMODEL_ID"),
        @JoinColumn(name="WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
    })
    private ActivityModel activityModel;
    
    @Id
    private int num;
    
    @javax.persistence.Column(name = "ACTIVITYMODEL_STEP", nullable = false, insertable = false, updatable = false)
    @javax.persistence.Id
    private int activityModelStep;
    
    @javax.persistence.Column(name = "WORKFLOWMODEL_ID", length=50, nullable = false, insertable = false, updatable = false)
    @javax.persistence.Id
    private String workflowModelId="";
    
    @javax.persistence.Column(name = "WORKSPACE_ID", length=50, nullable = false, insertable = false, updatable = false)
    @javax.persistence.Id
    private String workspaceId="";
    
    @Lob
    private String instructions;
    private String title;
    private int duration;
    
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name="WORKER_LOGIN", referencedColumnName="LOGIN"),
        @JoinColumn(name="WORKER_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
    })
    private User worker;

    public TaskModel(ActivityModel pActivityModel, int pNum, String pTitle, String pInstructions, User pWorker) {
        setActivityModel(pActivityModel);
        num=pNum;
        title=pTitle;
        worker=pWorker;
        instructions=pInstructions;
    }

    public TaskModel(ActivityModel pActivityModel, String pTitle, String pInstructions, User pWorker) {
        this(pActivityModel, 0,pTitle,pInstructions,pWorker);
    }
    public TaskModel() {

    }
    
    public User getWorker() {
        return worker;
    }

    public Task createTask() {
        return new Task(num, title,instructions,worker);
    }
    
    public String getTitle() {
        return title;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }


    public String getInstructions() {
        return instructions;
    }

    public void setWorker(User pWorker) {
        worker=pWorker;
    }

    public void setTitle(String pTitle) {
        title=pTitle;
    }

    public void setInstructions(String pInstructions) {
        instructions = pInstructions;
    }

    @XmlTransient
    public ActivityModel getActivityModel() {
        return activityModel;
    }

    public int getActivityModelStep() {
        return activityModelStep;
    }

    public String getWorkflowModelId() {
        return workflowModelId;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setActivityModel(ActivityModel activityModel) {
        this.activityModel = activityModel;
        activityModelStep=activityModel.getStep();
        workflowModelId=activityModel.getWorkflowModelId();
        workspaceId=activityModel.getWorkspaceId();
    }


    
    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

        @Override
    public int hashCode() {
        int hash = 1;
	hash = 31 * hash + workspaceId.hashCode();
	hash = 31 * hash + workflowModelId.hashCode();
        hash = 31 * hash + activityModelStep;
        hash = 31 * hash + num;
	return hash;
    }
    
    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof TaskModel))
            return false;
        TaskModel model = (TaskModel) pObj;
        return ((model.workspaceId.equals(workspaceId)) && (model.workflowModelId.equals(workflowModelId)) && (model.activityModelStep==activityModelStep) && (model.num==num));
    }
    
    @Override
    public String toString() {
        return workspaceId + "-" + workflowModelId + "-" + activityModelStep + "-" + num;
    }
    

    @Override
    public TaskModel clone() {
        TaskModel clone = null;
        try {
            clone = (TaskModel) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
        return clone;
    }

}
