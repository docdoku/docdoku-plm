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


import com.docdoku.core.document.DocumentMasterKey;
import com.docdoku.core.document.Folder;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class link that gathers a document collection and a given folder.
 *
 * @author Taylor LABEJOF
 * @version 2.0, 25/08/14
 * @since   V2.0
 */

@Table(name="BASELINEDFOLDER")
@Entity
public class BaselinedFolder implements Serializable, Comparable<BaselinedFolder> {
    @EmbeddedId
    private BaselinedFolderKey baselinedFolderKey;

    @ManyToOne( optional=false, fetch=FetchType.EAGER)
    @JoinColumn(name="FOLDERSCOLLECTION_ID", referencedColumnName="ID")
    private FoldersCollection foldersCollection;

    @Column(name="COMPLETEPATH")
    private String completePath="";

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name="PARENTFOLDER_FOLDERSCOLLECTION_ID", referencedColumnName="FOLDERSCOLLECTION_ID", insertable = true, updatable = true),
            @JoinColumn(name="PARENTFOLDER_COMPLETEPATH", referencedColumnName="COMPLETEPATH", insertable = true, updatable = true),
    })
    private BaselinedFolder parentFolder;

    @OneToMany(mappedBy="baselinedFolder", cascade=CascadeType.ALL, fetch=FetchType.LAZY, orphanRemoval=true)
    private List<BaselinedDocument> baselinedDocuments = new ArrayList<>();


    public BaselinedFolder(){
    }

    public BaselinedFolder(FoldersCollection foldersCollection, String completePath) {
        this.baselinedFolderKey = new BaselinedFolderKey(foldersCollection.getId(),completePath);
        this.foldersCollection = foldersCollection;
        this.completePath = completePath;
        if(!isRoot() && !isHome()){
            int index = completePath.lastIndexOf('/');
            parentFolder = new BaselinedFolder(foldersCollection, completePath.substring(0, index));
        }
    }

    public BaselinedFolder(FoldersCollection foldersCollection, Folder folder) {
        this(foldersCollection,folder.getCompletePath());
    }

    public BaselinedFolderKey getKey() {
        return baselinedFolderKey;
    }

    @XmlTransient
    public FoldersCollection getFoldersCollection() {
        return foldersCollection;
    }
    public void setFoldersCollection(FoldersCollection foldersCollection) {
        this.foldersCollection = foldersCollection;
    }

    public String getCompletePath() {
        return completePath;
    }
    public void setCompletePath(String completePath) {
        this.completePath = completePath;
    }

    public BaselinedFolder getParentFolder() {
        return parentFolder;
    }
    public void setParentFolder(BaselinedFolder parentFolder) {
        this.parentFolder = parentFolder;
    }

    public List<BaselinedDocument> getBaselinedDocuments() {
        return baselinedDocuments;
    }
    public BaselinedDocument getBaselinedDocument(DocumentMasterKey documentMasterKey) {
        for(BaselinedDocument  baselinedDocument : baselinedDocuments){
            DocumentMasterKey target = baselinedDocument.getTargetDocument()
                                                        .getDocumentRevision()
                                                        .getDocumentMaster()
                                                        .getKey();
            if(documentMasterKey.equals(target)){
                return baselinedDocument;
            }
        }
        return null;
    }

    public boolean isRoot() {
        return !completePath.contains("/");
    }

    public boolean isHome() {
        try {
            int index = completePath.lastIndexOf('/');
            return completePath.charAt(index+1) == '~';
        } catch (IndexOutOfBoundsException pIOOBEx) {
            Logger.getLogger(BaselinedFolder.class.getName()).log(Level.FINEST,null, pIOOBEx);
            return false;
        }
    }

    public String getShortName() {
        if(isRoot()) {
            return completePath;
        }

        int index = completePath.lastIndexOf('/');
        return completePath.substring(index + 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BaselinedFolder that = (BaselinedFolder) o;

        return completePath.equals(that.completePath)
                && foldersCollection.equals(that.foldersCollection);

    }

    @Override
    public int hashCode() {
        int result = foldersCollection.hashCode();
        result = 31 * result + completePath.hashCode();
        return result;
    }

    /**
     * Compare to balinedFolder of the same Collection
     * @param baselinedFolder Baselined to compare
     * @return &lt;0 if A lt B; 0 if A = B; &gt;0 if A gt B
     */
    @Override
    public int compareTo(BaselinedFolder baselinedFolder) {
        return completePath.compareTo(baselinedFolder.completePath);
    }
}
