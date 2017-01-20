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

package com.docdoku.server.rest.dto.baseline;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

@XmlRootElement
@ApiModel(value="BaselinedPartDTO", description="This class is a representation of a {@link com.docdoku.core.configuration.BaselinedPart} entity")
public class BaselinedPartDTO implements Serializable {

    @ApiModelProperty(value = "Part number")
    private String number;

    @ApiModelProperty(value = "Part name")
    private String name;

    @ApiModelProperty(value = "Part version")
    private String version;

    @ApiModelProperty(value = "Part iteration")
    private int iteration;

    @ApiModelProperty(value = "Part available iterations")
    private List<BaselinedPartOptionDTO> availableIterations;

    public BaselinedPartDTO() {
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getIteration() {
        return iteration;
    }

    public void setIteration(int iteration) {
        this.iteration = iteration;
    }


    public List<BaselinedPartOptionDTO> getAvailableIterations() {
        return availableIterations;
    }

    public void setAvailableIterations(List<BaselinedPartOptionDTO> availableIterations) {
        this.availableIterations = availableIterations;
    }

}