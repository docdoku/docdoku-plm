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

package com.docdoku.core.document;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.common.FileHolder;
import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.meta.InstanceAttributeTemplate;
import com.docdoku.core.security.ACL;
import com.docdoku.core.workflow.WorkflowModel;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

/**
 * A model object from which we can create a {@link DocumentMaster}.
 * Creating a document through a model offers the ability to enforce a input
 * mask for the document ID, as well as some insuring that the starting
 * iteration defines some custom attributes or has some specific binary files.
 * 
 * @author Florent Garin
 * @version 1.1, 23/01/12
 * @since   V1.0
 */
@Table(name="DOCUMENTMASTERTEMPLATE")
@javax.persistence.IdClass(com.docdoku.core.document.DocumentMasterTemplateKey.class)
@javax.persistence.Entity
@NamedQueries({
        @NamedQuery(name="DocumentMasterTemplate.findWhereLOV", query="SELECT d FROM DocumentMasterTemplate d WHERE EXISTS ( SELECT i FROM InstanceAttributeTemplate i, ListOfValuesAttributeTemplate il WHERE i member of d.attributeTemplates AND i = il AND il.lov.name = :lovName AND il.lov.workspaceId = :workspace_id)"),
        @NamedQuery(name="DocumentMasterTemplate.findWhereWorkflowModel", query="SELECT d FROM DocumentMasterTemplate d WHERE :workflowModel = d.workflowModel")
})
public class DocumentMasterTemplate implements Serializable, FileHolder, Comparable<DocumentMasterTemplate> {

    @Column(length=100)
    @javax.persistence.Id
    private String id="";
    
    @javax.persistence.Column(name = "WORKSPACE_ID", length=100, nullable = false, insertable = false, updatable = false)
    @javax.persistence.Id
    private String workspaceId="";
    
    private boolean idGenerated;
    
    private String documentType;
    
    private String mask;

