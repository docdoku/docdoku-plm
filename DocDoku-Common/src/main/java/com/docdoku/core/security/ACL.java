/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010 DocDoku SARL
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

import com.docdoku.core.common.User;
import com.docdoku.core.common.UserGroup;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;

/**
 * This class can be attached to a
 * <a href="MasterDocument.html">MasterDocument</a> so that an access control
 * list will be applied.
 * In that way, the default access rights defined at the workspace level will be
 * overridden.
 *
 * @author Florent GARIN
 * @version 1.1, 17/07/09
 * @since   V1.1
 */
@Entity
public class ACL implements Serializable, Cloneable{


    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Id
    private int id;


    @OneToMany(cascade=CascadeType.ALL, mappedBy="acl", fetch=FetchType.EAGER)
    @MapKey(name="principal")
    private Map<User,ACLUserEntry> userEntries=new HashMap<User,ACLUserEntry>();

    @OneToMany(cascade=CascadeType.ALL, mappedBy="acl", fetch=FetchType.EAGER)
    @MapKey(name="principal")
    private Map<UserGroup,ACLUserGroupEntry> groupEntries=new HashMap<UserGroup,ACLUserGroupEntry>();

    public enum Permission{FORBIDDEN, READ_ONLY, FULL_ACCESS}

    private boolean enabled=true;

    public ACL(){
        
    }

    public int getId() {
        return id;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean hasReadAccess(User user){
        ACLUserEntry userAccess=userEntries.get(user);
        if(userAccess!=null)
            return !userAccess.getPermission().equals(Permission.FORBIDDEN);
        else{
            for(Map.Entry<UserGroup, ACLUserGroupEntry> entry:groupEntries.entrySet()){
                if(entry.getKey().isMember(user))
                    if(!entry.getValue().getPermission().equals(Permission.FORBIDDEN))
                        return true;
            }
        }
        return false;
    }

    public boolean hasWriteAccess(User user){
        ACLUserEntry userAccess=userEntries.get(user);
        if(userAccess!=null)
            return userAccess.getPermission().equals(Permission.FULL_ACCESS);
        else{
            for(Map.Entry<UserGroup, ACLUserGroupEntry> entry:groupEntries.entrySet()){
                if(entry.getKey().isMember(user))
                    if(entry.getValue().getPermission().equals(Permission.FULL_ACCESS))
                        return true;
            }
        }
        return false;
    }

    
    public void addEntry(User user, Permission perm){
        userEntries.put(user, new ACLUserEntry(this,user,perm));
    }

    public void addEntry(UserGroup group, Permission perm){
        groupEntries.put(group, new ACLUserGroupEntry(this,group,perm));
    }

    public void removeEntry(User user){
        userEntries.remove(user);
    }

    public void removeEntry(UserGroup group){
        groupEntries.remove(group);
    }

    /**
     * perform a deep clone operation
     */
    @Override
    public ACL clone() {
        ACL clone = null;
        try {
            clone = (ACL) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
        //perform a deep copy
        Map<User,ACLUserEntry> clonedUserEntries = new HashMap<User,ACLUserEntry>();
        for (Map.Entry<User,ACLUserEntry> entry : clonedUserEntries.entrySet()) {
            ACLUserEntry aclEntry = entry.getValue().clone();
            aclEntry.setACL(clone);
            clonedUserEntries.put(entry.getKey(),aclEntry);
        }
        clone.userEntries = clonedUserEntries;

        //perform a deep copy
        Map<UserGroup,ACLUserGroupEntry> clonedGroupEntries = new HashMap<UserGroup,ACLUserGroupEntry>();
        for (Map.Entry<UserGroup,ACLUserGroupEntry> entry : clonedGroupEntries.entrySet()) {
            ACLUserGroupEntry aclEntry = entry.getValue().clone();
            aclEntry.setACL(clone);
            clonedGroupEntries.put(entry.getKey(),aclEntry);
        }
        clone.groupEntries = clonedGroupEntries;
        return clone;
    }

}
