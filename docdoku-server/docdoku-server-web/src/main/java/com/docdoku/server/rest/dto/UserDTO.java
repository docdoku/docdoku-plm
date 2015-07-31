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

/**
 * @author Florent Garin
 */
public class UserDTO implements Serializable {

    private String workspaceId;
    private String login;
    private String name;
    private String email;
    private String language;

    private WorkspaceMembership membership;

    public UserDTO() {

    }

    public UserDTO(String workspaceId, String login, String name) {
        this.workspaceId = workspaceId;
        this.login = login;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLogin() {
        return login;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public WorkspaceMembership getMembership() {
        return membership;
    }

    public void setMembership(WorkspaceMembership membership) {
        this.membership = membership;
    }


    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

}
