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

package com.docdoku.core.document.baseline;


import com.docdoku.core.document.DocumentIteration;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;

/**
 * Class link that gathers a document collection and a given document iteration.
 *
 * @author Taylor LABEJOF
 * @version 2.0, 25/08/14
 * @since   V2.0
 */

@Table(name="BASELINEDDOCUMENT")
@Entity
public class BaselinedDocument implements Serializable{
    @EmbeddedId
    private BaselinedDocumentKey baselinedDocumentKey;

    @ManyToOne(optional=false, fetch=FetchType.EAGER)
    @JoinColumn(name="DOCUMENTSCOLLECTION_ID", referencedColumnName="ID")
    private DocumentsCollection documentsCollection;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name="TARGET_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID"),
            @JoinColumn(name="TARGET_DOCUMENTMASTER_ID", referencedColumnName="DOCUMENTMASTER_ID"),
            @JoinColumn(name="TARGET_DOCUMENTREVISION_VERSION", referencedColumnName="DOCUMENTREVISION_VERSION"),
            @JoinColumn(name="TARGET_ITERATION", referencedColumnName="ITERATION")
    })
    private DocumentIteration targetDocument;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name="BASELINEDFOLDER_FOLDERSCOLLECTION_ID", referencedColumnName="FOLDERSCOLLECTION_ID"),
            @JoinColumn(name="BASELINEDFOLDER_COMPLETEPATH", referencedColumnName="COMPLETEPATH"),
    })
    private BaselinedFolder baselinedFolder;

    @Column(name = "TARGET_DOCUMENTREVISION_VERSION", length=10, nullable = false, insertable = false, updatable = false)
    private String targetDocumentVersion="";
    @Column(name = "TARGET_ITERATION", nullable = false, insertable = false, updatable = false)
    private int targetDocumentIteration;

    public BaselinedDocument(){
    }

    public BaselinedDocument(DocumentsCollection documentsCollection, DocumentIteration targetDocument) {
        this.documentsCollection = documentsCollection;
        this.targetDocument =targetDocument;
        this.baselinedDocumentKey = new BaselinedDocumentKey(documentsCollection.getId(),targetDocument.getWorkspaceId(), targetDocument.getDocumentMasterId());
        this.targetDocumentIteration = targetDocument.getIteration();
        this.targetDocumentVersion = targetDocument.getDocumentVersion();
    }

    public BaselinedDocumentKey getKey() {
        return baselinedDocumentKey;
    }

    @XmlTransient
    public DocumentsCollection getDocumentsCollection(){
        return documentsCollection;
    }
    public void setDocumentsCollection(DocumentsCollection documentsCollection) {
        this.documentsCollection = documentsCollection;
    }

    public BaselinedFolder getBaselinedFolder() {
        return baselinedFolder;
    }
    public void setBaselinedFolder(BaselinedFolder baselinedFolder) {
        this.baselinedFolder = baselinedFolder;
    }

    public DocumentIteration getTargetDocument() {
        return targetDocument;
    }

    public String targetDocumentMasterId(){
        return targetDocument.getDocumentMasterId();
    }

    public String getTargetDocumentVersion() {
        return targetDocumentVersion;
    }
    public void setTargetDocumentVersion(String targetDocumentVersion) {
        this.targetDocumentVersion = targetDocumentVersion;
    }

    public int getTargetDocumentIteration() {
        return targetDocumentIteration;
    }
    public void setTargetDocumentIteration(int targetDocumentIteration) {
        this.targetDocumentIteration = targetDocumentIteration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BaselinedDocument that = (BaselinedDocument) o;

        return documentsCollection.equals(that.documentsCollection)
                && targetDocument.equals(that.targetDocument);
    }

    @Override
    public int hashCode() {
        int result = documentsCollection.hashCode();
        result = 31 * result + targetDocument.hashCode();
        return result;
    }
}
