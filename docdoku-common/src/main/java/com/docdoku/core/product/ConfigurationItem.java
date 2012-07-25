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
package com.docdoku.core.product;

import com.docdoku.core.common.Workspace;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;

/**
 * This class represents an entire product or some portion
 * than is planned for production.
 * Configuration management is done using this class that controls
 * the composition of constituents for actual units of production.
 * 
 * All the effectivities that reference a given ConfigurationItem
 * must be of the same type.
 * Application logic should insure it's not possible to mix effectivity
 * types for the same configuration item.
 * 
 * @author Florent Garin
 * @version 1.1, 31/10/11
 * @since   V1.1
 */
@NamedQuery(name="ConfigurationItem.getEffectivities",query="SELECT e FROM Effectivity e WHERE e.configurationItem = :configurationItem")
@javax.persistence.IdClass(com.docdoku.core.product.ConfigurationItemKey.class)
@Entity
public class ConfigurationItem implements Serializable {


    @Column(length = 50)
    @Id
    private String id = "";
    
    @Id
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Workspace workspace;
    

    private String description;
       
    /**
     * The top level of the design of the configuration item
     * which is the context for effectivities.
     */
    @ManyToOne(fetch= FetchType.LAZY, optional=false)
    @JoinColumns({
        @JoinColumn(name = "PARTMASTER_NUMBER", referencedColumnName = "NUMBER"),
        @JoinColumn(name = "PARTMASTER_WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID")
    })
    private PartMaster designItem;

    public ConfigurationItem() {
    }

    public ConfigurationItem(Workspace pWorkspace, String pId, String pDescription) {
        this.workspace=pWorkspace;
        this.id=pId;
        this.description=pDescription;
    }
    
    public PartMaster getDesignItem() {
        return designItem;
    }

    public void setDesignItem(PartMaster designItem) {
        this.designItem = designItem;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Workspace getWorkspace() {
        return workspace;
    }
    
    public String getWorkspaceId() {
        return workspace == null ? "" : workspace.getId();
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

        @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof ConfigurationItem))
            return false;
        ConfigurationItem ci = (ConfigurationItem) pObj;
        return ((ci.id.equals(id)) && (ci.getWorkspaceId().equals(getWorkspaceId())));
    }
    
    @Override
    public int hashCode() {
        int hash = 1;
	hash = 31 * hash + getWorkspaceId().hashCode();
	hash = 31 * hash + id.hashCode();
	return hash;
    }
    
    @Override
    public String toString() {
        return id;
    }
    
    
}
