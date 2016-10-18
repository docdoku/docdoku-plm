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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Morgan Guimard
 */
@XmlRootElement
@ApiModel(value = "SharedPartDTO", description = "This class is a representation of a {@link com.docdoku.core.sharing.SharedPart} entity")
public class SharedPartDTO implements Serializable {

    @ApiModelProperty(value = "Shared part token")
    private String uuid;

    @ApiModelProperty(value = "Workspace id")
    private String workspaceId;

    @ApiModelProperty(value = "Shared part password")
    private String password;

    @ApiModelProperty(value = "Shared part expire date")
    private Date expireDate;

    @ApiModelProperty(value = "Shared part login creator")
    private String userLogin;

    @ApiModelProperty(value = "Shared part number")
    private String partMasterNumber;

    @ApiModelProperty(value = "Shared part version")
    private String partMasterVersion;

    public SharedPartDTO() {
    }

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
