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

package com.docdoku.core.document;

import com.docdoku.core.common.Workspace;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.FetchType;

/**
 * A tag is just a label pinned on a document.  
 * 
 * @author Florent GARIN
 * @version 1.0, 02/06/08
 * @since   V1.0
 */
@javax.persistence.IdClass(com.docdoku.core.document.TagKey.class)
@javax.persistence.Entity
public class Tag implements Serializable {
    
    @javax.persistence.Column(name = "WORKSPACE_ID", length=50, nullable = false, insertable = false, updatable = false)
    @javax.persistence.Id
    private String workspaceId="";

    @Column(length=50)
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
        if (!(pObj instanceof Tag))
            return false;
        Tag tag = (Tag) pObj;
        
        return tag.workspaceId.equals(workspaceId)
        && tag.label.equals(label);
    }
    
    @Override
    public String toString() {
        return label;
    }
}
