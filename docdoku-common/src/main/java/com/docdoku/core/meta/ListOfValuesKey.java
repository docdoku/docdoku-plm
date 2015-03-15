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

package com.docdoku.core.meta;

import java.io.Serializable;

/**
 *
 * @author Florent Garin
 */
public class ListOfValuesKey implements Serializable {

    private String workspaceId;
    private String name;

    public ListOfValuesKey() {
    }

    public ListOfValuesKey(String pWorkspaceId, String pName) {
        workspaceId=pWorkspaceId;
        name =pName;
    }
    
    @Override
    public int hashCode() {
        int hash = 1;
        hash = 31 * hash + workspaceId.hashCode();
        hash = 31 * hash + name.hashCode();
        return hash;
    }
    
    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof ListOfValuesKey)) {
            return false;
        }
        ListOfValuesKey key = (ListOfValuesKey) pObj;
        return key.workspaceId.equals(workspaceId) && key.name.equals(name);
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    public String getWorkspaceId() {
        return workspaceId;
    }
    
    public void setWorkspaceId(String pWorkspaceId) {
        workspaceId = pWorkspaceId;
    }
    
    
    public String getName(){
        return name;
    }
    
    public void setName(String pName){
        name =pName;
    }
}
