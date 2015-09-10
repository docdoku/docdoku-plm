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

import com.docdoku.core.common.Workspace;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.document.Folder;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * Baselines refer to specific configurations of documents, they could be seen as
 * "snapshots in time" of folders with their content. More concretely, baselines are collections
 * of items (like documents and folders) at a given iteration.
 *
 * @author Taylor LABEJOF
 * @version 2.0, 25/08/14
 * @since V2.0
 */
@Table(name="DOCUMENTBASELINE")
@Entity
public class DocumentBaseline implements Serializable {
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Id
    private int id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumns({
            @JoinColumn(name = "WORKSPACE_ID", referencedColumnName = "ID")
    })
    private Workspace workspace;

    @Column(nullable = false)
    private String name;

    @Lob
    private String description;

    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

    @OneToOne(cascade = CascadeType.ALL,fetch = FetchType.LAZY, orphanRemoval = true)
    private FolderCollection folderCollection =new FolderCollection();


    public DocumentBaseline() {
    }
    public DocumentBaseline(Workspace workspace, String name, String description) {
        this.workspace = workspace;
        this.name = name;
        this.description = description;
        this.creationDate = new Date();
    }

    public Map<BaselinedFolderKey, BaselinedFolder> getBaselinedFolders() {
        return folderCollection.getBaselinedFolders();
    }
    public void removeAllBaselinedFolders() {
        folderCollection.removeAllBaselinedFolders();
    }

    public BaselinedFolder addBaselinedFolder(Folder folder){
        return folderCollection.addBaselinedFolder(folder);
    }
    public BaselinedFolder addBaselinedFolder(BaselinedFolder baselinedFolder){
        return folderCollection.addBaselinedFolder(baselinedFolder);
    }
    public boolean hasBasedLinedFolder(String completePath){
        return folderCollection.hasBaselinedFolder(completePath);
    }
    public BaselinedFolder getBaselinedFolder(String completePath){
        return folderCollection.getBaselinedFolder(completePath);
    }

    public void addBaselinedDocument(DocumentIteration targetDocument){
        Folder location = targetDocument.getDocumentRevision().getLocation();
        BaselinedFolder baselinedFolder = folderCollection.getBaselinedFolder(location.getCompletePath());
        if(baselinedFolder==null) {
            baselinedFolder = addBaselinedFolder(location);
        }
        baselinedFolder.addDocumentIteration(targetDocument);
    }
    public boolean hasBaselinedDocument(DocumentRevisionKey documentRevisionKey){
        if(folderCollection != null){
            return folderCollection.hasDocumentRevision(documentRevisionKey);
        }
        return false;
    }
    public DocumentIteration getBaselinedDocument(DocumentRevisionKey documentRevisionKey){
        return (folderCollection!=null) ? folderCollection.getDocumentIteration(documentRevisionKey) : null;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Date getCreationDate() {
        return (creationDate!=null) ? (Date) creationDate.clone(): null;
    }
    public void setCreationDate(Date creationDate) {
        this.creationDate = (Date) creationDate.clone();
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public FolderCollection getFolderCollection() {
        return folderCollection;
    }

    public Workspace getWorkspace() {
        return workspace;
    }
    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DocumentBaseline)) {
            return false;
        }

        DocumentBaseline baseline = (DocumentBaseline) o;
        return id == baseline.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}