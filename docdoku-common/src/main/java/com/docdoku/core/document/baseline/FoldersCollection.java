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
package com.docdoku.core.document.baseline;

import com.docdoku.core.common.User;
import com.docdoku.core.document.Folder;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This class maintains a collection of folders which cannot hold
 * more than one <a href="Folder.html">Folder</a> in
 * the same <a href="PartMaster.html">PartMaster</a>.
 *
 * DocumentsCollection is a foundation for the definition of <a href="BaselineDM.html">BaselineDM</a>
 * and <a href="Folder.html">Folder</a>.
 *
 * @author Taylor LABEJOF
 * @version 2.0, 25/08/14
 * @since   V2.0
 */
@Table(name="FOLDERSCOLLECTION")
@Entity
public class FoldersCollection implements Serializable {

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

    @MapKey(name="completePath")
    @OneToMany(mappedBy="foldersCollection", cascade=CascadeType.ALL, fetch=FetchType.LAZY, orphanRemoval=true)
    private Map<String, BaselinedFolder> baselinedFolders = new HashMap<>();

    public FoldersCollection() {
    }

    public void removeAllBaselinedFolders() {
        baselinedFolders.clear();
    }

    public Map<String, BaselinedFolder> getBaselinedFolders() {
        return baselinedFolders;
    }

    public void addBaselinedFolder(Folder folder){
        BaselinedFolder baselinedFolder = new BaselinedFolder(this,folder);
        baselinedFolders.put(baselinedFolder.getCompletePath(),baselinedFolder);
    }

    public void addBaselinedFolder(BaselinedFolder baselinedFolder){
        baselinedFolders.put(baselinedFolder.getCompletePath(),baselinedFolder);
    }

    public BaselinedFolder getBaselinedFolder(String completePath){
        return baselinedFolders.get(completePath);
    }

    public boolean hasBaselinedFolder(String completePath){
        return baselinedFolders.containsKey(completePath);
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
        if (!(o instanceof FoldersCollection)) {
            return false;
        }

        FoldersCollection collection = (FoldersCollection) o;
        return id == collection.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
