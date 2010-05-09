/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010 DocDoku SARL
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
package com.docdoku.gwt.explorer.shared;

import java.io.Serializable;


public class InstanceAttributeTemplateDTO implements Serializable {

    private String name;
    private AttributeType attributeType;

    public enum AttributeType {
        TEXT, NUMBER, DATE, BOOLEAN, URL
    }

    public InstanceAttributeTemplateDTO() {
    }

    public InstanceAttributeTemplateDTO(String pName, AttributeType pAttributeType) {
        name = pName;
        attributeType = pAttributeType;
    }

    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public InstanceAttributeTemplateDTO.AttributeType getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(InstanceAttributeTemplateDTO.AttributeType attributeType) {
        this.attributeType = attributeType;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof InstanceAttributeTemplateDTO)) {
            return false;
        }
        InstanceAttributeTemplateDTO attr = (InstanceAttributeTemplateDTO) pObj;
        return attr.name.equals(name);
    }
    
    @Override
    public String toString() {
        return name + "-" + attributeType;
    }
}
