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

package com.docdoku.server.rest.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Florent Garin
 */
@XmlRootElement
public class DocumentRevisionDTO implements Serializable, Comparable<DocumentRevisionDTO> {

    private String workspaceId;
    private String id;
    private String documentMasterId;
    private String version;
    private String type;
    private UserDTO author;
    private Date creationDate;
    @XmlElement(nillable = true)
    private String commentLink;
    private String title;

    @XmlElement(nillable = true)
    private UserDTO checkOutUser;

    @XmlElement(nillable = true)
    private Date checkOutDate;

    private String[] tags;
    private String description;
    private boolean iterationSubscription;
    private boolean stateSubscription;
    private List<DocumentIterationDTO> documentIterations;
    private WorkflowDTO workflow;
    private String path;
    private String lifeCycleState;
    private boolean publicShared;
    private boolean attributesLocked;

    @XmlElement(nillable = true)
    private ACLDTO acl;

    public DocumentRevisionDTO() {
    }

    public DocumentRevisionDTO(String workspaceId, String id, String version) {
        this.workspaceId = workspaceId;
        this.id = id;
        this.documentMasterId = id;
        this.version = version;
    }

    public DocumentRevisionDTO(String workspaceId, String id, String title, String version) {
        this.workspaceId = workspaceId;
        this.id = id;
        this.documentMasterId = id;
        this.title = title;
        this.version = version;
    }

    public UserDTO getAuthor() {
        return author;
    }
    public void setAuthor(UserDTO author) {
        this.author = author;
    }

    public UserDTO getCheckOutUser() {
        return checkOutUser;
    }
    public void setCheckOutUser(UserDTO checkOutUser) {
        this.checkOutUser = checkOutUser;
    }
        
    public Date getCheckOutDate() {
        return (checkOutDate!=null) ? (Date) checkOutDate.clone() : null;
    }
    public void setCheckOutDate(Date checkOutDate) {
        this.checkOutDate = (checkOutDate!=null) ? (Date) checkOutDate.clone() : null;
    }

    public Date getCreationDate() {
        return (creationDate!=null) ? (Date) creationDate.clone() : null;
    }
    public void setCreationDate(Date creationDate) {
        this.creationDate = (creationDate!=null) ? (Date) creationDate.clone() : null;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }
    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public WorkflowDTO getWorkflow() {
        return workflow;
    }
    public void setWorkflow(WorkflowDTO workflow) {
        this.workflow = workflow;
    }

    public String getLifeCycleState() {
        if (lifeCycleState != null) {
            return lifeCycleState;
        }
        if (workflow != null) {
            return workflow.getLifeCycleState();
        }
        return null;
    }
    public void setLifeCycleState(String lifeCycleState) {
        this.lifeCycleState = lifeCycleState;
    }

    public String[] getTags() {
        return tags;
    }
    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }


    public List<DocumentIterationDTO> getDocumentIterations() {
        return documentIterations;
    }
    public void setDocumentIterations(List<DocumentIterationDTO> documentIterations) {
        this.documentIterations = documentIterations;
    }

    @XmlTransient
    public DocumentIterationDTO getLastIteration() {
        if (documentIterations != null) {
            int index = documentIterations.size() - 1;
            if (index < 0) {
                return null;
            } else {
                return documentIterations.get(index);
            }
        } else {
            return null;
        }
    }
    
    public String getId() {
        return id+"-"+version;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getDocumentMasterId() {
        return documentMasterId;
    }
    public void setDocumentMasterId(String documentMasterId) {
        this.documentMasterId = documentMasterId;
    }

    public String getCommentLink() {
        return commentLink;
    }

    public void setCommentLink(String commentLink) {
        this.commentLink = commentLink;
    }

    public boolean isIterationSubscription() {
        return iterationSubscription;
    }
    public void setIterationSubscription(boolean iterationSubscription) {
        this.iterationSubscription = iterationSubscription;
    }

    public boolean isStateSubscription() {
        return stateSubscription;
    }
    public void setStateSubscription(boolean stateSubscription) {
        this.stateSubscription = stateSubscription;
    }

    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    
    public boolean isPublicShared() {
        return publicShared;
    }
    public void setPublicShared(boolean publicShared) {
        this.publicShared = publicShared;
    }

    public ACLDTO getAcl() {
        return acl;
    }
    public void setAcl(ACLDTO acl) {
        this.acl = acl;
    }

    public boolean isAttributesLocked() {
        return attributesLocked;
    }
    public void setAttributesLocked(boolean attributesLocked) {
        this.attributesLocked = attributesLocked;
    }

    @Override
    public String toString() {
        return workspaceId + "-" + id + "-" + version;
    }

    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof DocumentRevisionDTO)) {
            return false;
        }
        DocumentRevisionDTO docR = (DocumentRevisionDTO) pObj;
        return docR.id.equals(id) &&
                docR.workspaceId.equals(workspaceId) &&
                docR.version.equals(version);

    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = 31 * hash + workspaceId.hashCode();
        hash = 31 * hash + id.hashCode();
        hash = 31 * hash + version.hashCode();
        return hash;
    }

    public int compareTo(DocumentRevisionDTO pDocR) {
        int wksComp = workspaceId.compareTo(pDocR.workspaceId);
        if (wksComp != 0) {
            return wksComp;
        }
        int idComp = id.compareTo(pDocR.id);
        if (idComp != 0) {
            return idComp;
        } else {
            return version.compareTo(pDocR.version);
        }
    }
}
