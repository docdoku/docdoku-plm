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

package com.docdoku.core.workflow;

import java.io.Serializable;

/**
 *
 * @author Florent GARIN
 */
public class TaskKey implements Serializable {
    

    private int workflowId;
    private int activityStep;
    private int num;
    
    public TaskKey() {
    }
    
    public TaskKey(int pActivityWorkflowId, int pActivityStep, int pNum) {
        workflowId=pActivityWorkflowId;
        activityStep=pActivityStep;
        num=pNum;
    }
    
    @Override
    public int hashCode() {
        int hash = 1;
	hash = 31 * hash + workflowId;
	hash = 31 * hash + activityStep;
        hash = 31 * hash + num;
	return hash;
    }
    
    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof TaskKey))
            return false;
        TaskKey key = (TaskKey) pObj;
        return ((key.workflowId==workflowId) && (key.activityStep==activityStep) && (key.num==num));
    }
    
    @Override
    public String toString() {
        return workflowId + "-" + activityStep + "-" + num;
    }

    public int getActivityStep() {
        return activityStep;
    }

    public int getActivityWorkflowId() {
        return workflowId;
    }

    public int getNum() {
        return num;
    }

    public void setActivityStep(int activityStep) {
        this.activityStep = activityStep;
    }

    public void setActivityWorkflowId(int activityWorkflowId) {
        this.workflowId = activityWorkflowId;
    }

    public void setNum(int num) {
        this.num = num;
    }
}
