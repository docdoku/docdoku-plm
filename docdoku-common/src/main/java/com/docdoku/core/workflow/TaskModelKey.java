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

import java.io.Serializable;

/**
 *
 * @author Florent Garin
 */
public class TaskModelKey implements Serializable {
    

    private String workspaceId;
    private String workflowModelId;
    private int activityModelId;
    private int num;
    
    public TaskModelKey() {
    }
    
    public TaskModelKey(String pWorkspaceId, String pWorkflowModelId, int pActivityModelId, int pNum) {
        workspaceId=pWorkspaceId;
        workflowModelId=pWorkflowModelId;
        activityModelId=pActivityModelId;
        num=pNum;
    }
    
    @Override
    public int hashCode() {
        int hash = 1;
        hash = 31 * hash + workspaceId.hashCode();
        hash = 31 * hash + workflowModelId.hashCode();
        hash = 31 * hash + activityModelId;
        hash = 31 * hash + num;
        return hash;
    }
    
    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof TaskModelKey)) {
            return false;
        }
        TaskModelKey key = (TaskModelKey) pObj;
        return key.workspaceId.equals(workspaceId) &&
               key.workflowModelId.equals(workflowModelId) &&
               key.activityModelId==activityModelId &&
               key.num==num;
    }
    
    @Override
    public String toString() {
        return workspaceId + "-" + workflowModelId + "-" + activityModelId + "-" + num;
    }

    public String getWorkflowModelId() {
        return workflowModelId;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkflowModelId(String pWorkflowModelId) {
        this.workflowModelId = pWorkflowModelId;
    }

    public void setWorkspaceId(String pWorkspaceId) {
        this.workspaceId = pWorkspaceId;
    }

    public int getNum() {
        return num;
    }
    public void setNum(int num) {
        this.num = num;
    }

    public int getActivityModelId() {
        return activityModelId;
    }

    public void setActivityModelId(int activityModelId) {
        this.activityModelId = activityModelId;
    }
}