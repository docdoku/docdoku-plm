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
import com.docdoku.core.security.ACL.Permission;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;

/**
 * Class that belongs to the ACL classe and makes the mapping between a user
 * and a associated permission.
 *
 * @author Florent Garin
 * @version 1.1, 17/07/09
 * @since   V1.1
 */
@Table(name="ACLUSERENTRY")
@Entity
@IdClass(com.docdoku.core.security.ACLUserEntryKey.class)
public class ACLUserEntry implements Serializable, Cloneable {

    @Id
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name="ACL_ID", referencedColumnName="ID")
    protected ACL acl;

    @Id
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name = "PRINCIPAL_LOGIN", referencedColumnName = "LOGIN"),
        @JoinColumn(name = "PRINCIPAL_WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID")
    })
    private User principal;

    private Permission permission;

    public ACLUserEntry() {
    }

    public ACLUserEntry(ACL acl, User principal, Permission permission) {
        setACL(acl);
        setPrincipal(principal);
        setPermission(permission);
    }

    public void setACL(ACL pACL) {
        this.acl = pACL;
    }

    @XmlTransient
    public ACL getAcl() {
        return acl;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    public void setPrincipal(User pPrincipal) {
        this.principal = pPrincipal;
    }

    public Permission getPermission() {
        return permission;
    }

    public User getPrincipal() {
        return principal;
    }

    public String getPrincipalLogin() {
        return principal.getLogin();
    }

    @Override
    public ACLUserEntry clone() {
        ACLUserEntry clone = null;
        try {
            clone = (ACLUserEntry) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
        return clone;
    }
    
}
