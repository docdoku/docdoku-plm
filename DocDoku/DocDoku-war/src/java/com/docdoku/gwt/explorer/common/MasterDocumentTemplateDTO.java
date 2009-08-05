package com.docdoku.gwt.explorer.common;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Florent GARIN
 */
public class MasterDocumentTemplateDTO implements Serializable{
    
    private String workspaceId;
    private String id;
    private String documentType;
    private String author;
    private Date creationDate;
    private boolean idGenerated;
    private String mask;

    private Map<String,String> files;
    private Set<InstanceAttributeTemplateDTO> attributeTemplates;
    
    public MasterDocumentTemplateDTO(){
        
    }
    
    public MasterDocumentTemplateDTO(String workspaceId, String id, String documentType) {
        this.workspaceId=workspaceId;
        this.id=id;
        this.documentType=documentType;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
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

    public Map<String, String> getFiles() {
        return files;
    }

    public void setFiles(Map<String, String> files) {
        this.files = files;
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
