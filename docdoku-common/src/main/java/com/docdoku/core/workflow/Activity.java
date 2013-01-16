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

package com.docdoku.core.workflow;

import com.docdoku.core.util.Tools;
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
 * @author Florent Garin
 * @version 1.0, 02/06/08
 * @since   V1.0
 */
@Table(name="ACTIVITY")
@javax.persistence.IdClass(com.docdoku.core.workflow.ActivityKey.class)
@XmlSeeAlso({SerialActivity.class, ParallelActivity.class})
@Inheritance()
@Entity
public abstract class Activity implements Serializable, Cloneable {

    
    @Id
    protected int step;
    
    @Id
    @ManyToOne(optional=false, fetch=FetchType.EAGER)
    @JoinColumn(name="WORKFLOW_ID", referencedColumnName="ID")
    protected Workflow workflow;

    @OneToMany(mappedBy="activity", cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    @OrderBy(value="num")
    protected List<Task> tasks=new LinkedList<Task>();
       
    
    protected String lifeCycleState;
    
    
    public Activity(int pStep, String pLifeCycleState){
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
    
    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
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
    
    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof Activity))
            return false;
        Activity activity = (Activity) pObj;
        return ((activity.step==step) && (Tools.safeEquals(activity.workflow, workflow)));
    }
    
    @Override
    public int hashCode() {
        int hash = 1;
	hash = 31 * hash + (workflow==null?0:workflow.hashCode());
	hash = 31 * hash + step;
	return hash;
    }
    
    
    public int getWorkflowId() {
        return workflow==null?0:workflow.getId();
    }

    public ActivityKey getKey() {
        return new ActivityKey(getWorkflowId(), step);
    }
    

}