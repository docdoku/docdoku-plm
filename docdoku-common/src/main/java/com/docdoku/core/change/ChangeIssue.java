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

/**
 * Represents an identified issue.
 * The issue may result in one or more {@link ChangeRequest}.
 *
 * @author Florent Garin
 * @version 2.0, 06/01/14
 * @since V2.0
 */
@Table(name="CHANGEISSUE")
@Entity
@AssociationOverrides({
        @AssociationOverride(
            name="tags",
            joinTable = @JoinTable(name="CHANGEISSUE_TAG",
                                    inverseJoinColumns={
                                            @JoinColumn(name="TAG_LABEL", referencedColumnName="LABEL"),
                                            @JoinColumn(name="TAG_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
                                    },
                                    joinColumns={
                                            @JoinColumn(name="CHANGEISSUE_ID", referencedColumnName="ID")
                                    }
            )
        ),
        @AssociationOverride(
            name="affectedDocuments",
            joinTable = @JoinTable(name="CHANGEISSUE_AFFECTED_DOCUMENT",
                                    inverseJoinColumns={
                                            @JoinColumn(name="DOCUMENTMASTER_ID", referencedColumnName="DOCUMENTMASTER_ID"),
                                            @JoinColumn(name="DOCUMENTREVISION_VERSION", referencedColumnName="DOCUMENTREVISION_VERSION"),
                                            @JoinColumn(name="DOCUMENTMASTER_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID"),
                                            @JoinColumn(name = "ITERATION", referencedColumnName = "ITERATION")
                                    },
                                    joinColumns={
                                            @JoinColumn(name="CHANGEISSUE_ID", referencedColumnName="ID")
                                    }
            )
        ),
        @AssociationOverride(
            name="affectedParts",
            joinTable = @JoinTable(name="CHANGEISSUE_AFFECTED_PART",
                                    inverseJoinColumns={
                                            @JoinColumn(name="PARTMASTER_PARTNUMBER", referencedColumnName="PARTMASTER_PARTNUMBER"),
                                            @JoinColumn(name="PARTREVISION_VERSION", referencedColumnName="PARTREVISION_VERSION"),
                                            @JoinColumn(name="PARTMASTER_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID"),
                                            @JoinColumn(name = "ITERATION", referencedColumnName = "ITERATION")
                                    },
                                    joinColumns={
                                            @JoinColumn(name="CHANGEISSUE_ID", referencedColumnName="ID")
                                    }
            )
        )
})
@NamedQueries({
        @NamedQuery(name="ChangeIssue.findChangeIssuesByWorkspace",query="SELECT DISTINCT c FROM ChangeIssue c WHERE c.workspace.id = :workspaceId"),
        @NamedQuery(name="ChangeIssue.findByReference", query="SELECT c FROM ChangeIssue c WHERE c.name LIKE :name AND c.workspace.id = :workspaceId")
})
public class ChangeIssue extends ChangeItem {

    /**
     * Identifies the person or organization at the origin of the change, may be null
     * if it is the user who created the object.
     */
    private String initiator;

    public ChangeIssue() {
    }

    public ChangeIssue(Workspace pWorkspace, String pName, User pAuthor) {
        super(pWorkspace, pName, pAuthor);
    }

    public ChangeIssue(String name, Workspace workspace, User author, User assignee, Date creationDate, String description, Priority priority, Category category, String initiator) {
        super(name, workspace, author, assignee, creationDate, description, priority, category);
        this.initiator = initiator;
    }

    public void setInitiator(String initiator) {                                                                        // TODO Find utility of this attribute
        this.initiator = initiator;
    }

    public String getInitiator() {
        return initiator;
    }
}
