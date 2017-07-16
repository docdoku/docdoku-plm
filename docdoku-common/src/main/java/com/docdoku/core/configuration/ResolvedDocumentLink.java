/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
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

import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentLink;

import java.io.Serializable;

/**
 * This class is used to carry additional information along the document
 * link itself. More precisely, this information is the target document
 * iteration to consider which may vary according to the chosen
 * resolution process.
 *
 * As a reminder, the stored {@link DocumentLink} targets
 * a document revision not a document iteration.
 *
 * Instances of this class are not persisted.
 *
 * @author Morgan Guimard
 */
public class ResolvedDocumentLink implements Serializable {
    private DocumentLink documentLink;
    private DocumentIteration documentIteration;

    public ResolvedDocumentLink() {
    }

    public ResolvedDocumentLink(DocumentLink documentLink, DocumentIteration documentIteration) {
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
