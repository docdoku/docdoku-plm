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

import com.docdoku.core.document.DocumentRevision;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@XmlRootElement
public class PartIterationDTO implements Serializable {

    private String workspaceId;
    private int iteration;
    private String nativeCADFile;
    private String iterationNote;
    private UserDTO author;

    private Date creationDate;
    private Date modificationDate;
    private Date checkInDate;
    private List<InstanceAttributeDTO> instanceAttributes;
    private List<InstanceAttributeTemplateDTO> instanceAttributeTemplates;
    private List<PartUsageLinkDTO> components;
    private List<DocumentRevisionDTO> linkedDocuments;
    private String number;
    private String name;
    private String version;
    private List<String> attachedFiles;

    public PartIterationDTO() {
    }

    public PartIterationDTO(String pWorkspaceId, String pName, String pNumber, String pVersion, int pIteration) {
        workspaceId = pWorkspaceId;
        number = pNumber;
        name = pName;
        version = pVersion;
        iteration = pIteration;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public int getIteration() {
        return iteration;
    }

    public void setIteration(int iteration) {
        this.iteration = iteration;
    }

    public String getNativeCADFile() {
        return nativeCADFile;
    }

    public void setNativeCADFile(String nativeCADFile) {
        this.nativeCADFile = nativeCADFile;
    }

    public String getIterationNote() {
        return iterationNote;
    }

    public void setIterationNote(String iterationNote) {
        this.iterationNote = iterationNote;
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

    public List<InstanceAttributeDTO> getInstanceAttributes() {
        return instanceAttributes;
    }

    public void setInstanceAttributes(List<InstanceAttributeDTO> instanceAttributes) {
        this.instanceAttributes = instanceAttributes;
    }

    public List<PartUsageLinkDTO> getComponents() {
        return components;
    }

    public void setComponents(List<PartUsageLinkDTO> components) {
        this.components = components;
    }

    public List<DocumentRevisionDTO> getLinkedDocuments() {
        return linkedDocuments;
    }

    public void setLinkedDocuments(List<DocumentRevisionDTO> linkedDocuments) {
        this.linkedDocuments = linkedDocuments;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<InstanceAttributeTemplateDTO> getInstanceAttributeTemplates() {
        return instanceAttributeTemplates;
    }

    public void setInstanceAttributeTemplates(List<InstanceAttributeTemplateDTO> instanceAttributeTemplates) {
        this.instanceAttributeTemplates = instanceAttributeTemplates;
    }

    public List<String> getAttachedFiles() {
        return attachedFiles;
    }

    public void setAttachedFiles(List<String> attachedFiles) {
        this.attachedFiles = attachedFiles;
    }

}