    @OneToOne(orphanRemoval = true, cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    private ACL acl;

    @OneToMany(cascade={CascadeType.REMOVE,CascadeType.REFRESH}, fetch=FetchType.EAGER)
    @JoinTable(name="DOCUMENTMASTERTEMPLATE_BINRES",
        inverseJoinColumns={
            @JoinColumn(name="ATTACHEDFILE_FULLNAME", referencedColumnName="FULLNAME")
        },
        joinColumns={
            @JoinColumn(name="WORKSPACE_ID", referencedColumnName="WORKSPACE_ID"),
            @JoinColumn(name="DOCUMENTMASTERTEMPLATE_ID", referencedColumnName="ID")
        }
    )
    private Set<BinaryResource> attachedFiles = new HashSet<>();

    @OrderColumn(name="ATTR_ORDER")
    @OneToMany(cascade={CascadeType.ALL}, fetch=FetchType.EAGER, orphanRemoval = true)
    @JoinTable(name="DOCUMENTMASTERTEMPLATE_ATTR",
            inverseJoinColumns={
                    @JoinColumn(name="INSTANCEATTRIBUTETEMPLATE_ID", referencedColumnName="ID")
            },
            joinColumns={
                    @JoinColumn(name="WORKSPACE_ID", referencedColumnName="WORKSPACE_ID"),
                    @JoinColumn(name="DOCUMENTMASTERTEMPLATE_ID", referencedColumnName="ID")
            }
    )
    private List<InstanceAttributeTemplate> attributeTemplates=new ArrayList<>();

    private boolean attributesLocked;
    
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name="AUTHOR_LOGIN", referencedColumnName="LOGIN"),
        @JoinColumn(name="AUTHOR_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
    })
    private User author;
    
    @javax.persistence.Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date creationDate;
    
    @javax.persistence.ManyToOne(optional=false, fetch=FetchType.EAGER)
    private Workspace workspace;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name="WORKFLOWMODEL_ID", referencedColumnName="ID"),
            @JoinColumn(name="WORKFLOWMODEL_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
    })
    private WorkflowModel workflowModel;


    @Column(name = "WORKFLOWMODEL_ID", length=100, insertable = false, updatable = false)
    private String workflowModelId;



    public DocumentMasterTemplate() {
    }
    
    public DocumentMasterTemplate(Workspace pWorkspace, String pId, User pAuthor, String pDocumentType, String pMask) {
        id=pId;
        setWorkspace(pWorkspace);
        author = pAuthor;
        mask = pMask;   
        documentType=pDocumentType;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getWorkflowModelId() {
        return workflowModelId;
    }

    public void setWorkflowModelId(String workflowModelId) {
        this.workflowModelId = workflowModelId;
    }

    public WorkflowModel getWorkflowModel() {
        return workflowModel;
    }

    public void setWorkflowModel(WorkflowModel workflowModel) {
        this.workflowModel = workflowModel;
        if (workflowModel == null) {
            setWorkflowModelId(null);
        } else {
            setWorkflowModelId(workflowModel.getId());
        }
    }

    public String getMask(){
        return mask;
    }
    
    public void setMask(String pMask){
        mask=pMask;
    }

    public void setAttachedFiles(Set<BinaryResource> attachedFiles) {
        this.attachedFiles = attachedFiles;
    }


    public void setId(String id) {
        this.id = id;
    }

    public boolean isIdGenerated() {
        return idGenerated;
    }

    public void setIdGenerated(boolean idGenerated) {
        this.idGenerated = idGenerated;
    }
    
    public boolean removeFile(BinaryResource pBinaryResource){
        return attachedFiles.remove(pBinaryResource);
    }
    
    public void addFile(BinaryResource pBinaryResource){
        attachedFiles.add(pBinaryResource);
    }
    @Override
    public Set<BinaryResource> getAttachedFiles() {
        return attachedFiles;
    }

    public List<InstanceAttributeTemplate> getAttributeTemplates() {
        return attributeTemplates;
    }
    
    public void setAttributeTemplates(List<InstanceAttributeTemplate> pAttributeTemplates) {
        attributeTemplates=pAttributeTemplates;
    }

    public boolean isAttributesLocked() {
        return attributesLocked;
    }

    public void setAttributesLocked(boolean attributesLocked) {
        this.attributesLocked = attributesLocked;
    }

    public void setAuthor(User pAuthor) {
        author = pAuthor;
    }
    
    public User getAuthor() {
        return author;
    }
    
    public void setCreationDate(Date pCreationDate) {
        creationDate = pCreationDate;
    }
    
    public Date getCreationDate() {
        return creationDate;
    }
    
    public void setWorkspace(Workspace pWorkspace){
        workspace=pWorkspace;
        workspaceId=workspace.getId();
    }
    public ACL getAcl() {
        return acl;
    }

    public void setAcl(ACL acl) {
        this.acl = acl;
    }
    public Workspace getWorkspace(){
        return workspace;
    }
    
    public String getId(){
        return id;
    }
    
    public String getWorkspaceId(){
        return workspaceId;
    }
    
    public DocumentMasterTemplateKey getKey() {
        return new DocumentMasterTemplateKey(workspaceId, id);
    }
    
    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof DocumentMasterTemplate)) {
            return false;
        }
        DocumentMasterTemplate template = (DocumentMasterTemplate) pObj;
        return template.id.equals(id) && template.workspaceId.equals(workspaceId);
    }
    
    @Override
    public int hashCode() {
        int hash = 1;
        hash = 31 * hash + workspaceId.hashCode();
        hash = 31 * hash + id.hashCode();
        return hash;
    }
    
    @Override
    public String toString() {
        return id;
    }
    
    public int compareTo(DocumentMasterTemplate pTemplate) {
        int wksComp = workspaceId.compareTo(pTemplate.workspaceId);
        if (wksComp != 0) {
            return wksComp;
        } else {
            return id.compareTo(pTemplate.id);
        }
    }
    
}
