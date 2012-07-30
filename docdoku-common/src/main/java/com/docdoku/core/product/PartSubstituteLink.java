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
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;

/**
 * This class is related to a <code>PartUsageLink</code> to indicate
 * a replacement part that could be used instead.
 * 
 * @author Florent Garin
 * @version 1.1, 16/10/11
 * @since   V1.1
 */
@Entity
public class PartSubstituteLink implements Serializable {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private int id;
    private String referenceDescription;
    private String comment;
    
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private PartMaster substitute;

    @OrderColumn(name = "CADINSTANCE_ORDER")
    @CollectionTable(name = "PARTSUBSTITUTELINK_CADINSTANCE", joinColumns = {
        @JoinColumn(name = "PARTSUBSTITUTELINK_ID", referencedColumnName = "ID")
    })
    @ElementCollection(fetch = FetchType.LAZY)
    private List<CADInstance> cadInstances = new LinkedList<CADInstance>();

    public PartSubstituteLink() {
    }

    public PartMaster getSubstitute() {
        return substitute;
    }

    public void setSubstitute(PartMaster substitute) {
        this.substitute = substitute;
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
