/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011 DocDoku SARL
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

package com.docdoku.gwt.explorer.shared;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Florent Garin
 */
public class MasterDocumentDTO implements Serializable, Comparable<MasterDocumentDTO> {

    private String workspaceId;
    private String id;
    private String version;
    private String type;
    private UserDTO author;
    private Date creationDate;
    private String title;
    private UserDTO checkOutUser;
    private Date checkOutDate;
    private String[] tags;
    private String description;
    private boolean iterationSubscription;
    private boolean stateSubscription;
    private List<DocumentDTO> documentIterations;
    private WorkflowDTO workflow;

    public MasterDocumentDTO() {
    }

    public MasterDocumentDTO(String workspaceId, String id, String version) {
        this.workspaceId = workspaceId;
        this.id = id;
        this.version = version;

    }

    public UserDTO getAuthor() {
        return author;
    }

    public Date getCheckOutDate() {
        return checkOutDate;
    }

    public UserDTO getCheckOutUser() {
        return checkOutUser;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public WorkflowDTO getWorkflow() {
        return workflow;
    }

    public void setWorkflow(WorkflowDTO workflow) {
        this.workflow = workflow;
    }

    public String getLifeCycleState() {
        if (workflow != null)
            return workflow.getLifeCycleState();
        else
            return null;
    }

    public String[] getTags() {
        return tags;
    }

    public String getTitle() {
        return title;
    }

    public void setAuthor(UserDTO author) {
        this.author = author;
    }

    public void setCheckOutDate(Date checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public void setCheckOutUser(UserDTO checkOutUser) {
        this.checkOutUser = checkOutUser;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setWorkspaceID(String workspaceID) {
        this.workspaceId = workspaceID;
    }

    public void setID(String id) {
        this.id = id;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<DocumentDTO> getDocumentIterations() {
        return documentIterations;
    }

    public void setDocumentIterations(List<DocumentDTO> documentIterations) {
        this.documentIterations = documentIterations;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DocumentDTO getLastIteration() {
        int index = documentIterations.size() - 1;
        if (index < 0) {
            return null;
        } else {
            return documentIterations.get(index);
        }
    }

  
    public boolean isIterationSubscription() {
        return iterationSubscription;
    }

    public void setIterationSubscription(boolean iterationSubscription) {
        this.iterationSubscription = iterationSubscription;
    }

    public boolean isStateSubscription() {
        return stateSubscription;
    }

    public void setStateSubscription(boolean stateSubscription) {
        this.stateSubscription = stateSubscription;
    }

    @Override
    public String toString() {
        return workspaceId + "-" + id + "-" + version;
    }

    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof MasterDocumentDTO)) {
            return false;
        }
        MasterDocumentDTO mdoc = (MasterDocumentDTO) pObj;
        return ((mdoc.id.equals(id)) && (mdoc.workspaceId.equals(workspaceId)) && (mdoc.version.equals(version)));

    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = 31 * hash + workspaceId.hashCode();
        hash = 31 * hash + id.hashCode();
        hash = 31 * hash + version.hashCode();
        return hash;
    }

    public int compareTo(MasterDocumentDTO pMDoc) {
        int wksComp = workspaceId.compareTo(pMDoc.workspaceId);
        if (wksComp != 0) {
            return wksComp;
        }
        int idComp = id.compareTo(pMDoc.id);
        if (idComp != 0) {
            return idComp;
        } else {
            return version.compareTo(pMDoc.version);
        }
    }
}
