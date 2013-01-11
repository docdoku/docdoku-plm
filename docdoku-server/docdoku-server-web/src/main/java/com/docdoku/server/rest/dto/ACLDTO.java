/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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

package com.docdoku.server.rest.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class ACLDTO implements Serializable{

    private Map<String,Permission> userEntries=new HashMap<String,Permission>();
    private Map<String,Permission> groupEntries=new HashMap<String,Permission>();

    public enum Permission{FORBIDDEN, READ_ONLY, FULL_ACCESS}


    public ACLDTO(){
        
    }

    
    
    public void addUserEntry(String login, Permission perm){
        userEntries.put(login, perm);
    }

    public void addGroupEntry(String groupId, Permission perm){
        groupEntries.put(groupId, perm);
    }

    public void removeUserEntry(String login){
        userEntries.remove(login);
    }

    public void removeGroupEntry(String groupId){
        groupEntries.remove(groupId);
    }

    public Map<String, Permission> getGroupEntries() {
        return groupEntries;
    }

    public Map<String, Permission> getUserEntries() {
        return userEntries;
    }

    
}
