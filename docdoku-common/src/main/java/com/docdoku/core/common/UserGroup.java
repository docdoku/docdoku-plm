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

package com.docdoku.core.common;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

/**
 * Class which gathers users in a workspace context.
 * 
 * @author Florent Garin
 * @version 1.1, 8/07/09
 * @since   V1.1
 */
@Table(name="USERGROUP")
@javax.persistence.IdClass(com.docdoku.core.common.UserGroupKey.class)
@javax.persistence.Entity
public class UserGroup implements Serializable, Cloneable {

    @Column(length=50)
    @javax.persistence.Id
    private String id="";

    @javax.persistence.Column(name = "WORKSPACE_ID", length=50, nullable = false, insertable = false, updatable = false)
    @javax.persistence.Id
    private String workspaceId="";

    @javax.persistence.ManyToOne(optional=false, fetch=FetchType.EAGER)
    private Workspace workspace;

    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(name="USERGROUP_USER",
    inverseJoinColumns={
        @JoinColumn(name="USER_LOGIN", referencedColumnName="LOGIN"),
       @JoinColumn(name="USER_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
    },
    joinColumns={
        @JoinColumn(name="USERGROUP_ID", referencedColumnName="ID"),
        @JoinColumn(name="USERGROUP_ID_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
    })
    private Set<User> users=new HashSet<User>();

    public UserGroup(){

    }

    public UserGroup(Workspace pWorkspace, String pId) {
        setWorkspace(pWorkspace);
        id=pId;
    }

    public void setWorkspace(Workspace pWorkspace){
        workspace=pWorkspace;
        workspaceId=workspace.getId();
    }
    
    public Set<User> getUsers() {
        return users;
    }

    public boolean addUser(User pUser){
        return users.add(pUser);
    }

    public boolean removeUser(User pUser){
        return users.remove(pUser);
    }

    public boolean isMember(User user){
        return users.contains(user);
    }
    public String getId() {
        return id;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    
}
