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

package com.docdoku.core.security;

import javax.persistence.Table;

/**
 * Useful class for adding users to a security group. 
 * Actually, all users belong to, and only to, the "users" group.
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
    public static final String GUEST_PROXY_ROLE_ID ="guest-proxy";

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
