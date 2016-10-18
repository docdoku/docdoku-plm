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
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author Florent Garin
 */
@XmlRootElement
@ApiModel(value="DocumentIterationDTO", description="This class is the representation of a {@link com.docdoku.core.document.DocumentIteration} entity")
public class DocumentIterationDTO implements Serializable {

    @ApiModelProperty(value = "Workspace id")
    private String workspaceId;

    @ApiModelProperty(value = "Document iteration id")
    private String id;

    @ApiModelProperty(value = "Document master id")
    private String documentMasterId;

    @ApiModelProperty(value = "Document version")
    private String version;

    @ApiModelProperty(value = "Document iteration")
    private int iteration;

    @ApiModelProperty(value = "Document creation date")
    private Date creationDate;

    @ApiModelProperty(value = "Document modification date")
    private Date modificationDate;

    @ApiModelProperty(value = "Document check in date")
    private Date checkInDate;

    @ApiModelProperty(value = "Document title")
    private String title;

    @ApiModelProperty(value = "Document author")
    private UserDTO author;

    @XmlElement(nillable = true)
    @ApiModelProperty(value = "Revision note")
    private String revisionNote;

    @ApiModelProperty(value = "Document iteration attached files")
    private List<BinaryResourceDTO> attachedFiles;

    @ApiModelProperty(value = "Document iteration attributes")
    private List<InstanceAttributeDTO> instanceAttributes;

    @ApiModelProperty(value = "Document iteration linked documents")
    private List<DocumentRevisionDTO> linkedDocuments;

    public DocumentIterationDTO() {
    }

    public DocumentIterationDTO(String pWorkspaceId, String pDocumentMasterId, String pVersion, int pIteration) {
        workspaceId = pWorkspaceId;
        documentMasterId = pDocumentMasterId;
        version = pVersion;
        iteration = pIteration;
    }

    public UserDTO getAuthor() {
        return author;
    }

    public void setAuthor(UserDTO author) {
        this.author = author;
    }

    public String getId() {
        return documentMasterId + "-" + version + "-" + iteration;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(Date modificationDate) {
        this.modificationDate = modificationDate;
    }

    public Date getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(Date checkInDate) {
        this.checkInDate = checkInDate;
    }

    public String getRevisionNote() {
        return revisionNote;
    }

    public void setRevisionNote(String pRevisionNote) {
        revisionNote = pRevisionNote;
    }

    public List<BinaryResourceDTO> getAttachedFiles() {
        return attachedFiles;
    }

    public void setAttachedFiles(List<BinaryResourceDTO> attachedFiles) {
        this.attachedFiles = attachedFiles;
    }

    public List<DocumentRevisionDTO> getLinkedDocuments() {
        return linkedDocuments;
    }

    public void setLinkedDocuments(List<DocumentRevisionDTO> linkedDocuments) {
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
        return workspaceId + "-" + documentMasterId + "-" + version + "-" + iteration;
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

    public int getIteration() {
        return iteration;
    }

    public void setIteration(int pIteration) {
        iteration = pIteration;
    }

    public DocumentRevisionDTO getDocumentRevision() {
        return new DocumentRevisionDTO(workspaceId, id + "-" + version, version);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
