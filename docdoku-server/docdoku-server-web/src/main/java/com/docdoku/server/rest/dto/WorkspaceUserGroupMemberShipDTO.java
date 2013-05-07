package com.docdoku.server.rest.dto;


import java.io.Serializable;

public class WorkspaceUserGroupMemberShipDTO implements Serializable{

    private String workspaceId;
    private String memberId;
    private boolean readOnly;

    public WorkspaceUserGroupMemberShipDTO() {
    }

    public WorkspaceUserGroupMemberShipDTO(String workspaceId, String memberId, boolean readOnly) {
        this.workspaceId = workspaceId;
        this.memberId = memberId;
        this.readOnly = readOnly;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
}
