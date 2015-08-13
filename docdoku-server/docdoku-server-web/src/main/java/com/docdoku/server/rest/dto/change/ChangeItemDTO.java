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

import com.docdoku.core.change.ChangeItem;
import com.docdoku.server.rest.dto.ACLDTO;
import com.docdoku.server.rest.dto.DocumentIterationDTO;
import com.docdoku.server.rest.dto.PartIterationDTO;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@XmlRootElement
public class ChangeItemDTO implements Serializable {
    private int id;
    private String name;
    private String workspaceId;
    private String author;
    private String authorName;
    private String assignee;
    private String assigneeName;
    private Date creationDate;
    private String description;
    private ChangeItem.Priority priority;
    private ChangeItem.Category category;
    private List<DocumentIterationDTO> affectedDocuments;
    private List<PartIterationDTO> affectedParts;
    private String[] tags;
    @XmlElement(nillable = true)
    private ACLDTO acl;
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

    public ChangeItem.Priority getPriority() {
        return priority;
    }
    public void setPriority(ChangeItem.Priority priority) {
        this.priority = priority;
    }

    public ChangeItem.Category getCategory() {
        return category;
    }
    public void setCategory(ChangeItem.Category category) {
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
