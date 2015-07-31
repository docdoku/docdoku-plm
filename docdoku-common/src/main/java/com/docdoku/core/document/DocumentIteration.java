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

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.common.FileHolder;
import com.docdoku.core.common.User;
import com.docdoku.core.meta.InstanceAttribute;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.*;

/**
 * This DocumentIteration class represents the iterated part of a document.
 * The iteration attribute indicates the order in which the modifications
 * have been made on the document.
 * 
 * @author Florent Garin
 * @version 1.0, 02/06/08
 * @since   V1.0
 */
@Table(name = "DOCUMENTITERATION")
@javax.persistence.IdClass(com.docdoku.core.document.DocumentIterationKey.class)
@NamedQueries ({
    @NamedQuery(name="DocumentIteration.findByBinaryResource", query = "SELECT d FROM DocumentIteration d WHERE :binaryResource member of d.attachedFiles")
})
@javax.persistence.Entity
public class DocumentIteration implements Serializable, FileHolder, Comparable<DocumentIteration>, Cloneable {

    @Id
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name = "DOCUMENTMASTER_ID", referencedColumnName = "DOCUMENTMASTER_ID"),
        @JoinColumn(name = "DOCUMENTREVISION_VERSION", referencedColumnName = "VERSION"),
        @JoinColumn(name = "WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID")
    })
    private DocumentRevision documentRevision;

    @javax.persistence.Id
    private int iteration;

    @OneToMany(cascade = {CascadeType.REMOVE, CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinTable(name = "DOCUMENTITERATION_BINRES",
    inverseJoinColumns = {
        @JoinColumn(name = "ATTACHEDFILE_FULLNAME", referencedColumnName = "FULLNAME")
    },
    joinColumns = {
        @JoinColumn(name = "WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID"),
        @JoinColumn(name = "DOCUMENTMASTER_ID", referencedColumnName = "DOCUMENTMASTER_ID"),
        @JoinColumn(name = "DOCUMENTREVISION_VERSION", referencedColumnName = "DOCUMENTREVISION_VERSION"),
        @JoinColumn(name = "ITERATION", referencedColumnName = "ITERATION")
    })
    private Set<BinaryResource> attachedFiles = new HashSet<>();
    private String revisionNote;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name = "AUTHOR_LOGIN", referencedColumnName = "LOGIN"),
        @JoinColumn(name = "AUTHOR_WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID")
    })
    private User author;
    
    @javax.persistence.Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date creationDate;

    @javax.persistence.Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date modificationDate;

    @javax.persistence.Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date checkInDate;
    
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "DOCUMENTITERATION_DOCUMENTLINK",
    inverseJoinColumns = {
        @JoinColumn(name = "DOCUMENTLINK_ID", referencedColumnName = "ID")
    },
    joinColumns = {
        @JoinColumn(name = "WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID"),
        @JoinColumn(name = "DOCUMENTMASTER_ID", referencedColumnName = "DOCUMENTMASTER_ID"),
        @JoinColumn(name = "DOCUMENTREVISION_VERSION", referencedColumnName = "DOCUMENTREVISION_VERSION"),
        @JoinColumn(name = "ITERATION", referencedColumnName = "ITERATION")
    })
    private Set<DocumentLink> linkedDocuments = new HashSet<>();
    
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderColumn(name="ATTRIBUTE_ORDER")
    @JoinTable(name = "DOCUMENTITERATION_ATTRIBUTE",
    inverseJoinColumns = {
        @JoinColumn(name = "INSTANCEATTRIBUTE_ID", referencedColumnName = "ID")
    },
    joinColumns = {
        @JoinColumn(name = "WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID"),
        @JoinColumn(name = "DOCUMENTMASTER_ID", referencedColumnName = "DOCUMENTMASTER_ID"),
        @JoinColumn(name = "DOCUMENTREVISION_VERSION", referencedColumnName = "DOCUMENTREVISION_VERSION"),
        @JoinColumn(name = "ITERATION", referencedColumnName = "ITERATION")
    })
    private List<InstanceAttribute> instanceAttributes = new ArrayList<>();

    public DocumentIteration() {
    }

    public DocumentIteration(DocumentRevision pDocumentRevision, User pAuthor) {
        DocumentIteration lastDoc = pDocumentRevision.getLastIteration();
        int newIteration = 1;

        if (lastDoc != null) {
            newIteration = lastDoc.getIteration() + 1;
            Date lastModificationDate = lastDoc.modificationDate;
            modificationDate = lastModificationDate;
        }

        setDocumentRevision(pDocumentRevision);
        iteration = newIteration;
        author = pAuthor;
        checkInDate = null;
    }

    public void setDocumentRevision(DocumentRevision documentRevision) {
        this.documentRevision = documentRevision;
    }


    public String getWorkspaceId() {
        return documentRevision==null?"":documentRevision.getWorkspaceId();
    }

    public String getId() {
        return documentRevision==null?"":documentRevision.getId();
    }

    public String getVersion() {
        return documentRevision==null?"":documentRevision.getVersion();
    }

    public String getDocumentMasterId() {
        return getId();
    }

    public int getIteration() {
        return iteration;
    }

    public void setRevisionNote(String pRevisionNote) {
        revisionNote = pRevisionNote;
    }

    public String getRevisionNote() {
        return revisionNote;
    }

    public void setAttachedFiles(Set<BinaryResource> attachedFiles) {
        this.attachedFiles = attachedFiles;
    }

    public void setIteration(int iteration) {
        this.iteration = iteration;
    }

    public boolean removeFile(BinaryResource pBinaryResource) {
        return attachedFiles.remove(pBinaryResource);
    }

    public void addFile(BinaryResource pBinaryResource) {
        attachedFiles.add(pBinaryResource);
    }

    @Override
    public Set<BinaryResource> getAttachedFiles() {
        return attachedFiles;
    }

    public DocumentRevisionKey getDocumentRevisionKey() {
        return documentRevision==null?new DocumentRevisionKey(new DocumentMasterKey("",""),""):documentRevision.getKey();
    }
    public DocumentIterationKey getKey() {
        return new DocumentIterationKey(getDocumentRevisionKey(),iteration);
    }


    public void setAuthor(User pAuthor) {
        author = pAuthor;
    }

    public User getAuthor() {
        return author;
    }

    @XmlTransient
    public DocumentRevision getDocumentRevision() {
        return documentRevision;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = (creationDate!=null) ? (Date) creationDate.clone() : null;
    }

    public Date getCreationDate() {
        return (creationDate!=null) ? (Date) creationDate.clone() : null;
    }

    public void setModificationDate(Date modificationDate) {
        this.modificationDate = (modificationDate!=null) ? (Date) modificationDate.clone() : null;
    }

    public Date getModificationDate() {
        return (modificationDate!=null) ? (Date) modificationDate.clone() : null;
    }

    public void setCheckInDate(Date checkInDate) {
        this.checkInDate = (checkInDate!=null) ? (Date) checkInDate.clone() : null;
    }

    public Date getCheckInDate() {
        return (checkInDate!=null) ? (Date) checkInDate.clone() : null;
    }

    public Set<DocumentLink> getLinkedDocuments() {
        return linkedDocuments;
    }

    public void setLinkedDocuments(Set<DocumentLink> pLinkedDocuments) {
        linkedDocuments=pLinkedDocuments;
    }

    public List<InstanceAttribute> getInstanceAttributes() {
        return instanceAttributes;
    }

    public void setInstanceAttributes(List<InstanceAttribute> pInstanceAttributes) {
        instanceAttributes=pInstanceAttributes;
    }

    public String getTitle() {
        return documentRevision==null ? "" : this.documentRevision.getTitle();
    }

    @Override
    public String toString() {
        return documentRevision + "-" + iteration;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = 31 * hash + getWorkspaceId().hashCode();
        hash = 31 * hash + getId().hashCode();
        hash = 31 * hash + getVersion().hashCode();
        hash = 31 * hash + iteration;
        return hash;
    }

    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof DocumentIteration)) {
            return false;
        }
        DocumentIteration docI = (DocumentIteration) pObj;
        return docI.getId().equals(getId()) &&
                docI.getWorkspaceId().equals(getWorkspaceId()) &&
                docI.getVersion().equals(getVersion()) &&
                docI.iteration==iteration;
    }




    @Override
    public int compareTo(DocumentIteration pDoc) {

        int wksComp = getWorkspaceId().compareTo(pDoc.getWorkspaceId());
        if (wksComp != 0) {
            return wksComp;
        }
        int docmIdComp = getId().compareTo(pDoc.getId());
        if (docmIdComp != 0) {
            return docmIdComp;
        }
        int docmVersionComp = getVersion().compareTo(pDoc.getVersion());
        if (docmVersionComp != 0) {
            return docmVersionComp;
        } else {
            return iteration - pDoc.iteration;
        }
    }

    /**
     * perform a deep clone operation
     */
    @Override
    public DocumentIteration clone() {
        DocumentIteration clone = null;
        try {
            clone = (DocumentIteration) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
        //perform a deep copy
        clone.attachedFiles = new HashSet<>(attachedFiles);

        Set<DocumentLink> clonedLinks = new HashSet<>();
        for (DocumentLink link : linkedDocuments) {
            DocumentLink clonedLink = link.clone();
            clonedLinks.add(clonedLink);
        }
        clone.linkedDocuments = clonedLinks;

        //perform a deep copy
        List<InstanceAttribute> clonedInstanceAttributes = new ArrayList<>();
        for (InstanceAttribute attribute : instanceAttributes) {
            InstanceAttribute clonedAttribute = attribute.clone();
            clonedInstanceAttributes.add(clonedAttribute);
        }
        clone.instanceAttributes = clonedInstanceAttributes;

        if (creationDate != null) {
            clone.creationDate = (Date) creationDate.clone();
        }
        if (modificationDate != null) {
            clone.modificationDate = (Date) modificationDate.clone();
        }
        if (checkInDate != null) {
            clone.checkInDate = (Date) checkInDate.clone();
        }
        return clone;
    }
}