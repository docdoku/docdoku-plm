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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

/**
 * @author Morgan Guimard
 */
@XmlRootElement
@ApiModel(value="InstanceAttributeDescriptorDTO", description="This class is the representation of an {@link com.docdoku.core.meta.InstanceAttributeDescriptor} object")
public class InstanceAttributeDescriptorDTO implements Serializable {

    @ApiModelProperty(value = "Instance attribute name")
    private String name;

    @ApiModelProperty(value = "Instance attribute type")
    private String type;

    @ApiModelProperty(value = "Instance attribute list of value items")
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
