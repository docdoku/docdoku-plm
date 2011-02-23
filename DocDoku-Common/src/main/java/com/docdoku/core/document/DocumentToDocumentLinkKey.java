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

import java.io.Serializable;

/**
 *
 * @author Florent GARIN
 */
public class DocumentToDocumentLinkKey implements Serializable{
    
   
    private int fromDocumentIteration;
    private String fromDocumentMasterDocumentId;
    private String fromDocumentMasterDocumentVersion;
    private String fromDocumentWorkspaceId;
    
    private int toDocumentIteration;
    private String toDocumentMasterDocumentId;
    private String toDocumentMasterDocumentVersion;
    private String toDocumentWorkspaceId;
    
    public DocumentToDocumentLinkKey() {
    }
    
    public DocumentToDocumentLinkKey(String pFromDocumentWorkspaceId, String pFromDocumentMasterDocumentId, String pFromDocumentMasterDocumentVersion, int pFromDocumentIteration, String pToDocumentWorkspaceId, String pToDocumentMasterDocumentId, String pToDocumentMasterDocumentVersion, int pToDocumentIteration) {
        fromDocumentWorkspaceId=pFromDocumentWorkspaceId;
        fromDocumentMasterDocumentId=pFromDocumentMasterDocumentId;
        fromDocumentMasterDocumentVersion=pFromDocumentMasterDocumentVersion;
        fromDocumentIteration=pFromDocumentIteration;
        
        toDocumentWorkspaceId=pToDocumentWorkspaceId;
        toDocumentMasterDocumentId=pToDocumentMasterDocumentId;
        toDocumentMasterDocumentVersion=pToDocumentMasterDocumentVersion;
        toDocumentIteration=pToDocumentIteration;
    }

    public int getFromDocumentIteration() {
        return fromDocumentIteration;
    }

    public String getFromDocumentMasterDocumentId() {
        return fromDocumentMasterDocumentId;
    }

    public String getFromDocumentMasterDocumentVersion() {
        return fromDocumentMasterDocumentVersion;
    }

    public String getFromDocumentWorkspaceId() {
        return fromDocumentWorkspaceId;
    }

    public int getToDocumentIteration() {
        return toDocumentIteration;
    }

    public String getToDocumentMasterDocumentId() {
        return toDocumentMasterDocumentId;
    }

    public String getToDocumentMasterDocumentVersion() {
        return toDocumentMasterDocumentVersion;
    }

    public String getToDocumentWorkspaceId() {
        return toDocumentWorkspaceId;
    }

    public void setFromDocumentIteration(int fromDocumentIteration) {
        this.fromDocumentIteration = fromDocumentIteration;
    }

    public void setFromDocumentMasterDocumentId(String fromDocumentMasterDocumentId) {
        this.fromDocumentMasterDocumentId = fromDocumentMasterDocumentId;
    }

    public void setFromDocumentMasterDocumentVersion(String fromDocumentMasterDocumentVersion) {
        this.fromDocumentMasterDocumentVersion = fromDocumentMasterDocumentVersion;
    }

    public void setFromDocumentWorkspaceId(String fromDocumentWorkspaceId) {
        this.fromDocumentWorkspaceId = fromDocumentWorkspaceId;
    }

    public void setToDocumentIteration(int toDocumentIteration) {
        this.toDocumentIteration = toDocumentIteration;
    }

    public void setToDocumentMasterDocumentId(String toDocumentMasterDocumentId) {
        this.toDocumentMasterDocumentId = toDocumentMasterDocumentId;
    }

    public void setToDocumentMasterDocumentVersion(String toDocumentMasterDocumentVersion) {
        this.toDocumentMasterDocumentVersion = toDocumentMasterDocumentVersion;
    }

    public void setToDocumentWorkspaceId(String toDocumentWorkspaceId) {
        this.toDocumentWorkspaceId = toDocumentWorkspaceId;
    }
    
    
    
    
    @Override
    public String toString() {
        return fromDocumentWorkspaceId + "-" + fromDocumentMasterDocumentId  + "-" + fromDocumentMasterDocumentVersion + "-" + fromDocumentIteration + "/" + toDocumentWorkspaceId + "-" + toDocumentMasterDocumentId  + "-" + toDocumentMasterDocumentVersion + "-" + toDocumentIteration;
    }

    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof DocumentToDocumentLinkKey))
            return false;
        DocumentToDocumentLinkKey key = (DocumentToDocumentLinkKey) pObj;
        return ((key.fromDocumentWorkspaceId.equals(fromDocumentWorkspaceId)) && (key.fromDocumentMasterDocumentId.equals(fromDocumentMasterDocumentId)) && (key.fromDocumentMasterDocumentVersion.equals(fromDocumentMasterDocumentVersion)) && (key.fromDocumentIteration==fromDocumentIteration) && (key.toDocumentWorkspaceId.equals(toDocumentWorkspaceId)) && (key.toDocumentMasterDocumentId.equals(toDocumentMasterDocumentId)) && (key.toDocumentMasterDocumentVersion.equals(toDocumentMasterDocumentVersion)) && (key.toDocumentIteration==toDocumentIteration));
    }

    @Override
    public int hashCode() {
        int hash = 1;
	hash = 31 * hash + fromDocumentWorkspaceId.hashCode();
	hash = 31 * hash + fromDocumentMasterDocumentId.hashCode();
        hash = 31 * hash + fromDocumentMasterDocumentVersion.hashCode();
        hash = 31 * hash + fromDocumentIteration;
        hash = 31 * hash + toDocumentWorkspaceId.hashCode();
	hash = 31 * hash + toDocumentMasterDocumentId.hashCode();
        hash = 31 * hash + toDocumentMasterDocumentVersion.hashCode();
        hash = 31 * hash + toDocumentIteration;
	return hash;
    }
    
}
