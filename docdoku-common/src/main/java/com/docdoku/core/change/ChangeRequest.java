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

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * This class represents a request for a change,
 * which addresses one or more {@link ChangeIssue}.
 * @author Florent Garin
 * @version 2.0, 09/01/14
 * @since V2.0
 */
@Table(name="CHANGEREQUEST")
@Entity
@AssociationOverrides({
        @AssociationOverride(
                name="tags",
                joinTable = @JoinTable(name="CHANGEREQUEST_TAG",
                        inverseJoinColumns={
                                @JoinColumn(name="TAG_LABEL", referencedColumnName="LABEL"),
                                @JoinColumn(name="TAG_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
                        },
                        joinColumns={
                                @JoinColumn(name="CHANGEREQUEST_ID", referencedColumnName="ID")
                        }
                )
        ),
        @AssociationOverride(
                name="affectedDocuments",
                joinTable = @JoinTable(name="CHANGEREQ_AFFECTED_DOCUMENT",
                        inverseJoinColumns={
                                @JoinColumn(name="DOCUMENTMASTER_ID", referencedColumnName="DOCUMENTMASTER_ID"),
                                @JoinColumn(name="DOCUMENTREVISION_VERSION", referencedColumnName="DOCUMENTREVISION_VERSION"),
                                @JoinColumn(name="DOCUMENTMASTER_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID"),
                                @JoinColumn(name = "ITERATION", referencedColumnName = "ITERATION")
                        },
                        joinColumns={
                                @JoinColumn(name="CHANGEREQUEST_ID", referencedColumnName="ID")
                        }
                )
        ),
        @AssociationOverride(
                name="affectedParts",
                joinTable = @JoinTable(name="CHANGEREQ_AFFECTED_PART",
                        inverseJoinColumns={
                                @JoinColumn(name="PARTMASTER_PARTNUMBER", referencedColumnName="PARTMASTER_PARTNUMBER"),
                                @JoinColumn(name="PARTREVISION_VERSION", referencedColumnName="PARTREVISION_VERSION"),
                                @JoinColumn(name="PARTMASTER_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID"),
                                @JoinColumn(name = "ITERATION", referencedColumnName = "ITERATION")
                        },
                        joinColumns={
                                @JoinColumn(name="CHANGEREQUEST_ID", referencedColumnName="ID")
                        }
                )
        )
})
@NamedQueries({
        @NamedQuery(name="ChangeRequest.findChangeRequestsByWorkspace",query="SELECT DISTINCT c FROM ChangeRequest c WHERE c.workspace.id = :workspaceId"),
        @NamedQuery(name="ChangeRequest.countRequestByMilestonesAndWorkspace",query="SELECT COUNT(r) FROM ChangeRequest r WHERE r.workspace.id = :workspaceId AND r.milestone.id = :milestoneId"),
        @NamedQuery(name="ChangeRequest.getRequestByMilestonesAndWorkspace",query="SELECT DISTINCT r FROM ChangeRequest r WHERE r.workspace.id = :workspaceId AND r.milestone.id = :milestoneId"),
        @NamedQuery(name="ChangeRequest.findByReference", query="SELECT c FROM ChangeRequest c WHERE c.name LIKE :name AND c.workspace.id = :workspaceId"),
        @NamedQuery(name="ChangeRequest.findByChangeIssue", query="SELECT c FROM ChangeRequest c WHERE c.workspace.id = :workspaceId AND :changeIssue member of c.addressedChangeIssues")
})
public class ChangeRequest extends ChangeItem {


    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="CHANGEREQUEST_CHANGEISSUE",
            inverseJoinColumns={
                    @JoinColumn(name="CHANGEISSUE_ID", referencedColumnName="ID")
            },
            joinColumns={
                    @JoinColumn(name="CHANGEREQUEST_ID", referencedColumnName="ID")
            })
    private Set<ChangeIssue> addressedChangeIssues=new HashSet<>();

    @ManyToOne(fetch = FetchType.EAGER)
    private Milestone milestone;

    public ChangeRequest() {
    }

    public ChangeRequest(Workspace pWorkspace, String pName, User pAuthor) {
        super(pWorkspace, pName, pAuthor);
    }

    public ChangeRequest(String name, Workspace workspace, User author, User assignee, Date creationDate, String description, Priority priority, Category category, Milestone milestone) {
        super(name, workspace, author, assignee, creationDate, description, priority, category);
        this.milestone = milestone;
    }

    public Milestone getMilestone() {
        return milestone;
    }
    public void setMilestone(Milestone milestone) {
        this.milestone = milestone;
    }

    public Set<ChangeIssue> getAddressedChangeIssues() {
        return addressedChangeIssues;
    }
    public void setAddressedChangeIssues(Set<ChangeIssue> addressedChangeIssues) {
        this.addressedChangeIssues = addressedChangeIssues;
    }

    public int getMilestoneId(){
        return (milestone!=null) ? milestone.getId() : -1;
    }
}
