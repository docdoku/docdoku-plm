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

package com.docdoku.core.entities.keys;

import java.io.Serializable;

/**
 *
 * @author Florent GARIN
 */
public class InstanceAttributeTemplateKey implements Serializable {
    
    private String workspaceId;
    private String masterDocumentTemplateId;
    private String name;
    
    public InstanceAttributeTemplateKey() {
    }
    
    public InstanceAttributeTemplateKey(String pWorkspaceId, String pMasterDocumentTemplateId, String pName) {
        workspaceId=pWorkspaceId;
        masterDocumentTemplateId=pMasterDocumentTemplateId;
        name=pName;
    }
    
    @Override
    public int hashCode() {
        int hash = 1;
	hash = 31 * hash + workspaceId.hashCode();
	hash = 31 * hash + masterDocumentTemplateId.hashCode();
        hash = 31 * hash + name.hashCode();
	return hash;
    }
    
    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof InstanceAttributeTemplateKey))
            return false;
        InstanceAttributeTemplateKey key = (InstanceAttributeTemplateKey) pObj;
        return ((key.masterDocumentTemplateId.equals(masterDocumentTemplateId)) && (key.workspaceId.equals(workspaceId)) && (key.name.equals(name)));
    }
    
    @Override
    public String toString() {
        return workspaceId + "-" + masterDocumentTemplateId + "-" + name;
    }
    
    public String getWorkspaceId() {
        return workspaceId;
    }
    
    public void setWorkspaceId(String pWorkspaceId) {
        workspaceId = pWorkspaceId;
    }

    public String getMasterDocumentTemplateId() {
        return masterDocumentTemplateId;
    }

    public String getName() {
        return name;
    }

    public void setMasterDocumentTemplateId(String masterDocumentTemplateId) {
        this.masterDocumentTemplateId = masterDocumentTemplateId;
    }

    public void setName(String name) {
        this.name = name;
    }
    
}
