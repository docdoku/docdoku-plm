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
public class MasterDocumentKey implements Serializable, Comparable<MasterDocumentKey>, Cloneable {

    private String workspaceId;
    private String id;
    private String version;


    public MasterDocumentKey() {
    }
    
    public MasterDocumentKey(String pWorkspaceId, String pId, String pVersion) {
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
        if (!(pObj instanceof MasterDocumentKey))
            return false;
        MasterDocumentKey key = (MasterDocumentKey) pObj;
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

    public int compareTo(MasterDocumentKey pMDocKey) {
        int wksComp = workspaceId.compareTo(pMDocKey.workspaceId);
        if (wksComp != 0)
            return wksComp;
        int idComp = id.compareTo(pMDocKey.id);
        if (idComp != 0)
            return idComp;
        else
            return version.compareTo(pMDocKey.version);
    }
    
    @Override
    public MasterDocumentKey clone() {
        MasterDocumentKey clone = null;
        try {
            clone = (MasterDocumentKey) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
        return clone;
    }
}