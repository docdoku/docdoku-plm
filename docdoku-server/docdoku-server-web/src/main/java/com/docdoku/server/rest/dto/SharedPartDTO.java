package com.docdoku.server.rest.dto;

import java.io.Serializable;
import java.util.Date;

public class SharedPartDTO implements Serializable {

    private String uuid;
    private String workspaceId;
    private String password;
    private Date expireDate;
    private String userLogin;

    private String partMasterNumber;
    private String partMasterVersion;

    public SharedPartDTO(){}

    public SharedPartDTO(String uuid, String workspaceId, String password, Date expireDate, String userLogin) {
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

    public String getPartMasterNumber() {
        return partMasterNumber;
    }

    public void setPartMasterNumber(String partMasterNumber) {
        this.partMasterNumber = partMasterNumber;
    }

    public String getPartMasterVersion() {
        return partMasterVersion;
    }

    public void setPartMasterVersion(String partMasterVersion) {
        this.partMasterVersion = partMasterVersion;
    }
}
