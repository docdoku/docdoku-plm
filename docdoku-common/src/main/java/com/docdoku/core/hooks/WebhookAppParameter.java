package com.docdoku.core.hooks;

import java.io.Serializable;

public class WebhookAppParameter implements Serializable {

    private String name;
    private String value;

    public WebhookAppParameter() {
    }

    public WebhookAppParameter(String name, String value) {
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
