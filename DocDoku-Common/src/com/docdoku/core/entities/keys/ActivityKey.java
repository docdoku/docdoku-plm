/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009 DocDoku SARL
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
package com.docdoku.core.entities.keys;

import java.io.Serializable;

/**
 *
 * @author Florent GARIN
 */
public class ActivityKey implements Serializable {

    private int workflowId;
    private int step;

    public ActivityKey() {
    }

    public ActivityKey(int pWorkflowId, int pStep) {
        workflowId = pWorkflowId;
        step = pStep;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = 31 * hash + workflowId;
        hash = 31 * hash + step;
        return hash;
    }

    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof ActivityKey)) {
            return false;
        }
        ActivityKey key = (ActivityKey) pObj;
        return ((key.workflowId == workflowId) && (key.step == step));
    }

    @Override
    public String toString() {
        return workflowId + "-" + step;
    }

    public int getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(int workflowId) {
        this.workflowId = workflowId;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int pStep) {
        step = pStep;
    }
}
