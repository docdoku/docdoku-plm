package com.docdoku.server.rest.dto;

import java.util.Set;

public class PartTemplateCreationDTO {

    private String workspaceId;
    private String reference;
    private String partType;
    private boolean idGenerated;
    private String mask;
    private String attachedFiles;
    private Set<InstanceAttributeTemplateDTO> attributeTemplates;

    public PartTemplateCreationDTO(){
    }

    public PartTemplateCreationDTO(String workspaceId, String partType) {
        this.workspaceId=workspaceId;
        this.partType=partType;
    }

    public String getPartType() {
        return partType;
    }

    public void setPartType(String partType) {
        this.partType = partType;
    }

    public String getMask() {
        return mask;
    }

    public void setMask(String mask) {
        this.mask = mask;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public String getAttachedFiles() {
        return attachedFiles;
    }

    public void setAttachedFiles(String attachedFiles) {
        this.attachedFiles = attachedFiles;
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

    public void setAttributeTemplates(Set<InstanceAttributeTemplateDTO> attributeTemplates) {
        this.attributeTemplates = attributeTemplates;
    }

    public Set<InstanceAttributeTemplateDTO> getAttributeTemplates() {
        return attributeTemplates;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

}
