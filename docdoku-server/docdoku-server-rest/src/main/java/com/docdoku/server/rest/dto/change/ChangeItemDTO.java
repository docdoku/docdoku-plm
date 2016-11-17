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

package com.docdoku.server.rest.dto.change;

import com.docdoku.core.change.ChangeItemCategory;
import com.docdoku.core.change.ChangeItemPriority;
import com.docdoku.server.rest.dto.ACLDTO;
import com.docdoku.server.rest.dto.DocumentIterationDTO;
import com.docdoku.server.rest.dto.PartIterationDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@XmlRootElement
@ApiModel(value="ChangeItemDTO", description="This class is the representation of a {@link com.docdoku.core.change.ChangeItem} entity")
public class ChangeItemDTO implements Serializable {

    @ApiModelProperty(value = "Change item id")
    private int id;

    @ApiModelProperty(value = "Change item name")
    private String name;

    @ApiModelProperty(value = "Workspace id")
    private String workspaceId;

    @ApiModelProperty(value = "Change item author login")
    private String author;

    @ApiModelProperty(value = "Change item author name")
    private String authorName;

    @ApiModelProperty(value = "Change item assignee user login")
    private String assignee;

    @ApiModelProperty(value = "Change item assignee user name")
    private String assigneeName;

    @ApiModelProperty(value = "Change item creation date")
    private Date creationDate;

    @ApiModelProperty(value = "Change item description")
    private String description;

    @ApiModelProperty(value = "Change item priority")
    private ChangeItemPriority priority;

    @ApiModelProperty(value = "Change item category")
    private ChangeItemCategory category;

    @ApiModelProperty(value = "Change item affected documents")
    private List<DocumentIterationDTO> affectedDocuments;

    @ApiModelProperty(value = "Change item affected parts")
    private List<PartIterationDTO> affectedParts;

    @ApiModelProperty(value = "Change item tag list")
    private String[] tags;

    @ApiModelProperty(value = "Change item ACL")
    @XmlElement(nillable = true)
    private ACLDTO acl;

    @ApiModelProperty(value = "Change item writable flag")
    private boolean writable;

    public ChangeItemDTO() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName;
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

    public ChangeItemPriority getPriority() {
        return priority;
    }

    public void setPriority(ChangeItemPriority priority) {
        this.priority = priority;
    }

    public ChangeItemCategory getCategory() {
        return category;
    }

    public void setCategory(ChangeItemCategory category) {
        this.category = category;
    }

    public List<DocumentIterationDTO> getAffectedDocuments() {
        return affectedDocuments;
    }

    public void setAffectedDocuments(List<DocumentIterationDTO> affectedDocuments) {
        this.affectedDocuments = affectedDocuments;
    }

    public List<PartIterationDTO> getAffectedParts() {
        return affectedParts;
    }

    public void setAffectedParts(List<PartIterationDTO> affectedParts) {
        this.affectedParts = affectedParts;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public ACLDTO getAcl() {
        return acl;
    }

    public void setAcl(ACLDTO acl) {
        this.acl = acl;
    }

    public boolean isWritable() {
        return writable;
    }

    public void setWritable(boolean writable) {
        this.writable = writable;
    }
}
