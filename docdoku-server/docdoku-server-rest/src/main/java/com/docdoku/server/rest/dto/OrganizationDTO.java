package com.docdoku.server.rest.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@ApiModel(value = "OrganizationDTO", description = "This class is the representation of an {@link com.docdoku.core.common.Organization} entity")
public class OrganizationDTO implements Serializable {

    @ApiModelProperty(value = "Name of the organization")
    private String name;

    @ApiModelProperty(value = "Description of the organization")
    private String description;

    public OrganizationDTO() {
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

}
