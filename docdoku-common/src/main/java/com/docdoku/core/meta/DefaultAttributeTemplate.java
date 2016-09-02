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

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * A generic implementation of {@link InstanceAttributeTemplate} that can instantiate
 * simple primitive based attributes.
 * 
 * @author Florent Garin
 * @version 1.1, 23/01/12
 * @since   V1.0
 */
@Table(name="DEFAULTIATTRIBUTETEMPLATE")
@Entity
public class DefaultAttributeTemplate extends InstanceAttributeTemplate {


    private AttributeType attributeType;

    public DefaultAttributeTemplate() {
    }

    public DefaultAttributeTemplate(String pName, AttributeType pAttributeType) {
        super(pName);
        attributeType = pAttributeType;
    }


    @Override
    public AttributeType getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(AttributeType attributeType) {
        this.attributeType = attributeType;
    }

    @Override
    public InstanceAttribute createInstanceAttribute() {
        InstanceAttribute attr = null;
        if(attributeType!=null){
            switch (attributeType) {
                case TEXT:
                    attr = new InstanceTextAttribute();
                    break;
                case NUMBER:
                    attr = new InstanceNumberAttribute();
                    break;
                case BOOLEAN:
                    attr = new InstanceBooleanAttribute();
                    break;
                case DATE:
                    attr = new InstanceDateAttribute();
                    break;
                case URL:
                    attr = new InstanceURLAttribute();
                    break;
                case LONG_TEXT:
                    attr = new InstanceLongTextAttribute();
                    break;
                default:
                    return null;
            }

            attr.setName(name);
            attr.setMandatory(mandatory);
            attr.setLocked(locked);
        }

        return attr;
    }

    @Override
    public String toString() {
        return name + "-" + attributeType;
    }
}
