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
package com.docdoku.server.configuration.spec;

import com.docdoku.core.common.User;
import com.docdoku.core.configuration.DocumentBaseline;
import com.docdoku.core.configuration.DocumentConfigSpec;
import com.docdoku.core.configuration.FolderCollection;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentRevision;


/**
 * @author Morgan Guimard
 */
public class BaselineDocumentConfigSpec extends DocumentConfigSpec {

    private DocumentBaseline documentBaseline;
    private User user;

    public BaselineDocumentConfigSpec(){
    }

    public BaselineDocumentConfigSpec(DocumentBaseline documentBaseline, User user) {
        this.documentBaseline = documentBaseline;
        this.user = user;
    }

    public int getFolderCollectionId(){
        return documentBaseline.getFolderCollection().getId();
    }

    @Override
    public DocumentIteration filter(DocumentRevision documentRevision) {
        FolderCollection folderCollection = documentBaseline==null ? null : documentBaseline.getFolderCollection();     // Prevent NullPointerException
        if(folderCollection != null){
            DocumentIteration docI = folderCollection.getDocumentIteration(documentRevision.getKey());
            if(docI!=null){
                return docI;
            }
        }

        return null;
    }

    public DocumentBaseline getDocumentBaseline() {
        return documentBaseline;
    }

    public void setDocumentBaseline(DocumentBaseline documentBaseline) {
        this.documentBaseline = documentBaseline;
    }

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

}