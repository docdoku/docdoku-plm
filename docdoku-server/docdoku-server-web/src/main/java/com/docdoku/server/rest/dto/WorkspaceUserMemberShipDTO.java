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
