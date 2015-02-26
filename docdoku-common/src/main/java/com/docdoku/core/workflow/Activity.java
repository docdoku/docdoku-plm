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

import javax.persistence.*;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * A base class which represents a group of {@link Task}
 * linked to a step of a {@link Workflow}.
 * It's the responsibility of the concrete implementation to decide how
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
    protected List<Task> tasks=new LinkedList<>();
    protected String lifeCycleState;
    @ManyToOne(optional = true,fetch=FetchType.EAGER)
    @JoinTable (
            name="ACTIVITY_RELAUNCH",
            joinColumns={
                    @JoinColumn(name="ACTIVITY_STEP", referencedColumnName="STEP"),
                    @JoinColumn(name="ACTIVITY_WORKFLOW_ID", referencedColumnName="WORKFLOW_ID")
            },
            inverseJoinColumns={
                    @JoinColumn(name="RELAUNCH_STEP", referencedColumnName = "STEP"),
                    @JoinColumn(name="RELAUNCH_WORKFLOW_ID", referencedColumnName = "WORKFLOW_ID")
            }
    )
    private Activity relaunchActivity;

    public Activity(){

    }
    public Activity(int pStep, String pLifeCycleState){
        step=pStep;
        lifeCycleState=pLifeCycleState;
    }

    public ActivityKey getKey() {
        return new ActivityKey(getWorkflowId(), step);
    }

    public int getStep(){
        return step;
    }
    public void setStep(int pStep){
        step=pStep;
    }

    public List<Task> getTasks() {
        return tasks;
    }
    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public String getLifeCycleState(){
        return lifeCycleState;
    }
    public void setLifeCycleState(String pLifeCycleState){
        lifeCycleState=pLifeCycleState;
    }

    @XmlTransient
    public Workflow getWorkflow() {
        return workflow;
    }
    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }
    public int getWorkflowId() {
        return workflow==null ? 0 : workflow.getId();
    }

    @XmlTransient
    public Activity getRelaunchActivity() {
        return relaunchActivity;
    }
    public void setRelaunchActivity(Activity relaunchActivity) {
        this.relaunchActivity = relaunchActivity;
    }

    public abstract Collection<Task> getOpenTasks();

    public abstract boolean isComplete();
    public abstract boolean isStopped();
    public boolean isInProgress() {
        if (!isComplete() && !isStopped()) {
            for (Task task : tasks) {
                if(task.isInProgress()){
                    return true;
                }
            }
        }
        return false;
    }
    public boolean isToDo(){
        for(Task task : tasks){
            if(task.isNotToBeDone()){
                return false;
            }
        }
        return true;
    }

    public abstract void relaunch();

    /**
     * perform a deep clone operation
     */
    @Override
    public Activity clone() {
        try {
            Activity clone = (Activity) super.clone();
            //perform a deep copy
            List<Task> clonedTasks = new LinkedList<>();
            for (Task task : tasks) {
                Task clonedTask=task.clone();
                clonedTask.setActivity(clone);
                clonedTasks.add(clonedTask);
            }
            clone.tasks = clonedTasks;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Activity activity = (Activity) o;

        return step == activity.step
                && (workflow != null ? workflow.equals(activity.workflow) : activity.workflow == null);
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = 31 * hash + (workflow==null?0:workflow.hashCode());
        hash = 31 * hash + step;
        return hash;
    }
}