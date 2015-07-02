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

import java.io.Serializable;

/**
 *
 * @author Florent Garin
 */
public class WorkspaceUserMembershipKey implements Serializable {

    private String memberWorkspaceId;
    private String memberLogin;
    private String workspaceId;

    public WorkspaceUserMembershipKey() {
    }

    public WorkspaceUserMembershipKey(String pWorkspaceId, String pMemberWorkspaceId, String pMemberLogin) {
        workspaceId = pWorkspaceId;
        memberWorkspaceId = pMemberWorkspaceId;
        memberLogin = pMemberLogin;
    }

    public String getMemberLogin() {
        return memberLogin;
    }

    public String getMemberWorkspaceId() {
        return memberWorkspaceId;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setMemberLogin(String memberLogin) {
        this.memberLogin = memberLogin;
    }

    public void setMemberWorkspaceId(String memberWorkspaceId) {
        this.memberWorkspaceId = memberWorkspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    

    @Override
    public String toString() {
        return workspaceId + "/" + memberWorkspaceId + "-" + memberLogin;
    }

    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof WorkspaceUserMembershipKey)) {
            return false;
        }
        WorkspaceUserMembershipKey key = (WorkspaceUserMembershipKey) pObj;
        return key.workspaceId.equals(workspaceId) && key.memberWorkspaceId.equals(memberWorkspaceId) && key.memberLogin.equals(memberLogin);
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = 31 * hash + workspaceId.hashCode();
        hash = 31 * hash + memberWorkspaceId.hashCode();
        hash = 31 * hash + memberLogin.hashCode();

        return hash;
    }
}
