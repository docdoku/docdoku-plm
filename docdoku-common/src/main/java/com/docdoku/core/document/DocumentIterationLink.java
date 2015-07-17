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

package com.docdoku.core.document;

import java.io.Serializable;

public class DocumentIterationLink implements Serializable {
    private DocumentLink documentLink;
    private DocumentIteration documentIteration;

    public DocumentIterationLink() {
    }

    public DocumentIterationLink(DocumentLink documentLink, DocumentIteration documentIteration) {
        this.documentLink = documentLink;
        this.documentIteration = documentIteration;
    }

    public DocumentLink getDocumentLink() {
        return documentLink;
    }

    public void setDocumentLink(DocumentLink documentLink) {
        this.documentLink = documentLink;
    }

    public DocumentIteration getDocumentIteration() {
        return documentIteration;
    }

    public void setDocumentIteration(DocumentIteration documentIteration) {
        this.documentIteration = documentIteration;
    }
}
