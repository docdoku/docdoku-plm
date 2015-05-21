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
import com.docdoku.core.common.FileHolder;
import com.docdoku.core.common.User;
import com.docdoku.core.document.DocumentLink;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.meta.InstanceAttributeTemplate;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.*;

/**
 * This class encapsulates the various states of a part whereas its unchanging
 * attributes are hold on a {@link PartMaster}.
 *
 * @author Florent Garin
 * @version 1.1, 18/05/11
 * @since   V1.1
 */
@Table(name="PARTITERATION")
@IdClass(com.docdoku.core.product.PartIterationKey.class)
@NamedQueries({
        @NamedQuery(name="PartIteration.findUsedByAsSubstitute", query="SELECT p FROM PartIteration p JOIN p.components l JOIN l.substitutes s WHERE s.substitute = :partMaster"),
        @NamedQuery(name="PartIteration.findUsedByAsComponent", query="SELECT p FROM PartIteration p JOIN p.components l WHERE l.component = :partMaster"),
        @NamedQuery(name="PartIteration.findDistinctInstanceAttributes", query="SELECT DISTINCT i FROM InstanceAttribute i LEFT JOIN PartIteration p WHERE p.partRevision.partMaster.workspace.id = :workspaceId AND i member of p.instanceAttributes"),
})
@Entity
public class PartIteration implements Serializable, FileHolder, Comparable<PartIteration>, Cloneable {
    
