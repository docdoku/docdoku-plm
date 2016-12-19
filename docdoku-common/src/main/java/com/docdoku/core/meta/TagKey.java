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

package com.docdoku.core.meta;

import java.io.Serializable;

/**
 *
 * @author Florent Garin
 */
public class TagKey implements Serializable {
    
    private String workspace;
    private String label;
    
    public TagKey() {
    }
    
    public TagKey(String pWorkspaceId, String pLabel) {
        workspace=pWorkspaceId;
        label=pLabel;
    }
    
    @Override
    public int hashCode() {
        int hash = 1;
        hash = 31 * hash + workspace.hashCode();
        hash = 31 * hash + label.hashCode();
        return hash;
    }
    
    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof TagKey)) {
            return false;
        }
        TagKey key = (TagKey) pObj;
        return key.workspace.equals(workspace) && key.label.equals(label);
    }
    
    @Override
    public String toString() {
        return label;
    }
    
    public String getWorkspace() {
        return workspace;
    }
    
    public void setWorkspace(String pWorkspaceId) {
        workspace = pWorkspaceId;
    }
    
    
    public String getLabel(){
        return label;
    }
    
    public void setLabel(String pLabel){
        label=pLabel;
    }
}
