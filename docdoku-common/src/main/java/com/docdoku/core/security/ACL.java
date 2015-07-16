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

import com.docdoku.core.common.User;
import com.docdoku.core.common.UserGroup;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This class can be attached to any entity so that an access control
 * list will be applied.
 * In that way, the default access rights defined at the workspace level will be
 * overridden.
 *
 * @author Florent Garin
 * @version 1.1, 17/07/09
 * @since   V1.1
 */
@Table(name="ACL")
@Entity
@NamedQueries ({
    @NamedQuery(name="ACL.removeUserEntries", query = "DELETE FROM ACLUserEntry a WHERE a.acl.id = :aclId"),
    @NamedQuery(name="ACL.removeUserGroupEntries", query = "DELETE FROM ACLUserGroupEntry a WHERE a.acl.id = :aclId")
})
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

    public enum Permission{
        FORBIDDEN,
        READ_ONLY,
        FULL_ACCESS
    }

    private boolean enabled=true;

    public ACL(){
        
    }

    public void setId(int id) {
        this.id = id;
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
                if(entry.getKey().isMember(user) && !entry.getValue().getPermission().equals(Permission.FORBIDDEN)) {
                    return true;
                }
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
                if(entry.getKey().isMember(user) && entry.getValue().getPermission().equals(Permission.FULL_ACCESS)) {
                    return true;
                }
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

    public Map<User, ACLUserEntry> getUserEntries() {
        return userEntries;
    }

    public void setUserEntries(Map<User, ACLUserEntry> userEntries) {
        this.userEntries = userEntries;
    }

    public Map<UserGroup, ACLUserGroupEntry> getGroupEntries() {
        return groupEntries;
    }

    public void setGroupEntries(Map<UserGroup, ACLUserGroupEntry> groupEntries) {
        this.groupEntries = groupEntries;
    }

    /**
     * perform a deep clone operation
     */
    @Override
    public ACL clone() {
        ACL clone;
        try {
            clone = (ACL) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
        //perform a deep copy
        Map<User,ACLUserEntry> clonedUserEntries = new HashMap<>();
        for (Map.Entry<User,ACLUserEntry> entry : userEntries.entrySet()) {
            ACLUserEntry aclEntry = entry.getValue().clone();
            aclEntry.setACL(clone);
            clonedUserEntries.put(entry.getKey(),aclEntry);
        }
        clone.userEntries = clonedUserEntries;

        //perform a deep copy
        Map<UserGroup,ACLUserGroupEntry> clonedGroupEntries = new HashMap<>();
        for (Map.Entry<UserGroup,ACLUserGroupEntry> entry : groupEntries.entrySet()) {
            ACLUserGroupEntry aclEntry = entry.getValue().clone();
            aclEntry.setACL(clone);
            clonedGroupEntries.put(entry.getKey(),aclEntry);
        }
        clone.groupEntries = clonedGroupEntries;
        return clone;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ACL)) {
            return false;
        }
        ACL acl = (ACL) obj;
        return acl.id == id;
    }

    @Override
    public int hashCode() {
        return id;
    }

}
