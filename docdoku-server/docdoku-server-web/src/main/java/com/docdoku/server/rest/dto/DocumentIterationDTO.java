/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2014 DocDoku SARL
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

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Florent Garin
 */
public class DocumentIterationDTO implements Serializable {

    private String workspaceId;
    private String id;
    private String documentMasterId;
    private String documentRevisionVersion;
    private int iteration;
    private Date creationDate;
    private UserDTO author;
    @XmlElement(nillable = true)
    private String revisionNote;
    private List<String> attachedFiles;
    private List<InstanceAttributeDTO> instanceAttributes;
    private List<DocumentIterationDTO> linkedDocuments;

    public DocumentIterationDTO() {
    }

    public DocumentIterationDTO(String pWorkspaceId, String pDocumentMasterId, String pDocumentRevisionVersion, int pIteration) {
        workspaceId = pWorkspaceId;
        documentMasterId = pDocumentMasterId;
        documentRevisionVersion = pDocumentRevisionVersion;
        iteration = pIteration;
    }

    public UserDTO getAuthor() {
        return author;
    }

    public String getId() {
        return documentMasterId+"-"+documentRevisionVersion+"-"+iteration;
    }

    public void setId(String id) {
        this.id = id;
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
        return workspaceId + "-" + documentMasterId + "-" + documentRevisionVersion + "-" + iteration;
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

    public String getDocumentRevisionVersion() {
        return documentRevisionVersion;
    }

    public void setDocumentRevisionVersion(String pDocumentRevisionVersion) {
        this.documentRevisionVersion = pDocumentRevisionVersion;
    }

    public int getIteration() {
        return iteration;
    }

    public void setIteration(int pIteration) {
        iteration = pIteration;
    }

    public DocumentRevisionDTO getDocumentRevision() {
        return new DocumentRevisionDTO(workspaceId, id+"-"+documentRevisionVersion, documentRevisionVersion);
    }
}
