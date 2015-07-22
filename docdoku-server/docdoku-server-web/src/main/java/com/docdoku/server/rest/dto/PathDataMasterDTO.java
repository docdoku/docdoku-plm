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
import java.util.ArrayList;
import java.util.List;

public class PathDataMasterDTO implements Serializable {

    private Integer id;
    private String path;
    private String serialNumber;
    private LightPartLinkListDTO partLinksList;
    private List<PathDataIterationDTO> pathDataIterations = new ArrayList<PathDataIterationDTO>();
    private List<InstanceAttributeDTO> partAttributes;
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
        if (this == o){
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PathDataMasterDTO that = (PathDataMasterDTO) o;

        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}