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

import javax.persistence.*;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.io.Serializable;

/**
 * Base class for all instance attributes.
 *
 * @author Florent Garin
 * @version 1.0, 02/06/08
 * @since V1.0
 */
@Table(name = "INSTANCEATTRIBUTE")
@XmlSeeAlso({InstanceTextAttribute.class, InstanceNumberAttribute.class, InstanceDateAttribute.class, InstanceBooleanAttribute.class, InstanceURLAttribute.class, InstanceListOfValuesAttribute.class})
@Inheritance()
@Entity
public abstract class InstanceAttribute implements Serializable, Cloneable {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private int id;

    protected String name = "";

    protected boolean mandatory;

    protected boolean locked;

    public InstanceAttribute() {
    }

    public InstanceAttribute(String pName, boolean pMandatory) {
        name = pName;
        mandatory = pMandatory;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameWithoutWhiteSpace() {
        return this.name.replaceAll(" ", "_");
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof InstanceAttribute)) {
            return false;
        }
        InstanceAttribute attribute = (InstanceAttribute) pObj;
        return attribute.id == id;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public InstanceAttribute clone() {
        try {
            return (InstanceAttribute) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public abstract Object getValue();

    public abstract boolean setValue(Object pValue);

    public boolean isValueEquals(Object pValue) {
        Object value = getValue();
        return value != null && value.equals(pValue);
    }
}
