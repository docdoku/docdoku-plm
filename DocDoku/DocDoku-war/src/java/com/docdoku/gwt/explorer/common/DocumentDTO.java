/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,  
 * but WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
 * GNU General Public License for more details.  
 *  
 * You should have received a copy of the GNU General Public License  
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.  
 */

package com.docdoku.gwt.explorer.common;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Florent GARIN
 */
public class DocumentDTO implements Serializable {
    
    private String workspaceId;
    private String masterDocumentId;
    private String masterDocumentVersion;
    private int iteration;


    private Date creationDate;
    private String author;
    private String revisionNote;



    private Map<String,String> files;
    private Map<String,InstanceAttributeDTO> attributes;
    private Set<DocumentDTO> links;

    public DocumentDTO() {
    }
    
    public DocumentDTO(String pWorkspaceId, String pMasterDocumentId, String pMasterDocumentVersion, int pIteration) {
        workspaceId=pWorkspaceId;
        masterDocumentId=pMasterDocumentId;
        masterDocumentVersion=pMasterDocumentVersion;
        iteration=pIteration;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
    
  

    public void setRevisionNote(String pRevisionNote) {
        revisionNote = pRevisionNote;
    }

    public String getRevisionNote() {
        return revisionNote;
    }

    public Map<String,String> getFiles() {
        return files;
    }

    public Set<DocumentDTO> getLinks() {
        return links;
    }

    public void setFiles(Map<String,String> files) {
        this.files = files;
    }

    public void setLinks(Set<DocumentDTO> links) {
        this.links = links;
    }

    public Map<String,InstanceAttributeDTO> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String,InstanceAttributeDTO> attributes) {
        this.attributes = attributes;
    }


    @Override
    public String toString() {
        return workspaceId + "-" + masterDocumentId + "-" + masterDocumentVersion + "-" + iteration;
    }
    
    public String getWorkspaceId() {
        return workspaceId;
    }
    
    public void setWorkspaceId(String pWorkspaceId) {
        workspaceId = pWorkspaceId;
    }
    
    public String getMasterDocumentId() {
        return masterDocumentId;
    }
    
    public void setMasterDocumentId(String pMasterDocumentId) {
        masterDocumentId = pMasterDocumentId;
    }

    public String getMasterDocumentVersion() {
        return masterDocumentVersion;
    }

    public void setMasterDocumentVersion(String pMasterDocumentVersion) {
        this.masterDocumentVersion = pMasterDocumentVersion;
    }
    
    
    public int getIteration(){
        return iteration;
    }
    
    public void setIteration(int pIteration){
        iteration=pIteration;
    }
}
