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

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * @author Yassine Belouad
 */
@XmlRootElement
@ApiModel(value="DocumentCreationDTO", description="Use this class to create a new {@link com.docdoku.core.document.DocumentLink} entity")
public class DocumentCreationDTO implements Serializable, Comparable<DocumentCreationDTO> {

    @ApiModelProperty(value = "Workspace id")
    private String workspaceId;

    @ApiModelProperty(value = "Document master id")
    private String reference;

    @ApiModelProperty(value = "Document version")
    private String version;

    @ApiModelProperty(value = "Document type")
    private String type;

    @ApiModelProperty(value = "Document title")
    private String title;

    @ApiModelProperty(value = "Document description")
    private String description;

    @ApiModelProperty(value = "Workflow id to use")
    private String workflowModelId;

    @ApiModelProperty(value = "Workflow to instantiate")
    private String templateId;

    @ApiModelProperty(value = "Folder path")
    private String path;

    @ApiModelProperty(value = "Role mapping for workflow instantiation")
    private RoleMappingDTO[] roleMapping;

    @ApiModelProperty(value = "ACL to set on document")
    private ACLDTO acl;

    public DocumentCreationDTO() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setWorkspaceID(String workspaceID) {
        this.workspaceId = workspaceID;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public void setDocumentMsTemplate(String templateId) {
        this.templateId = templateId;
    }

    public String getWorkflowModelId() {
        return workflowModelId;
    }

    public void setWorkflowModelId(String workflowModelId) {
        this.workflowModelId = workflowModelId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }


    public RoleMappingDTO[] getRoleMapping() {
        return roleMapping;
    }

    public void setRoleMapping(RoleMappingDTO[] roleMapping) {
        this.roleMapping = roleMapping;
    }

    public ACLDTO getAcl() {
        return acl;
    }

    public void setAcl(ACLDTO acl) {
        this.acl = acl;
    }

    @Override
    public String toString() {
        return workspaceId + "-" + reference + "-" + version;
    }

    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof DocumentCreationDTO)) {
            return false;
        }
        DocumentCreationDTO docM = (DocumentCreationDTO) pObj;
        return docM.reference.equals(reference) && docM.workspaceId.equals(workspaceId) && docM.version.equals(version);

    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = 31 * hash + workspaceId.hashCode();
        hash = 31 * hash + reference.hashCode();
        hash = 31 * hash + version.hashCode();
        return hash;
    }

    public int compareTo(DocumentCreationDTO pDocM) {
        int wksComp = workspaceId.compareTo(pDocM.workspaceId);
        if (wksComp != 0) {
            return wksComp;
        }
        int refComp = reference.compareTo(pDocM.reference);
        if (refComp != 0) {
            return refComp;
        } else {
            return version.compareTo(pDocM.version);
        }
    }
}
