package com.docdoku.server.rest.dto;

import java.io.Serializable;
import java.util.Date;

public class SharedDocumentDTO implements Serializable {

    private String uuid;
    private String workspaceId;
    private String password;
    private Date expireDate;
    private String userLogin;

    private String documentMasterId;
    private String documentMasterVersion;

    public SharedDocumentDTO(){}

    public SharedDocumentDTO(String uuid, String workspaceId, String password, Date expireDate, String userLogin) {
        this.uuid = uuid;
        this.workspaceId = workspaceId;
        this.password = password;
        this.expireDate = expireDate;
        this.userLogin = userLogin;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Date expireDate) {
        this.expireDate = expireDate;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public String getDocumentMasterId() {
        return documentMasterId;
    }

    public void setDocumentMasterId(String documentMasterId) {
        this.documentMasterId = documentMasterId;
    }

    public String getDocumentMasterVersion() {
        return documentMasterVersion;
    }

    public void setDocumentMasterVersion(String documentMasterVersion) {
        this.documentMasterVersion = documentMasterVersion;
    }
}
