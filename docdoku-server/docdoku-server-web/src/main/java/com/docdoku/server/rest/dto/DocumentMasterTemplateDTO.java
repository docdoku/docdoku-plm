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

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Florent Garin
 */
public class DocumentMasterTemplateDTO implements Serializable{
    
    private String workspaceId;
    private String id;
    private String documentType;
    private UserDTO author;
    private Date creationDate;
    private boolean idGenerated;
    private String mask;
    private String workflowModelId;

    private List<String> attachedFiles;
    private List<InstanceAttributeTemplateDTO> attributeTemplates;
    private boolean attributesLocked;
    private ACLDTO acl;
    
    public DocumentMasterTemplateDTO(){
        
    }
    
    public DocumentMasterTemplateDTO(String workspaceId, String id, String documentType) {
        this.workspaceId=workspaceId;
        this.id=id;
        this.documentType=documentType;
    }

    public UserDTO getAuthor() {
        return author;
    }

    public void setAuthor(UserDTO author) {
        this.author = author;
    }

    
    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getMask() {
        return mask;
    }

    public void setMask(String mask) {
        this.mask = mask;
    }


    public String getWorkflowModelId() {
        return workflowModelId;
    }

    public void setWorkflowModelId(String workflowModelId) {
        this.workflowModelId = workflowModelId;
    }

    public String getId() {
        return id;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public List<String> getAttachedFiles() {
        return attachedFiles;
    }

    public void setAttachedFiles(List<String> attachedFiles) {
        this.attachedFiles = attachedFiles;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public boolean isIdGenerated() {
        return idGenerated;
    }

    public void setIdGenerated(boolean idGenerated) {
        this.idGenerated = idGenerated;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<InstanceAttributeTemplateDTO> getAttributeTemplates() {
        return attributeTemplates;
    }

    public void setAttributeTemplates(List<InstanceAttributeTemplateDTO> attributeTemplates) {
        this.attributeTemplates = attributeTemplates;
    }

    public boolean isAttributesLocked() {
        return attributesLocked;
    }

    public void setAttributesLocked(boolean attributesLocked) {
        this.attributesLocked = attributesLocked;
    }

    public ACLDTO getAcl() {
        return acl;
    }

    public void setAcl(ACLDTO acl) {
        this.acl = acl;
    }
}
