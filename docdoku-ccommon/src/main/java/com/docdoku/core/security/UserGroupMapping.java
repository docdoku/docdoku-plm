/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,  
 * but WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
 * GNU General Public License for more details.  
 *  
 * You should have received a copy of the GNU General Public License  
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.  
 */

package com.docdoku.core.security;

/**
 * Useful class for adding users to a security group. 
 * Actually, all users belong to, and only to, the "users" group.
 * 
 * @author Florent Garin
 * @version 1.0, 02/06/08
 * @since   V1.0
 */
@javax.persistence.Entity
public class UserGroupMapping implements java.io.Serializable {
    
    @javax.persistence.Id
    private String login="";
    
    private String groupName;
    
    public final static String REGULAR_USER_ROLE_ID="users";
    
    public UserGroupMapping() {
    }
    
    public UserGroupMapping(String pLogin) {
        this(pLogin,REGULAR_USER_ROLE_ID);
    }
    
    public UserGroupMapping(String pLogin, String pRole) {
        login=pLogin;
        groupName=pRole;
    }
}
