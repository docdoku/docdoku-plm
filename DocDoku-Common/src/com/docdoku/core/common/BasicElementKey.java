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

package com.docdoku.core.common;

import java.io.Serializable;

/**
 *
 * @author Florent GARIN
 */
public class BasicElementKey implements Serializable {
    
    private String workspaceId;
    private String id;
    
    public BasicElementKey() {
    }
    
    public BasicElementKey(String pWorkspaceId, String pId) {
        workspaceId=pWorkspaceId;
        id=pId;
    }
    
    @Override
    public int hashCode() {
        int hash = 1;
	hash = 31 * hash + workspaceId.hashCode();
	hash = 31 * hash + id.hashCode();
	return hash;
    }
    
    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof BasicElementKey))
            return false;
        BasicElementKey key = (BasicElementKey) pObj;
        return ((key.id.equals(id)) && (key.workspaceId.equals(workspaceId)));
    }
    
    @Override
    public String toString() {
        return workspaceId + "-" + id;
    }
    
    public String getWorkspaceId() {
        return workspaceId;
    }
    
    public void setWorkspaceId(String pWorkspaceId) {
        workspaceId = pWorkspaceId;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String pId) {
        id = pId;
    }
    
}
