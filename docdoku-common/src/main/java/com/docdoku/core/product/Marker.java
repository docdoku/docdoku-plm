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
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Represents a marker on the 3D scene, actually a
 * <a href="ConfigurationItem.html">ConfigurationItem</a>.
 * May be attached to one or several <a href="PartMaster.html">PartMaster</a>s.
 * 
 * @author Florent Garin
 * @version 1.1, 14/08/12
 * @since   V1.1
 */
@Entity
public class Marker implements Serializable{

    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Id
    private int id;

    @OneToMany(orphanRemoval=true, cascade= CascadeType.ALL, fetch= FetchType.EAGER)
    @JoinTable(
    inverseJoinColumns={
        @JoinColumn(name="EFFECTIVITY_ID", referencedColumnName="ID")
    },
    joinColumns={
        @JoinColumn(name="MARKER_ID", referencedColumnName="ID")
    })
    private Set<Effectivity> effectivities = new HashSet<Effectivity>();
        
    @ManyToMany(fetch= FetchType.LAZY)
    @JoinTable(
    inverseJoinColumns={
        @JoinColumn(name="RELATEDPART_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID"),
        @JoinColumn(name="RELATEDPART_NUMBER", referencedColumnName="NUMBER")
    },
    joinColumns={
        @JoinColumn(name="MARKER_ID", referencedColumnName="ID")
    })
    private Set<PartMaster> relatedParts = new HashSet<PartMaster>();
    

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name = "AUTHOR_LOGIN", referencedColumnName = "LOGIN"),
        @JoinColumn(name = "AUTHOR_WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID")
    })
    private User author;
    
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date creationDate;
    
    private String title;
    
    @Lob
    private String description;
    
    /**
     * Position on x axis.
     */
    private double x;
    
    /**
     * Position on y axis.
     */
    private double y;
    
    /**
     * Position on z axis.
     */
    private double z;
    
    public Marker() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Set<Effectivity> getEffectivities() {
        return effectivities;
    }

    public void setEffectivities(Set<Effectivity> effectivities) {
        this.effectivities = effectivities;
    }

    public Set<PartMaster> getRelatedParts() {
        return relatedParts;
    }

    public void setRelatedParts(Set<PartMaster> relatedParts) {
        this.relatedParts = relatedParts;
    }


    public void setX(double x) {
        this.x = x;
    }

    public double getX() {
        return x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getY() {
        return y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public double getZ() {
        return z;
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
   
    
    
}
