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
package com.docdoku.server.rest.dto;

import java.io.Serializable;

/**
 *
 * @author Yassine Belouad
 */
public class InstanceAttributeDTO  implements Serializable{
    
    private String name;
    private Type type;
    public enum Type {

        TEXT, NUMBER, DATE, BOOLEAN, URL
    }
    private String value;
    
    public InstanceAttributeDTO(){
    
    }

    public InstanceAttributeDTO(String pName, Type pType, String pValue){
        this.name=pName;
        this.type=pType;
        this.value=pValue;
    }
    
    public InstanceAttributeDTO(String pName, String pType, String pValue){
        this(pName,InstanceAttributeDTO.Type.valueOf(pType),pValue);
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
    
    public InstanceAttributeDTO.Type getType(){
        return type;
    }
    
    public void setType(InstanceAttributeDTO.Type type) {
        this.type = type;
    }

    public void setValue(String value) {
        this.value = value;
    }

    
    
}
