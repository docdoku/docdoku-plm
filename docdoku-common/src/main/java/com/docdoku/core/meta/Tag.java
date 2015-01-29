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

import com.docdoku.core.common.Workspace;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * A tag is just a label pinned on an entity.
 * 
 * @author Florent Garin
 * @version 1.0, 02/06/08
 * @since   V1.0
 */
@Table(name="TAG")
@javax.persistence.IdClass(TagKey.class)
@javax.persistence.Entity
public class Tag implements Serializable {
    
    @javax.persistence.Column(name = "WORKSPACE_ID", length=100, nullable = false, insertable = false, updatable = false)
    @javax.persistence.Id
    private String workspaceId="";

    @Column(length=100)
    @javax.persistence.Id
    private String label="";
    
    @javax.persistence.ManyToOne(optional=false, fetch=FetchType.EAGER)
    private Workspace workspace;

    public Tag() {
    }
    
    public Tag(Workspace pWorkspace, String pLabel) {
        setWorkspace(pWorkspace);
        label=pLabel;
    }
    
    public void setWorkspace(Workspace pWorkspace){
        workspace=pWorkspace;
        workspaceId=workspace.getId();
    }

    public String getLabel() {
        return label;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Workspace getWorkspace() {
        return workspace;
    }
   
    
    @Override
    public int hashCode() {
        int hash = 1;
        hash = 31 * hash + workspaceId.hashCode();
        hash = 31 * hash + label.hashCode();
        return hash;
    }
    
    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof Tag)) {
            return false;
        }
        Tag tag = (Tag) pObj;
        
        return tag.workspaceId.equals(workspaceId)
                && tag.label.equals(label);
    }
    
    @Override
    public String toString() {
        return label;
    }
}
