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

/**
 * Identity class of {@link DocumentIteration} objects.
 *
 * @author Florent Garin
 */
public class DocumentIterationKey implements Serializable {

    private DocumentRevisionKey documentRevision;
    private int iteration;
    
    public DocumentIterationKey() {
    }

    public DocumentIterationKey(String pWorkspaceId, String pId, String pVersion, int pIteration) {
        documentRevision= new DocumentRevisionKey(pWorkspaceId, pId, pVersion);
        iteration=pIteration;
    }

    public DocumentIterationKey(DocumentRevisionKey pDocumentRevisionKey, int pIteration) {
        documentRevision=pDocumentRevisionKey;
        iteration=pIteration;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = 31 * hash + documentRevision.hashCode();
        hash = 31 * hash + iteration;
        return hash;
    }

    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof DocumentIterationKey))
            return false;
        DocumentIterationKey key = (DocumentIterationKey) pObj;
        return key.documentRevision.equals(documentRevision) && key.iteration==iteration;
    }

    @Override
    public String toString() {
        return documentRevision + "-" + iteration;
    }

    public int getIteration(){
        return iteration;
    }
    
    public void setIteration(int pIteration){
        iteration=pIteration;
    }

    public DocumentRevisionKey getDocumentRevision() {
        return documentRevision;
    }

    public void setDocumentRevision(DocumentRevisionKey documentRevision) {
        this.documentRevision = documentRevision;
    }


    public String getWorkspaceId() {
        return documentRevision.getDocumentMaster().getWorkspace();
    }

    public String getDocumentMasterId() {
        return documentRevision.getDocumentMaster().getId();
    }

    public String getDocumentRevisionVersion(){
        return documentRevision.getVersion();
    }
}
