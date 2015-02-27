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

import com.docdoku.core.common.User;
import com.docdoku.core.common.Version;
import com.docdoku.core.common.Workspace;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class holds the unchanging aspects of a document.
 * From that object, we can navigate to all the revisions and then
 * the iterations of the document where most of the data sits.
 * 
 * @author Florent Garin
 * @version 1.1, 23/01/12
 * @since   V1.0
 */
@Table(name="DOCUMENTMASTER")
@IdClass(com.docdoku.core.document.DocumentMasterKey.class)
@Entity
@NamedQueries ({
    @NamedQuery(name="DocumentMaster.findByWorkspace", query="SELECT dm FROM DocumentMaster dm WHERE dm.workspace.id = :workspaceId ORDER BY dm.creationDate DESC")
})
public class DocumentMaster implements Serializable, Comparable<DocumentMaster> {

    @Column(name="ID", length=100)
    @Id
    private String id="";

    @Id
    @ManyToOne(optional=false, fetch=FetchType.EAGER)
    private Workspace workspace;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name="AUTHOR_LOGIN", referencedColumnName="LOGIN"),
        @JoinColumn(name="AUTHOR_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
    })
    private User author;
    
    @javax.persistence.Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date creationDate;
    
    private String type;
    
    @OneToMany(mappedBy = "documentMaster", cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    @OrderBy("version ASC")
    private List<DocumentRevision> documentRevisions = new ArrayList<>();

    private boolean attributesLocked;

    public DocumentMaster() {
    }
    
    public DocumentMaster(Workspace pWorkspace,
            String pId,
            User pAuthor) {
        this(pWorkspace, pId);
        author = pAuthor;
    }
    
    private DocumentMaster(Workspace pWorkspace, String pId) {
        id=pId;
        setWorkspace(pWorkspace);
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

    public void setId(String id) {
        this.id = id;
    }
    public String getId(){
        return id;
    }

    public List<DocumentRevision> getDocumentRevisions() {
        return documentRevisions;
    }
    public void setDocumentRevisions(List<DocumentRevision> documentRevisions) {
        this.documentRevisions = documentRevisions;
    }

    public DocumentRevision getDocumentRevision(String version) {
        for(DocumentRevision documentRevision : documentRevisions){
            if(documentRevision.getVersion().equals(version)){
                return documentRevision;
            }
        }
        return null;
    }


    public DocumentRevision getLastRevision() {
        int index = documentRevisions.size()-1;
        if(index < 0) {
            return null;
        } else {
            return documentRevisions.get(index);
        }
    }

    public DocumentRevision removeLastRevision() {
        int index = documentRevisions.size()-1;
        if(index < 0) {
            return null;
        } else {
            return documentRevisions.remove(index);
        }
    }

    public void removeRevision(DocumentRevision documentRevision) {
        documentRevisions.remove(documentRevision);
    }

    public DocumentRevision createNextRevision(User pUser){
        DocumentRevision lastRev=getLastRevision();
        Version version;
        if(lastRev==null) {
            version = new Version("A");
        } else{
            version = new Version(lastRev.getVersion());
            version.increase();
        }

        DocumentRevision rev = new DocumentRevision(this,version,pUser);
        documentRevisions.add(rev);
        return rev;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public DocumentMasterKey getKey() {
        return new DocumentMasterKey(getWorkspaceId(), id);
    }

    public String getWorkspaceId() {
        return workspace == null ? "" : workspace.getId();
    }



    public void setWorkspace(Workspace pWorkspace){
        workspace=pWorkspace;
    }
    public Workspace getWorkspace(){
        return workspace;
    }



    
    @Override
    public String toString() {
        return id;
    }
    
    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof DocumentMaster)) {
            return false;
        }
        DocumentMaster docM = (DocumentMaster) pObj;
        return docM.id.equals(id) && docM.getWorkspaceId().equals(getWorkspaceId());
        
    }
    
    @Override
    public int hashCode() {
        int hash = 1;
        hash = 31 * hash + getWorkspaceId().hashCode();
        hash = 31 * hash + id.hashCode();
        return hash;
    }

    @Override
    public int compareTo(DocumentMaster pDocM) {
        int wksComp = getWorkspaceId().compareTo(pDocM.getWorkspaceId());
        if (wksComp != 0) {
            return wksComp;
        } else {
            return id.compareTo(pDocM.id);
        }

    }

    public boolean isAttributesLocked() {
        return attributesLocked;
    }

    public void setAttributesLocked(boolean attributesLocked) {
        this.attributesLocked = attributesLocked;
    }
}
