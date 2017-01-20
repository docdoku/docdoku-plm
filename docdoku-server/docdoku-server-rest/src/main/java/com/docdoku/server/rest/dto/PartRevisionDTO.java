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

import com.docdoku.core.product.PartRevisionStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@XmlRootElement
@ApiModel(value="PartRevisionDTO", description="This class is a representation of a {@link com.docdoku.core.product.PartRevision} entity")
public class PartRevisionDTO implements Serializable {

    @ApiModelProperty(value = "Part last iteration number")
    @XmlElement(nillable = true)
    int lastIterationNumber;

    @ApiModelProperty(value = "Part key")
    private String partKey;

    @ApiModelProperty(value = "Part number")
    private String number;

    @ApiModelProperty(value = "Part name")
    private String version;

    @ApiModelProperty(value = "Part version")
    private String type;

    @ApiModelProperty(value = "Part name")
    private String name;

    @ApiModelProperty(value = "Part author")
    private UserDTO author;

    @ApiModelProperty(value = "Part creation date")
    private Date creationDate;

    @ApiModelProperty(value = "Part modification date")
    private Date modificationDate;

    @ApiModelProperty(value = "Part check in date")
    private Date checkInDate;

    @ApiModelProperty(value = "Part description")
    private String description;

    @ApiModelProperty(value = "Part iteration list")
    private List<PartIterationDTO> partIterations;

    @ApiModelProperty(value = "Part check out user if any")
    @XmlElement(nillable = true)
    private UserDTO checkOutUser;

    @ApiModelProperty(value = "Part check out date if any")
    @XmlElement(nillable = true)
    private Date checkOutDate;

    @ApiModelProperty(value = "Instantiated workflow if any")
    @XmlElement(nillable = true)
    private WorkflowDTO workflow;

    @ApiModelProperty(value = "Current lifecycle state")
    private String lifeCycleState;

    @ApiModelProperty(value = "Standard part flag")
    private boolean standardPart;

    @ApiModelProperty(value = "Workspace id")
    private String workspaceId;

    @ApiModelProperty(value = "Public shared flag")
    private boolean publicShared;

    @ApiModelProperty(value = "Part ACL")
    @XmlElement(nillable = true)
    private ACLDTO acl;

    @ApiModelProperty(value = "Attributes locked flag")
    private boolean attributesLocked;

    @ApiModelProperty(value = "Part status")
    @XmlElement(nillable = true)
    private PartRevisionStatus status;

    @ApiModelProperty(value = "Part tag list")
    private String[] tags;

    @ApiModelProperty(value = "Hooked modification notifications")
    private List<ModificationNotificationDTO> notifications;

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

    public PartRevisionDTO() {
    }

    public PartRevisionDTO(String workspaceId, String number, String name, String version) {
        this.number = number;
        this.name = name;
        this.version = version;
        this.workspaceId = workspaceId;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
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

    public UserDTO getAuthor() {
        return author;
    }

    public void setAuthor(UserDTO author) {
        this.author = author;
    }

    public Date getCreationDate() {
        return (creationDate != null) ? (Date) creationDate.clone() : null;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = (creationDate != null) ? (Date) creationDate.clone() : null;
    }

    public Date getModificationDate() {
        return (modificationDate != null) ? (Date) modificationDate.clone() : null;
    }

    public void setModificationDate(Date modificationDate) {
        this.modificationDate = (modificationDate != null) ? (Date) modificationDate.clone() : null;
    }

    public Date getCheckInDate() {
        return (checkInDate != null) ? (Date) checkInDate.clone() : null;
    }

    public void setCheckInDate(Date checkInDate) {
        this.checkInDate = (checkInDate != null) ? (Date) checkInDate.clone() : null;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isStandardPart() {
        return standardPart;
    }

    public void setStandardPart(boolean standardPart) {
        this.standardPart = standardPart;
    }

    public List<PartIterationDTO> getPartIterations() {
        return partIterations;
    }

    public void setPartIterations(List<PartIterationDTO> partIterations) {
        this.partIterations = partIterations;
    }

    public List<ModificationNotificationDTO> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<ModificationNotificationDTO> notifications) {
        this.notifications = notifications;
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

    public WorkflowDTO getWorkflow() {
        return workflow;
    }

    public void setWorkflow(WorkflowDTO workflow) {
        this.workflow = workflow;
    }

    public String getPartKey() {
        return partKey;
    }

    public void setPartKey(String partKey) {
        this.partKey = partKey;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getLifeCycleState() {
        return lifeCycleState;
    }

    public void setLifeCycleState(String lifeCycleState) {
        this.lifeCycleState = lifeCycleState;
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

    public PartRevisionStatus getStatus() {
        return status;
    }

    public void setStatus(PartRevisionStatus status) {
        this.status = status;
    }

    public int getLastIterationNumber() {
        return lastIterationNumber;
    }

    public void setLastIterationNumber(int lastIterationNumber) {
        this.lastIterationNumber = lastIterationNumber;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PartRevisionDTO partRevisionDTO = (PartRevisionDTO) o;

        return number.equals(partRevisionDTO.number) && version.equals(partRevisionDTO.version) && workspaceId.equals(partRevisionDTO.workspaceId);

    }

    @Override
    public int hashCode() {
        int result = number.hashCode();
        result = 31 * result + version.hashCode();
        result = 31 * result + workspaceId.hashCode();
        return result;
    }
}