package com.docdoku.server.rest.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

@XmlRootElement
@ApiModel(value = "DateBasedEffectivityDTO",
        description = "This class is the representation of an {@link com.docdoku.core.common.DateBasedEffectivity} entity",
        parent = EffectivityDTO.class)
public class DateBasedEffectivityDTO extends EffectivityDTO implements Serializable {

    @ApiModelProperty(value = "Start date of the Effectivity")
    private Date startDate;

    @ApiModelProperty(value = "End date of the Effectivity")
    private Date endDate;

    public DateBasedEffectivityDTO() {
    }

    public Date getStartDate() { return startDate; }

    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }

    public void setEndDate(Date endDate) { this.endDate = endDate; }
}
