package com.docdoku.core.hooks;

import com.docdoku.core.common.Workspace;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Table(name = "WEBHOOK")
@Entity
@NamedQueries({
        @NamedQuery(name = "Webhook.findByWorkspace", query = "SELECT distinct(w) FROM Webhook w WHERE w.workspace.id = :workspaceId")
})
public class Webhook implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    protected String name;

    protected boolean active;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    protected Workspace workspace;

    @OneToOne(orphanRemoval = true, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private WebhookApp webhookApp;

    public Webhook(WebhookApp webhookApp, String name, boolean active, Workspace workspace) {
        this.webhookApp = webhookApp;
        this.name = name;
        this.active = active;
        this.workspace = workspace;
    }

    public Webhook() {
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

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public WebhookApp getWebhookApp() {
        return webhookApp;
    }

    public void setWebhookApp(WebhookApp webhookApp) {
        this.webhookApp = webhookApp;
    }

    public List<WebhookAppParameter> getParameters() {
        return webhookApp.getParameters();
    }

    public String getAppName() {
        return webhookApp.getAppName();
    }
}
