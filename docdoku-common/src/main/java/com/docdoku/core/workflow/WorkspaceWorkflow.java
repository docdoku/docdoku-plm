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

import com.docdoku.core.common.Workspace;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link Workflow}s are intended to be attached to a business object like
 * a document or a part for instance.
 * However sometimes we just need to start a workflow not linked to a particular document or part.
 * This especially happens when we want to use a workflow to manage an object living in an external
 * system.
 * Hence this class wraps the necessary data for managing a running workflow virtually attached to
 * an external object like the ordered list of aborted workflows. Moreover for convenience access,
 * instances of this class have a string based id, unique in the context of a specific workspace.
 *
 *
 * @version 2.5, 27/06/16
 * @since   V2.5
 *
 * @author Morgan Guimard
 */
@Table(name="WORKSPACE_WORKFLOW")
@IdClass(WorkspaceWorkflowKey.class)
@javax.persistence.Entity
public class WorkspaceWorkflow implements Serializable {

    @Column(name="ID", length=100)
    @Id
    private String id="";

    @Id
    @ManyToOne(optional=false, fetch=FetchType.EAGER)
    private Workspace workspace;
    
    @OneToOne(orphanRemoval=true, cascade= CascadeType.ALL, fetch=FetchType.EAGER)
    private Workflow workflow;    


    @OrderBy("abortedDate")
    @OneToMany(orphanRemoval=true, cascade= CascadeType.ALL, fetch= FetchType.EAGER)
    @JoinTable(name="WORKSPACE_ABORTED_WORKFLOW",
            inverseJoinColumns={
                    @JoinColumn(name="WORKFLOW_ID", referencedColumnName="ID")
            },
            joinColumns={
                    @JoinColumn(name="WORKSPACE_WORKFLOW_ID", referencedColumnName="ID"),
                    @JoinColumn(name="WORKSPACE_WORKFLOW_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
            })
    private List<Workflow> abortedWorkflows = new ArrayList<>();

    public WorkspaceWorkflow(){
    }

    public WorkspaceWorkflow(Workspace workspace, String id, Workflow workflow) {
        setWorkspace(workspace);
        this.id = id;
        this.workflow = workflow;
    }


    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public String getWorkspaceId() {
        return workspace == null ? "" : workspace.getId();
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