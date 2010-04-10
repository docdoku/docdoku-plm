/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010 DocDoku SARL
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
package com.docdoku.core.entities;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Base class for all instance attributes.  
 * 
 * @author Florent GARIN
 * @version 1.0, 02/06/08
 * @since   V1.0
 */
@javax.persistence.IdClass(com.docdoku.core.entities.keys.InstanceAttributeKey.class)
@XmlSeeAlso({InstanceTextAttribute.class, InstanceNumberAttribute.class, InstanceDateAttribute.class, InstanceBooleanAttribute.class, InstanceURLAttribute.class})
@Inheritance()
@Entity
public abstract class InstanceAttribute implements Serializable, Cloneable {

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name = "MASTERDOCUMENT_ID", referencedColumnName = "MASTERDOCUMENT_ID"),
        @JoinColumn(name = "MASTERDOCUMENT_VERSION", referencedColumnName = "MASTERDOCUMENT_VERSION"),
        @JoinColumn(name = "WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID"),
        @JoinColumn(name = "DOCUMENT_ITERATION", referencedColumnName = "ITERATION")
    })
    protected Document document;

    @javax.persistence.Column(name = "MASTERDOCUMENT_ID", length=50, nullable = false, insertable = false, updatable = false)
    @javax.persistence.Id
    private String masterDocumentId = "";

    @javax.persistence.Column(name = "MASTERDOCUMENT_VERSION", length=10, nullable = false, insertable = false, updatable = false)
    @javax.persistence.Id
    private String masterDocumentVersion = "";

    @javax.persistence.Column(name = "WORKSPACE_ID", length=50, nullable = false, insertable = false, updatable = false)
    @javax.persistence.Id
    private String workspaceId = "";

    @javax.persistence.Column(name = "DOCUMENT_ITERATION", nullable = false, insertable = false, updatable = false)
    @javax.persistence.Id
    private int documentIteration;
    
    @Column(length=50)
    @javax.persistence.Id
    protected String name = "";

    protected String attributeValue;

    public InstanceAttribute() {
    }

    public InstanceAttribute(Document pDoc, String pName) {
        setDocument(pDoc);
        name = pName;
    }

    public void setDocument(Document pDocument) {
        document = pDocument;
        masterDocumentId = pDocument.getMasterDocumentId();
        masterDocumentVersion = pDocument.getMasterDocumentVersion();
        workspaceId = pDocument.getWorkspaceId();
        documentIteration = pDocument.getIteration();
    }

    @XmlTransient
    public Document getDocument() {
        return document;
    }

    public String getMasterDocumentId() {
        return masterDocumentId;
    }

    public String getMasterDocumentVersion() {
        return masterDocumentVersion;
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

    @Override
    public int hashCode() {
        int hash = 1;
        hash = 31 * hash + workspaceId.hashCode();
        hash = 31 * hash + masterDocumentId.hashCode();
        hash = 31 * hash + masterDocumentVersion.hashCode();
        hash = 31 * hash + documentIteration;
        hash = 31 * hash + name.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof InstanceAttribute)) {
            return false;
        }
        InstanceAttribute attribute = (InstanceAttribute) pObj;
        return ((attribute.masterDocumentId.equals(masterDocumentId)) && (attribute.workspaceId.equals(workspaceId)) && (attribute.masterDocumentVersion.equals(masterDocumentVersion)) && (attribute.documentIteration == documentIteration) && (attribute.name.equals(name)));
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public InstanceAttribute clone() {
        InstanceAttribute clone = null;
        try {
            clone = (InstanceAttribute) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
        return clone;
    }

    public abstract Object getValue();

    public abstract boolean setValue(Object pValue);

    public boolean isValueEquals(Object pValue) {
        Object value = getValue();
        return value == null ? false : value.equals(pValue);
    }
}
