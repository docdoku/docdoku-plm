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

package com.docdoku.core.workflow;


import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;

import javax.persistence.*;
import java.io.Serializable;

/**
 * This class is the model used to create roles in a workspace
 *
 * @author Morgan Guimard
 */
@Table(name="ROLE")
@IdClass(RoleKey.class)
@javax.persistence.Entity
@NamedQueries({
        @NamedQuery(name="Role.findByWorkspace", query="SELECT r FROM Role r WHERE r.workspace.id = :workspaceId")
})
public class Role implements Serializable {

    @Column(length = 100)
    @Id
    private String name = "";

    @Id
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Workspace workspace;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumns({
            @JoinColumn(name="DEFAULT_ASSIGNEE_LOGIN", referencedColumnName="LOGIN"),
            @JoinColumn(name="DEFAULT_ASSIGNEE_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
    })
    private User defaultAssignee;

    public Role(){
    }

    public Role(String name, Workspace workspace){
        this.name = name;
        this.workspace = workspace;
    }

    public Role(String name, Workspace workspace, User defaultAssignee){
        this.name = name;
        this.workspace = workspace;
        this.defaultAssignee = defaultAssignee;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public User getDefaultAssignee() {
        return defaultAssignee;
    }

    public void setDefaultAssignee(User defaultAssignee) {
        this.defaultAssignee = defaultAssignee;
    }
}
