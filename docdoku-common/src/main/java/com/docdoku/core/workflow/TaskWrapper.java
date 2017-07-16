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

import java.io.Serializable;

/**
 * This class is used to carry contextual information along the task itself.
 *
 * Instances of this class are not persisted.
 *
 * @author Morgan Guimard
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
