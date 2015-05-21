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
package com.docdoku.core.configuration;


import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.common.FileHolder;
import com.docdoku.core.document.DocumentLink;
import com.docdoku.core.meta.InstanceAttribute;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.*;

/**
 * @author: Chadid Asmae
 */
@Table(name="PATHDATAITERATION")
@Entity
@IdClass(PathDataIterationKey.class)
public class PathDataIteration implements Serializable, FileHolder {

    @Id
    @ManyToOne(optional=false, fetch=FetchType.EAGER)
    @JoinColumns({
            @JoinColumn(name="PATHDATAMASTER_ID", referencedColumnName="ID")
    })
    private PathDataMaster pathDataMaster;

    @Id
    @Column(name="ITERATION")
    private int iteration;

    @Lob
    private String noteIteration;

    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date  dateIteration;

    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderColumn(name="ATTRIBUTE_ORDER")
    @JoinTable(name = "PATHDATAITERATION_ATTRIBUTE",
            inverseJoinColumns = {
                    @JoinColumn(name = "INSTANCEATTRIBUTE_ID", referencedColumnName = "ID")
            },
            joinColumns = {
                    @JoinColumn(name="PATHDATA_ITERATION", referencedColumnName="ITERATION"),
                    @JoinColumn(name="PATHDATAMASTER_ID", referencedColumnName="PATHDATAMASTER_ID")
            })
    private List<InstanceAttribute> instanceAttributes = new ArrayList<>();


    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "PATHDATAITERATION_DOCUMENTLINK",
            inverseJoinColumns = {
                    @JoinColumn(name = "DOCUMENTLINK_ID", referencedColumnName = "ID")
            },
            joinColumns = {
                    @JoinColumn(name="PATHDATA_ITERATION", referencedColumnName="ITERATION"),
                    @JoinColumn(name="PATHDATAMASTER_ID", referencedColumnName="PATHDATAMASTER_ID")
            })
    private Set<DocumentLink> linkedDocuments = new HashSet<>();

    @OneToMany(cascade = {CascadeType.REMOVE, CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinTable(name = "PATHDATAITERATION_BINRES",
            inverseJoinColumns = {
                    @JoinColumn(name = "ATTACHEDFILE_FULLNAME", referencedColumnName = "FULLNAME")
            },
            joinColumns = {
                    @JoinColumn(name="PATHDATA_ITERATION", referencedColumnName="ITERATION"),
                    @JoinColumn(name="PATHDATAMASTER_ID", referencedColumnName="PATHDATAMASTER_ID")
            })
    private Set<BinaryResource> attachedFiles = new HashSet<>();

    public PathDataIteration() {
    }

    public PathDataIteration(int iteration, PathDataMaster pathDataMaster,Date date) {
        this.iteration = iteration;
        this.pathDataMaster = pathDataMaster;
        this.dateIteration = date;
    }

    @XmlTransient
    public PathDataMaster getPathDataMaster() {
        return pathDataMaster;
    }

    public void setPathDataMaster(PathDataMaster pathDataMaster) {
        this.pathDataMaster = pathDataMaster;
    }

    public String getNoteIteration() {
        return noteIteration;
    }

    public int getIteration() {
        return iteration;
    }

    public void setIteration(int iteration) {
        this.iteration = iteration;
    }

    public Date getDateIteration() {
        return dateIteration;
    }

    public void setDateIteration(Date dateIteration) {
        this.dateIteration = dateIteration;
    }

    public Set<DocumentLink> getLinkedDocuments() {
        return linkedDocuments;
    }

    public void setLinkedDocuments(Set<DocumentLink> linkedDocuments) {
        this.linkedDocuments = linkedDocuments;
    }

    public void setNoteIteration(String noteIteration) {
        this.noteIteration = noteIteration;
    }

    public List<InstanceAttribute> getInstanceAttributes() {
        return instanceAttributes;
    }

    public void setInstanceAttributes(List<InstanceAttribute> instanceAttributes) {
        this.instanceAttributes = instanceAttributes;
    }

    @Override
    public Set<BinaryResource> getAttachedFiles() {
        return attachedFiles;
    }

    public void setAttachedFiles(Set<BinaryResource> attachedFiles) {
        this.attachedFiles = attachedFiles;
    }

    public void addFile(BinaryResource binaryResource) {
        attachedFiles.add(binaryResource);
    }

    public void removeFile(BinaryResource file) {
        attachedFiles.remove(file);
    }
}
