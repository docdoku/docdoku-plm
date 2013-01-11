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

import java.io.Serializable;
import java.util.*;

/**
 *
 * @author Florent Garin
 */
public class DocumentIterationDTO implements Serializable {

    private String workspaceId;
    private String documentMasterId;
    private String documentMasterVersion;
    private int iteration;
    private Date creationDate;
    private UserDTO author;
    private String revisionNote;
    private List<String> attachedFiles;
    private List<InstanceAttributeDTO> instanceAttributes;
    private List<DocumentIterationDTO> linkedDocuments;

    public DocumentIterationDTO() {
    }

    public DocumentIterationDTO(String pWorkspaceId, String pDocumentMasterId, String pDocumentMasterVersion, int pIteration) {
        workspaceId = pWorkspaceId;
        documentMasterId = pDocumentMasterId;
        documentMasterVersion = pDocumentMasterVersion;
        iteration = pIteration;
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

    public void setRevisionNote(String pRevisionNote) {
        revisionNote = pRevisionNote;
    }

    public String getRevisionNote() {
        return revisionNote;
    }

    public List<String> getAttachedFiles() {
        return attachedFiles;
    }

    public List<DocumentIterationDTO> getLinkedDocuments() {
        return linkedDocuments;
    }

    public void setAttachedFiles(List<String> attachedFiles) {
        this.attachedFiles = attachedFiles;
    }

    public void setLinkedDocuments(List<DocumentIterationDTO> linkedDocuments) {
        this.linkedDocuments = linkedDocuments;
    }

    public List<InstanceAttributeDTO> getInstanceAttributes() {
        return instanceAttributes;
    }

    public void setInstanceAttributes(List<InstanceAttributeDTO> instanceAttributes) {
        this.instanceAttributes = instanceAttributes;
    }

    @Override
    public String toString() {
        return workspaceId + "-" + documentMasterId + "-" + documentMasterVersion + "-" + iteration;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String pWorkspaceId) {
        workspaceId = pWorkspaceId;
    }

    public String getDocumentMasterId() {
        return documentMasterId;
    }

    public void setDocumentMasterId(String pDocumentMasterId) {
        documentMasterId = pDocumentMasterId;
    }

    public String getDocumentMasterVersion() {
        return documentMasterVersion;
    }

    public void setDocumentMasterVersion(String pDocumentMasterVersion) {
        this.documentMasterVersion = pDocumentMasterVersion;
    }

    public int getIteration() {
        return iteration;
    }

    public void setIteration(int pIteration) {
        iteration = pIteration;
    }
}
