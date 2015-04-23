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
    private PartMinimalListDTO partsPath ;
    private List<PathDataIterationDTO> pathDataIterations = new ArrayList<PathDataIterationDTO>();

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

    public PartMinimalListDTO getPartsPath() {
        return partsPath;
    }

    public void setPartsPath(PartMinimalListDTO partsPath) {
        this.partsPath = partsPath;
    }

    public List<PathDataIterationDTO> getPathDataIterations() {
        return pathDataIterations;
    }

    public void setPathDataIterations(List<PathDataIterationDTO> pathDataIterations) {
        this.pathDataIterations = pathDataIterations;
    }
}