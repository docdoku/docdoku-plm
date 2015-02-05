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
public class ActivityKey implements Serializable {

    private int workflow;
    private int step;

    public ActivityKey() {
    }

    public ActivityKey(int pWorkflowId, int pStep) {
        workflow = pWorkflowId;
        step = pStep;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = 31 * hash + workflow;
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
        return key.workflow == workflow && key.step == step;
    }

    @Override
    public String toString() {
        return workflow + "-" + step;
    }

    public int getWorkflowId() {
        return workflow;
    }

    public void setWorkflowId(int workflowId) {
        this.workflow = workflowId;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int pStep) {
        step = pStep;
    }
}
