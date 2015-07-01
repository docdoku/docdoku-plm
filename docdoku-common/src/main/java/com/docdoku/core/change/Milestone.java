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

import com.docdoku.core.common.Workspace;
import com.docdoku.core.security.ACL;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * A milestone acts like a container for change items.
 * This is useful for associating changes with specific features or project phases.
 *
 * @author Florent Garin
 * @version 2.0, 05/02/14
 * @since V2.0
 */
@Table(name = "MILESTONE")
@javax.persistence.Entity
@NamedQueries({
        @NamedQuery(name = "Milestone.findMilestonesByWorkspace", query = "SELECT DISTINCT m FROM Milestone m WHERE m.workspace.id = :workspaceId"),
        @NamedQuery(name = "Milestone.findMilestonesByTitleAndWorkspace", query = "SELECT DISTINCT m FROM Milestone m WHERE m.workspace.id = :workspaceId AND m.title = :title")
})
public class Milestone implements Serializable {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private int id;

    private String title = "";

    @Temporal(TemporalType.TIMESTAMP)
    private Date dueDate;

    @Lob
    private String description;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Workspace workspace;

    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private ACL acl;

    public Milestone() {
    }

    public Milestone(Workspace pWorkspace, String pTitle) {
        setWorkspace(pWorkspace);
        title = pTitle;
    }

    public Milestone(String title, Date dueDate, String description, Workspace workspace) {
        this.title = title;
        this.dueDate = dueDate;
        this.description = description;
        this.workspace = workspace;
    }

    public int getId() {
        return id;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public String getWorkspaceId() {
        return workspace.getId();
    }

    public void setWorkspace(Workspace pWorkspace) {
        workspace = pWorkspace;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ACL getACL() {
        return acl;
    }

    public void setACL(ACL acl) {
        this.acl = acl;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof Milestone))
            return false;
        Milestone milestone = (Milestone) pObj;

        return milestone.id == id;
    }

    @Override
    public String toString() {
        return title;
    }
}
