package com.docdoku.server.rest.dto;

import java.util.Date;
import java.util.List;
import java.util.Set;

public class PartMasterTemplateDTO {

    private String workspaceId;
    private String id;
    private String partType;
    private UserDTO author;
    private Date creationDate;
    private boolean idGenerated;
    private String mask;

    private List<String> attachedFiles;
    private Set<InstanceAttributeTemplateDTO> attributeTemplates;

    public PartMasterTemplateDTO(){
    }

    public PartMasterTemplateDTO(String workspaceId, String id, String partType) {
        this.workspaceId=workspaceId;
        this.id=id;
        this.partType=partType;
    }

    public UserDTO getAuthor() {
        return author;
    }

    public void setAuthor(UserDTO author) {
        this.author = author;
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

    public void setAttributeTemplates(Set<InstanceAttributeTemplateDTO> attributeTemplates) {
        this.attributeTemplates = attributeTemplates;
    }

    public Set<InstanceAttributeTemplateDTO> getAttributeTemplates() {
        return attributeTemplates;
    }

    public String getPartType() {
        return partType;
    }

    public void setPartType(String partType) {
        this.partType = partType;
    }
}