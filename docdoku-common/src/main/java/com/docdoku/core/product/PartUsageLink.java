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

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

/**
 * A link between an assembly represented as
 * <a href="PartIteration.html">PartIteration</a>
 * and a part represented as <a href="PartMaster.html">PartMaster</a>. 
 * 
 * @author Florent Garin
 * @version 1.1, 15/10/11
 * @since   V1.1
 */
@Table(name="PARTUSAGELINK")
@Entity
public class PartUsageLink implements Serializable {

    
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private int id;
    
    private double amount;
    private String unit;
    
    private String referenceDescription;
    
    @Column(name="COMMENTDATA")
    private String comment;
    
    @ManyToOne(optional=false, fetch=FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name = "COMPONENT_WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID"),
        @JoinColumn(name = "COMPONENT_PARTNUMBER", referencedColumnName = "PARTNUMBER")
    })
    private PartMaster component;
    
    @OrderColumn(name="PARTSUBSTITUTE_ORDER")
    @OneToMany(orphanRemoval=true, cascade=CascadeType.ALL, fetch=FetchType.LAZY)
    @JoinTable(name="PUSAGELINK_PSUBSTITUTELINK",
    inverseJoinColumns={
        @JoinColumn(name="PARTSUBSTITUTE_ID", referencedColumnName="ID")
    },
    joinColumns={
        @JoinColumn(name="PARTUSAGELINK_ID", referencedColumnName="ID")
    })
    private List<PartSubstituteLink> substitutes=new LinkedList<PartSubstituteLink>();

    
    @OrderColumn(name="CADINSTANCE_ORDER")
    @CollectionTable(name="PARTUSAGELINK_CADINSTANCE",joinColumns={
        @JoinColumn(name="PARTUSAGELINK_ID", referencedColumnName="ID")
    })
    @ElementCollection(fetch=FetchType.LAZY)
    private List<CADInstance> cadInstances=new LinkedList<CADInstance>();

    public PartUsageLink() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setComponent(PartMaster component) {
        this.component = component;
    }

    public PartMaster getComponent() {
        return component;
    }

    public List<PartSubstituteLink> getSubstitutes() {
        return substitutes;
    }

    public void setSubstitutes(List<PartSubstituteLink> substitutes) {
        this.substitutes = substitutes;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getReferenceDescription() {
        return referenceDescription;
    }

    public void setReferenceDescription(String referenceDescription) {
        this.referenceDescription = referenceDescription;
    }

    public List<CADInstance> getCadInstances() {
        return cadInstances;
    }

    public void setCadInstances(List<CADInstance> cadInstances) {
        this.cadInstances = cadInstances;
    }
    
    
}
