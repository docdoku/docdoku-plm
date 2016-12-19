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
@ApiModel(value="PartUsageLinkDTO", description="This class is a representation of a {@link com.docdoku.core.product.PartUsageLink} entity")
public class PartUsageLinkDTO implements Serializable {

    @ApiModelProperty(value = "Part usage link id")
    private int id;

    @ApiModelProperty(value = "Complete path in context")
    private String fullId;

    @ApiModelProperty(value = "Amount for usage")
    private double amount;

    @ApiModelProperty(value = "Link comment")
    private String comment;

    @ApiModelProperty(value = "Component used")
    private ComponentDTO component;

    @ApiModelProperty(value = "Link description")
    private String referenceDescription;

    @ApiModelProperty(value = "Unit for amount")
    private String unit;

    @ApiModelProperty(value = "Optional link flag")
    private boolean optional;

    @ApiModelProperty(value = "List of CAD instances")
    private List<CADInstanceDTO> cadInstances;

    @ApiModelProperty(value = "List of substitute links")
    private List<PartSubstituteLinkDTO> substitutes;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public ComponentDTO getComponent() {
        return component;
    }

    public void setComponent(ComponentDTO component) {
        this.component = component;
    }

    public String getReferenceDescription() {
        return referenceDescription;
    }

    public void setReferenceDescription(String referenceDescription) {
        this.referenceDescription = referenceDescription;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public List<CADInstanceDTO> getCadInstances() {
        return cadInstances;
    }

    public void setCadInstances(List<CADInstanceDTO> cadInstances) {
        this.cadInstances = cadInstances;
    }

    public List<PartSubstituteLinkDTO> getSubstitutes() {
        return substitutes;
    }

    public void setSubstitutes(List<PartSubstituteLinkDTO> substitutes) {
        this.substitutes = substitutes;
    }

    public String getFullId() {
        return fullId;
    }

    public void setFullId(String fullId) {
        this.fullId = fullId;
    }
}
