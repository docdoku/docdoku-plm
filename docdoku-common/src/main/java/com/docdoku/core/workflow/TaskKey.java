/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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
 * @author Florent Garin
 */
public class TaskKey implements Serializable {
    

    private ActivityKey activity;
    private int num;
    
    public TaskKey() {
    }
    
    public TaskKey(ActivityKey pActivityKey, int pNum) {
        activity=pActivityKey;
        num=pNum;
    }
    
    @Override
    public int hashCode() {
        int hash = 1;
	hash = 31 * hash + activity.hashCode();
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
        return ((key.activity.equals(activity)) && (key.num==num));
    }
    
    @Override
    public String toString() {
        return activity.toString() + "-" + num;
    }

    public ActivityKey getActivity() {
        return activity;
    }

    public void setActivity(ActivityKey activity) {
        this.activity = activity;
    }

    public int getNum() {
        return num;
    }


    public void setNum(int num) {
        this.num = num;
    }
}
