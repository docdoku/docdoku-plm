/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011 DocDoku SARL
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
package com.docdoku.core.log;

import java.io.Serializable;
import java.util.*;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * The <code>DocumentLog</code> class represents an entry in the log
 * table that keeps track of all activities around a specific document.
 * 
 * @author Florent Garin
 * @version 1.1, 22/09/11
 * @since   V1.1
 */
@javax.persistence.Entity
@NamedQueries ({
    @NamedQuery(name="findLogByDocumentAndUserAndEvent", query="SELECT l FROM DocumentLog l WHERE l.userLogin = :userLogin AND l.documentWorkspaceId = :documentWorkspaceId AND l.documentMasterDocumentId = :documentMasterDocumentId AND l.documentMasterDocumentVersion = :documentMasterDocumentVersion AND l.documentIteration = :documentIteration and l.event = :event ORDER BY l.logDate")
})
public class DocumentLog implements Serializable, Cloneable {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private int id;
    @javax.persistence.Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date logDate;
    private String documentWorkspaceId;
    private String documentMasterDocumentId;
    private String documentMasterDocumentVersion;
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

    public String getDocumentMasterDocumentId() {
        return documentMasterDocumentId;
    }

    public void setDocumentMasterDocumentId(String documentMasterDocumentId) {
        this.documentMasterDocumentId = documentMasterDocumentId;
    }

    public String getDocumentMasterDocumentVersion() {
        return documentMasterDocumentVersion;
    }

    public void setDocumentMasterDocumentVersion(String documentMasterDocumentVersion) {
        this.documentMasterDocumentVersion = documentMasterDocumentVersion;
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