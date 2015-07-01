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
package com.docdoku.core.log;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * The DocumentLog class represents an entry in the log
 * table that keeps track of all activities around a specific document.
 * 
 * @author Florent Garin
 * @version 1.1, 22/09/11
 * @since   V1.1
 */
@Table(name="DOCUMENTLOG")
@javax.persistence.Entity
@NamedQueries ({
    @NamedQuery(name="findLogByDocumentAndUserAndEvent", query="SELECT l FROM DocumentLog l WHERE l.userLogin = :userLogin AND l.documentWorkspaceId = :documentWorkspaceId AND l.documentId = :documentId AND l.documentVersion = :documentVersion AND l.documentIteration = :documentIteration AND l.event = :event ORDER BY l.logDate")
})
public class DocumentLog implements Serializable {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private int id;
    @javax.persistence.Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date logDate;
    private String documentWorkspaceId;
    private String documentId;
    private String documentVersion;
    private int documentIteration;
    private String userLogin;
    private String event;
    private String info;
    
    
    public DocumentLog() {
    }

    public int getDocumentIteration() {
        return documentIteration;
    }

    public void setDocumentIteration(int documentIteration) {
        this.documentIteration = documentIteration;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getDocumentVersion() {
        return documentVersion;
    }

    public void setDocumentVersion(String documentVersion) {
        this.documentVersion = documentVersion;
    }

    public String getDocumentWorkspaceId() {
        return documentWorkspaceId;
    }

    public void setDocumentWorkspaceId(String documentWorkspaceId) {
        this.documentWorkspaceId = documentWorkspaceId;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public Date getLogDate() {
        return logDate;
    }

    public void setLogDate(Date logDate) {
        this.logDate = logDate;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    
}