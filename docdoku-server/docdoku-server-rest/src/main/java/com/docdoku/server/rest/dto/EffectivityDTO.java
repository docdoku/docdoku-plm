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

import com.docdoku.core.product.ConfigurationItemKey;
import com.docdoku.core.product.TypeEffectivity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

@XmlRootElement
@ApiModel(value = "EffectivityDTO",
        description = "This class is the representation of an {@link com.docdoku.core.common.Effectivity} entity",
        subTypes = {SerialNumberBasedEffectivityDTO.class, DateBasedEffectivityDTO.class, LotBasedEffectivityDTO.class})
public class EffectivityDTO implements Serializable {

    @ApiModelProperty(value = "Id of the effectivity")
    private int id;

    @ApiModelProperty(value = "Name of the effectivity")
    private String name;

    @ApiModelProperty(value = "Description of the effectivity")
    private String description;

    @ApiModelProperty(value = "Configuration Item Key linked to the effectivity")
    private ConfigurationItemKey configurationItemKey;

    @ApiModelProperty(value = "Discriminator of the effectivity type")
    private TypeEffectivity typeEffectivity;

    @ApiModelProperty(value = "Start number of the Serial Number")
    private String startNumber;

    @ApiModelProperty(value = "End number of the Serial Number")
    private String endNumber;

    @ApiModelProperty(value = "Start date of the Effectivity")
    private Date startDate;

    @ApiModelProperty(value = "End date of the Effectivity")
    private Date endDate;

    @ApiModelProperty(value = "Start number of the Lot")
    private String startLotId;

    @ApiModelProperty(value = "End number of the Lot")
    private String endLotId;

    public EffectivityDTO() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ConfigurationItemKey getConfigurationItemKey() {
        return configurationItemKey;
    }

    public void setConfigurationItemKey(ConfigurationItemKey configurationItemKey) {
        this.configurationItemKey = configurationItemKey;
    }

    public TypeEffectivity getTypeEffectivity() {
        return typeEffectivity;
    }

    public void setTypeEffectivity(TypeEffectivity typeEffectivity) {
        this.typeEffectivity = typeEffectivity;
    }

    public String getStartNumber() {
        return startNumber;
    }

    public void setStartNumber(String startNumber) {
        this.startNumber = startNumber;
    }

    public String getEndNumber() {
        return endNumber;
    }

    public void setEndNumber(String endNumber) {
        this.endNumber = endNumber;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getStartLotId() {
        return startLotId;
    }

    public void setStartLotId(String startLotId) {
        this.startLotId = startLotId;
    }

    public String getEndLotId() {
        return endLotId;
    }

    public void setEndLotId(String endLotId) {
        this.endLotId = endLotId;
    }
}
