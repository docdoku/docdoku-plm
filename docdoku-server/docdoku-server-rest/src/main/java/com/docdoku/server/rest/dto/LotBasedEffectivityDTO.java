package com.docdoku.server.rest.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@ApiModel(value = "LotBasedEffectivityDTO", description = "This class is the representation of an {@link com.docdoku.core.common.LotBasedEffectivity} entity")
public class LotBasedEffectivityDTO implements Serializable {

    @ApiModelProperty(value = "Id of the effectivity")
    private String id;

    @ApiModelProperty(value = "Name of the effectivity")
    private String name;

    @ApiModelProperty(value = "Description of the effectivity")
    private String description;

    @ApiModelProperty(value = "Start number of the Lot")
    private String startLotId;

    @ApiModelProperty(value = "End number of the Lot")
    private String endLotId;

    public LotBasedEffectivityDTO() {
    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public String getStartLotId() { return startLotId; }

    public void setStartLotId(String startLotId) { this.startLotId = startLotId; }

    public String getEndLotId() { return endLotId; }

    public void setEndLotId(String endLotId) { this.endLotId = endLotId; }
}
