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

package com.docdoku.server.rest.dto;

import io.swagger.annotations.ApiModel;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@XmlRootElement
@ApiModel(value="TaskDTO", description="This class is a representation of a {@link com.docdoku.core.workflow.Task} entity")
public class TaskDTO implements Serializable {

    private String closureComment;
    private String title;
    private String instructions;
    private int targetIteration;
    private Date closureDate;
    private String signature;

    private List<UserDTO> assignedUsers = new ArrayList<>();
    private List<UserGroupDTO> assignedGroups = new ArrayList<>();

    private UserDTO worker;
    private Status status;

    private String workspaceId;

    private int workflowId;
    private int activityStep;
    private int num;

    private String holderType;
    private String holderReference;
    private String holderVersion;

    public TaskDTO() {
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getClosureComment() {
        return closureComment;
    }

    public void setClosureComment(String closureComment) {
        this.closureComment = closureComment;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public int getTargetIteration() {
        return targetIteration;
    }

    public void setTargetIteration(int targetIteration) {
        this.targetIteration = targetIteration;
    }

    public Date getClosureDate() {
        return (closureDate != null) ? (Date) closureDate.clone() : null;
    }

    public void setClosureDate(Date date) {
        this.closureDate = (date != null) ? (Date) date.clone() : null;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public UserDTO getWorker() {
        return worker;
    }

    public void setWorker(UserDTO worker) {
        this.worker = worker;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public int getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(int workflowId) {
        this.workflowId = workflowId;
    }

    public int getActivityStep() {
        return activityStep;
    }

    public void setActivityStep(int activityStep) {
        this.activityStep = activityStep;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getHolderType() {
        return holderType;
    }

    public void setHolderType(String holderType) {
        this.holderType = holderType;
    }

    public String getHolderReference() {
        return holderReference;
    }

    public void setHolderReference(String holderReference) {
        this.holderReference = holderReference;
    }

    public String getHolderVersion() {
        return holderVersion;
    }

    public void setHolderVersion(String holderVersion) {
        this.holderVersion = holderVersion;
    }

    public List<UserDTO> getAssignedUsers() {
        return assignedUsers;
    }

    public void setAssignedUsers(List<UserDTO> assignedUsers) {
        this.assignedUsers = assignedUsers;
    }

    public List<UserGroupDTO> getAssignedGroups() {
        return assignedGroups;
    }

    public void setAssignedGroups(List<UserGroupDTO> assignedGroups) {
        this.assignedGroups = assignedGroups;
    }

    public enum Status {
        NOT_STARTED, IN_PROGRESS, APPROVED, REJECTED, NOT_TO_BE_DONE
    }
}
