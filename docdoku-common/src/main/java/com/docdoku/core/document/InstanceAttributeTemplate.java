/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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

import com.docdoku.core.meta.InstanceTextAttribute;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.meta.InstanceURLAttribute;
import com.docdoku.core.meta.InstanceNumberAttribute;
import com.docdoku.core.meta.InstanceBooleanAttribute;
import com.docdoku.core.meta.InstanceDateAttribute;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

/**
 * This class which is a part of
 * <a href="DocumentMasterTemplate.html">DocumentMasterTemplate</a>
 * hold the definition of the custom attribute of the documents
 * created by the template.
 * 
 * @author Florent Garin
 * @version 1.1, 23/01/12
 * @since   V1.0
 */
@Table(name="INSTANCEATTRIBUTETEMPLATE")
@javax.persistence.IdClass(com.docdoku.core.document.InstanceAttributeTemplateKey.class)
@Entity
public class InstanceAttributeTemplate implements Serializable {

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name = "WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID"),
        @JoinColumn(name = "DOCUMENTMASTERTEMPLATE_ID", referencedColumnName = "ID")
    })
    private DocumentMasterTemplate documentMasterTemplate;

    @javax.persistence.Column(name = "WORKSPACE_ID", length=50, nullable = false, insertable = false, updatable = false)
    @javax.persistence.Id
    private String workspaceId = "";

    @javax.persistence.Column(name = "DOCUMENTMASTERTEMPLATE_ID", length=50, nullable = false, insertable = false, updatable = false)
    @javax.persistence.Id
    private String documentMasterTemplateId = "";
    
    @Column(length=50)
    @javax.persistence.Id
    private String name = "";

    private AttributeType attributeType;

    public enum AttributeType {

        TEXT, NUMBER, DATE, BOOLEAN, URL
    }

    public InstanceAttributeTemplate() {
    }

    public InstanceAttributeTemplate(DocumentMasterTemplate pDocumentMasterTemplate, String pName, AttributeType pAttributeType) {
        setDocumentMasterTemplate(pDocumentMasterTemplate);
        name = pName;
        attributeType = pAttributeType;
    }

    public void setDocumentMasterTemplate(DocumentMasterTemplate pDocumentMasterTemplate) {
        documentMasterTemplate = pDocumentMasterTemplate;
        documentMasterTemplateId = documentMasterTemplate.getId();
        workspaceId = documentMasterTemplate.getWorkspaceId();

    }

    @XmlTransient
    public DocumentMasterTemplate getDocumentMasterTemplate() {
        return documentMasterTemplate;
    }

    public String getDocumentMasterTemplateId() {
        return documentMasterTemplateId;
    }

    public String getName() {
        return name;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public InstanceAttributeTemplate.AttributeType getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(InstanceAttributeTemplate.AttributeType attributeType) {
        this.attributeType = attributeType;
    }

    public InstanceAttribute createInstanceAttribute() {
        InstanceAttribute attr = null;
        switch (attributeType) {
            case TEXT:
                attr = new InstanceTextAttribute();
                attr.setName(name);
                break;
            case NUMBER:
                attr = new InstanceNumberAttribute();
                attr.setName(name);
                break;
            case BOOLEAN:
                attr = new InstanceBooleanAttribute();
                attr.setName(name);
                break;
            case DATE:
                attr = new InstanceDateAttribute();
                attr.setName(name);
                break;
            case URL :
                attr = new InstanceURLAttribute();
                attr.setName(name);
                break;
        }
        return attr;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = 31 * hash + workspaceId.hashCode();
        hash = 31 * hash + documentMasterTemplateId.hashCode();
        hash = 31 * hash + name.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof InstanceAttributeTemplate)) {
            return false;
        }
        InstanceAttributeTemplate attr = (InstanceAttributeTemplate) pObj;
        return ((attr.documentMasterTemplateId.equals(documentMasterTemplateId)) && (attr.workspaceId.equals(workspaceId)) && (attr.name.equals(name)));
    }

    @Override
    public String toString() {
        return name + "-" + attributeType;
    }
}
