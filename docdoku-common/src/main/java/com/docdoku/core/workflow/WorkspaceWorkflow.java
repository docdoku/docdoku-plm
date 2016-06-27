/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2015 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,  
 * but WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
 * GNU Affero General Public License for more details.  
 *  
 * You should have received a copy of the GNU Affero General Public License  
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.  
 */
package com.docdoku.core.workflow;

import com.docdoku.core.workflow.Workflow;

import javax.persistence.*;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Workflows held by workspace
 *
 * @author Morgan Guimard
 */
@Table(name="WORKSPACE_WORKFLOW")
@javax.persistence.Entity
public class WorkspaceWorkflow implements Serializable {

    @Id
    @Column(name = "WORKSPACE_ID")
    private String workspaceId;

    @Id
    private String id;
    
    @OneToOne(orphanRemoval=true, cascade= CascadeType.ALL, fetch=FetchType.EAGER)
    private Workflow workflow;    
    
    @OneToMany(orphanRemoval=true, cascade= CascadeType.ALL, fetch= FetchType.EAGER)
    @JoinTable(name="WORKSPACE_ABORTED_WORKFLOW",
            inverseJoinColumns={
                    @JoinColumn(name="WORKFLOW_ID", referencedColumnName="ID")
            },
            joinColumns={
                    @JoinColumn(name="WORKSPACE_WORKFLOW_ID", referencedColumnName="ID"),
                    @JoinColumn(name="WORKSPACE_WORKFLOW_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
            })
    private List<Workflow> abortedWorkflows = new LinkedList<>();

    public WorkspaceWorkflow(){
    }

    public WorkspaceWorkflow(String workspaceId, String id, Workflow workflow) {
        this.workspaceId = workspaceId;
        this.id = id;
        this.workflow = workflow;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }

    public List<Workflow> getAbortedWorkflows() {
        return abortedWorkflows;
    }

    public void setAbortedWorkflows(List<Workflow> abortedWorkflows) {
        this.abortedWorkflows = abortedWorkflows;
    }

    public void addAbortedWorkflows(Workflow abortedWorkflow) {
        this.abortedWorkflows.add(abortedWorkflow);
    }

}