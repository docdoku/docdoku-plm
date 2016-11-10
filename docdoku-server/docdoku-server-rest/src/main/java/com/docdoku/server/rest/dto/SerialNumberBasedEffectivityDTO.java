package com.docdoku.server.rest.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@ApiModel(value = "SerialNumberBasedEffectivityDTO", description = "This class is the representation of an {@link com.docdoku.core.common.SerialNumberBasedEffectivity} entity")
public class SerialNumberBasedEffectivityDTO implements Serializable {

    @ApiModelProperty(value = "Id of the effectivity")
    private String id;

    @ApiModelProperty(value = "Name of the effectivity")
    private String name;

    @ApiModelProperty(value = "Description of the effectivity")
    private String description;

    @ApiModelProperty(value = "Start number of the Serial Number")
    private String startNumber;

    @ApiModelProperty(value = "End number of the Serial Number")
    private String endNumber;

    public SerialNumberBasedEffectivityDTO() {
    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public String getStartNumber() { return startNumber; }

    public void setStartNumber(String startNumber) { this.startNumber = startNumber; }

    public String getEndNumber() { return endNumber; }

    public void setEndNumber(String endNumber) { this.endNumber = endNumber; }
}
