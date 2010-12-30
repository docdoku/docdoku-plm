/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,  
 * but WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
 * GNU General Public License for more details.  
 *  
 * You should have received a copy of the GNU General Public License  
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.  
 */

package com.docdoku.gwt.explorer.shared;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Florent GARIN
 * 
 */
public class UserGroupDTO implements Serializable, Cloneable {

    private String id;
    private String workspaceId;

    private WorkspaceMembership membership;


    private Set<UserDTO> users=new HashSet<UserDTO>();

    public UserGroupDTO(){

    }

    public UserGroupDTO(String workspaceId, String id) {
        this.workspaceId=workspaceId;
        this.id=id;
    }

    public WorkspaceMembership getMembership() {
        return membership;
    }

    public void setMembership(WorkspaceMembership membership) {
        this.membership = membership;
    }

    
    
    public Set<UserDTO> getUsers() {
        return users;
    }

    
    public String getId() {
        return id;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUsers(Set<UserDTO> users) {
        this.users = users;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    
    
}
