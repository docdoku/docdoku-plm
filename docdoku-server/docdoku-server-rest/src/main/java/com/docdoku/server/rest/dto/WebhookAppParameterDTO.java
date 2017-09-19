package com.docdoku.server.rest.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@ApiModel(value = "WebhookAppParameterDTO", description = "This class carries on specific webhook app properties")
public class WebhookAppParameterDTO implements Serializable {

    @ApiModelProperty(value = "Webhook app parameter name")
    private String name;

    @ApiModelProperty(value = "Webhook app parameter value")
    private String value;

    public WebhookAppParameterDTO() {
    }

    public WebhookAppParameterDTO(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
