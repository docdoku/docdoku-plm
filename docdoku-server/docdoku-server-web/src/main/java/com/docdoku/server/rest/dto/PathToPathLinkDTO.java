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

public class PathToPathLinkDTO implements Serializable{

    private Integer id;
    private String type;
    private List<LightPartLinkDTO> sourceComponents;
    private List<LightPartLinkDTO> targetComponents;
    private String description;

    public PathToPathLinkDTO() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<LightPartLinkDTO> getSourceComponents() {
        return sourceComponents;
    }

    public void setSourceComponents(List<LightPartLinkDTO> sourceComponents) {
        this.sourceComponents = sourceComponents;
    }

    public List<LightPartLinkDTO> getTargetComponents() {
        return targetComponents;
    }

    public void setTargetComponents(List<LightPartLinkDTO> targetComponents) {
        this.targetComponents = targetComponents;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}