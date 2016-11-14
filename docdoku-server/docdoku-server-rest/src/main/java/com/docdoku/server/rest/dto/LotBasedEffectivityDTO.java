package com.docdoku.server.rest.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@ApiModel(value = "LotBasedEffectivityDTO",
        description = "This class is the representation of an {@link com.docdoku.core.common.LotBasedEffectivity} entity",
        parent = EffectivityDTO.class)
public class LotBasedEffectivityDTO extends EffectivityDTO implements Serializable {

    @ApiModelProperty(value = "Start number of the Lot")
    private String startLotId;

    @ApiModelProperty(value = "End number of the Lot")
    private String endLotId;

    public LotBasedEffectivityDTO() {
    }

    public String getStartLotId() { return startLotId; }

    public void setStartLotId(String startLotId) { this.startLotId = startLotId; }

    public String getEndLotId() { return endLotId; }

    public void setEndLotId(String endLotId) { this.endLotId = endLotId; }
}
