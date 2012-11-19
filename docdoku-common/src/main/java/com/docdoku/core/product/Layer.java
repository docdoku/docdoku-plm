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

import com.docdoku.core.common.User;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * A Layer is a collection of <a href="Marker.html">Marker</a>s that can be
 * manipulated as a whole.
 * Layers belong to a <a href="ConfigurationItem.html">ConfigurationItem</a>.
 * 
 * @author Florent Garin
 * @version 1.1, 14/08/12
 * @since   V1.1
 */
@Table(name="LAYER")
@Entity
@NamedQuery(name="Layer.findLayersByConfigurationItem",query="SELECT DISTINCT l FROM Layer l WHERE l.configurationItem.id = :configurationItemId AND l.configurationItem.workspace.id = :workspaceId")
public class Layer implements Serializable{

    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Id
    private int id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name = "AUTHOR_LOGIN", referencedColumnName = "LOGIN"),
        @JoinColumn(name = "AUTHOR_WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID")
    })
    private User author;
    
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date creationDate;
    
    private String name;
    
    @ManyToMany(fetch= FetchType.LAZY)
    @JoinTable(name="LAYER_MARKER",
    inverseJoinColumns={
        @JoinColumn(name="MARKER_ID", referencedColumnName="ID")
    },
    joinColumns={
        @JoinColumn(name="LAYER_ID", referencedColumnName="ID")
    })
    private Set<Marker> markers = new HashSet<Marker>();
        
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name = "CONFIGURATIONITEM_ID", referencedColumnName = "ID"),
        @JoinColumn(name = "CONFIGURATIONITEM_WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID")
    })
    private ConfigurationItem configurationItem;
        
    public Layer() {
    }

    public Layer(String pName, User pAuthor, ConfigurationItem pConfigurationItem) {
        this.name=pName;
        this.author=pAuthor;
        this.configurationItem=pConfigurationItem;
    }
    
    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Set<Marker> getMarkers() {
        return markers;
    }

    public void setMarkers(Set<Marker> markers) {
        this.markers = markers;
    }

    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ConfigurationItem getConfigurationItem() {
        return configurationItem;
    }

    public void setConfigurationItem(ConfigurationItem configurationItem) {
        this.configurationItem = configurationItem;
    }

    public void addMarker(Marker marker) {
        this.markers.add(marker);
    }

    public void removeMarker(Marker marker) {
        this.markers.remove(marker);
    }

}
