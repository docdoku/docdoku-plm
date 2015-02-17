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

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * The Organization class represents an entity which groups users
 * or more precisely {@link Account}
 * as its validity spreads across workspaces.
 *
 * @author Florent Garin
 * @version 2.0, 30/05/14
 * @since V2.0
 */
@Table(name = "ORGANIZATION")
@javax.persistence.Entity
public class Organization implements Serializable {

    @Column(length = 100)
    @javax.persistence.Id
    private String name = "";

    @javax.persistence.OneToOne(optional = false, fetch = FetchType.EAGER)
    private Account owner;

    @Lob
    private String description;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "organization")
    protected Set<Account> members = new HashSet<>();

    public Organization() {

    }
    public Organization(String pName, Account pOwner, String pDescription) {
        name = pName;
        owner = pOwner;
        description = pDescription;
    }

    public Set<Account> getMembers() {
        return members;
    }
    public boolean addMember(Account pAccount){
        return members.add(pAccount);
    }
    public boolean removeMember(Account pAccount){
        return members.remove(pAccount);
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public Account getOwner() {
        return owner;
    }
    public void setOwner(Account owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }
        if (o == null || getClass() != o.getClass()){
            return false;
        }

        Organization that = (Organization) o;

         return  name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}