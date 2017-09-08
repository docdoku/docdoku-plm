package com.docdoku.server.rest.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@ApiModel(value = "WebhookDTO", description = "This class is a representation of a {@link com.docdoku.core.hooks.Webhook} entity")
public class WebhookDTO {

    @ApiModelProperty(value = "Webhook id")
    private int id;

    @ApiModelProperty(value = "Webhook name")
    private String name;

    @ApiModelProperty(value = "Webhook active flag")
    private boolean active;

    public WebhookDTO() {
    }

    public WebhookDTO(int id, String name, boolean active) {
        this.id = id;
        this.name = name;
        this.active = active;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
