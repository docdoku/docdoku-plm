package com.docdoku.server.rest.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@ApiModel(value = "SimpleWebhookDTO", description = "This class is a representation of a {@link com.docdoku.core.hooks.SimpleWebhookApp} entity")
public class SimpleWebhookDTO implements Serializable {

    @ApiModelProperty(value = "SimpleWebhookDTO webhook")
    private WebhookDTO webhook;

    @ApiModelProperty(value = "SimpleWebhookApp method")
    private String method;

    @ApiModelProperty(value = "SimpleWebhookApp uri")
    private String uri;

    @ApiModelProperty(value = "SimpleWebhookApp authorization")
    private String authorization;

    public SimpleWebhookDTO() {
    }

    public SimpleWebhookDTO(WebhookDTO webhook, String method, String uri, String authorization) {
        this.webhook = webhook;
        this.method = method;
        this.uri = uri;
        this.authorization = authorization;
    }

    public WebhookDTO getWebhook() {
        return webhook;
    }

    public void setWebhook(WebhookDTO webhook) {
        this.webhook = webhook;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }
}
