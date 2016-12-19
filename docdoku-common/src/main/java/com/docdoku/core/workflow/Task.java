/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
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

import com.docdoku.core.common.User;
import com.docdoku.core.common.UserGroup;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.*;

/**
 * Task is the smallest unit of work in a workflow.
 *
 * @author Florent Garin
 * @version 2.5, 27/06/16
 * @since V1.0
 */
@Table(name = "TASK")
@javax.persistence.IdClass(com.docdoku.core.workflow.TaskKey.class)
@Entity
@NamedQueries ({
    @NamedQuery(name="Task.findInProgressTasks", query="SELECT DISTINCT t FROM Task t LEFT JOIN t.assignedUsers au LEFT JOIN t.assignedGroups ag LEFT JOIN ag.users agu WHERE ((au.login = :login AND au.workspaceId = :workspaceId) OR (agu.login = :login AND agu.workspaceId = :workspaceId)) AND t.status = com.docdoku.core.workflow.Task.Status.IN_PROGRESS"),
    @NamedQuery(name="Task.findAssignedTasks", query="SELECT DISTINCT t FROM Task t LEFT JOIN t.assignedUsers au LEFT JOIN t.assignedGroups ag LEFT JOIN ag.users agu WHERE ((au.login = :login AND au.workspaceId = :workspaceId) OR (agu.login = :login AND agu.workspaceId = :workspaceId))")
})
public class Task implements Serializable, Cloneable {
    @Id
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumns({
            @JoinColumn(name = "ACTIVITY_STEP", referencedColumnName = "STEP"),
            @JoinColumn(name = "WORKFLOW_ID", referencedColumnName = "WORKFLOW_ID")
    })
    private Activity activity;

    @Id
    private int num;

    private String title;

    @Lob
    private String instructions;

    private int duration = 1;

    @javax.persistence.Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date startDate;

    /**
     * The user who effectively performed the task, which hence is in
     * the {@link Status#APPROVED} or {@link Status#REJECTED} state.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumns({
            @JoinColumn(name = "WORKER_LOGIN", referencedColumnName = "LOGIN"),
            @JoinColumn(name = "WORKER_WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID")
    })
    private User worker;

    @ManyToMany
    @JoinTable(name="TASK_USER",
    inverseJoinColumns={
            @JoinColumn(name="USER_LOGIN", referencedColumnName="LOGIN"),
            @JoinColumn(name="USER_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
    },
    joinColumns={
            @JoinColumn(name="TASK_NUM", referencedColumnName="NUM"),
            @JoinColumn(name="ACTIVITY_STEP", referencedColumnName="ACTIVITY_STEP"),
            @JoinColumn(name="WORKFLOW_ID", referencedColumnName="WORKFLOW_ID")
    })
    private Set<User> assignedUsers=new HashSet<>();


    @ManyToMany
    @JoinTable(name="TASK_USERGROUP",
    inverseJoinColumns={
            @JoinColumn(name="USERGROUP_ID", referencedColumnName="ID"),
            @JoinColumn(name="USERGROUP_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
    },
    joinColumns={
            @JoinColumn(name="TASK_NUM", referencedColumnName="NUM"),
            @JoinColumn(name="ACTIVITY_STEP", referencedColumnName="ACTIVITY_STEP"),
            @JoinColumn(name="WORKFLOW_ID", referencedColumnName="WORKFLOW_ID")
    })
    private Set<UserGroup> assignedGroups=new HashSet<>();

    private int targetIteration;
    private String closureComment;

    @javax.persistence.Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date closureDate;

    @Lob
    private String signature;

    private Status status = Status.NOT_STARTED;

    /**
     * Enumeration for the possible status of a task.
     *
     * {@link Status#NOT_STARTED} indicates that the task is not started yet.
     *
     * {@link Status#IN_PROGRESS} indicates that the task has been started and is currently in progress.
     *
     * {@link Status#APPROVED} indicates that the task has been closed and its output status is approved.
     *
     * {@link Status#REJECTED} indicates that the task has been closed and its output status is rejected.
     *
     * {@link Status#NOT_TO_BE_DONE} indicates that the task has been marked as not to be done which means
     * that the task will never be started. This status is notably used when a workflow has been relaunched
     * to identify the tasks which are considered definitely done and thus not to be done again.
     */
    public enum Status {
        NOT_STARTED, IN_PROGRESS, APPROVED, REJECTED, NOT_TO_BE_DONE
    }


    public Task() {
    }
    public Task(int pNum, String pTitle, String pInstructions, Collection<User> assignedU, Collection<UserGroup> assignedG) {
        num = pNum;
        title = pTitle;
        if(assignedU!=null)assignedUsers.addAll(assignedU);
        if(assignedG!=null)assignedGroups.addAll(assignedG);
        instructions = pInstructions;
    }

    public TaskKey getKey() {
        return new TaskKey(new ActivityKey(getWorkflowId(), getActivityStep()), num);
    }

