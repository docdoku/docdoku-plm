/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,  
 * but WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
 * GNU General Public License for more details.  
 *  
 * You should have received a copy of the GNU General Public License  
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.  
 */

package com.docdoku.server.rest.dto;

import com.docdoku.server.rest.dto.UserDTO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WorkflowModelDTO implements Serializable {

    private String id;
    private String reference;
    private String finalLifeCycleState;
    private UserDTO author;
    private Date creationDate;
    private List<ActivityModelDTO> activityModels;

    public WorkflowModelDTO() {
        activityModels = new ArrayList<ActivityModelDTO>();
    }

    public WorkflowModelDTO(String id) {
        this.id = id;
    }

    public WorkflowModelDTO(String id, String reference, String finalLifeCycleState, UserDTO author, Date creationDate, List<ActivityModelDTO> activityModels) {
        this.id = id;
        this.reference = reference;
        this.finalLifeCycleState = finalLifeCycleState;
        this.author = author;
        this.creationDate = creationDate;
        this.activityModels = activityModels;
    }

    public WorkflowModelDTO(String id, UserDTO pAuthor) {
        this.id = id;
        this.reference = id;
        this.author = pAuthor;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void addActivity(ActivityModelDTO activity) {
        this.activityModels.add(activity);
    }

    public void removeActivity(ActivityModelDTO activity) {
        this.activityModels.remove(activity);
    }

    public List<ActivityModelDTO> getActivityModels() {
        return this.activityModels;
    }

    public void setFinalLifeCycleState(String finalLifeCycleState) {
        this.finalLifeCycleState = finalLifeCycleState;
    }

    public String getFinalLifeCycleState() {
        return finalLifeCycleState;
    }

    public UserDTO getAuthor() {
        return author;
    }

    public void setAuthor(UserDTO author) {
        this.author = author;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public void setActivityModels(List<ActivityModelDTO> activityModels) {
        this.activityModels = activityModels;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }


}
