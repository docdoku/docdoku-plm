/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
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
public class DocumentToDocumentLinkKey implements Serializable{
    
   
    private int fromDocumentIteration;
    private String fromDocumentDocumentMasterId;
    private String fromDocumentDocumentMasterVersion;
    private String fromDocumentWorkspaceId;
    
    private int toDocumentIteration;
    private String toDocumentDocumentMasterId;
    private String toDocumentDocumentMasterVersion;
    private String toDocumentWorkspaceId;
    
    public DocumentToDocumentLinkKey() {
    }
    
    public DocumentToDocumentLinkKey(String pFromDocumentWorkspaceId, String pFromDocumentDocumentMasterId, String pFromDocumentDocumentMasterVersion, int pFromDocumentIteration, String pToDocumentWorkspaceId, String pToDocumentDocumentMasterId, String pToDocumentDocumentMasterVersion, int pToDocumentIteration) {
        fromDocumentWorkspaceId=pFromDocumentWorkspaceId;
        fromDocumentDocumentMasterId=pFromDocumentDocumentMasterId;
        fromDocumentDocumentMasterVersion=pFromDocumentDocumentMasterVersion;
        fromDocumentIteration=pFromDocumentIteration;
        
        toDocumentWorkspaceId=pToDocumentWorkspaceId;
        toDocumentDocumentMasterId=pToDocumentDocumentMasterId;
        toDocumentDocumentMasterVersion=pToDocumentDocumentMasterVersion;
        toDocumentIteration=pToDocumentIteration;
    }

    public int getFromDocumentIteration() {
        return fromDocumentIteration;
    }

    public String getFromDocumentDocumentMasterId() {
        return fromDocumentDocumentMasterId;
    }

    public String getFromDocumentDocumentMasterVersion() {
        return fromDocumentDocumentMasterVersion;
    }

    public String getFromDocumentWorkspaceId() {
        return fromDocumentWorkspaceId;
    }

    public int getToDocumentIteration() {
        return toDocumentIteration;
    }

    public String getToDocumentDocumentMasterId() {
        return toDocumentDocumentMasterId;
    }

    public String getToDocumentDocumentMasterVersion() {
        return toDocumentDocumentMasterVersion;
    }

    public String getToDocumentWorkspaceId() {
        return toDocumentWorkspaceId;
    }

    public void setFromDocumentIteration(int fromDocumentIteration) {
        this.fromDocumentIteration = fromDocumentIteration;
    }

    public void setFromDocumentDocumentMasterId(String fromDocumentDocumentMasterId) {
        this.fromDocumentDocumentMasterId = fromDocumentDocumentMasterId;
    }

    public void setFromDocumentDocumentMasterVersion(String fromDocumentDocumentMasterVersion) {
        this.fromDocumentDocumentMasterVersion = fromDocumentDocumentMasterVersion;
    }

    public void setFromDocumentWorkspaceId(String fromDocumentWorkspaceId) {
        this.fromDocumentWorkspaceId = fromDocumentWorkspaceId;
    }

    public void setToDocumentIteration(int toDocumentIteration) {
        this.toDocumentIteration = toDocumentIteration;
    }

    public void setToDocumentDocumentMasterId(String toDocumentDocumentMasterId) {
        this.toDocumentDocumentMasterId = toDocumentDocumentMasterId;
    }

    public void setToDocumentDocumentMasterVersion(String toDocumentDocumentMasterVersion) {
        this.toDocumentDocumentMasterVersion = toDocumentDocumentMasterVersion;
    }

    public void setToDocumentWorkspaceId(String toDocumentWorkspaceId) {
        this.toDocumentWorkspaceId = toDocumentWorkspaceId;
    }
    
    
    
    
    @Override
    public String toString() {
        return fromDocumentWorkspaceId + "-" + fromDocumentDocumentMasterId  + "-" + fromDocumentDocumentMasterVersion + "-" + fromDocumentIteration + "/" + toDocumentWorkspaceId + "-" + toDocumentDocumentMasterId  + "-" + toDocumentDocumentMasterVersion + "-" + toDocumentIteration;
    }

    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof DocumentToDocumentLinkKey))
            return false;
        DocumentToDocumentLinkKey key = (DocumentToDocumentLinkKey) pObj;
        return ((key.fromDocumentWorkspaceId.equals(fromDocumentWorkspaceId)) && (key.fromDocumentDocumentMasterId.equals(fromDocumentDocumentMasterId)) && (key.fromDocumentDocumentMasterVersion.equals(fromDocumentDocumentMasterVersion)) && (key.fromDocumentIteration==fromDocumentIteration) && (key.toDocumentWorkspaceId.equals(toDocumentWorkspaceId)) && (key.toDocumentDocumentMasterId.equals(toDocumentDocumentMasterId)) && (key.toDocumentDocumentMasterVersion.equals(toDocumentDocumentMasterVersion)) && (key.toDocumentIteration==toDocumentIteration));
    }

    @Override
    public int hashCode() {
        int hash = 1;
	hash = 31 * hash + fromDocumentWorkspaceId.hashCode();
	hash = 31 * hash + fromDocumentDocumentMasterId.hashCode();
        hash = 31 * hash + fromDocumentDocumentMasterVersion.hashCode();
        hash = 31 * hash + fromDocumentIteration;
        hash = 31 * hash + toDocumentWorkspaceId.hashCode();
	hash = 31 * hash + toDocumentDocumentMasterId.hashCode();
        hash = 31 * hash + toDocumentDocumentMasterVersion.hashCode();
        hash = 31 * hash + toDocumentIteration;
	return hash;
    }
    
}
