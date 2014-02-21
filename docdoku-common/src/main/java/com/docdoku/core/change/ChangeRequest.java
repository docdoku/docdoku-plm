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

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * This class represents a request for a change,
 * which addresses one or more <a href="ChangeIssue.html">ChangeIssue</a>.
 * @author Florent Garin
 * @version 2.0, 09/01/14
 * @since   V2.0
 */
@Table(name="CHANGEREQUEST")
@Entity
@NamedQueries({
        @NamedQuery(name="ChangeRequest.findChangeRequestsByWorkspace",query="SELECT DISTINCT c FROM ChangeRequest c WHERE c.workspace.id = :workspaceId"),
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

    private Priority priority;

    public ChangeRequest() {
    }

    public ChangeRequest(Workspace pWorkspace, String pName, User pAuthor) {
        super(pWorkspace, pName, pAuthor);
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Milestone getMilestone() {
        return milestone;
    }

    public void setMilestone(Milestone milestone) {
        this.milestone = milestone;
    }
}
