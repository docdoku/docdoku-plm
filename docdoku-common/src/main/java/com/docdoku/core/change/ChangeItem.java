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


package com.docdoku.core.change;

import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.meta.Tag;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.security.ACL;

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

    @OneToOne(orphanRemoval = true, cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    protected ACL acl;

    @Temporal(TemporalType.TIMESTAMP)
    protected java.util.Date creationDate;

    @Lob
    protected String description;

    protected Priority priority;

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
    protected Set<PartIteration> affectedParts = new HashSet<>();

    @ManyToMany
    protected Set<DocumentIteration> affectedDocuments = new HashSet<>();

    @ManyToMany(fetch=FetchType.EAGER)
    protected Set<Tag> tags=new HashSet<>();

    public ChangeItem(Workspace pWorkspace, String pName, User pAuthor) {
        workspace=pWorkspace;
        name=pName;
        author=pAuthor;
    }

    protected ChangeItem(String name, Workspace workspace, User author, User assignee, Date creationDate, String description, Priority priority, Category category) {
        this.name = name;
        this.workspace = workspace;
        this.author = author;
        this.assignee = assignee;
        this.creationDate = creationDate;
        this.description = description;
        this.priority = priority;
        this.category = category;
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

    public Priority getPriority() {
        return priority;
    }
    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Date getCreationDate() {
        return creationDate;
    }
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public ACL getACL() {
        return acl;
    }
    public void setACL(ACL acl) {
        this.acl = acl;
    }

    public Set<Tag> getTags() {
        return tags;
    }
    public void setTags(Set<Tag> pTags) {
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

    public String getAuthorName() {
        return author.getName();
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

    public String getAssigneeName() {
        return assignee == null ? null : assignee.getName();
    }

    public Set<DocumentIteration> getAffectedDocuments() {
        return affectedDocuments;
    }
    public void setAffectedDocuments(Set<DocumentIteration> affectedDocuments) {
        this.affectedDocuments = affectedDocuments;
    }

    public Set<PartIteration> getAffectedParts() {
        return affectedParts;
    }
    public void setAffectedParts(Set<PartIteration> affectedParts) {
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