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

import com.docdoku.core.common.User;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.document.Folder;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This class maintains a collection of folders.
 *
 * FolderCollection is a foundation for the definition of {@link DocumentBaseline}.
 *
 * @author Taylor LABEJOF
 * @version 2.0, 25/08/14
 * @since V2.0
 */
@Table(name="FOLDERCOLLECTION")
@Entity
public class FolderCollection implements Serializable {

    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Id
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "AUTHOR_LOGIN", referencedColumnName = "LOGIN"),
            @JoinColumn(name = "AUTHOR_WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID")
    })
    private User author;

    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

    @MapKey(name="baselinedFolderKey")
    @OneToMany(mappedBy="folderCollection", cascade=CascadeType.ALL, fetch=FetchType.LAZY, orphanRemoval=true)
    private Map<BaselinedFolderKey, BaselinedFolder> baselinedFolders = new HashMap<>();


    public FolderCollection() {
    }

    public Map<BaselinedFolderKey, BaselinedFolder> getBaselinedFolders() {
        return baselinedFolders;
    }
    public void removeAllBaselinedFolders() {
        baselinedFolders.clear();
    }

    public BaselinedFolder addBaselinedFolder(Folder folder){
        BaselinedFolder baselinedFolder = new BaselinedFolder(this,folder);
        baselinedFolders.put(baselinedFolder.getBaselinedFolderKey(),baselinedFolder);
        return  baselinedFolder;
    }
    public BaselinedFolder addBaselinedFolder(BaselinedFolder baselinedFolder){
        baselinedFolders.put(baselinedFolder.getBaselinedFolderKey(),baselinedFolder);
        return  baselinedFolder;
    }

    public BaselinedFolder getBaselinedFolder(String completePath){
        return baselinedFolders.get(new BaselinedFolderKey(id,completePath));
    }
    public boolean hasBaselinedFolder(String completePath){
        return baselinedFolders.containsKey(new BaselinedFolderKey(id,completePath));
    }

    public DocumentIteration getDocumentIteration(DocumentRevisionKey documentRevisionKey){
        for(BaselinedFolder folder : baselinedFolders.values()){
            DocumentIteration documentIteration = folder.getDocumentIteration(documentRevisionKey);
            if(documentIteration!=null){
                return documentIteration;
            }
        }
        return null;
    }
    public boolean hasDocumentRevision(DocumentRevisionKey documentRevisionKey){
        for(BaselinedFolder folder : baselinedFolders.values()){
            boolean hasDocument = folder.hasDocumentRevision(documentRevisionKey);
            if(hasDocument){
                return true;
            }
        }
        return false;
    }

    public Date getCreationDate() {
        return (creationDate!=null)? (Date) creationDate.clone() : null;
    }
    public void setCreationDate(Date creationDate) {
        this.creationDate = (creationDate!=null)? (Date) creationDate.clone() : null;
    }

    public User getAuthor() {
        return author;
    }
    public void setAuthor(User author) {
        this.author = author;
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
        if (!(o instanceof FolderCollection)) {
            return false;
        }

        FolderCollection collection = (FolderCollection) o;
        return id == collection.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
