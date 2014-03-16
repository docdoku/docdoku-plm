/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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


package com.docdoku.core.change;

import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.meta.Tag;
import com.docdoku.core.product.PartRevision;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Abstract parent class from which change objects are derived.
 *
 * @author Florent Garin
 * @version 2.0, 10/01/14
 * @since V2.0
 */
@MappedSuperclass
public abstract class ChangeItem implements Serializable {


    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    protected int id;

    protected String name;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    protected Workspace workspace;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumns({
            @JoinColumn(name = "AUTHOR_LOGIN", referencedColumnName = "LOGIN"),
            @JoinColumn(name = "AUTHOR_WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID")
    })
    protected User author;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumns({
            @JoinColumn(name="ASSIGNEE_LOGIN", referencedColumnName="LOGIN"),
            @JoinColumn(name="ASSIGNEE_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
    })
    protected User assignee;

    @Temporal(TemporalType.TIMESTAMP)
    protected java.util.Date creationDate;

    @Lob
    protected String description;

    public enum Priority {
        LOW, HIGH, MEDIUM, EMERGENCY
    }

    /**
     * An adaptive change maintains functionality for a different platform or
     * environment.
     * A corrective change corrects a defect.
     * A perfective change adds functionality.
     * A preventive change improves maintainability.
     */
    protected Category category;

    public enum Category {
        ADAPTIVE, CORRECTIVE, PERFECTIVE, PREVENTIVE, OTHER
    }


    @ManyToMany
    @JoinTable(name="CHANGEITEM_AFFECTED_PART",
            inverseJoinColumns={
                    @JoinColumn(name="PARTMASTER_PARTNUMBER", referencedColumnName="PARTMASTER_PARTNUMBER"),
                    @JoinColumn(name="PARTREVISION_VERSION", referencedColumnName="VERSION"),
                    @JoinColumn(name="PARTMASTER_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
            },
            joinColumns={
                    @JoinColumn(name="CHANGEITEM_ID", referencedColumnName="ID")
            })
    private Set<PartRevision> affectedParts = new HashSet<>();

    @ManyToMany
    @JoinTable(name="CHANGEITEM_AFFECTED_DOCUMENT",
            inverseJoinColumns={
                    @JoinColumn(name="DOCUMENTMASTER_ID", referencedColumnName="DOCUMENTMASTER_ID"),
                    @JoinColumn(name="DOCUMENTREVISION_VERSION", referencedColumnName="VERSION"),
                    @JoinColumn(name="DOCUMENTMASTER_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
            },
            joinColumns={
                    @JoinColumn(name="CHANGEITEM_ID", referencedColumnName="ID")
            })
    private Set<DocumentRevision> affectedDocuments = new HashSet<>();


    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(name="CHANGEITEM_TAG",
            inverseJoinColumns={
                    @JoinColumn(name="TAG_LABEL", referencedColumnName="LABEL"),
                    @JoinColumn(name="TAG_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
            },
            joinColumns={
                    @JoinColumn(name="CHANGEITEM_ID", referencedColumnName="ID")
            })
    private Set<Tag> tags=new HashSet<Tag>();

    public ChangeItem(Workspace pWorkspace, String pName, User pAuthor) {
        workspace=pWorkspace;
        name=pName;
        author=pAuthor;
    }

    public ChangeItem() {
    }

    public int getId() {
        return id;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public User getAssignee() {
        return assignee;
    }

    public void setAssignee(User assignee) {
        this.assignee = assignee;
    }

    public Set<DocumentRevision> getAffectedDocuments() {
        return affectedDocuments;
    }

    public void setAffectedDocuments(Set<DocumentRevision> affectedDocuments) {
        this.affectedDocuments = affectedDocuments;
    }

    public Set<PartRevision> getAffectedParts() {
        return affectedParts;
    }

    public void setAffectedParts(Set<PartRevision> affectedParts) {
        this.affectedParts = affectedParts;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWorkspaceId() {
        return workspace == null ? "" : workspace.getId();
    }
}
