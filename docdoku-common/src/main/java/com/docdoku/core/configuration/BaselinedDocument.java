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


import com.docdoku.core.document.DocumentIteration;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;

/**
 * Class link that gathers a document collection and a given document iteration.
 *
 * @author Taylor LABEJOF
 * @version 2.0, 25/08/14
 * @since V2.0
 */

@Table(name = "BASELINEDDOCUMENT")
@Entity
public class BaselinedDocument implements Serializable {
    @EmbeddedId
    private BaselinedDocumentKey baselinedDocumentKey;

    //@MapsId("documentCollectionId")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "DOCUMENTCOLLECTION_ID", referencedColumnName = "ID")
    private DocumentCollection documentCollection;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "TARGET_WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID"),
            @JoinColumn(name = "TARGET_DOCUMENTMASTER_ID", referencedColumnName = "DOCUMENTMASTER_ID"),
            @JoinColumn(name = "TARGET_DOCREVISION_VERSION", referencedColumnName = "DOCUMENTREVISION_VERSION"),
            @JoinColumn(name = "TARGET_ITERATION", referencedColumnName = "ITERATION")
    })
    private DocumentIteration targetDocument;

    @Column(name = "TARGET_ITERATION", nullable = false, insertable = false, updatable = false)
    private int targetDocumentIteration;

    public BaselinedDocument() {
    }

    public BaselinedDocument(DocumentCollection documentCollection, DocumentIteration targetDocument) {
        this.documentCollection = documentCollection;
        this.targetDocument = targetDocument;
        this.baselinedDocumentKey = new BaselinedDocumentKey(documentCollection.getId(), targetDocument.getWorkspaceId(), targetDocument.getDocumentMasterId(), targetDocument.getVersion());
        this.targetDocumentIteration = targetDocument.getIteration();
    }

    public BaselinedDocumentKey getKey() {
        return baselinedDocumentKey;
    }

    @XmlTransient
    public DocumentCollection getDocumentCollection() {
        return documentCollection;
    }

    public void setDocumentCollection(DocumentCollection documentCollection) {
        this.documentCollection = documentCollection;
    }

    public DocumentIteration getTargetDocument() {
        return targetDocument;
    }

    public String getTargetDocumentMasterId() {
        return targetDocument.getDocumentMasterId();
    }

    public String getTargetDocumentVersion() {
        return targetDocument.getVersion();
    }

    public int getTargetDocumentIteration() {
        return targetDocumentIteration;
    }

    public void setTargetDocumentIteration(int targetDocumentIteration) {
        this.targetDocumentIteration = targetDocumentIteration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BaselinedDocument that = (BaselinedDocument) o;

        if (baselinedDocumentKey != null ? !baselinedDocumentKey.equals(that.baselinedDocumentKey) : that.baselinedDocumentKey != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return baselinedDocumentKey != null ? baselinedDocumentKey.hashCode() : 0;
    }
}
