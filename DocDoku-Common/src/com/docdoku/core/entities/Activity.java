/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010 DocDoku SARL
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

package com.docdoku.core.entities;

import java.io.Serializable;
import java.util.*;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

/**
 * A base class which represents a group of <a href="Task.html">Task</a> 
 * linked to a step of a <a href="Workflow.html">Workflow</a>.
 * It's the responsability of the concrete implementation to decide how
 * the workflow will progress to the next step and thus launch the execution
 * of the next Activity.
 * 
 * @author Florent GARIN
 * @version 1.0, 02/06/08
 * @since   V1.0
 */
@javax.persistence.IdClass(com.docdoku.core.entities.keys.ActivityKey.class)
@XmlSeeAlso({SerialActivity.class, ParallelActivity.class})
@Inheritance()
@Entity
public abstract class Activity implements Serializable, Cloneable {

    
    @javax.persistence.Id
    protected int step;
    
    @javax.persistence.Column(name = "WORKFLOW_ID", nullable = false, insertable = false, updatable = false)
    @javax.persistence.Id
    private int workflowId;
    
    @ManyToOne(optional=false, fetch=FetchType.EAGER)
    protected Workflow workflow;

    @OneToMany(mappedBy="activity", cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    @OrderBy(value="num")
    protected List<Task> tasks=new LinkedList<Task>();
       
    
    protected String lifeCycleState;
    
    
    public Activity(int pStep, List<Task> pTasks, String pLifeCycleState){
        tasks=pTasks;
        step=pStep;
        lifeCycleState=pLifeCycleState;
    }
    
    public Activity(){
        
    }
    
    public int getStep(){
        return step;
    }


    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public void setStep(int pStep){
        step=pStep;
    }
    
    public String getLifeCycleState(){
        return lifeCycleState;
    }
    
    public void setLifeCycleState(String pLifeCycleState){
        lifeCycleState=pLifeCycleState;
    }

    public java.util.List<Task> getTasks() {
        return tasks;
    }
    
    public int getWorkflowId() {
        return workflowId;
    }

    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
        workflowId=workflow.getId();
    }

    @XmlTransient
    public Workflow getWorkflow() {
        return workflow;
    }

    /**
     * perform a deep clone operation
     */
    @Override
    public Activity clone() {
        Activity clone = null;
        try {
            clone = (Activity) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
        //perform a deep copy
        List<Task> clonedTasks = new LinkedList<Task>();
        for (Task task : tasks) {
            Task clonedTask=task.clone();
            clonedTask.setActivity(clone);
            clonedTasks.add(clonedTask);
        }
        clone.tasks = clonedTasks;
        return clone;
    }

    
    public abstract Collection<Task> getOpenTasks();

    public abstract boolean isComplete();

    public abstract boolean isStopped(); 
    
}