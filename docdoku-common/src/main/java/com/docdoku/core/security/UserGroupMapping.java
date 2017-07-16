/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
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

import com.docdoku.core.common.UserGroup;

import javax.persistence.Table;

/**
 * Useful class for managing security group as used by application servers.
 * This class has nothing to do with {@link UserGroup} and the context-aware security
 * model where a user may be granted full access on a given object and not be allowed
 * to see another one. This class just defines static groups which will lead to one of
 * the 3 global roles supported by the application: "users", "admin" and "guest".
 *
 * All regular users belong to, and only to, the "users" group.
 * "admin" is for the superuser account who can perform administration tasks.
 * "guest" role is for non logged users who access items that have been published.
 *
 * @author Florent Garin
 * @version 1.0, 02/06/08
 * @since   V1.0
 */
@Table(name="USERGROUPMAPPING")
@javax.persistence.Entity
public class UserGroupMapping implements java.io.Serializable {

    @javax.persistence.Id
    private String login="";
    private String groupName;
    
    public static final String REGULAR_USER_ROLE_ID="users";
    public static final String ADMIN_ROLE_ID ="admin";
    public static final String GUEST_ROLE_ID ="guest";

    public UserGroupMapping() {
    }
    
    public UserGroupMapping(String pLogin) {
        this(pLogin,REGULAR_USER_ROLE_ID);
    }
    
    public UserGroupMapping(String pLogin, String pRole) {
        login=pLogin;
        groupName=pRole;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
