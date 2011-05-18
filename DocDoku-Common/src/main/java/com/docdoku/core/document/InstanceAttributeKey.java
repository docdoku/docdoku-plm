/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011 DocDoku SARL
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

import java.io.Serializable;

/**
 *
 * @author Florent Garin
 */
public class InstanceAttributeKey implements Serializable {
    
    private String workspaceId;
    private String masterDocumentId;
    private String masterDocumentVersion;
    private int documentIteration;    
    private String name;
    
    public InstanceAttributeKey() {
    }
    
    public InstanceAttributeKey(String pWorkspaceId, String pMasterDocumentId, String pMasterDocumentVersion, int pDocumentIteration, String pName) {
        workspaceId=pWorkspaceId;
        masterDocumentId=pMasterDocumentId;
        masterDocumentVersion=pMasterDocumentVersion;
        documentIteration=pDocumentIteration;
        name=pName;
    }
    
    @Override
    public int hashCode() {
        int hash = 1;
	hash = 31 * hash + workspaceId.hashCode();
	hash = 31 * hash + masterDocumentId.hashCode();
        hash = 31 * hash + masterDocumentVersion.hashCode();
        hash = 31 * hash + documentIteration;
        hash = 31 * hash + name.hashCode();
	return hash;
    }
    
    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof InstanceAttributeKey))
            return false;
        InstanceAttributeKey key = (InstanceAttributeKey) pObj;
        return ((key.masterDocumentId.equals(masterDocumentId)) && (key.workspaceId.equals(workspaceId))  && (key.masterDocumentVersion.equals(masterDocumentVersion)) && (key.documentIteration==documentIteration) && (key.name.equals(name)));
    }
    
    @Override
    public String toString() {
        return workspaceId + "-" + masterDocumentId + "-" + masterDocumentVersion + "-" + documentIteration + "-" + name;
    }
    
    public String getWorkspaceId() {
        return workspaceId;
    }
    
    public void setWorkspaceId(String pWorkspaceId) {
        workspaceId = pWorkspaceId;
    }

    public String getMasterDocumentId() {
        return masterDocumentId;
    }

    public String getMasterDocumentVersion() {
        return masterDocumentVersion;
    }

    public void setMasterDocumentId(String masterDocumentId) {
        this.masterDocumentId = masterDocumentId;
    }

    public void setMasterDocumentVersion(String masterDocumentVersion) {
        this.masterDocumentVersion = masterDocumentVersion;
    }

    public int getDocumentIteration() {
        return documentIteration;
    }

    public void setDocumentIteration(int documentIteration) {
        this.documentIteration = documentIteration;
    }

    
    
    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }
    
}
