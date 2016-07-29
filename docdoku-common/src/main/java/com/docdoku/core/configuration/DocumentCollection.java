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

import com.docdoku.core.common.User;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentRevisionKey;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This class maintains a collection of document iterations which cannot hold
 * more than one {@link com.docdoku.core.document.DocumentIteration} in
 * the same {@link com.docdoku.core.document.DocumentRevision}.
 *
 * @author Taylor LABEJOF
 * @version 2.0, 25/08/14
 * @since V2.0
 */
@Table(name="DOCUMENTCOLLECTION")
@Entity
public class DocumentCollection implements Serializable {

    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Id
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "AUTHOR_LOGIN", referencedColumnName = "LOGIN"),
            @JoinColumn(name = "AUTHOR_WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID")
    })
    private User author;

    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

    @MapKey(name="baselinedDocumentKey")
    @OneToMany(mappedBy="documentCollection", cascade=CascadeType.ALL, fetch=FetchType.LAZY, orphanRemoval=true)
    private Map<BaselinedDocumentKey, BaselinedDocument> baselinedDocuments = new HashMap<>();

    public DocumentCollection() {
    }

    public Map<BaselinedDocumentKey, BaselinedDocument> getBaselinedDocuments() {
        return baselinedDocuments;
    }
    public void removeAllBaselinedDocuments() {
        baselinedDocuments.clear();
    }

    public BaselinedDocument addBaselinedDocument(DocumentIteration targetDocument){
        BaselinedDocument baselinedDocument = new BaselinedDocument(this, targetDocument);
        baselinedDocuments.put(baselinedDocument.getKey(), baselinedDocument);
        return baselinedDocument;
    }

    public BaselinedDocument getBaselinedDocument(BaselinedDocumentKey baselinedDocumentKey){
        return baselinedDocuments.get(baselinedDocumentKey);
    }
    public BaselinedDocument getBaselinedDocument(DocumentRevisionKey documentRevisionKey){
        BaselinedDocumentKey baselinedDocumentKey = getBaselinedDocumentKey(documentRevisionKey);
        return getBaselinedDocument(baselinedDocumentKey);
    }
    public boolean hasBaselinedDocument(DocumentRevisionKey documentRevisionKey){
        BaselinedDocumentKey baselinedDocumentKey = getBaselinedDocumentKey(documentRevisionKey);
        return baselinedDocuments.containsKey(baselinedDocumentKey);
    }

    public Date getCreationDate() {
        return (creationDate!=null) ? (Date) creationDate.clone() : null;
    }
    public void setCreationDate(Date creationDate) {
        this.creationDate = (creationDate!=null) ? (Date) creationDate.clone() : null;
    }

    public User getAuthor() {
        return author;
    }
    public void setAuthor(User author) {
        this.author = author;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    private BaselinedDocumentKey getBaselinedDocumentKey(DocumentRevisionKey documentRevisionKey) {
        return new BaselinedDocumentKey(id, documentRevisionKey.getWorkspaceId(), documentRevisionKey.getDocumentMasterId(), documentRevisionKey.getVersion());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DocumentCollection)) {
            return false;
        }

        DocumentCollection collection = (DocumentCollection) o;
        return id == collection.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}