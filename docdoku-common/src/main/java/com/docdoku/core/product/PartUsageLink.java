/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
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
@Table(name = "PARTUSAGELINK")
@Entity
@NamedQueries({
    @NamedQuery(name="PartUsageLink.findByComponent",query="SELECT u FROM PartUsageLink u WHERE u.component.number LIKE :partNumber AND u.component.workspace.id = :workspaceId"),
    @NamedQuery(name="PartUsageLink.getPartOwner",query="SELECT p FROM PartIteration p WHERE :usage MEMBER OF p.components")
})
public class PartUsageLink implements Serializable, Cloneable {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private int id;
    private double amount;
    private String unit;
    private String referenceDescription;
    @Column(name = "COMMENTDATA")
    private String comment;
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name = "COMPONENT_WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID"),
        @JoinColumn(name = "COMPONENT_PARTNUMBER", referencedColumnName = "PARTNUMBER")
    })
    private PartMaster component;
    @OrderColumn(name = "PARTSUBSTITUTE_ORDER")
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "PUSAGELINK_PSUBSTITUTELINK",
    inverseJoinColumns = {
        @JoinColumn(name = "PARTSUBSTITUTE_ID", referencedColumnName = "ID")
    },
    joinColumns = {
        @JoinColumn(name = "PARTUSAGELINK_ID", referencedColumnName = "ID")
    })
    private List<PartSubstituteLink> substitutes = new LinkedList<PartSubstituteLink>();
    
    @OrderColumn(name = "CADINSTANCE_ORDER")
    @JoinTable(name = "PARTUSAGELINK_CADINSTANCE",
    inverseJoinColumns = {
        @JoinColumn(name = "CADINSTANCE_ID", referencedColumnName = "ID")
    },
    joinColumns = {
        @JoinColumn(name = "PARTUSAGELINK_ID", referencedColumnName = "ID")
    })
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CADInstance> cadInstances = new LinkedList<CADInstance>();

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

    @Override
    public PartUsageLink clone() {
        PartUsageLink clone = null;
        try {
            clone = (PartUsageLink) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }

        //perform a deep copy
        List<PartSubstituteLink> clonedSubstitutes = new LinkedList<PartSubstituteLink>();
        for (PartSubstituteLink substitute : substitutes) {
            PartSubstituteLink clonedSubstitute = substitute.clone();
            clonedSubstitutes.add(clonedSubstitute);
        }
        clone.substitutes = clonedSubstitutes;

        List<CADInstance> clonedCADInstances = new LinkedList<CADInstance>();
        for (CADInstance cadInstance : cadInstances) {
            CADInstance clonedCADInstance = cadInstance.clone();
            clonedCADInstances.add(clonedCADInstance);
        }
        clone.cadInstances = clonedCADInstances;

        return clone;
    }
}
