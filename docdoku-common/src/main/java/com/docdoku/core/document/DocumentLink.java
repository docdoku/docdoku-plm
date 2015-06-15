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

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;

/**
 * This is a link class used to connect an object to a {@link DocumentRevision}.
 * Documents are not linked directly but rather through this class to get
 * a loosely coupling and to carry additional information.
 * 
 * @author Florent Garin
 * @version 1.1, 28/01/13
 * @since   V1.0
 */
@Table(name="DOCUMENTLINK")
@javax.persistence.Entity
@NamedQueries ({
    @NamedQuery(name="DocumentLink.findDocumentOwner", query = "SELECT d FROM DocumentIteration d WHERE :link MEMBER OF d.linkedDocuments"),
    @NamedQuery(name="DocumentLink.findPartOwner", query = "SELECT p FROM PartIteration p WHERE :link MEMBER OF p.linkedDocuments"),
    @NamedQuery(name="DocumentLink.findProductInstanceIteration", query = "SELECT p FROM ProductInstanceIteration p JOIN p.linkedDocuments dl where dl.targetDocument = :documentRevision"),
    @NamedQuery(name="DocumentLink.findPathData", query = "SELECT p FROM PathDataIteration p JOIN p.linkedDocuments dl where dl.targetDocument = :documentRevision"),
    @NamedQuery(name="DocumentLink.findInverseDocumentLinks", query = "SELECT d FROM DocumentIteration d JOIN d.linkedDocuments dl where dl.targetDocument = :documentRevision"),
    @NamedQuery(name="DocumentLink.findInversePartLinks", query = "SELECT p FROM PartIteration p JOIN p.linkedDocuments dl where dl.targetDocument = :documentRevision")
})
public class DocumentLink implements Serializable, Cloneable{

    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Id
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name="TARGET_DOCUMENTMASTER_ID", referencedColumnName="DOCUMENTMASTER_ID"),
        @JoinColumn(name="TARGET_DOCREVISION_VERSION", referencedColumnName="VERSION"),
        @JoinColumn(name="TARGET_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
    })
    private DocumentRevision targetDocument;

    @Column(name = "TARGET_DOCUMENTMASTER_ID", length=100, insertable = false, updatable = false)
    private String targetDocumentMasterId ="";

    @Column(name = "TARGET_DOCREVISION_VERSION", length=10, insertable = false, updatable = false)
    private String targetDocumentVersion ="";

    @Column(name = "TARGET_WORKSPACE_ID", length=100, insertable = false, updatable = false)
    private String targetDocumentWorkspaceId="";

    @Column(name="COMMENTDATA")
    private String comment;

    
    public DocumentLink() {
    }
    
    public DocumentLink(DocumentRevision pTargetDocument, String pComment){
        setTargetDocument(pTargetDocument);
        comment=pComment;
    }

    public DocumentLink(DocumentRevision pTargetDocument){
        setTargetDocument(pTargetDocument);
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    @XmlTransient
    public DocumentRevision getTargetDocument() {
        return targetDocument;
    }

    public DocumentRevisionKey getTargetDocumentKey(){
        return new DocumentRevisionKey(targetDocumentWorkspaceId, targetDocumentMasterId, targetDocumentVersion);
    }

    public String getTargetDocumentMasterId() {
        return targetDocumentMasterId;
    }

    public String getTargetDocumentVersion() {
        return targetDocumentVersion;
    }

    public String getTargetDocumentWorkspaceId() {
        return targetDocumentWorkspaceId;
    }

    public void setTargetDocumentMasterId(String targetDocumentMasterId) {
        this.targetDocumentMasterId = targetDocumentMasterId;
    }

    public void setTargetDocumentVersion(String targetDocumentVersion) {
        this.targetDocumentVersion = targetDocumentVersion;
    }

    public void setTargetDocumentWorkspaceId(String targetDocumentWorkspaceId) {
        this.targetDocumentWorkspaceId = targetDocumentWorkspaceId;
    }

    

    public void setTargetDocument(DocumentRevision targetDocument) {
        this.targetDocument = targetDocument;
        targetDocumentMasterId =targetDocument.getId();
        targetDocumentVersion =targetDocument.getVersion();
        targetDocumentWorkspaceId=targetDocument.getWorkspaceId();
    }

    
    @Override
    public String toString() {
        return targetDocumentMasterId +"-"+ targetDocumentVersion;
    }

    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof DocumentLink)) {
            return false;
        }
        DocumentLink link = (DocumentLink) pObj;
        return link.id == id;
    }

    @Override
    public int hashCode() {
        return id;
    }
    

    @Override
    public DocumentLink clone() {
        DocumentLink clone;
        try {
            clone = (DocumentLink) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
        
        return clone;
    }

    public String getDocumentTitle() {
        return this.getTargetDocument().getTitle();
    }

}
