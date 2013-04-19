package com.docdoku.core.sharing;

import java.io.Serializable;

public class SharedEntityKey implements Serializable{

    private String workspace;
    private String uuid;

    public SharedEntityKey() {
    }

    public SharedEntityKey(String workspace, String uuid) {
        this.workspace = workspace;
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }


    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String pWorkspace) {
        workspace = pWorkspace;
    }

    @Override
    public String toString() {
        return workspace + "-" + uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SharedEntityKey that = (SharedEntityKey) o;

        if (uuid != null ? !uuid.equals(that.uuid) : that.uuid != null) return false;
        if (workspace != null ? !workspace.equals(that.workspace) : that.workspace != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = workspace != null ? workspace.hashCode() : 0;
        result = 31 * result + (uuid != null ? uuid.hashCode() : 0);
        return result;
    }
}
