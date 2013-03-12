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
package com.docdoku.core.meta;

import java.io.Serializable;
import javax.persistence.*;

/**
 * This class holds the definition of the custom attribute of the documents and parts
 * created by the template.
 * 
 * @author Florent Garin
 * @version 1.1, 23/01/12
 * @since   V1.0
 */
@Table(name="INSTANCEATTRIBUTETEMPLATE")
@Entity
public class InstanceAttributeTemplate implements Serializable {

    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Id
    private int id;

    @Column(length=50)
    private String name = "";

    private AttributeType attributeType;

    public enum AttributeType {

        TEXT, NUMBER, DATE, BOOLEAN, URL
    }

    public InstanceAttributeTemplate() {
    }

    public InstanceAttributeTemplate(String pName, AttributeType pAttributeType) {
        name = pName;
        attributeType = pAttributeType;
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

    public InstanceAttributeTemplate.AttributeType getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(InstanceAttributeTemplate.AttributeType attributeType) {
        this.attributeType = attributeType;
    }

    public InstanceAttribute createInstanceAttribute() {
        InstanceAttribute attr = null;
        switch (attributeType) {
            case TEXT:
                attr = new InstanceTextAttribute();
                attr.setName(name);
                break;
            case NUMBER:
                attr = new InstanceNumberAttribute();
                attr.setName(name);
                break;
            case BOOLEAN:
                attr = new InstanceBooleanAttribute();
                attr.setName(name);
                break;
            case DATE:
                attr = new InstanceDateAttribute();
                attr.setName(name);
                break;
            case URL :
                attr = new InstanceURLAttribute();
                attr.setName(name);
                break;
        }
        return attr;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InstanceAttributeTemplate that = (InstanceAttributeTemplate) o;

        if (id != that.id) return false;
        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return name + "-" + attributeType;
    }
}
