package com.docdoku.server.rest.dto;

/**
 * Created by laurentlevan on 29/06/16.
 */
public class LightPartRevisionDTO {

    private String workspaceId;
    private String number;
    private String version;

    public LightPartRevisionDTO() {
    }

    public LightPartRevisionDTO(String workspaceId, String number, String version){
        this.workspaceId = workspaceId;
        this.number = number;
        this.version = version;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
