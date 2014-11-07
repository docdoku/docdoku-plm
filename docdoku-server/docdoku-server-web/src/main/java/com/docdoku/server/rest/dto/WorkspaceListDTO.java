package com.docdoku.server.rest.dto;

import java.util.ArrayList;
import java.util.List;

public class WorkspaceListDTO {
    private List<WorkspaceDTO> administratedWorkspaces = new ArrayList<>();
    private List<WorkspaceDTO> allWorkspaces = new ArrayList<>();

    public WorkspaceListDTO() {
    }

    public void addAdministratedWorkspaces(WorkspaceDTO workspace) {
        this.administratedWorkspaces.add(workspace);
    }

    public List<WorkspaceDTO> getAdministratedWorkspaces() {
        return administratedWorkspaces;
    }

    public void setAdministratedWorkspaces(List<WorkspaceDTO> administratedWorkspaces) {
        this.administratedWorkspaces = administratedWorkspaces;
    }

    public List<WorkspaceDTO> getAllWorkspaces() {
        return allWorkspaces;
    }

    public void setAllWorkspaces(List<WorkspaceDTO> allWorkspaces) {
        this.allWorkspaces = allWorkspaces;
    }

    public void addAllWorkspaces(WorkspaceDTO workspace) {
        this.allWorkspaces.add(workspace);
    }
}
