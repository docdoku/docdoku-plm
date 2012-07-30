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

import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.common.User;
import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.common.FileHolder;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlTransient;

/**
 * This <code>DocumentIteration</code> class represents the iterated part of a document.
 * The iteration attribute indicates the order in which the modifications
 * have been made on the document.
 * 
 * @author Florent Garin
 * @version 1.0, 02/06/08
 * @since   V1.0
 */
@javax.persistence.IdClass(com.docdoku.core.document.DocumentIterationKey.class)
@javax.persistence.Entity
public class DocumentIteration implements Serializable, FileHolder, Comparable<DocumentIteration>, Cloneable {
    
    
    @ManyToOne(optional=false, fetch=FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name="DOCUMENTMASTER_ID", referencedColumnName="ID"),
        @JoinColumn(name="DOCUMENTMASTER_VERSION", referencedColumnName="VERSION"),
        @JoinColumn(name="WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
    })
    private DocumentMaster documentMaster;
    
    @javax.persistence.Id
    private int iteration;
    
    @javax.persistence.Column(name = "DOCUMENTMASTER_ID", length=50, nullable = false, insertable = false, updatable = false)
    @javax.persistence.Id
    private String documentMasterId="";
    
    @javax.persistence.Column(name = "DOCUMENTMASTER_VERSION", length=10, nullable = false, insertable = false, updatable = false)
    @javax.persistence.Id
    private String documentMasterVersion="";
    
    @javax.persistence.Column(name = "WORKSPACE_ID", length=50, nullable = false, insertable = false, updatable = false)
    @javax.persistence.Id
    private String workspaceId="";
    
    
    @OneToMany(cascade={CascadeType.REMOVE,CascadeType.REFRESH}, fetch=FetchType.EAGER)
    @JoinTable(
    inverseJoinColumns={
        @JoinColumn(name="ATTACHEDFILE_FULLNAME", referencedColumnName="FULLNAME")
    },
    joinColumns={
        @JoinColumn(name="DOCUMENT_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID"),
        @JoinColumn(name="DOCUMENT_DOCUMENTMASTER_ID", referencedColumnName="DOCUMENTMASTER_ID"),
        @JoinColumn(name="DOCUMENT_DOCUMENTMASTER_VERSION", referencedColumnName="DOCUMENTMASTER_VERSION"),
        @JoinColumn(name="DOCUMENT_ITERATION", referencedColumnName="ITERATION")
    })
    private Set<BinaryResource> attachedFiles = new HashSet<BinaryResource>();
    
    private String revisionNote;
    
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name="AUTHOR_LOGIN", referencedColumnName="LOGIN"),
        @JoinColumn(name="AUTHOR_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
    })
    private User author;
    
    @javax.persistence.Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date creationDate;
    
    @OneToMany(mappedBy = "fromDocument", cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    private Set<DocumentToDocumentLink> linkedDocuments=new HashSet<DocumentToDocumentLink>();
    
    @OneToMany(orphanRemoval=true, cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    @MapKey(name="name")
    @JoinTable(
    inverseJoinColumns={
        @JoinColumn(name="INSTANCEATTRIBUTE_ID", referencedColumnName="ID")
    },
    joinColumns={
        @JoinColumn(name="DOCUMENT_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID"),
        @JoinColumn(name="DOCUMENT_DOCUMENTMASTER_ID", referencedColumnName="DOCUMENTMASTER_ID"),
        @JoinColumn(name="DOCUMENT_DOCUMENTMASTER_VERSION", referencedColumnName="DOCUMENTMASTER_VERSION"),
        @JoinColumn(name="DOCUMENT_ITERATION", referencedColumnName="ITERATION")
    })
    private Map<String, InstanceAttribute> instanceAttributes=new HashMap<String, InstanceAttribute>();

    public DocumentIteration() {
    }
    
    
    public DocumentIteration(DocumentMaster pDocumentMaster, int pIteration, User pAuthor) {
        setDocumentMaster(pDocumentMaster);
        iteration = pIteration;
        author = pAuthor;
    }
    
    public void setDocumentMaster(DocumentMaster pDocumentMaster) {
        documentMaster = pDocumentMaster;
        documentMasterId=pDocumentMaster.getId();
        documentMasterVersion=pDocumentMaster.getVersion();
        workspaceId=pDocumentMaster.getWorkspaceId();
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
    
    public boolean removeFile(BinaryResource pBinaryResource){
        return attachedFiles.remove(pBinaryResource);
    }
    
    public void addFile(BinaryResource pBinaryResource){
        attachedFiles.add(pBinaryResource);
    }
    
    @Override
    public Set<BinaryResource> getAttachedFiles() {
        return attachedFiles;
    }
    
    public DocumentIterationKey getKey() {
        return new DocumentIterationKey(workspaceId, documentMasterId, documentMasterVersion, iteration);
    }
    
    public String getDocumentMasterId() {
        return documentMasterId;
    }
    
    public String getDocumentMasterVersion() {
        return documentMasterVersion;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }
    
    public void setAuthor(User pAuthor) {
        author = pAuthor;
    }
    
    public User getAuthor() {
        return author;
    }
    
    @XmlTransient
    public DocumentMaster getDocumentMaster() {
        return documentMaster;
    }
    
    public void setCreationDate(Date pCreationDate) {
        creationDate = pCreationDate;
    }
    
    public Date getCreationDate() {
        return creationDate;
    }
     
    public Set<DocumentToDocumentLink> getLinkedDocuments() {
        return linkedDocuments;
    }
    
    public void setLinkedDocuments(Set<DocumentToDocumentLink> pLinkedDocuments) {
        linkedDocuments.retainAll(pLinkedDocuments);
        pLinkedDocuments.removeAll(linkedDocuments);
        linkedDocuments.addAll(pLinkedDocuments);
    }
    
    public Map<String, InstanceAttribute> getInstanceAttributes() {
        return instanceAttributes;
    }


    

    public void setInstanceAttributes(Map<String, InstanceAttribute> pInstanceAttributes) {
        for (InstanceAttribute attr : pInstanceAttributes.values()) {
            InstanceAttribute attrToUpdate = instanceAttributes.get(attr.getName());
            if (attrToUpdate != null) {
                attrToUpdate.setValue(attr.getValue());
            }
        }

        instanceAttributes.values().retainAll(pInstanceAttributes.values());
        pInstanceAttributes.values().removeAll(instanceAttributes.values());
        instanceAttributes.putAll(pInstanceAttributes);
    }
    
    public void setInstanceAttributes(Collection<InstanceAttribute> pInstanceAttributes) {
        for (InstanceAttribute attr : pInstanceAttributes) {
            InstanceAttribute attrToUpdate = instanceAttributes.get(attr.getName());
            if (attrToUpdate != null) {
                attrToUpdate.setValue(attr.getValue());
            }
        }

        instanceAttributes.values().retainAll(pInstanceAttributes);
        pInstanceAttributes.removeAll(instanceAttributes.values());
        
        for(InstanceAttribute attr:pInstanceAttributes)
            instanceAttributes.put(attr.getName(),attr);
    }
    
    @Override
    public String toString() {
        return documentMasterId + "-" + documentMasterVersion + "-" + iteration;
    }
    
    @Override
    public int hashCode() {
        int hash = 1;
	hash = 31 * hash + workspaceId.hashCode();
	hash = 31 * hash + documentMasterId.hashCode();
        hash = 31 * hash + documentMasterVersion.hashCode();
        hash = 31 * hash + iteration;
	return hash;
    }
    
    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof DocumentIteration))
            return false;
        DocumentIteration document = (DocumentIteration) pObj;
        return ((document.documentMasterId.equals(documentMasterId)) && (document.workspaceId.equals(workspaceId))  && (document.documentMasterVersion.equals(documentMasterVersion)) && (document.iteration==iteration));
    }
    
    @Override
    public int compareTo(DocumentIteration pDoc) {
        
        int wksComp = workspaceId.compareTo(pDoc.workspaceId);
        if (wksComp != 0)
            return wksComp;
        int docmIdComp = documentMasterId.compareTo(pDoc.documentMasterId);
        if (docmIdComp != 0)
            return docmIdComp;
        int docmVersionComp = documentMasterVersion.compareTo(pDoc.documentMasterVersion);
        if (docmVersionComp != 0)
            return docmVersionComp;
        else
            return iteration-pDoc.iteration;
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
        clone.attachedFiles = new HashSet<BinaryResource>(attachedFiles);
        
        Set<DocumentToDocumentLink> clonedLinks=new HashSet<DocumentToDocumentLink>();
        for (DocumentToDocumentLink link : linkedDocuments) {
            DocumentToDocumentLink clonedLink=link.clone();
            clonedLink.setFromDocument(clone);
            clonedLinks.add(clonedLink);
        }
        clone.linkedDocuments=clonedLinks;
        
        //perform a deep copy
        Map<String, InstanceAttribute> clonedInstanceAttributes = new HashMap<String, InstanceAttribute>();
        for (InstanceAttribute attribute : instanceAttributes.values()) {
            InstanceAttribute clonedAttribute=attribute.clone();
            clonedInstanceAttributes.put(clonedAttribute.getName(),clonedAttribute);
        }
        clone.instanceAttributes = clonedInstanceAttributes;
        
        if(creationDate!=null)
            clone.creationDate = (Date) creationDate.clone();
        return clone;
    }
}