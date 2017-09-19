package com.docdoku.core.hooks;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.io.Serializable;
import java.util.List;

@XmlSeeAlso({SimpleWebhookApp.class, SNSWebhookApp.class})
@Inheritance
@Table(name = "WEBHOOKAPP")
@Entity
public abstract class WebhookApp implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    public WebhookApp() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public abstract String getAppName();

}
