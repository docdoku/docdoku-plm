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
package com.docdoku.core.security;

import com.docdoku.core.common.UserGroup;
import com.docdoku.core.common.Workspace;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Class that holds information on how a specific user group belongs to a
 * workspace.
 * 
 * @author Florent Garin
 * @version 1.1, 08/07/09
 * @since   V1.1
 */
@Table(name="WORKSPACEUSERGROUPMEMBERSHIP")
@javax.persistence.IdClass(com.docdoku.core.security.WorkspaceUserGroupMembershipKey.class)
@javax.persistence.Entity
public class WorkspaceUserGroupMembership implements Serializable {

    @javax.persistence.Column(name = "WORKSPACE_ID", length=100, nullable = false, insertable = false, updatable = false)
    @javax.persistence.Id
    private String workspaceId = "";
    @javax.persistence.ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Workspace workspace;
    @javax.persistence.Column(name = "MEMBER_WORKSPACE_ID", length=100, nullable = false, insertable = false, updatable = false)
    @javax.persistence.Id
    private String memberWorkspaceId = "";
    @javax.persistence.Column(name = "MEMBER_ID", nullable = false, insertable = false, updatable = false)
    @javax.persistence.Id
    private String memberId = "";
    
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name = "MEMBER_ID", referencedColumnName = "ID"),
        @JoinColumn(name = "MEMBER_WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID")
    })
    private UserGroup member;

    private boolean readOnly;

    public WorkspaceUserGroupMembership() {
    }

    public WorkspaceUserGroupMembership(Workspace pWorkspace, UserGroup pMember) {
        setWorkspace(pWorkspace);
        setMember(pMember);
    }
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setWorkspace(Workspace pWorkspace) {
        workspace = pWorkspace;
        workspaceId = workspace.getId();
    }

    public void setMember(UserGroup pMember) {
        this.member = pMember;
        this.memberId=member.getId();
        this.memberWorkspaceId=member.getWorkspaceId();
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public UserGroup getMember() {
        return member;
    }

    public String getMemberId() {
        return memberId;
    }

    public String getMemberWorkspaceId() {
        return memberWorkspaceId;
    }
}
