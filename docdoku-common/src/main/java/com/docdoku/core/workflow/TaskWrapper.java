package com.docdoku.core.workflow;

import java.io.Serializable;

/**
 * @author : Morgan Guimard
 */

public class TaskWrapper implements Serializable{

    private Task task;

    private String workspaceId;
    private String holderType;
    private String holderReference;
    private String holderVersion;


    public TaskWrapper(Task task, String workspaceId) {
        this.task = task;
        this.workspaceId = workspaceId;
    }

    public TaskWrapper(Task task, String workspaceId, String holderType, String holderReference, String holderVersion) {
        this(task,workspaceId);
        this.holderType = holderType;
        this.holderReference = holderReference;
        this.holderVersion = holderVersion;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public String getHolderType() {
        return holderType;
    }

    public void setHolderType(String holderType) {
        this.holderType = holderType;
    }

    public String getHolderReference() {
        return holderReference;
    }

    public void setHolderReference(String holderReference) {
        this.holderReference = holderReference;
    }

    public String getHolderVersion() {
        return holderVersion;
    }

    public void setHolderVersion(String holderVersion) {
        this.holderVersion = holderVersion;
    }
}
