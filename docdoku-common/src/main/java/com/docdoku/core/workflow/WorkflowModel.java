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

import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.security.ACL;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

/**
 * This class is the model used to create instances of
 * {@link Workflow} attached to documents or parts.
 *
 * @author Florent Garin
 * @version 1.0, 02/06/08
 * @since   V1.0
 */
@Table(name="WORKFLOWMODEL")
@javax.persistence.IdClass(com.docdoku.core.workflow.WorkflowModelKey.class)
@javax.persistence.Entity
public class WorkflowModel implements Serializable, Cloneable {

    @Column(length=100)
    @javax.persistence.Id
    private String id="";

    @javax.persistence.Column(name = "WORKSPACE_ID", length=100, nullable = false, insertable = false, updatable = false)
    @javax.persistence.Id
    private String workspaceId="";



    @OneToMany(mappedBy = "workflowModel", cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    @OrderBy("step ASC")
    private List<ActivityModel> activityModels=new LinkedList<>();

    private String finalLifeCycleState;


    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name="AUTHOR_LOGIN", referencedColumnName="LOGIN"),
        @JoinColumn(name="AUTHOR_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
    })
    private User author;

    @javax.persistence.Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date creationDate;

    @javax.persistence.ManyToOne(optional=false, fetch=FetchType.EAGER)
    private Workspace workspace;

    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private ACL acl;


    public WorkflowModel() {

    }

    public WorkflowModel(Workspace pWorkspace, String pId, User pAuthor, String pFinalLifeCycleState) {
        this(pWorkspace,pId,pAuthor,pFinalLifeCycleState,new LinkedList<>());
    }

    public WorkflowModel(Workspace pWorkspace, String pId, User pAuthor, String pFinalLifeCycleState, ActivityModel[] pActivityModels) {
        this(pWorkspace, pId, pAuthor, pFinalLifeCycleState,new LinkedList<>(Arrays.asList(pActivityModels)));
    }
    public WorkflowModel(Workspace pWorkspace, String pId, User pAuthor, String pFinalLifeCycleState, List<ActivityModel> pActivityModels) {
        id=pId;
        setWorkspace(pWorkspace);
        author = pAuthor;
        finalLifeCycleState=pFinalLifeCycleState;
        activityModels = pActivityModels;
    }


    public void addActivityModel(int pStep, ActivityModel pActivity) {
        activityModels.add(pStep, pActivity);
        for(int i=pStep;i<activityModels.size();i++){
            activityModels.get(i).setStep(i);
        }
    }

    public int numberOfSteps(){
        return activityModels.size();
    }

    public ActivityModel removeActivityModel(int pStep) {
        ActivityModel activityModel =activityModels.remove(pStep);
        for(int i=pStep;i<activityModels.size();i++){
            activityModels.get(i).setStep(i);
        }
        return activityModel;
    }

    public List<ActivityModel> getActivityModels() {
        return activityModels;
    }

    public void setActivityModels(List<ActivityModel> activityModels) {
        this.activityModels=activityModels;
    }


    public ActivityModel setActivityModel(int pStep, ActivityModel pActivity) {
        pActivity.setStep(pStep);
        return activityModels.set(pStep, pActivity);
    }

    public ACL getAcl() {
        return acl;
    }

    public void setAcl(ACL acl) {
        this.acl = acl;
    }

    public Workflow createWorkflow(Map<Role, User> roleUserMap) {
        Workflow workflow = new Workflow(finalLifeCycleState);
        List<Activity> activities = workflow.getActivities();
        for(ActivityModel model:activityModels){
            Activity activity = model.createActivity(roleUserMap);
            activity.setWorkflow(workflow);
            if(model.getRelaunchActivity()!=null){
                activity.setRelaunchActivity(activities.get(model.getRelaunchActivity().getStep()));
            }
            activities.add(activity);
        }
        return workflow;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String lifeCycleStateOfStep(int pStep) {
        if(pStep == activityModels.size()) {
            return finalLifeCycleState;
        }
        return activityModels.get(pStep).getLifeCycleState();
    }

    public ActivityModel getActivityModel(int pIndex) {
        return activityModels.get(pIndex);
    }

    public String getFinalLifeCycleState() {
        return finalLifeCycleState;
    }
    public void setFinalLifeCycleState(String pFinalLifeCycleState) {
        finalLifeCycleState = pFinalLifeCycleState;
    }

    public void setAuthor(User pAuthor) {
        author = pAuthor;
    }

    public User getAuthor() {
        return author;
    }

    public void setCreationDate(Date pCreationDate) {
        creationDate = pCreationDate;
    }

    public Date getCreationDate() {
        return  creationDate;
    }

    public void setWorkspace(Workspace pWorkspace){
        workspace=pWorkspace;
        workspaceId=workspace.getId();
    }

    public Workspace getWorkspace(){
        return workspace;
    }

    public String getWorkspaceId(){
        return workspaceId;
    }

    public WorkflowModelKey getKey() {
        return new WorkflowModelKey(workspaceId, id);
    }

    public List<String> getLifeCycle(){
        List<String> lc=new LinkedList<>();
        for(ActivityModel activityModel:activityModels) {
            lc.add(activityModel.getLifeCycleState());
        }
        return lc;
    }

    public String getId(){
        return id;
    }

    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof WorkflowModel)) {
            return false;
        }
        WorkflowModel workflow = (WorkflowModel) pObj;
        return workflow.id.equals(id) && workflow.workspaceId.equals(workspaceId);
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = 31 * hash + workspaceId.hashCode();
        hash = 31 * hash + id.hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return id;
    }

    /**
     * perform a deep clone operation
     */
    @Override
    public WorkflowModel clone() {
        //TODO relaunchActivity reference should be changed!
        WorkflowModel clone;
        try {
            clone = (WorkflowModel) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
        //perform a deep copy
        List<ActivityModel> clonedActivityModels = new LinkedList<>();
        for (ActivityModel activityModel : activityModels) {
            ActivityModel clonedActivityModel=activityModel.clone();
            clonedActivityModel.setWorkflowModel(clone);
            clonedActivityModels.add(clonedActivityModel);
        }
        clone.activityModels = clonedActivityModels;

        if(creationDate!=null) {
            clone.creationDate = (Date) creationDate.clone();
        }
        return clone;
    }


}