/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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

package com.docdoku.core.document;

import java.io.Serializable;

/**
 *
 * @author Florent Garin
 */
public class DocumentIterationKey implements Serializable {
    
    private String workspaceId;
    private String documentMasterId;
    private String documentMasterVersion;
    private int iteration;
    
    public DocumentIterationKey() {
    }
    
    public DocumentIterationKey(String pWorkspaceId, String pDocumentMasterId, String pDocumentMasterVersion, int pIteration) {
        workspaceId=pWorkspaceId;
        documentMasterId=pDocumentMasterId;
        documentMasterVersion=pDocumentMasterVersion;
        iteration=pIteration;
    }
    
    @Override
    public int hashCode() {
        int hash = 1;
	hash = 31 * hash + workspaceId.hashCode();
	hash = 31 * hash + documentMasterId.hashCode();
        hash = 31 * hash + documentMasterVersion.hashCode();
        hash = 31 * hash + iteration;
	return hash;
    }
    
    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof DocumentIterationKey))
            return false;
        DocumentIterationKey key = (DocumentIterationKey) pObj;
        return ((key.documentMasterId.equals(documentMasterId)) && (key.workspaceId.equals(workspaceId))  && (key.documentMasterVersion.equals(documentMasterVersion)) && (key.iteration==iteration));
    }
    
    @Override
    public String toString() {
        return workspaceId + "-" + documentMasterId + "-" + documentMasterVersion + "-" + iteration;
    }
    
    public String getWorkspaceId() {
        return workspaceId;
    }
    
    public void setWorkspaceId(String pWorkspaceId) {
        workspaceId = pWorkspaceId;
    }
    
    public String getDocumentMasterId() {
        return documentMasterId;
    }
    
    public void setDocumentMasterId(String pDocumentMasterId) {
        documentMasterId = pDocumentMasterId;
    }

    public String getDocumentMasterVersion() {
        return documentMasterVersion;
    }

    public void setDocumentMasterVersion(String pDocumentMasterVersion) {
        this.documentMasterVersion = pDocumentMasterVersion;
    }
    
    
    public int getIteration(){
        return iteration;
    }
    
    public void setIteration(int pIteration){
        iteration=pIteration;
    }
}
