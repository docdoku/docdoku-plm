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
 * This class holds the definition of the custom attribute of the documents and parts
 * created by the template.
 * 
 * @author Florent Garin
 * @version 1.1, 23/01/12
 * @since   V1.0
 */
@Table(name="INSTANCEATTRIBUTETEMPLATE")
@XmlSeeAlso({DefaultAttributeTemplate.class, ListOfValuesAttributeTemplate.class})
@Inheritance()
@Entity
public abstract class InstanceAttributeTemplate implements Serializable, Cloneable {

    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Id
    protected int id;

    @Column(length=100)
    protected String name = "";

    protected boolean mandatory;

    protected boolean locked;

    public enum AttributeType {

        TEXT, NUMBER, DATE, BOOLEAN, URL, LOV
    }


    public InstanceAttributeTemplate() {
    }

    public InstanceAttributeTemplate(String pName) {
        name = pName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public abstract InstanceAttribute createInstanceAttribute();

    public abstract AttributeType getAttributeType();

    @Override
    public InstanceAttributeTemplate clone() {
        try {
            return (InstanceAttributeTemplate) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        InstanceAttributeTemplate that = (InstanceAttributeTemplate) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return name;
    }
}
