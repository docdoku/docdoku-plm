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
import java.util.List;

@XmlRootElement
@ApiModel(value = "ImportPreviewDTO", description = "This class is a representation of a {@link com.docdoku.core.product.ImportPreview} entity")
public class ImportPreviewDTO implements Serializable {

    @ApiModelProperty(value = "Part revisions that will be checked out")
    private List<LightPartRevisionDTO> partRevsToCheckout;


    @ApiModelProperty(value = "Part masters that will be created")
    private List<PartCreationDTO> partsToCreate;

    public ImportPreviewDTO() {
    }

    public ImportPreviewDTO(List<LightPartRevisionDTO> partRevsToCheckout, List<PartCreationDTO> partsToCreate) {
        this.partRevsToCheckout = partRevsToCheckout;
        this.partsToCreate = partsToCreate;
    }

    public List<LightPartRevisionDTO> getPartRevsToCheckout() {
        return partRevsToCheckout;
    }

    public void setPartRevsToCheckout(List<LightPartRevisionDTO> partRevsToCheckout) {
        this.partRevsToCheckout = partRevsToCheckout;
    }

    public List<PartCreationDTO> getPartsToCreate() {
        return partsToCreate;
    }

    public void setPartsToCreate(List<PartCreationDTO> partsToCreate) {
        this.partsToCreate = partsToCreate;
    }
}