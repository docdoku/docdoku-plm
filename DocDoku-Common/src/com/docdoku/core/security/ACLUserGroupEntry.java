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
package com.docdoku.core.security;

import com.docdoku.core.security.ACL.Permission;
import com.docdoku.core.common.UserGroup;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;

/**
 * Class that belongs to the ACL classe and makes the mapping between a group
 * and a associated permission.
 *
 * @author Florent GARIN
 * @version 1.1, 17/07/09
 * @since   V1.1
 */
@Entity
@javax.persistence.IdClass(com.docdoku.core.security.ACLUserGroupEntryKey.class)
public class ACLUserGroupEntry implements Serializable, Cloneable {

    @javax.persistence.Column(name = "ACL_ID", nullable = false, insertable = false, updatable = false)
    @javax.persistence.Id
    private int aclId;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private ACL acl;

    @javax.persistence.Column(name = "PRINCIPAL_ID", length=50, nullable = false, insertable = false, updatable = false)
    @javax.persistence.Id
    private String principalId = "";

    @javax.persistence.Column(name = "PRINCIPAL_WORKSPACE_ID", length=50, nullable = false, insertable = false, updatable = false)
    @javax.persistence.Id
    private String principalWorkspaceId="";

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name = "PRINCIPAL_ID", referencedColumnName = "ID"),
        @JoinColumn(name = "PRINCIPAL_WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID")
    })
    private UserGroup principal;

    private Permission permission;

    public ACLUserGroupEntry(){

    }



    public ACLUserGroupEntry(ACL acl, UserGroup principal, Permission permission) {
        setACL(acl);
        setPrincipal(principal);
        setPermission(permission);
    }

    public void setACL(ACL pACL) {
        this.acl = pACL;
        this.aclId=pACL.getId();
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    public void setPrincipal(UserGroup pPrincipal) {
        this.principal = pPrincipal;
        this.principalId=pPrincipal.getId();
        this.principalWorkspaceId=pPrincipal.getWorkspaceId();
    }

    public Permission getPermission() {
        return permission;
    }

    public UserGroup getPrincipal() {
        return principal;
    }

    public String getPrincipalId() {
        return principalId;
    }

    public String getPrincipalWorkspaceId() {
        return principalWorkspaceId;
    }

    @Override
    public ACLUserGroupEntry clone() {
        ACLUserGroupEntry clone = null;
        try {
            clone = (ACLUserGroupEntry) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
        return clone;
    }
    
}
