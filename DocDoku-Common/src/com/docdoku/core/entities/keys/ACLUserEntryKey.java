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
package com.docdoku.core.entities.keys;

import java.io.Serializable;

/**
 *
 * @author Florent GARIN
 */
public class ACLUserEntryKey implements Serializable {

    private String principalWorkspaceId;
    private String principalLogin;
    private int aclId;

    public ACLUserEntryKey() {
    }

    public ACLUserEntryKey(int aclId, String principalWorkspaceId, String principalLogin) {
        this.aclId = aclId;
        this.principalWorkspaceId = principalWorkspaceId;
        this.principalLogin = principalLogin;
    }

    public int getAclId() {
        return aclId;
    }

    public String getPrincipalLogin() {
        return principalLogin;
    }

    public String getPrincipalWorkspaceId() {
        return principalWorkspaceId;
    }

    public void setAclId(int aclId) {
        this.aclId = aclId;
    }

    public void setPrincipalLogin(String principalLogin) {
        this.principalLogin = principalLogin;
    }

    public void setPrincipalWorkspaceId(String principalWorkspaceId) {
        this.principalWorkspaceId = principalWorkspaceId;
    }



    @Override
    public String toString() {
        return aclId + "/" + principalWorkspaceId + "-" + principalLogin;
    }

    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof ACLUserEntryKey)) {
            return false;
        }
        ACLUserEntryKey key = (ACLUserEntryKey) pObj;
        return ((key.aclId==aclId) && (key.principalWorkspaceId.equals(principalWorkspaceId)) && (key.principalLogin.equals(principalLogin)));
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = 31 * hash + principalWorkspaceId.hashCode();
        hash = 31 * hash + principalLogin.hashCode();
        hash = 31 * hash + aclId;
        return hash;
    }
}
