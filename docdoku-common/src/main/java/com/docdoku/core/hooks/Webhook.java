package com.docdoku.core.hooks;

import com.docdoku.core.common.Workspace;

import javax.persistence.*;
import java.io.Serializable;

@Table(name = "WEBHOOK")
@Entity
@NamedQueries({
        @NamedQuery(name = "Webhook.findByWorkspace", query = "SELECT distinct(w) FROM Webhook w WHERE w.workspace.id = :workspaceId")
})

public class Webhook implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    private boolean active;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Workspace workspace;

    public Webhook(String name, boolean active, Workspace workspace) {
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

    public String getWorkspaceId() {
        return workspace.getId();
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }
}
