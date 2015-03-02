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

package com.docdoku.core.meta;

import com.docdoku.core.common.Workspace;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A list of values is basically a named collection of name-value pair.
 *
 * @author Florent Garin
 * @version 2.0, 27/02/15
 * @since V2.0
 */
@Table(name = "LOV")
@javax.persistence.IdClass(ListOfValuesKey.class)
@javax.persistence.Entity
public class ListOfValues implements Serializable {

    @Column(name = "WORKSPACE_ID", length = 100, nullable = false, insertable = false, updatable = false)
    @Id
    private String workspaceId = "";

    @Column(length = 100)
    @Id
    private String name = "";

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Workspace workspace;

    @ElementCollection(fetch = FetchType.EAGER)
    @OrderColumn(name = "NAMEVALUE_ORDER")
    @CollectionTable(
            name = "LOV_NAMEVALUE",
            joinColumns = {@JoinColumn(name = "LOV_NAME", referencedColumnName = "NAME"),
                    @JoinColumn(name = "LOV_WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID")
            }
    )
    private List<NameValuePair> values=new ArrayList<>();

    public ListOfValues() {
    }

    public ListOfValues(Workspace pWorkspace, String pName) {
        setWorkspace(pWorkspace);
        name = pName;
    }

    public void setWorkspace(Workspace pWorkspace) {
        workspace = pWorkspace;
        workspaceId = workspace.getId();
    }

    public String getName() {
        return name;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public List<NameValuePair> getValues() {
        return values;
    }

    public void setValues(List<NameValuePair> values) {
        this.values = values;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = 31 * hash + workspaceId.hashCode();
        hash = 31 * hash + name.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof ListOfValues)) {
            return false;
        }
        ListOfValues lov = (ListOfValues) pObj;

        return lov.workspaceId.equals(workspaceId)
                && lov.name.equals(name);
    }

    @Override
    public String toString() {
        return name;
    }

}
