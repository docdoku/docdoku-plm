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

package com.docdoku.server.rest.dto;


import java.io.Serializable;

public class WorkspaceUserMemberShipDTO implements Serializable {

    private String workspaceId;
    private UserDTO member;
    private boolean readOnly;

    public WorkspaceUserMemberShipDTO() {
    }

    public WorkspaceUserMemberShipDTO(String workspaceId, UserDTO member, boolean readOnly) {
        this.workspaceId = workspaceId;
        this.member = member;
        this.readOnly = readOnly;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public UserDTO getMember() {
        return member;
    }

    public void setMember(UserDTO member) {
        this.member = member;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
}
