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
package com.docdoku.core.product;

import com.docdoku.core.common.User;
import com.docdoku.core.meta.InstanceAttribute;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
import javax.persistence.OrderBy;
import javax.persistence.OrderColumn;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlTransient;

/**
 * This class encapsulates the various states of a part whereas its unchanging
 * attributes are hold on a <code>PartMaster</code>.
 *
 * @author Florent Garin
 * @version 1.1, 18/05/11
 * @since   V1.1
 */
@IdClass(com.docdoku.core.product.PartIterationKey.class)
@Entity
public class PartIteration implements Serializable, Comparable<PartIteration> {
    
    @Id
    @ManyToOne(optional=false, fetch=FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name="PARTMASTER_NUMBER", referencedColumnName="PARTMASTER_NUMBER"),
        @JoinColumn(name="PARTREVISION_VERSION", referencedColumnName="VERSION"),
        @JoinColumn(name="WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
    })
    private PartRevision partRevision;
    
    @Id
    private int iteration;

    @OrderBy("quality")
    @OneToMany(cascade = {CascadeType.REMOVE, CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinTable(inverseJoinColumns = {
        @JoinColumn(name = "GEOMETRY_FULLNAME", referencedColumnName = "FULLNAME")
    },
    joinColumns = {
        @JoinColumn(name = "PARTITERATION_WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID"),
        @JoinColumn(name = "PARTITERATION_PARTMASTER_NUMBER", referencedColumnName = "PARTMASTER_NUMBER"),
        @JoinColumn(name = "PARTITERATION_PARTREVISION_VERSION", referencedColumnName = "PARTREVISION_VERSION"),
        @JoinColumn(name = "PARTITERATION_ITERATION", referencedColumnName = "ITERATION")
    })
    private List<Geometry> geometries = new LinkedList<Geometry>();

    private String iterationNote;

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
        @JoinColumn(name="PARTITERATION_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID"),
        @JoinColumn(name="PARTITERATION_PARTMASTER_NUMBER", referencedColumnName="PARTMASTER_NUMBER"),
        @JoinColumn(name="PARTITERATION_PARTREVISION_VERSION", referencedColumnName="PARTREVISION_VERSION"),
        @JoinColumn(name="PARTITERATION_ITERATION", referencedColumnName="ITERATION")
    })
    private Map<String, InstanceAttribute> instanceAttributes=new HashMap<String, InstanceAttribute>();

    @OrderColumn(name="COMPONENT_ORDER")
    @OneToMany(orphanRemoval=true, cascade=CascadeType.ALL, fetch=FetchType.LAZY)
    @JoinTable(
    inverseJoinColumns={
        @JoinColumn(name="COMPONENT_ID", referencedColumnName="ID")
    },
    joinColumns={
        @JoinColumn(name="PARTITERATION_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID"),
        @JoinColumn(name="PARTITERATION_PARTMASTER_NUMBER", referencedColumnName="PARTMASTER_NUMBER"),
        @JoinColumn(name="PARTITERATION_PARTREVISION_VERSION", referencedColumnName="PARTREVISION_VERSION"),
        @JoinColumn(name="PARTITERATION_ITERATION", referencedColumnName="ITERATION")
    })
    private List<PartUsageLink> components=new LinkedList<PartUsageLink>();
    
    /*
    private Type type;
    public enum Type {COMPONENT, INSEPARABLE_ASSEMBLY, SEPARABLE_ASSEMBLY}
    */
    
    private Source source;   
    public enum Source {MAKE, BUY}
    
    public PartIteration(){
    }
    
    
    public PartIteration(PartRevision pPartRevision, int pIteration, User pAuthor) {
        setPartRevision(pPartRevision);
        iteration = pIteration;
        author = pAuthor;
    }
    
    public String getWorkspaceId() {
        return partRevision==null?"":partRevision.getWorkspaceId();
    }

    public List<Geometry> getGeometries() {
        return geometries;
    }

    public void setGeometries(List<Geometry> geometries) {
        this.geometries = geometries;
    }

    
    public boolean removeGeometry(Geometry pGeometry){
        return geometries.remove(pGeometry);
    }
    
    public void addGeometry(Geometry pGeometry){
        geometries.add(pGeometry);
    }    
    
    public String getPartNumber() {
        return partRevision==null?"":partRevision.getPartNumber();
    }
    
    public String getPartVersion() {
        return partRevision==null?"":partRevision.getVersion();
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public String getIterationNote() {
        return iterationNote;
    }

    public void setIterationNote(String iterationNote) {
        this.iterationNote = iterationNote;
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

    @XmlTransient
    public PartRevision getPartRevision() {
        return partRevision;
    }

    public void setPartRevision(PartRevision partRevision) {
        this.partRevision = partRevision;
    }

    public List<PartUsageLink> getComponents() {
        return components;
    }

    public void setComponents(List<PartUsageLink> components) {
        this.components = components;
    }
    
    public PartIterationKey getKey() {
        return new PartIterationKey(partRevision.getKey(),iteration);
    }
    
    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }   
    
    public boolean isAssembly(){
        return components==null?false:!components.isEmpty();
    }

    @Override
    public int hashCode() {
        int hash = 1;
	hash = 31 * hash + getWorkspaceId().hashCode();
	hash = 31 * hash + getPartNumber().hashCode();
        hash = 31 * hash + getPartVersion().hashCode();
        hash = 31 * hash + iteration;
	return hash;
    }
    
    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof PartIteration))
            return false;
        PartIteration partI = (PartIteration) pObj;
        return ((partI.getPartNumber().equals(getPartNumber())) && (partI.getWorkspaceId().equals(getWorkspaceId()))  && (partI.getPartVersion().equals(getPartVersion())) && (partI.iteration==iteration));
    }

    
    @Override
    public int compareTo(PartIteration pPart) {
        
        int wksComp = getWorkspaceId().compareTo(pPart.getWorkspaceId());
        if (wksComp != 0)
            return wksComp;
        int mpartNumberComp = getPartNumber().compareTo(pPart.getPartNumber());
        if (mpartNumberComp != 0)
            return mpartNumberComp;
        int mpartVersionComp = getPartVersion().compareTo(pPart.getPartVersion());
        if (mpartVersionComp != 0)
            return mpartVersionComp;
        else
            return iteration-pPart.iteration;
    }
    
}
