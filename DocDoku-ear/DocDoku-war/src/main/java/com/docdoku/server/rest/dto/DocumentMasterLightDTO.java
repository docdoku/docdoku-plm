/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.docdoku.server.rest.dto;

import com.docdoku.core.common.User;
import com.docdoku.core.document.DocumentIteration;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author yassinebelouad
 */
public class DocumentMasterLightDTO implements Serializable, Comparable<DocumentMasterLightDTO>  {
    
    private String workspaceId;
    private String id;
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
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
