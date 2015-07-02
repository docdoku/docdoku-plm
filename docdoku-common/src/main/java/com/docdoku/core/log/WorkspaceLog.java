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
 * The WorkspaceLog class represents an entry in the log
 * table that keeps track of all activities around a specific workspace.
 * 
 * @author Florent Garin
 * @version 1.1, 12/03/13
 * @since   V1.1
 */
@Table(name="WORKSPACELOG")
@javax.persistence.Entity
@NamedQueries ({
    @NamedQuery(name="findLogByWorkspaceAndUserAndEvent", query="SELECT l FROM WorkspaceLog l WHERE l.userLogin = :userLogin AND l.workspaceId = :workspaceId AND l.event = :event ORDER BY l.logDate")
})
public class WorkspaceLog implements Serializable {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private int id;
    @javax.persistence.Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date logDate;
    private String workspaceId;
    private String userLogin;
    private String event;
    private String info;


    public WorkspaceLog() {
    }

    public int getId() {
        return id;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String partWorkspaceId) {
        this.workspaceId = partWorkspaceId;
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