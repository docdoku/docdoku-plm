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

package com.docdoku.core.document;


import java.io.Serializable;

/**
 * Identity class of {@link DocumentRevision} objects.
 *
 * @author Florent Garin
 */
public class DocumentRevisionKey implements Serializable, Comparable<DocumentRevisionKey>, Cloneable {

    private DocumentMasterKey documentMaster;
    private String version;


    public DocumentRevisionKey() {
    }

    public DocumentRevisionKey(String pWorkspaceId, String pId, String pVersion) {
        documentMaster = new DocumentMasterKey(pWorkspaceId, pId);
        version = pVersion;
    }

    public DocumentRevisionKey(DocumentMasterKey pDocumentMasterKey, String pVersion) {
        documentMaster = pDocumentMasterKey;
        version = pVersion;
    }


    public String getWorkspaceId() {
        return documentMaster.getWorkspace();
    }

    public String getDocumentMasterId() {
        return documentMaster.getId();
    }

    public DocumentMasterKey getDocumentMaster() {
        return documentMaster;
    }

    public void setDocumentMaster(DocumentMasterKey documentMaster) {
        this.documentMaster = documentMaster;
    }


    public String getVersion() {
        return version;
    }

    public void setVersion(String pVersion) {
        version = pVersion;
    }

    @Override
    public String toString() {
        return documentMaster + "-" + version;
    }

    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof DocumentRevisionKey)) {
            return false;
        }
        DocumentRevisionKey key = (DocumentRevisionKey) pObj;
        return key.documentMaster.equals(documentMaster) && key.version.equals(version);
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = 31 * hash + documentMaster.hashCode();
        hash = 31 * hash + version.hashCode();
        return hash;
    }

    public int compareTo(DocumentRevisionKey pKey) {
        int wksMaster = documentMaster.compareTo(pKey.documentMaster);
        if (wksMaster != 0) {
            return wksMaster;
        } else {
            return version.compareTo(pKey.version);
        }
    }

    @Override
    public DocumentRevisionKey clone() {
        DocumentRevisionKey clone;
        try {
            clone = (DocumentRevisionKey) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
        return clone;
    }

}