    public int getNum() {
        return num;
    }
    public void setNum(int num) {
        this.num = num;
    }

    public int getWorkflowId() {
        return activity == null ? 0 : activity.getWorkflowId();
    }

    @XmlTransient
    public Activity getActivity() {
        return activity;
    }
    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public int getActivityStep() {
        return activity == null ? 0 : activity.getStep();
    }

    public Task.Status getStatus() {
        return status;
    }
    public void setStatus(Task.Status status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getInstructions() {
        return instructions;
    }
    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public User getWorker() {
        return worker;
    }
    public void setWorker(User worker) {
        this.worker = worker;
    }

    public Date getStartDate() {
        return (startDate!=null) ? (Date) startDate.clone(): null;
    }
    public void setStartDate(Date startDate) {
        this.startDate = (startDate!=null) ? (Date) startDate.clone(): null;
    }

    public int getDuration() {
        return duration;
    }
    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Date getClosureDate() {
        return (closureDate!=null) ? (Date) closureDate.clone() : null;
    }
    public void setClosureDate(Date closureDate) {
        this.closureDate = (closureDate!=null) ? (Date) closureDate.clone() : null;
    }

    public String getClosureComment() {
        return closureComment;
    }
    public void setClosureComment(String closureComment) {
        this.closureComment = closureComment;
    }

    public int getTargetIteration() {
        return targetIteration;
    }
    public void setTargetIteration(int targetIteration) {
        this.targetIteration = targetIteration;
    }

    public String getSignature() {
        return signature;
    }
    public void setSignature(String signature) {
        this.signature = signature;
    }

    public void reject(User pWorker, String pComment, int pTargetIteration) {
        reject(pWorker, pComment, pTargetIteration, null);
    }
    public void reject(User pWorker, String pComment, int pTargetIteration, String pSignature) {
        submit(pWorker, pComment,pTargetIteration,pSignature);
        status = Status.REJECTED;
    }

    public void approve(User pWorker, String pComment, int pTargetIteration) {
        approve(pWorker, pComment, pTargetIteration, null);
    }
    public void approve(User pWorker, String pComment, int pTargetIteration, String pSignature) {
        submit(pWorker, pComment,pTargetIteration,pSignature);
        status = Status.APPROVED;
    }

    private void submit(User pWorker, String pComment, int pTargetIteration, String pSignature){
        worker=pWorker;
        closureDate = new Date();
        closureComment = pComment;
        signature = pSignature;
        targetIteration = pTargetIteration;
    }

    public void start() {
        if (isNotStarted()) {
            startDate = new Date();
            status = Status.IN_PROGRESS;
        }
    }
    public void stop() {
        if (isInProgress()) {
            status = Status.NOT_STARTED;
        }
    }
    public void reset(Task.Status status) {
        setStatus(status);
        setSignature(null);
        setClosureComment(null);
        setClosureDate(null);
        setStartDate(null);
    }

    public boolean hasPotentialWorker(){
        return !assignedUsers.isEmpty() || !assignedGroups.isEmpty();
    }

    public boolean isPotentialWorker(User user){
        return assignedUsers.contains(user) || assignedGroups.stream().anyMatch(g->g.isMember(user));
    }

    public Set<User> getAssignedUsers() {
        return assignedUsers;
    }

    public void setAssignedUsers(Set<User> assignedUsers) {
        this.assignedUsers = assignedUsers;
    }

    public Set<UserGroup> getAssignedGroups() {
        return assignedGroups;
    }

    public void setAssignedGroups(Set<UserGroup> assignedGroups) {
        this.assignedGroups = assignedGroups;
    }

    public boolean isNotStarted() {
        //because of bug #6277781
        return status.ordinal() == Status.NOT_STARTED.ordinal();
    }
    public boolean isRejected() {
        //because of bug #6277781
        return status.ordinal() == Status.REJECTED.ordinal();
    }
    public boolean isApproved() {
        //because of bug #6277781
        return status.ordinal() == Status.APPROVED.ordinal();
    }
    public boolean isInProgress() {
        //because of bug #6277781
        return status.ordinal() == Status.IN_PROGRESS.ordinal();
    }
    public boolean isNotToBeDone() {
        //because of bug #6277781
        return status.ordinal() == Status.NOT_TO_BE_DONE.ordinal();
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = 31 * hash + (activity == null ? 0 : activity.hashCode());
        hash = 31 * hash + num;
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }
        if (o == null || getClass() != o.getClass()){
            return false;
        }

        Task task = (Task) o;

        return num == task.num && (activity != null ? activity.equals(task.activity) : task.activity == null);
    }

    @Override
    public String toString() {
        return title;
    }

    /**
     * perform a deep clone operation
     */
    @Override
    public Task clone() {
        try {
            Task clone = (Task) super.clone();

            if (startDate != null) {
                clone.startDate = (Date) startDate.clone();
            }
            if (closureDate != null) {
                clone.closureDate = (Date) closureDate.clone();
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }
}
