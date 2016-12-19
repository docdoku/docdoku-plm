/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
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

import com.docdoku.core.document.DocumentRevisionStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author Florent Garin
 */
@XmlRootElement
@ApiModel(value="DocumentRevisionDTO", description="This class is the representation of a {@link com.docdoku.core.document.DocumentRevision} entity")
public class DocumentRevisionDTO implements Serializable, Comparable<DocumentRevisionDTO> {

    @ApiModelProperty(value = "Workspace id")
    private String workspaceId;

    @ApiModelProperty(value = "Document key")
    private String id;

    @ApiModelProperty(value = "Document master id")
    private String documentMasterId;

    @ApiModelProperty(value = "Document version")
    private String version;

    @ApiModelProperty(value = "Document type")
    private String type;

    @ApiModelProperty(value = "Document author")
    private UserDTO author;

    @ApiModelProperty(value = "Document creation date")
    private Date creationDate;

    @ApiModelProperty(value = "Document comment link")
    @XmlElement(nillable = true)
    private String commentLink;

    @ApiModelProperty(value = "Document title")
    private String title;

    @ApiModelProperty(value = "Document check out user")
    @XmlElement(nillable = true)
    private UserDTO checkOutUser;

    @ApiModelProperty(value = "Document check out date")
    @XmlElement(nillable = true)
    private Date checkOutDate;

    @ApiModelProperty(value = "Document tag list")
    private String[] tags;

    @ApiModelProperty(value = "Document description")
    private String description;

    @ApiModelProperty(value = "Subscribe iteration notifications flag")
    private boolean iterationSubscription;

    @ApiModelProperty(value = "Subscribe state notifications flag")
    private boolean stateSubscription;

    @ApiModelProperty(value = "Document iterations list")
    private List<DocumentIterationDTO> documentIterations;

    @ApiModelProperty(value = "Document instantiated workflow")
    private WorkflowDTO workflow;

    @ApiModelProperty(value = "Document instantiated workflow id")
    private Integer workflowId;

    @ApiModelProperty(value = "Document folder")
    private String path;

    @ApiModelProperty(value = "Document folder route")
    private String routePath;

    @ApiModelProperty(value = "Current lifecycle state")
    private String lifeCycleState;

    @ApiModelProperty(value = "Public shared flag")
    private boolean publicShared;

    @ApiModelProperty(value = "Attributes locked flag")
    private boolean attributesLocked;

    @XmlElement(nillable = true)
    @ApiModelProperty(value = "Document status")
    private DocumentRevisionStatus status;

    @ApiModelProperty(value = "Obsolete date")
    private Date obsoleteDate;

    @ApiModelProperty(value = "Obsolete author")
    @XmlElement(nillable = true)
    private UserDTO obsoleteAuthor;

    @ApiModelProperty(value = "Released date")
    private Date releaseDate;

    @ApiModelProperty(value = "Released author")
    @XmlElement(nillable = true)
    private UserDTO releaseAuthor;

    @ApiModelProperty(value = "Document ACL")
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

    public Integer getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(Integer workflowId) {
        this.workflowId = workflowId;
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
        return (checkOutDate != null) ? (Date) checkOutDate.clone() : null;
    }

    public void setCheckOutDate(Date checkOutDate) {
        this.checkOutDate = (checkOutDate != null) ? (Date) checkOutDate.clone() : null;
    }

    public Date getCreationDate() {
        return (creationDate != null) ? (Date) creationDate.clone() : null;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = (creationDate != null) ? (Date) creationDate.clone() : null;
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

    public String getRoutePath() {
        return routePath;
    }

    public void setRoutePath(String routePath) {
        this.routePath = routePath;
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
        return id + "-" + version;
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

    public DocumentRevisionStatus getStatus() {
        return status;
    }

    public void setStatus(DocumentRevisionStatus status) {
        this.status = status;
    }

    public Date getObsoleteDate() {
        return obsoleteDate;
    }

    public void setObsoleteDate(Date obsoleteDate) {
        this.obsoleteDate = obsoleteDate;
    }

    public UserDTO getObsoleteAuthor() {
        return obsoleteAuthor;
    }

    public void setObsoleteAuthor(UserDTO obsoleteAuthor) {
        this.obsoleteAuthor = obsoleteAuthor;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releasedDate) {
        this.releaseDate = releasedDate;
    }

    public UserDTO getReleaseAuthor() {
        return releaseAuthor;
    }

    public void setReleaseAuthor(UserDTO releasedAuthor) {
        this.releaseAuthor = releasedAuthor;
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
