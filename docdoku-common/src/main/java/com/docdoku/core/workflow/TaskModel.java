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

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Map;

/**
 * This is the model for creating instances of {@link Task}
 * that belong to instances of {@link Activity} themselves
 * attached to instances of {@link Workflow}.
 * 
 * @author Florent Garin
 * @version 1.0, 02/06/08
 * @since   V1.0
 */
@Table(name="TASKMODEL")
@javax.persistence.IdClass(com.docdoku.core.workflow.TaskModelKey.class)
@NamedQueries({
        @NamedQuery(name="Role.findRolesInUseByRoleName", query="SELECT t FROM TaskModel t WHERE t.role.name = :roleName AND t.role.workspace = :workspace"),
        @NamedQuery(name="Role.findRolesInUse", query="SELECT t.role FROM TaskModel t WHERE t.role.workspace.id = :workspaceId")
})
@Entity
public class TaskModel implements Serializable, Cloneable {

    @Id
    @ManyToOne(optional=false, fetch=FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name="ACTIVITYMODEL_ID", referencedColumnName="ID")
    })
    private ActivityModel activityModel;
    
    @Id
    private int num;

    @Lob
    private String instructions;
    private String title;
    private int duration;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumns({
            @JoinColumn(name="ROLE_NAME", referencedColumnName="NAME"),
            @JoinColumn(name="ROLE_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
    })
    private Role role;


    public TaskModel(ActivityModel pActivityModel, int pNum, String pTitle, String pInstructions, Role pRole) {
        setActivityModel(pActivityModel);
        num=pNum;
        title=pTitle;
        role=pRole;
        instructions=pInstructions;
    }

    public TaskModel(ActivityModel pActivityModel, String pTitle, String pInstructions, Role pRole) {
        this(pActivityModel, 0,pTitle,pInstructions,pRole);
    }
    public TaskModel() {

    }

    public Task createTask(Map<Role,User> roleUserMap) {
        User worker = roleUserMap.get(role);
        return new Task(num, title,instructions,worker);
    }
    
    public String getTitle() {
        return title;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }


    public String getInstructions() {
        return instructions;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setTitle(String pTitle) {
        title=pTitle;
    }

    public void setInstructions(String pInstructions) {
        instructions = pInstructions;
    }

    @XmlTransient
    public ActivityModel getActivityModel() {
        return activityModel;
    }


    public void setActivityModel(ActivityModel activityModel) {
        this.activityModel = activityModel;
    }
    
    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getActivityModelId(){
        return activityModel==null?0:activityModel.getId();
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = 31 * hash + getActivityModelId();
        hash = 31 * hash + num;
        return hash;
    }
    
    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof TaskModel)) {
            return false;
        }
        TaskModel model = (TaskModel) pObj;
        return model.getActivityModelId()==getActivityModelId() &&
               model.num==num;
    }
    
    @Override
    public String toString() {
        return getActivityModelId() + "-" + num;
    }
    

    @Override
    public TaskModel clone() {
        TaskModel clone;
        try {
            clone = (TaskModel) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
        return clone;
    }

}
