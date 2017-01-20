/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
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
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@ApiModel(value="PathDataMasterDTO", description="This class is a representation of a {@link com.docdoku.core.product.PathDataMaster} entity")
public class PathDataMasterDTO implements Serializable {

    @ApiModelProperty(value = "Path data master id")
    private Integer id;

    @ApiModelProperty(value = "Complete path in context")
    private String path;

    @ApiModelProperty(value = "Product instance serial number")
    private String serialNumber;

    @ApiModelProperty(value = "List of part links")
    private LightPartLinkListDTO partLinksList;

    @ApiModelProperty(value = "Path data master iterations")
    private List<PathDataIterationDTO> pathDataIterations = new ArrayList<>();

    @ApiModelProperty(value = "Path data master attributes")
    private List<InstanceAttributeDTO> partAttributes;

    @ApiModelProperty(value = "Path data master attribute templates")
    private List<InstanceAttributeTemplateDTO> partAttributeTemplates;

    public PathDataMasterDTO() {
    }

    public PathDataMasterDTO(String path) {
        this.path = path;
    }

    public PathDataMasterDTO(Integer id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LightPartLinkListDTO getPartLinksList() {
        return partLinksList;
    }

    public void setPartLinksList(LightPartLinkListDTO partLinksList) {
        this.partLinksList = partLinksList;
    }

    public List<PathDataIterationDTO> getPathDataIterations() {
        return pathDataIterations;
    }

    public void setPathDataIterations(List<PathDataIterationDTO> pathDataIterations) {
        this.pathDataIterations = pathDataIterations;
    }

    public List<InstanceAttributeDTO> getPartAttributes() {
        return partAttributes;
    }

    public void setPartAttributes(List<InstanceAttributeDTO> partAttributes) {
        this.partAttributes = partAttributes;
    }

    public List<InstanceAttributeTemplateDTO> getPartAttributeTemplates() {
        return partAttributeTemplates;
    }

    public void setPartAttributeTemplates(List<InstanceAttributeTemplateDTO> partAttributeTemplates) {
        this.partAttributeTemplates = partAttributeTemplates;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PathDataMasterDTO that = (PathDataMasterDTO) o;

        return !(id != null ? !id.equals(that.id) : that.id != null);

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}