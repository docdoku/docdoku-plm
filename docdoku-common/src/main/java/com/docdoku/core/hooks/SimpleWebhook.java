package com.docdoku.core.hooks;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Table(name = "SIMPLEWEBHOOK")
@Entity
public class SimpleWebhook {

    @Id
    @OneToOne(orphanRemoval = true)
    private Webhook webhook;

    private String method;

    private String uri;

    private String authorization;


    public SimpleWebhook(Webhook webhook, String method, String authorization, String uri) {
        this.webhook = webhook;
        this.method = method;
        this.authorization = authorization;
        this.uri = uri;
    }

    public SimpleWebhook() {
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