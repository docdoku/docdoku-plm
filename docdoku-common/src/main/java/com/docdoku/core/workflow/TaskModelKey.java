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

    private int activityModel;
    private int num;
    
    public TaskModelKey() {
    }
    
    public TaskModelKey(int pActivityModelId, int pNum) {
        activityModel =pActivityModelId;
        num=pNum;
    }
    
    @Override
    public int hashCode() {
        int hash = 1;
        hash = 31 * hash + activityModel;
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
        return key.activityModel == activityModel &&
               key.num==num;
    }
    
    @Override
    public String toString() {
        return activityModel + "-" + num;
    }

    public int getNum() {
        return num;
    }
    public void setNum(int num) {
        this.num = num;
    }

    public int getActivityModel() {
        return activityModel;
    }

    public void setActivityModel(int activityModel) {
        this.activityModel = activityModel;
    }
}