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

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Identity class of {@link BaselinedFolder} objects defined as an embeddable object in order
 * to be used inside the baselined documents map in the {@link com.docdoku.core.configuration.DocumentCollection} class.
 *
 * @author Taylor LABEJOF
 * @version 2.0, 25/08/14
 * @since V2.0
 */
@Embeddable
public class BaselinedFolderKey implements Serializable{
    @Column(name = "FOLDERCOLLECTION_ID", nullable = false, insertable = false, updatable = false)
    private int folderCollection;
    @Column(name = "COMPLETEPATH", nullable = false, insertable = false, updatable = false)
    private String completePath;


    public BaselinedFolderKey(){
    }
    public BaselinedFolderKey(int folderCollection, String completePath) {
        this.folderCollection = folderCollection;
        this.completePath = completePath;
    }

    public int getFolderCollection() {
        return folderCollection;
    }
    public void setFolderCollection(int folderCollection) {
        this.folderCollection = folderCollection;
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

        return folderCollection == that.folderCollection
                && !(completePath != null ? !completePath.equals(that.completePath) : that.completePath != null);

    }

    @Override
    public int hashCode() {
        int result = folderCollection;
        result = 31 * result + (completePath != null ? completePath.hashCode() : 0);
        return result;
    }
}