    @Id
    @ManyToOne(optional=false, fetch=FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name="PARTMASTER_PARTNUMBER", referencedColumnName="PARTMASTER_PARTNUMBER"),
        @JoinColumn(name="PARTREVISION_VERSION", referencedColumnName="VERSION"),
        @JoinColumn(name="WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
    })
    private PartRevision partRevision;
    
    @Id
    private int iteration;

    @OneToMany(orphanRemoval=true, cascade = {CascadeType.REMOVE, CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinTable(name="PARTITERATION_GEOMETRY", inverseJoinColumns = {
        @JoinColumn(name = "GEOMETRY_FULLNAME", referencedColumnName = "FULLNAME")
    },
    joinColumns = {
        @JoinColumn(name = "WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID"),
        @JoinColumn(name = "PARTMASTER_PARTNUMBER", referencedColumnName = "PARTMASTER_PARTNUMBER"),
        @JoinColumn(name = "PARTREVISION_VERSION", referencedColumnName = "PARTREVISION_VERSION"),
        @JoinColumn(name = "ITERATION", referencedColumnName = "ITERATION")
    })
    private Set<Geometry> geometries = new HashSet<>();

    @OneToOne(orphanRemoval=true, cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    private BinaryResource nativeCADFile;

    @OneToMany(orphanRemoval = true, cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    @JoinTable(name="PARTITERATION_DOCUMENTLINK",
    inverseJoinColumns={
        @JoinColumn(name="DOCUMENTLINK_ID", referencedColumnName="ID")
    },
    joinColumns={
        @JoinColumn(name="WORKSPACE_ID", referencedColumnName="WORKSPACE_ID"),
        @JoinColumn(name="PARTMASTER_PARTNUMBER", referencedColumnName="PARTMASTER_PARTNUMBER"),
        @JoinColumn(name="PARTREVISION_VERSION", referencedColumnName="PARTREVISION_VERSION"),
        @JoinColumn(name="ITERATION", referencedColumnName="ITERATION")
    })
    private Set<DocumentLink> linkedDocuments=new HashSet<>();


    @OneToMany(orphanRemoval=true, cascade = {CascadeType.REMOVE, CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinTable(name="PARTITERATION_BINRES", inverseJoinColumns = {
        @JoinColumn(name = "ATTACHEDFILE_FULLNAME", referencedColumnName = "FULLNAME")
    },
    joinColumns = {
        @JoinColumn(name = "WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID"),
        @JoinColumn(name = "PARTMASTER_PARTNUMBER", referencedColumnName = "PARTMASTER_PARTNUMBER"),
        @JoinColumn(name = "PARTREVISION_VERSION", referencedColumnName = "PARTREVISION_VERSION"),
        @JoinColumn(name = "ITERATION", referencedColumnName = "ITERATION")
    })
    private Set<BinaryResource> attachedFiles = new HashSet<>();

    private String iterationNote;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name = "AUTHOR_LOGIN", referencedColumnName = "LOGIN"),
        @JoinColumn(name = "AUTHOR_WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID")
    })
    private User author;

    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date modificationDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date checkInDate;

    @OneToMany(orphanRemoval=true, cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    @OrderColumn(name="ATTRIBUTE_ORDER")
    @JoinTable(name="PARTITERATION_ATTRIBUTE",
    inverseJoinColumns={
        @JoinColumn(name="INSTANCEATTRIBUTE_ID", referencedColumnName="ID")
    },
    joinColumns={
        @JoinColumn(name="WORKSPACE_ID", referencedColumnName="WORKSPACE_ID"),
        @JoinColumn(name="PARTMASTER_PARTNUMBER", referencedColumnName="PARTMASTER_PARTNUMBER"),
        @JoinColumn(name="PARTREVISION_VERSION", referencedColumnName="PARTREVISION_VERSION"),
        @JoinColumn(name="ITERATION", referencedColumnName="ITERATION")
    })
    private List<InstanceAttribute> instanceAttributes=new ArrayList<>();

    @OneToMany(orphanRemoval=true, cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    @OrderColumn(name="ATTRIBUTE_ORDER")
    @JoinTable(name="PARTITERATION_PATHDATA_ATTR",
            inverseJoinColumns={
                    @JoinColumn(name="INSTANCEATTRIBUTE_TEMPLATE_ID", referencedColumnName="ID")
            },
            joinColumns={
                    @JoinColumn(name="WORKSPACE_ID", referencedColumnName="WORKSPACE_ID"),
                    @JoinColumn(name="PARTMASTER_PARTNUMBER", referencedColumnName="PARTMASTER_PARTNUMBER"),
                    @JoinColumn(name="PARTREVISION_VERSION", referencedColumnName="PARTREVISION_VERSION"),
                    @JoinColumn(name="ITERATION", referencedColumnName="ITERATION")
            })
    private List<InstanceAttributeTemplate> instanceAttributeTemplates=new ArrayList<>();

    @OrderColumn(name="COMPONENT_ORDER")
    @OneToMany(orphanRemoval=true, cascade=CascadeType.ALL, fetch=FetchType.LAZY)
    @JoinTable(name="PARTITERATION_PARTUSAGELINK",
    inverseJoinColumns={
        @JoinColumn(name="COMPONENT_ID", referencedColumnName="ID")
    },
    joinColumns={
        @JoinColumn(name="WORKSPACE_ID", referencedColumnName="WORKSPACE_ID"),
        @JoinColumn(name="PARTMASTER_PARTNUMBER", referencedColumnName="PARTMASTER_PARTNUMBER"),
        @JoinColumn(name="PARTREVISION_VERSION", referencedColumnName="PARTREVISION_VERSION"),
        @JoinColumn(name="ITERATION", referencedColumnName="ITERATION")
    })
    private List<PartUsageLink> components=new LinkedList<>();
    
    /*
    private Type type;
    public enum Type {COMPONENT, INSEPARABLE_ASSEMBLY, SEPARABLE_ASSEMBLY}
    */
    
    private Source source;   
    public enum Source {
        MAKE, BUY
    }
    
    public PartIteration() {
    }

    public PartIteration(PartRevision pPartRevision, User pAuthor) {
        PartIteration lastPart = pPartRevision.getLastIteration();
        int newIteration = 1;

        if (lastPart != null) {
            newIteration = lastPart.getIteration() + 1;
            Date lastModificationDate = lastPart.modificationDate;
            setModificationDate(lastModificationDate);
        }

        setPartRevision(pPartRevision);
        iteration = newIteration;
        author = pAuthor;
        checkInDate = null;
    }

    public PartIteration(PartRevision pPartRevision, int pIteration, User pAuthor) {
        this(pPartRevision, pAuthor);
        iteration = pIteration;
    }
    
    public String getWorkspaceId() {
        return partRevision==null?"":partRevision.getWorkspaceId();
    }

    public Set<Geometry> getGeometries() {
        return geometries;
    }
    public List<Geometry> getSortedGeometries() {
        List<Geometry> geometriesList = new ArrayList<>(geometries);
        Collections.sort(geometriesList);
        return geometriesList;
    }

    public void addGeometry(Geometry pGeometry){
        geometries.add(pGeometry);
    }
    public boolean removeGeometry(Geometry pGeometry){
        return geometries.remove(pGeometry);
    }

    public String getNumber() {
        return getPartNumber();
    }
    public String getPartNumber() {
        return partRevision==null?"":partRevision.getPartNumber();
    }

    public String getPartName() {
        return partRevision==null?"":partRevision.getPartName();
    }

    public String getVersion() {
        return getPartVersion();
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

    public BinaryResource getNativeCADFile() {
        return nativeCADFile;
    }
    public void setNativeCADFile(BinaryResource nativeCADFile) {
        this.nativeCADFile = nativeCADFile;
    }

    @Override
    public Set<BinaryResource> getAttachedFiles() {
        return attachedFiles;
    }
    public void setAttachedFiles(Set<BinaryResource> attachedFiles) {
        this.attachedFiles = attachedFiles;
    }

    public void addAttachedFile(BinaryResource pBinaryResource){
        attachedFiles.add(pBinaryResource);
    }
    public boolean removeAttachedFile(BinaryResource pBinaryResource){
        return attachedFiles.remove(pBinaryResource);
    }


    public Set<DocumentLink> getLinkedDocuments() {
        return linkedDocuments;
    }
    
    public void setLinkedDocuments(Set<DocumentLink> pLinkedDocuments) {
        linkedDocuments=pLinkedDocuments;
    }
    
    public Date getCreationDate() {
        return (creationDate!=null) ? (Date) creationDate.clone() : null;
    }
    public void setCreationDate(Date creationDate) {
        this.creationDate = (creationDate!=null) ? (Date) creationDate.clone() : null;
    }

    public Date getModificationDate() {
        return (modificationDate!=null) ? (Date) modificationDate.clone() : null;
    }
    public void setModificationDate(Date modificationDate) {
        this.modificationDate = (modificationDate!=null) ? (Date) modificationDate.clone() : null;
    }

    public Date getCheckInDate() {
        return (checkInDate!=null) ? (Date) checkInDate.clone() : null;
    }

    public void setCheckInDate(Date checkInDate) {
        this.checkInDate = (checkInDate!=null) ? (Date) checkInDate.clone() : null;
    }

    public List<InstanceAttribute> getInstanceAttributes() {
        return instanceAttributes;
    }

    public void setInstanceAttributes(List<InstanceAttribute> instanceAttributes) {
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

    public PartRevisionKey getPartRevisionKey() {
        return partRevision==null?new PartRevisionKey(new PartMasterKey("",""),""):partRevision.getKey();
    }
    public PartIterationKey getKey() {
        return new PartIterationKey(getPartRevisionKey(),iteration);
    }
    
    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }   
    
    public boolean isAssembly(){
        return components != null && !components.isEmpty();
    }

    public boolean isLastIteration(){
        return equals(partRevision.getLastIteration());
    }

    public String getName() {
        return partRevision==null ? "" : this.partRevision.getPartName();
    }

    public List<InstanceAttributeTemplate> getInstanceAttributeTemplates() {
        return instanceAttributeTemplates;
    }

    public void setInstanceAttributeTemplates(List<InstanceAttributeTemplate> instanceAttributeTemplates) {
        this.instanceAttributeTemplates = instanceAttributeTemplates;
    }

    @Override
    public String toString() {
        return partRevision + "-" + iteration;
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
        if (!(pObj instanceof PartIteration)){
            return false;
        }
        PartIteration partI = (PartIteration) pObj;
        return partI.getPartNumber().equals(getPartNumber()) &&
                partI.getWorkspaceId().equals(getWorkspaceId()) &&
                partI.getPartVersion().equals(getPartVersion()) &&
                partI.iteration==iteration;
    }

    @Override
    public int compareTo(PartIteration pPart) {
        
        int wksComp = getWorkspaceId().compareTo(pPart.getWorkspaceId());
        if (wksComp != 0) {
            return wksComp;
        }
        int mpartNumberComp = getPartNumber().compareTo(pPart.getPartNumber());
        if (mpartNumberComp != 0) {
            return mpartNumberComp;
        }
        int mpartVersionComp = getPartVersion().compareTo(pPart.getPartVersion());
        if (mpartVersionComp != 0) {
            return mpartVersionComp;
        }else {
            return iteration - pPart.iteration;
        }
    }

    /**
     * perform a deep clone operation
     */
    @Override
    public PartIteration clone() {
        PartIteration clone;
        try {
            clone = (PartIteration) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
        //perform a deep copy
        clone.attachedFiles = new HashSet<>(attachedFiles);

        Set<DocumentLink> clonedLinks = new HashSet<>();
        for (DocumentLink link : linkedDocuments) {
            DocumentLink clonedLink = link.clone();
            clonedLinks.add(clonedLink);
        }
        clone.linkedDocuments = clonedLinks;

        //perform a deep copy
        List<InstanceAttribute> clonedInstanceAttributes = new ArrayList<>();
        for (InstanceAttribute attribute : instanceAttributes) {
            InstanceAttribute clonedAttribute = attribute.clone();
            clonedInstanceAttributes.add(clonedAttribute);
        }
        clone.instanceAttributes = clonedInstanceAttributes;

        //perform a deep copy
        List<InstanceAttributeTemplate> clonedInstanceAttributeTemplates = new ArrayList<>();
        for (InstanceAttributeTemplate attribute : instanceAttributeTemplates) {
            InstanceAttributeTemplate clonedAttribute = attribute.clone();
            clonedInstanceAttributeTemplates.add(clonedAttribute);
        }
        clone.instanceAttributeTemplates = clonedInstanceAttributeTemplates;

        if (creationDate != null) {
            clone.creationDate = (Date) creationDate.clone();
        }
        if (modificationDate != null) {
            clone.modificationDate = (Date) modificationDate.clone();
        }
        if (checkInDate != null) {
            clone.checkInDate = (Date) checkInDate.clone();
        }
        return clone;
    }
}