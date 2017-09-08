package com.docdoku.core.hooks;

import com.docdoku.core.common.Workspace;

import javax.persistence.*;
import java.io.Serializable;

@MappedSuperclass
public abstract class Webhook implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected int id;

    protected String name;

    protected boolean active;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    protected Workspace workspace;

    protected Webhook(String name, boolean active, Workspace workspace) {
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
}
