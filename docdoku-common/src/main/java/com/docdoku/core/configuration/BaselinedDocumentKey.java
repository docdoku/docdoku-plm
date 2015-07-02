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

package com.docdoku.core.configuration;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Identity class of {@link BaselinedDocument} objects defined as an embeddable object in order
 * to be used inside the baselined documents map in the {@link DocumentCollection} class.
 *
 * @author Taylor LABEJOF
 * @version 2.0, 25/08/14
 * @since V2.0
 */
@Embeddable
public class BaselinedDocumentKey implements Serializable {
    @Column(name = "DOCUMENTCOLLECTION_ID", nullable = false, insertable = false, updatable = false)
    private int documentCollectionId;
    @Column(name = "TARGET_WORKSPACE_ID", length = 100, nullable = false, insertable = false, updatable = false)
    private String targetDocumentWorkspaceId = "";
    @Column(name = "TARGET_DOCUMENTMASTER_ID", length = 100, nullable = false, insertable = false, updatable = false)
    private String targetDocumentId = "";
    @Column(name = "TARGET_DOCREVISION_VERSION", length = 10, nullable = false, insertable = false, updatable = false)
    private String targetDocumentVersion = "";

    public BaselinedDocumentKey() {
    }

    public BaselinedDocumentKey(int documentCollectionId, String workspaceId, String documentMasterId, String targetDocumentVersion) {
        this.documentCollectionId = documentCollectionId;
        this.targetDocumentWorkspaceId = workspaceId;
        this.targetDocumentId = documentMasterId;
        this.targetDocumentVersion = targetDocumentVersion;
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

    public String getTargetDocumentVersion() {
        return targetDocumentVersion;
    }

    public void setTargetDocumentVersion(String targetDocumentVersion) {
        this.targetDocumentVersion = targetDocumentVersion;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()){
            return false;
        }

        BaselinedDocumentKey that = (BaselinedDocumentKey) o;

        if (documentCollectionId != that.documentCollectionId){
            return false;
        }
        if (targetDocumentId != null ? !targetDocumentId.equals(that.targetDocumentId) : that.targetDocumentId != null) {
            return false;
        }
        if (targetDocumentVersion != null ? !targetDocumentVersion.equals(that.targetDocumentVersion) : that.targetDocumentVersion != null) {
            return false;
        }
        if (targetDocumentWorkspaceId != null ? !targetDocumentWorkspaceId.equals(that.targetDocumentWorkspaceId) : that.targetDocumentWorkspaceId != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = documentCollectionId;
        result = 31 * result + (targetDocumentWorkspaceId != null ? targetDocumentWorkspaceId.hashCode() : 0);
        result = 31 * result + (targetDocumentId != null ? targetDocumentId.hashCode() : 0);
        result = 31 * result + (targetDocumentVersion != null ? targetDocumentVersion.hashCode() : 0);
        return result;
    }
}