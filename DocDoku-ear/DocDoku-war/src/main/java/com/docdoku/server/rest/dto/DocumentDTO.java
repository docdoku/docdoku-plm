/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
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

import com.docdoku.core.meta.*;
import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author Florent GARIN
 */
public class DocumentDTO implements Serializable {

    private String workspaceId;
    private String documentMasterId;
    private String documentMasterVersion;
    private int iteration;
    private Date creationDate;
    private UserDTO author;
    private String revisionNote;
    private Map<String, String> attachedFiles;
    private Map<String, InstanceAttributeDTO> instanceAttributes;
    private List<AttributesDTO> documentAttributes;
    private Set<DocumentDTO> linkedDocuments;

    public DocumentDTO() {
    }

    public DocumentDTO(String pWorkspaceId, String pDocumentMasterId, String pDocumentMasterVersion, int pIteration) {
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

    public Map<String, String> getAttachedFiles() {
        return attachedFiles;
    }

    public Set<DocumentDTO> getLinkedDocuments() {
        return linkedDocuments;
    }

    public void setAttachedFiles(Map<String, String> attachedFiles) {
        this.attachedFiles = attachedFiles;
    }

    public void setLinkedDocuments(Set<DocumentDTO> linkedDocuments) {
        this.linkedDocuments = linkedDocuments;
    }

    public Map<String, InstanceAttributeDTO> getInstanceAttributes() {
        return instanceAttributes;
    }

    public void setInstanceAttributes(Map<String, InstanceAttributeDTO> instanceAttributes) {
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

    public List<AttributesDTO> getDocumentAttributes() {
        if (instanceAttributes != null) {
            List<AttributesDTO> attributes = new ArrayList<AttributesDTO>();
            Object[] attributeList = getInstanceAttributes().values().toArray();

            for (int i = 0; i < attributeList.length; i++) {
                AttributesDTO attribute = new AttributesDTO();

                if (attributeList[i] instanceof InstanceTextAttribute) {
                    InstanceTextAttribute instanceAttribute = (InstanceTextAttribute) attributeList[i];
                    attribute.setName(instanceAttribute.getName());
                    attribute.setType(AttributesDTO.Type.TEXT);
                    attribute.setValue(instanceAttribute.getTextValue());
                } else if (attributeList[i] instanceof InstanceBooleanAttribute) {
                    InstanceBooleanAttribute instanceAttribute = (InstanceBooleanAttribute) attributeList[i];
                    attribute.setName(instanceAttribute.getName());
                    attribute.setType(AttributesDTO.Type.BOOLEAN);
                    attribute.setValue(Boolean.toString(instanceAttribute.isBooleanValue()));
                } else if (attributeList[i] instanceof InstanceNumberAttribute) {
                    InstanceNumberAttribute instanceAttribute = (InstanceNumberAttribute) attributeList[i];
                    attribute.setName(instanceAttribute.getName());
                    attribute.setType(AttributesDTO.Type.NUMBER);
                } else if (attributeList[i] instanceof InstanceDateAttribute) {
                    InstanceDateAttribute instanceAttribute = (InstanceDateAttribute) attributeList[i];
                    attribute.setName(instanceAttribute.getName());
                    attribute.setType(AttributesDTO.Type.DATE);
                    if(instanceAttribute.getDateValue()!=null){
                        attribute.setValue(instanceAttribute.getDateValue().toString());                    
                    }
                } else if (attributeList[i] instanceof InstanceURLAttribute) {
                    InstanceURLAttribute instanceAttribute = (InstanceURLAttribute) attributeList[i];
                    attribute.setName(instanceAttribute.getName());
                    attribute.setType(AttributesDTO.Type.URL);
                    attribute.setValue(instanceAttribute.getUrlValue());
                }
                attributes.add(attribute);
            }
            documentAttributes = attributes;
            return documentAttributes;
        } else {
            return documentAttributes;
        }
    }

    public void setDocumentAttributes(List<AttributesDTO> documentAttributes) {
        this.documentAttributes = documentAttributes;
    }

    public void setIteration(int pIteration) {
        iteration = pIteration;
    }
}
