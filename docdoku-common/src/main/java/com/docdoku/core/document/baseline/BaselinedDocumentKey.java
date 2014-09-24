/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2014 DocDoku SARL
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

package com.docdoku.core.document.baseline;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Identity class of <a href="BaselinedDocument.html">BaselinedDocument</a> objects defined as an embeddable object in order
 * to be used inside the baselined documents map in the <a href="DocumentCollection.html">DocumentCollection</a> class.
 *
 * @author Taylor LABEJOF
 * @version 2.0, 25/08/14
 * @since   V2.0
 */
@Embeddable
public class BaselinedDocumentKey implements Serializable{
    @Column(name = "DOCUMENTSCOLLECTION_ID", nullable = false, insertable = false, updatable = false)
    private int documentCollectionId;
    @Column(name = "TARGET_WORKSPACE_ID", length=100, nullable = false, insertable = false, updatable = false)
    private String targetDocumentWorkspaceId="";
    @Column(name = "TARGET_DOCUMENTMASTER_ID", length=100, nullable = false, insertable = false, updatable = false)
    private String targetDocumentId="";

    public BaselinedDocumentKey(){
    }

    public BaselinedDocumentKey(int documentsCollectionId, String workspaceId, String documentMasterId) {
        this.documentCollectionId = documentsCollectionId;
        this.targetDocumentWorkspaceId = workspaceId;
        this.targetDocumentId = documentMasterId;
    }

    public int getDocumentCollectionId() {
        return documentCollectionId;
    }
    public void setDocumentCollectionId(int documentCollectionId) {
        this.documentCollectionId = documentCollectionId;
    }

    public String getTargetDocumentWorkspaceId() {
        return targetDocumentWorkspaceId;
    }
    public void setTargetDocumentWorkspaceId(String targetDocumentWorkspaceId) {
        this.targetDocumentWorkspaceId = targetDocumentWorkspaceId;
    }

    public String getTargetDocumentId() {
        return targetDocumentId;
    }
    public void setTargetDocumentId(String targetDocumentId) {
        this.targetDocumentId = targetDocumentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BaselinedDocumentKey that = (BaselinedDocumentKey) o;

        return documentCollectionId == that.documentCollectionId
                && targetDocumentId.equals(that.targetDocumentId)
                && targetDocumentWorkspaceId.equals(that.targetDocumentWorkspaceId);

    }

    @Override
    public int hashCode() {
        int result = documentCollectionId;
        result = 31 * result + targetDocumentWorkspaceId.hashCode();
        result = 31 * result + targetDocumentId.hashCode();
        return result;
    }
}