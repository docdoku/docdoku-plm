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
import java.util.List;

/**
 *
 * @author Yassine Belouad
 */
public class InstanceAttributeDTO  implements Serializable{
    
    private String name;
    private boolean mandatory;
    private boolean locked;

    private Type type;
    public enum Type {
        TEXT, NUMBER, DATE, BOOLEAN, URL, LOV
    }
    private String value;

    private String lovName;

    private List<NameValuePairDTO> items;

    public InstanceAttributeDTO(){
    
    }

    public InstanceAttributeDTO(String pName, Type pType, String pValue, Boolean pMandatory, Boolean pLocked){
        this.name=pName;
        this.type=pType;
        this.value=pValue;
        this.mandatory=pMandatory;
        this.locked = pLocked;
    }
    
    public InstanceAttributeDTO(String pName, String pType, String pValue, Boolean pMandatory, Boolean pLocked){
        this(pName,InstanceAttributeDTO.Type.valueOf(pType),pValue,pMandatory,pLocked);
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }

    public InstanceAttributeDTO.Type getType(){
        return type;
    }
    public void setType(InstanceAttributeDTO.Type type) {
        this.type = type;
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

    public String getLovName() {
        return lovName;
    }

    public void setLovName(String lovName) {
        this.lovName = lovName;
    }

    public List<NameValuePairDTO> getItems() {
        return items;
    }

    public void setItems(List<NameValuePairDTO> items) {
        this.items = items;
    }
}
