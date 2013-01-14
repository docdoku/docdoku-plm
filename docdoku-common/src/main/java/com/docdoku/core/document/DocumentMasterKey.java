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
public class DocumentMasterKey implements Serializable, Comparable<DocumentMasterKey>, Cloneable {

    private String workspaceId;
    private String id;
    private String version;


    public DocumentMasterKey() {
    }
    
    public DocumentMasterKey(String pWorkspaceId, String pId, String pVersion) {
        workspaceId=pWorkspaceId;
        id=pId;
        version = pVersion;
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
    
    public String getVersion() {
        return version;
    }

    public void setVersion(String pVersion){
        version=pVersion;
    }
    
    @Override
    public String toString() {
        return workspaceId + "-" + id + "-" + version;
    }

    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof DocumentMasterKey))
            return false;
        DocumentMasterKey key = (DocumentMasterKey) pObj;
        return ((key.id.equals(id)) && (key.workspaceId.equals(workspaceId)) && (key.version.equals(version)));
    }

    @Override
    public int hashCode() {
        int hash = 1;
	hash = 31 * hash + workspaceId.hashCode();
	hash = 31 * hash + id.hashCode();
        hash = 31 * hash + version.hashCode();
	return hash;
    }

    public int compareTo(DocumentMasterKey pDocMKey) {
        int wksComp = workspaceId.compareTo(pDocMKey.workspaceId);
        if (wksComp != 0)
            return wksComp;
        int idComp = id.compareTo(pDocMKey.id);
        if (idComp != 0)
            return idComp;
        else
            return version.compareTo(pDocMKey.version);
    }
    
    @Override
    public DocumentMasterKey clone() {
        DocumentMasterKey clone = null;
        try {
            clone = (DocumentMasterKey) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
        return clone;
    }
}