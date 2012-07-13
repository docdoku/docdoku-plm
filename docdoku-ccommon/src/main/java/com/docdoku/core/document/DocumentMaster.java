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

import com.docdoku.core.common.Version;
import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.security.ACL;
import com.docdoku.core.workflow.Workflow;
import java.io.Serializable;
import java.util.*;
import javax.persistence.*;

/**
 * This is the wrapper object that holds the data of a document.
 * From that object, we can navigate to all the revisions and then
 * the iterations of the document, its workflow or its binary data files.
 * 
 * @author Florent Garin
 * @version 1.1, 23/01/12
 * @since   V1.0
 */
@javax.persistence.IdClass(com.docdoku.core.document.DocumentMasterKey.class)
@javax.persistence.Entity
public class DocumentMaster implements Serializable, Comparable<DocumentMaster>, Cloneable {
    
    
    @javax.persistence.Column(name = "WORKSPACE_ID", length=50, nullable = false, insertable = false, updatable = false)
    @javax.persistence.Id
    private String workspaceId="";

    @Column(length=50)
    @javax.persistence.Id
    private String id="";

    @Column(length=10)
    @javax.persistence.Id
    private String version="";
    
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name="AUTHOR_LOGIN", referencedColumnName="LOGIN"),
        @JoinColumn(name="AUTHOR_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
    })
    private User author;
    
    @javax.persistence.Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private java.util.Date creationDate;
    
    @javax.persistence.ManyToOne(optional=false, fetch=FetchType.EAGER)
    private Workspace workspace;
    
    private String title;
    
    private String type;
    
    @Lob
    private String description;
    
    @OneToMany(mappedBy = "documentMaster", cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    @OrderBy("iteration ASC")
    private List<DocumentIteration> documentIterations = new ArrayList<DocumentIteration>();
    
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name="CHECKOUTUSER_LOGIN", referencedColumnName="LOGIN"),
        @JoinColumn(name="CHECKOUTUSER_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
    })
    private User checkOutUser;
    
    @javax.persistence.Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date checkOutDate;
    
    @OneToOne(orphanRemoval=true, cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    private Workflow workflow;
    
    @ManyToOne(fetch=FetchType.EAGER)
    private Folder location;
    
    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(
    inverseJoinColumns={
        @JoinColumn(name="TAG_LABEL", referencedColumnName="LABEL"),
        @JoinColumn(name="TAG_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")        
    },
    joinColumns={
        @JoinColumn(name="DOCUMENTMASTER_ID", referencedColumnName="ID"),
        @JoinColumn(name="DOCUMENTMASTER_VERSION", referencedColumnName="VERSION"),
        @JoinColumn(name="DOCUMENTMASTER_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
    })
    private Set<Tag> tags=new HashSet<Tag>();
    
    @OneToOne(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    private ACL acl;

    public DocumentMaster() {
    }
    
    public DocumentMaster(Workspace pWorkspace,
            String pId,
            String pStringVersion,
            User pAuthor) {
        this(pWorkspace, pId);
        version=pStringVersion;
        author = pAuthor;
    }
    
    public DocumentMaster(Workspace pWorkspace, String pId,
            Version pVersion,
            User pAuthor) {
        this(pWorkspace, pId);
        version=pVersion.toString();
        author = pAuthor;
    }
    
    public DocumentMaster(Workspace pWorkspace, String pId, User pAuthor) {
        this(pWorkspace, pId);
        version = new Version().toString();
        author = pAuthor;
    }
    
    private DocumentMaster(Workspace pWorkspace, String pId) {
        id=pId;
        setWorkspace(pWorkspace);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    
    public void setTitle(String pTitle) {
        title = pTitle;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setDescription(String pDescription) {
        description = pDescription;
    }
    
    public String getDescription() {
        return description;
    }

    public ACL getACL() {
        return acl;
    }

    public void setACL(ACL acl) {
        this.acl = acl;
    }

    

    public DocumentMasterKey getKey() {
        return new DocumentMasterKey(workspaceId, id, version);
    }

        
    public String getVersion() {
        return version;
    }
    
    public DocumentIteration createNextIteration(User pUser){
        DocumentIteration lastDoc=getLastIteration();
        int iteration = lastDoc==null?1:lastDoc.getIteration() + 1;
        DocumentIteration doc = new DocumentIteration(this,iteration,pUser);
        documentIterations.add(doc);
        return doc;
    }

    
    public void setDocumentIterations(List<DocumentIteration> documentIterations) {
        this.documentIterations = documentIterations;
    }
    

    public List<DocumentIteration> getDocumentIterations() {
        return documentIterations;
    }
    
    public DocumentIteration getLastIteration() {
        int index = documentIterations.size()-1;
        if(index < 0)
            return null;
        else
            return documentIterations.get(index);
    }
    
    public DocumentIteration removeLastIteration() {
        int index = documentIterations.size()-1;
        if(index < 0)
            return null;
        else
            return documentIterations.remove(index);
    }
    
    public DocumentIteration getIteration(int pIteration) {
        return documentIterations.get(pIteration-1);
    }
    
    public int getNumberOfIterations() {
        return documentIterations.size();
    }
    
    public boolean isCheckedOut() {
        return (checkOutUser != null);
    }
    
    public boolean isCheckedOutBy(String pUser) {
        return (checkOutUser != null && checkOutUser.getLogin().equals(pUser));
    }
    
    public boolean isCheckedOutBy(User pUser) {
        return isCheckedOutBy(pUser.getLogin());
    }
    
    public User getCheckOutUser() {
        return checkOutUser;
    }
    
    public void setCheckOutUser(User pCheckOutUser) {
        checkOutUser = pCheckOutUser;
    }
    
    public Date getCheckOutDate() {
        return checkOutDate;
    }
    
    public void setCheckOutDate(Date pCheckOutDate) {
        checkOutDate = pCheckOutDate;
    }
    
    public void setAuthor(User pAuthor) {
        author = pAuthor;
    }
    
    public User getAuthor() {
        return author;
    }
    
    public void setCreationDate(Date pCreationDate) {
        creationDate = pCreationDate;
    }
    
    public Date getCreationDate() {
        return creationDate;
    }
    
    public void setWorkspace(Workspace pWorkspace){
        workspace=pWorkspace;
        workspaceId=workspace.getId();
    }
    
    public Workspace getWorkspace(){
        return workspace;
    }
    
    public String getWorkspaceId(){
        return workspaceId;
    }
    
    public String getId(){
        return id;
    }
    
    public Workflow getWorkflow() {
        return workflow;
    }
    
    public void setWorkflow(Workflow pWorkflow) {
        workflow = pWorkflow;
    }
    
    public String getLifeCycleState() {
        if (workflow != null)
            return workflow.getLifeCycleState();
        else
            return null;
    }
    
    public boolean hasWorkflow() {
        return (workflow != null);
    }
    
    public Set<Tag> getTags() {
        return tags;
    }
   

    public void setTags(java.util.Set<Tag> pTags) {
        tags.retainAll(pTags);
        pTags.removeAll(tags);
        tags.addAll(pTags);
    }
    
    public boolean addTag(Tag pTag){
        return tags.add(pTag);
    }
    
    public boolean removeTag(Tag pTag){
        return tags.remove(pTag);
    }
    
    @Override
    public String toString() {
        return id + "-" + version;
    }
    
    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof DocumentMaster))
            return false;
        DocumentMaster docM = (DocumentMaster) pObj;
        return ((docM.id.equals(id)) && (docM.workspaceId.equals(workspaceId)) && (docM.version.equals(version)));
        
    }
    
    @Override
    public int hashCode() {
        int hash = 1;
	hash = 31 * hash + workspaceId.hashCode();
	hash = 31 * hash + id.hashCode();
        hash = 31 * hash + version.hashCode();
	return hash;
    }

    public int compareTo(DocumentMaster pDocM) {
        int wksComp = workspaceId.compareTo(pDocM.workspaceId);
        if (wksComp != 0)
            return wksComp;
        int idComp = id.compareTo(pDocM.id);
        if (idComp != 0)
            return idComp;
        else
            return version.compareTo(pDocM.version);
    }
    
    public Folder getLocation() {
        return location;
    }
    
    public void setLocation(Folder pLocation) {
        location = pLocation;
    }
    

    public void setId(String id) {
        this.id = id;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    
    /**
     * perform a deep clone operation
     */
    @Override
    public DocumentMaster clone() {
        DocumentMaster clone = null;
        try {
            clone = (DocumentMaster) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
        //perform a deep copy
        List<DocumentIteration> clonedDocumentIterations = new ArrayList<DocumentIteration>();
        for (DocumentIteration document : documentIterations) {
            DocumentIteration clonedDocument=document.clone();
            clonedDocument.setDocumentMaster(clone);
            clonedDocumentIterations.add(clonedDocument);
        }
        clone.documentIterations = clonedDocumentIterations;
        
        if(workflow !=null)
            clone.workflow = workflow.clone();

        if(acl !=null)
            clone.acl = acl.clone();

        clone.tags = new HashSet<Tag>(tags);
        
        if(creationDate!=null)
            clone.creationDate = (Date) creationDate.clone();
        
        if(checkOutDate!=null)
            clone.checkOutDate = (Date) checkOutDate.clone();
        
        return clone;
    }
    
}
