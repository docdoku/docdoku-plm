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

package com.docdoku.core.product;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.meta.InstanceAttributeTemplate;
import com.docdoku.core.workflow.WorkflowModel;
import com.docdoku.core.security.ACL;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

/**
 * A model object from which we can create a {@link PartMaster}.
 * Creating a part through a model offers the ability to enforce a input
 * mask for the part ID, as well as some insuring that the starting
 * iteration defines some custom attributes or has some specific binary files.
 * 
 * @author Florent Garin
 * @version 1.1, 23/01/12
 * @since   V1.0
 */
@Table(name="PARTMASTERTEMPLATE")
@IdClass(PartMasterTemplateKey.class)
@Entity
@NamedQueries({
        @NamedQuery(name="PartMasterTemplate.findWhereLOV",
                query="SELECT p FROM PartMasterTemplate p WHERE EXISTS ( SELECT i FROM InstanceAttributeTemplate i, ListOfValuesAttributeTemplate il WHERE i member of p.attributeTemplates AND i = il AND il.lov.name = :lovName AND il.lov.workspaceId = :workspace_id)")
})
public class PartMasterTemplate implements Serializable, Comparable<PartMasterTemplate> {

    @Column(length=100)
    @Id
    private String id="";

    @Column(name = "WORKSPACE_ID", length=100, nullable = false, insertable = false, updatable = false)
    @Id
    private String workspaceId="";

    private boolean idGenerated;

    private String partType;

    private String mask;

    @OneToOne(orphanRemoval = true, cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    private ACL acl;

    @OneToOne(orphanRemoval=true, cascade = {CascadeType.REMOVE, CascadeType.REFRESH}, fetch = FetchType.EAGER)
    private BinaryResource attachedFile;

    @OrderColumn(name="ATTR_ORDER")
    @OneToMany(cascade={CascadeType.ALL}, fetch=FetchType.EAGER, orphanRemoval = true)
    @JoinTable(name="PARTMASTERTEMPLATE_ATTR",
            inverseJoinColumns={
                    @JoinColumn(name="INSTANCEATTRIBUTETEMPLATE_ID", referencedColumnName="ID")
            },
            joinColumns={
                    @JoinColumn(name="WORKSPACE_ID", referencedColumnName="WORKSPACE_ID"),
                    @JoinColumn(name="PARTMASTERTEMPLATE_ID", referencedColumnName="ID")
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

    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

    @ManyToOne(optional=false, fetch=FetchType.EAGER)
    private Workspace workspace;


    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name="WORKFLOWMODEL_ID", referencedColumnName="ID"),
            @JoinColumn(name="WORKFLOWMODEL_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
    })
    private WorkflowModel workflowModel;


    @Column(name = "WORKFLOWMODEL_ID", length=100, insertable = false, updatable = false)
    private String workflowModelId;

    @OrderColumn(name="ATTR_ORDER")
    @OneToMany(cascade={CascadeType.ALL}, fetch=FetchType.EAGER, orphanRemoval = true)
    @JoinTable(name="PARTMASTERTPL_INSTANCE_ATTR",
            inverseJoinColumns={
                    @JoinColumn(name="INSTANCEATTRIBUTETEMPLATE_ID", referencedColumnName="ID")
            },
            joinColumns={
                    @JoinColumn(name="WORKSPACE_ID", referencedColumnName="WORKSPACE_ID"),
                    @JoinColumn(name="PARTMASTERTEMPLATE_ID", referencedColumnName="ID")
            }
    )
    private List<InstanceAttributeTemplate> attributeInstanceTemplates = new ArrayList<>();


    public PartMasterTemplate() {
    }

    public PartMasterTemplate(Workspace pWorkspace, String pId, User pAuthor, String pPartType, String pMask, boolean pAttributesLocked) {
        id=pId;
        setWorkspace(pWorkspace);
        author = pAuthor;
        mask = pMask;   
        partType=pPartType;
        attributesLocked=pAttributesLocked;
    }

    public String getPartType() {
        return partType;
    }

    public void setPartType(String partType) {
        this.partType = partType;
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

    public void setId(String id) {
        this.id = id;
    }

    public boolean isIdGenerated() {
        return idGenerated;
    }

    public void setIdGenerated(boolean idGenerated) {
        this.idGenerated = idGenerated;
    }

    public BinaryResource getAttachedFile() {
        return attachedFile;
    }

    public void setAttachedFile(BinaryResource attachedFile) {
        this.attachedFile = attachedFile;
    }

    public ACL getAcl() {
        return acl;
    }

    public void setAcl(ACL acl) {
        this.acl = acl;
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
    
    public Workspace getWorkspace(){
        return workspace;
    }
    
    public String getId(){
        return id;
    }
    
    public String getWorkspaceId(){
        return workspaceId;
    }
    
    public PartMasterTemplateKey getKey() {
        return new PartMasterTemplateKey(workspaceId, id);
    }
    
    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof PartMasterTemplate)) {
            return false;
        }
        PartMasterTemplate template = (PartMasterTemplate) pObj;
        return ((template.id.equals(id)) && template.workspaceId.equals(workspaceId));
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
    
    public int compareTo(PartMasterTemplate pTemplate) {
        int wksComp = workspaceId.compareTo(pTemplate.workspaceId);
        if (wksComp != 0) {
            return wksComp;
        } else {
            return id.compareTo(pTemplate.id);
        }
    }

    public void setAttributeInstanceTemplates(List<InstanceAttributeTemplate> attributeInstanceTemplates) {
        this.attributeInstanceTemplates = attributeInstanceTemplates;
    }

    public List<InstanceAttributeTemplate> getAttributeInstanceTemplates() {
        return attributeInstanceTemplates;
    }
}
