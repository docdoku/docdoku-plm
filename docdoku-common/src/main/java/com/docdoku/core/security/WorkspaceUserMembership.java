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

import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Class that holds information on how a specific user belongs to a workspace.
 *   
 * 
 * @author Florent Garin
 * @version 1.1, 08/07/09
 * @since   V1.1
 */
@Table(name="WORKSPACEUSERMEMBERSHIP")
@javax.persistence.IdClass(com.docdoku.core.security.WorkspaceUserMembershipKey.class)
@javax.persistence.Entity
@NamedQueries({
        @NamedQuery(name="findCommonWorkspacesForGivenUsers", query="SELECT w FROM Workspace w WHERE EXISTS (SELECT u1.workspace FROM User u1, User u2 WHERE u1.workspace = u2.workspace AND u1.login = :userLogin1 AND u2.login = :userLogin2 AND u1.workspace IS NOT NULL)")
})
public class WorkspaceUserMembership implements Serializable {

    @javax.persistence.Column(name = "WORKSPACE_ID", length=100, nullable = false, insertable = false, updatable = false)
    @javax.persistence.Id
    private String workspaceId = "";
    @javax.persistence.ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Workspace workspace;
    @javax.persistence.Column(name = "MEMBER_WORKSPACE_ID", length=100, nullable = false, insertable = false, updatable = false)
    @javax.persistence.Id
    private String memberWorkspaceId = "";
    @javax.persistence.Column(name = "MEMBER_LOGIN", nullable = false, insertable = false, updatable = false)
    @javax.persistence.Id
    private String memberLogin = "";
    
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name = "MEMBER_LOGIN", referencedColumnName = "LOGIN"),
        @JoinColumn(name = "MEMBER_WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID")
    })
    private User member;

    private boolean readOnly;

    public WorkspaceUserMembership() {
    }

    public WorkspaceUserMembership(Workspace pWorkspace, User pMember) {
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

    public String getWorkspaceId() {
        return workspaceId;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setMember(User pMember) {
        this.member = pMember;
        this.memberLogin=member.getLogin();
        this.memberWorkspaceId=member.getWorkspaceId();
    }

    public String getMemberLogin() {
        return memberLogin;
    }

    
    public User getMember() {
        return member;
    }

    public String getMemberWorkspaceId() {
        return memberWorkspaceId;
    }
}
