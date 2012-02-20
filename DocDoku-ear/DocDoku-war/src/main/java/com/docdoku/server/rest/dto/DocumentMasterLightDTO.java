/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
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

package com.docdoku.server.rest.dto;

import com.docdoku.core.common.User;
import com.docdoku.core.document.DocumentIteration;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Yassine Belouad
 */
public class DocumentMasterLightDTO implements Serializable, Comparable<DocumentMasterLightDTO>  {
    
    private String workspaceId;
    private String id;
    private String reference;
    private String version;
    private String type;
    private String authorName;
    private Date creationDate;
    private Date lastIterationDate;
    private int lastIterationNumber;
    private String title;  
    private String checkOutUserName;
    private Date checkOutDate;
    private String lifeCycleState;

  public DocumentMasterLightDTO() {
  
  }

    public Date getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(Date checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getId() {
        return id+"-"+version;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReference() {
        reference = this.id;
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
    
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public Date getLastIterationDate() {
        return lastIterationDate;
    }

    public void setLastIterationDate(Date pLastIterationDate) {
            this.lastIterationDate = pLastIterationDate;
    }

    public int getLastIterationNumber() {
        return lastIterationNumber;
    }

    public void setLastIterationNumber(int pLastIteration) {
        this.lastIterationNumber = pLastIteration;        
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getCheckOutUserName() {
        return checkOutUserName;
    }

    public void setCheckOutUserName(String pCheckOutUserName) {
            this.checkOutUserName = pCheckOutUserName;        
    }

    public String getLifeCycleState() {
        return lifeCycleState;
    }

    public void setLifeCycleState(String lifeCycleState) {
        this.lifeCycleState = lifeCycleState;
    }

     
    @Override
    public int compareTo(DocumentMasterLightDTO pDocM) {
        int wksComp = workspaceId.compareTo(pDocM.workspaceId);
        if (wksComp != 0) {
            return wksComp;
        }
        int idComp = id.compareTo(pDocM.id);
        if (idComp != 0) {
            return idComp;
        } else {
            return version.compareTo(pDocM.version);
        }
    }


}
