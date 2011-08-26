/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011 DocDoku SARL
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
package com.docdoku.core.product;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.common.FileHolder;
import com.docdoku.core.common.User;
import com.docdoku.core.meta.InstanceAttribute;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * This class encapsulates the various states of a part whereas its unchanging
 * attributes are hold on a <code>MasterPart</code>.
 *
 * @author Florent Garin
 * @version 1.1, 18/05/11
 * @since   V1.1
 */
@IdClass(com.docdoku.core.product.PartKey.class)
@Entity
public class Part implements Serializable, FileHolder, Comparable<Part>, Cloneable {
    
    @Id
    @ManyToOne(optional=false, fetch=FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name="MASTERPART_NUMBER", referencedColumnName="NUMBER"),
        @JoinColumn(name="MASTERPART_VERSION", referencedColumnName="VERSION"),
        @JoinColumn(name="WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
    })
    private MasterPart masterPart;
    
    @Id
    private int iteration;
    
    

    @OneToMany(cascade = {CascadeType.REMOVE, CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinTable(inverseJoinColumns = {
        @JoinColumn(name = "ATTACHEDFILES_FULLNAME", referencedColumnName = "FULLNAME")
    },
    joinColumns = {
        @JoinColumn(name = "PART_WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID"),
        @JoinColumn(name = "PART_MASTERPART_NUMBER", referencedColumnName = "MASTERPART_NUMBER"),
        @JoinColumn(name = "PART_MASTERPART_VERSION", referencedColumnName = "MASTERPART_VERSION"),
        @JoinColumn(name = "PART_ITERATION", referencedColumnName = "ITERATION")
    })
    private Set<BinaryResource> attachedFiles = new HashSet<BinaryResource>();

    private String revisionNote;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name = "AUTHOR_LOGIN", referencedColumnName = "LOGIN"),
        @JoinColumn(name = "AUTHOR_WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID")
    })
    private User author;

    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

    @OneToMany(orphanRemoval=true, cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    @MapKey(name="name")
    @JoinTable(
    inverseJoinColumns={
        @JoinColumn(name="INSTANCEATTRIBUTE_ID", referencedColumnName="ID")
    },
    joinColumns={
        @JoinColumn(name="PART_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID"),
        @JoinColumn(name="PART_MASTERPART_NUMBER", referencedColumnName="MASTERPART_NUMBER"),
        @JoinColumn(name="PART_MASTERPART_VERSION", referencedColumnName="MASTERPART_VERSION"),
        @JoinColumn(name="PART_ITERATION", referencedColumnName="ITERATION")
    })
    private Map<String, InstanceAttribute> instanceAttributes=new HashMap<String, InstanceAttribute>();

    
    @Override
    public Set<BinaryResource> getAttachedFiles() {
        return attachedFiles;
    }
    
    public String getWorkspaceId() {
        return masterPart==null?"":masterPart.getWorkspaceId();
    }
    
    public String getMasterPartNumber() {
        return masterPart==null?"":masterPart.getNumber();
    }
    
    public String getMasterPartVersion() {
        return masterPart==null?"":masterPart.getVersion();
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public String getRevisionNote() {
        return revisionNote;
    }

    public void setRevisionNote(String revisionNote) {
        this.revisionNote = revisionNote;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Map<String, InstanceAttribute> getInstanceAttributes() {
        return instanceAttributes;
    }

    public void setInstanceAttributes(Map<String, InstanceAttribute> instanceAttributes) {
        this.instanceAttributes = instanceAttributes;
    }

    public int getIteration() {
        return iteration;
    }

    public void setIteration(int iteration) {
        this.iteration = iteration;
    }

    
    public void setAttachedFiles(Set<BinaryResource> attachedFiles) {
        this.attachedFiles = attachedFiles;
    }
    
    
    
    @Override
    public int compareTo(Part pPart) {
        
        int wksComp = getWorkspaceId().compareTo(pPart.getWorkspaceId());
        if (wksComp != 0)
            return wksComp;
        int mpartNumberComp = getMasterPartNumber().compareTo(pPart.getMasterPartNumber());
        if (mpartNumberComp != 0)
            return mpartNumberComp;
        int mpartVersionComp = getMasterPartVersion().compareTo(pPart.getMasterPartVersion());
        if (mpartVersionComp != 0)
            return mpartVersionComp;
        else
            return iteration-pPart.iteration;
    }
    
    /**
     * perform a deep clone operation
     */
    @Override
    public Part clone() {
        Part clone = null;
        try {
            clone = (Part) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
        //perform a deep copy
        clone.attachedFiles = new HashSet<BinaryResource>(attachedFiles);
        
        //perform a deep copy
        Map<String, InstanceAttribute> clonedInstanceAttributes = new HashMap<String, InstanceAttribute>();
        for (InstanceAttribute attribute : instanceAttributes.values()) {
            InstanceAttribute clonedAttribute=attribute.clone();
            clonedInstanceAttributes.put(clonedAttribute.getName(),clonedAttribute);
        }
        clone.instanceAttributes = clonedInstanceAttributes;
        
        if(creationDate!=null)
            clone.creationDate = (Date) creationDate.clone();
        return clone;
    }
    
}
