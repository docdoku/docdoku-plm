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
import com.docdoku.core.common.UserGroup;
import com.docdoku.core.common.Workspace;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(name="ROLE_USER",
    inverseJoinColumns={
            @JoinColumn(name="USER_LOGIN", referencedColumnName="LOGIN"),
            @JoinColumn(name="USER_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
    },
    joinColumns={
            @JoinColumn(name="ROLE_NAME", referencedColumnName="NAME"),
            @JoinColumn(name="ROLE_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID"),
    })
    private Set<User> defaultAssignedUsers=new HashSet<>();

    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(name="ROLE_USERGROUP",
    inverseJoinColumns={
            @JoinColumn(name="USERGROUP_ID", referencedColumnName="ID"),
            @JoinColumn(name="USERGROUP_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
    },
    joinColumns={
            @JoinColumn(name="ROLE_NAME", referencedColumnName="NAME"),
            @JoinColumn(name="ROLE_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
    })
    private Set<UserGroup> defaultAssignedGroups=new HashSet<>();

    public Role(){
    }

    public Role(String name, Workspace workspace){
        this.name = name;
        this.workspace = workspace;
    }

    public Role(String name, Workspace workspace, Set<User> defaultAssignedUsers, Set<UserGroup> defaultAssignedGroups){
        this.name = name;
        this.workspace = workspace;
        this.defaultAssignedUsers = defaultAssignedUsers;
        this.defaultAssignedGroups = defaultAssignedGroups;
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

    public Set<User> getDefaultAssignedUsers() {
        return defaultAssignedUsers;
    }

    public void setDefaultAssignedUsers(Set<User> defaultAssignedUsers) {
        this.defaultAssignedUsers = defaultAssignedUsers;
    }

    public Set<UserGroup> getDefaultAssignedGroups() {
        return defaultAssignedGroups;
    }

    public void setDefaultAssignedGroups(Set<UserGroup> defaultAssignedGroups) {
        this.defaultAssignedGroups = defaultAssignedGroups;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Role role = (Role) o;

        if (name != null ? !name.equals(role.name) : role.name != null) return false;
        return !(workspace != null ? !workspace.equals(role.workspace) : role.workspace != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (workspace != null ? workspace.hashCode() : 0);
        return result;
    }
}
