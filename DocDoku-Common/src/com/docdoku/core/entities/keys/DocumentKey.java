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
public class DocumentKey implements Serializable {
    
    private String workspaceId;
    private String masterDocumentId;
    private String masterDocumentVersion;
    private int iteration;
    
    public DocumentKey() {
    }
    
    public DocumentKey(String pWorkspaceId, String pMasterDocumentId, String pMasterDocumentVersion, int pIteration) {
        workspaceId=pWorkspaceId;
        masterDocumentId=pMasterDocumentId;
        masterDocumentVersion=pMasterDocumentVersion;
        iteration=pIteration;
    }
    
    @Override
    public int hashCode() {
        int hash = 1;
	hash = 31 * hash + workspaceId.hashCode();
	hash = 31 * hash + masterDocumentId.hashCode();
        hash = 31 * hash + masterDocumentVersion.hashCode();
        hash = 31 * hash + iteration;
	return hash;
    }
    
    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof DocumentKey))
            return false;
        DocumentKey key = (DocumentKey) pObj;
        return ((key.masterDocumentId.equals(masterDocumentId)) && (key.workspaceId.equals(workspaceId))  && (key.masterDocumentVersion.equals(masterDocumentVersion)) && (key.iteration==iteration));
    }
    
    @Override
    public String toString() {
        return workspaceId + "-" + masterDocumentId + "-" + masterDocumentVersion + "-" + iteration;
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
    
    public void setMasterDocumentId(String pMasterDocumentId) {
        masterDocumentId = pMasterDocumentId;
    }

    public String getMasterDocumentVersion() {
        return masterDocumentVersion;
    }

    public void setMasterDocumentVersion(String pMasterDocumentVersion) {
        this.masterDocumentVersion = pMasterDocumentVersion;
    }
    
    
    public int getIteration(){
        return iteration;
    }
    
    public void setIteration(int pIteration){
        iteration=pIteration;
    }
}
