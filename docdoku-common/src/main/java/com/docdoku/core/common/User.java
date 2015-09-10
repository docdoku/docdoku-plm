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

package com.docdoku.core.common;

import javax.persistence.FetchType;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * This class represents a user in the context of a specific workspace.
 * 
 * @author Florent Garin
 * @version 1.0, 02/06/08
 * @since V1.0
 */
@Table(name="USERDATA")
@javax.persistence.IdClass(com.docdoku.core.common.UserKey.class)
@javax.persistence.Entity
public class User implements Serializable, Cloneable {

    private String name;
    private String email;
    private String language;
    @javax.persistence.Column(name = "WORKSPACE_ID", length = 100, nullable = false, insertable = false, updatable = false)
    @javax.persistence.Id
    private String workspaceId = "";
    @javax.persistence.Id
    private String login = "";
    @javax.persistence.ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Workspace workspace;

    public User() {
    }

    public User(Workspace pWorkspace, String pLogin) {
        this(pWorkspace, pLogin, null, null, null);
    }

    public User(Workspace pWorkspace, String pLogin, String pName, String pEmail, String pLanguage) {
        setWorkspace(pWorkspace);
        login = pLogin;
        name = pName;
        email = pEmail;
        language = pLanguage;
    }

    public User(String pLanguage){
        language = pLanguage;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getName() {
        return name;
    }

    public void setName(String pName) {
        name = pName;
    }

    public void setEmail(String pEmail) {
        email = pEmail;
    }

    public String getEmail() {
        return email;
    }

    public void setLanguage(String pLanguage) {
        language = pLanguage;
    }

    public String getLanguage() {
        return language;
    }

    public boolean isAdministrator() {
        return login.equals(workspace.getAdmin().getLogin());
    }

    public UserKey getKey() {
        return new UserKey(workspaceId, login);
    }

    public void setWorkspace(Workspace pWorkspace) {
        workspace = pWorkspace;
        workspaceId = workspace.getId();
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    @Override
    public String toString() {
        return login;
    }

    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof User)) {
            return false;
        }
        User user = (User) pObj;
        return user.login.equals(login) && user.workspaceId.equals(workspaceId);
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = 31 * hash + workspaceId.hashCode();
        hash = 31 * hash + login.hashCode();
        return hash;
    }

    @Override
    public User clone() {
        User clone = null;
        try {
            clone = (User) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
        return clone;
    }
}