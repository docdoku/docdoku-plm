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

package com.docdoku.server.rest.dto;

import java.io.Serializable;


public class InstanceAttributeTemplateDTO implements Serializable {

    private String name;

    private boolean mandatory;

    private AttributeType attributeType;

    private String lovName;

    private boolean locked;

    public enum AttributeType {
        TEXT, NUMBER, DATE, BOOLEAN, URL, LOV
    }

    public InstanceAttributeTemplateDTO() {
    }

    public InstanceAttributeTemplateDTO(String pName, AttributeType pAttributeType, boolean pMandatory, boolean locked) {
        name = pName;
        attributeType = pAttributeType;
        mandatory = pMandatory;
        this.locked = locked;
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

    public InstanceAttributeTemplateDTO.AttributeType getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(InstanceAttributeTemplateDTO.AttributeType attributeType) {
        this.attributeType = attributeType;
    }

    public String getLovName() {
        return lovName;
    }

    public void setLovName(String lovName) {
        this.lovName = lovName;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
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
