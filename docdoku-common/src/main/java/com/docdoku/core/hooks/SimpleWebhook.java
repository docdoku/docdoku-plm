package com.docdoku.core.hooks;

import com.docdoku.core.common.Workspace;

import javax.persistence.Entity;
import javax.persistence.Table;

@Table(name = "SIMPLEWEBHOOK")
@Entity
public class SimpleWebhook extends Webhook {

    private String method;

    private String uri;

    private String authorization;

    public SimpleWebhook(String name, boolean active, Workspace workspace, String method, String authorization, String uri) {
        super(name, active, workspace);
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