/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2014 DocDoku SARL
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

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Identity class of <a href="BaselinedFolder.html">BaselinedFolder</a> objects defined as an embeddable object in order
 * to be used inside the baselined documents map in the <a href="DocumentsCollection.html">DocumentsCollection</a> class.
 *
 * @author Taylor LABEJOF
 * @version 2.0, 25/08/14
 * @since   V2.0
 */
@Embeddable
public class BaselinedFolderKey implements Serializable{
    @Column(name = "FOLDERSCOLLECTION_ID", nullable = false, insertable = false, updatable = false)
    private int foldersCollection;
    @Column(name = "COMPLETEPATH", nullable = false, insertable = false, updatable = false)
    private String completePath;


    public BaselinedFolderKey(){
    }
    public BaselinedFolderKey(int foldersCollection, String completePath) {
        this.foldersCollection = foldersCollection;
        this.completePath = completePath;
    }

    public int getFoldersCollection() {
        return foldersCollection;
    }
    public void setFoldersCollection(int foldersCollection) {
        this.foldersCollection = foldersCollection;
    }

    public String getCompletePath() {
        return completePath;
    }
    public void setCompletePath(String completePath) {
        this.completePath = completePath;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BaselinedFolderKey that = (BaselinedFolderKey) o;

        return foldersCollection == that.foldersCollection
                && !(completePath != null ? !completePath.equals(that.completePath) : that.completePath != null);

    }

    @Override
    public int hashCode() {
        int result = foldersCollection;
        result = 31 * result + (completePath != null ? completePath.hashCode() : 0);
        return result;
    }
}