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
 * @author Morgan Guimard
 */
public class InstanceAttributeDescriptorDTO implements Serializable{

    private String name;
    private String type;
    private List<NameValuePairDTO> lovItems;

    public InstanceAttributeDescriptorDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<NameValuePairDTO> getLovItems() {
        return lovItems;
    }

    public void setLovItems(List<NameValuePairDTO> lovItems) {
        this.lovItems = lovItems;
    }

}
