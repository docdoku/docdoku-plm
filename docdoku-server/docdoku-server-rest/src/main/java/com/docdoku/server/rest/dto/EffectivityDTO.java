package com.docdoku.server.rest.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

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

    public EffectivityDTO() {
    }

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }
}
