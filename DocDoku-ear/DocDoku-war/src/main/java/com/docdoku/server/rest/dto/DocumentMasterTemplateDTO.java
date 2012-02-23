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

import com.docdoku.server.rest.dto.InstanceAttributeTemplateDTO;
import com.docdoku.server.rest.dto.UserDTO;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Set;

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

    private Map<String,String> attachedFiles;
    private Set<InstanceAttributeTemplateDTO> attributeTemplates;
    
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

    
    
    
    public String getId() {
        return id;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public Map<String, String> getAttachedFiles() {
        return attachedFiles;
    }

    public void setAttachedFiles(Map<String, String> attachedFiles) {
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

    public void setAttributeTemplates(Set<InstanceAttributeTemplateDTO> attributeTemplates) {
        this.attributeTemplates = attributeTemplates;
    }

    public Set<InstanceAttributeTemplateDTO> getAttributeTemplates() {
        return attributeTemplates;
    }

    
    

}
