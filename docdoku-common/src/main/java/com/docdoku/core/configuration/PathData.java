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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Morgan Guimard
 */

@Table(name="PATHDATA")
@Entity
@NamedQueries({
        @NamedQuery(name= "PathData.findByPathAndProductInstanceMaster", query="SELECT p FROM PathData p JOIN ProductInstanceMaster l WHERE p member of l.pathDataList and p.path = :path and l = :productInstanceMaster")
})
public class PathData implements Serializable, FileHolder {

    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Id
    private int id;

    private String path;

    @Lob
    private String description;

    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderColumn(name="ATTRIBUTE_ORDER")
    @JoinTable(name = "PATHDATA_ATTRIBUTE",
            inverseJoinColumns = {
                    @JoinColumn(name = "INSTANCEATTRIBUTE_ID", referencedColumnName = "ID")
            },
            joinColumns = {
                    @JoinColumn(name="PATHDATA_ID", referencedColumnName="ID")
            })
    private List<InstanceAttribute> instanceAttributes = new ArrayList<>();


    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "PATHDATA__DOCUMENTLINK",
            inverseJoinColumns = {
                    @JoinColumn(name = "DOCUMENTLINK_ID", referencedColumnName = "ID")
            },
            joinColumns = {
                    @JoinColumn(name="PATHDATA_ID", referencedColumnName="ID")
            })
    private Set<DocumentLink> linkedDocuments = new HashSet<>();

    @OneToMany(cascade = {CascadeType.REMOVE, CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinTable(name = "PATHDATA_BINRES",
            inverseJoinColumns = {
                    @JoinColumn(name = "ATTACHEDFILE_FULLNAME", referencedColumnName = "FULLNAME")
            },
            joinColumns = {
                    @JoinColumn(name="PATHDATA_ID", referencedColumnName="ID")
            })
    private Set<BinaryResource> attachedFiles = new HashSet<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDescription() {
        return description;
    }

    public Set<DocumentLink> getLinkedDocuments() {
        return linkedDocuments;
    }

    public void setLinkedDocuments(Set<DocumentLink> linkedDocuments) {
        this.linkedDocuments = linkedDocuments;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<InstanceAttribute> getInstanceAttributes() {
        return instanceAttributes;
    }

    public void setInstanceAttributes(List<InstanceAttribute> instanceAttributes) {
        this.instanceAttributes = instanceAttributes;
    }

    @Override
    public Set<BinaryResource> getAttachedFiles() {
        return null;
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
