package com.docdoku.gwt.explorer.common;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Florent GARIN
 */
public class MasterDocumentDTO implements Serializable, Comparable<MasterDocumentDTO>{
    
    private String workspaceId;
    private String id;
    private String version;
    private String type;
    private String author;
    private Date creationDate;
    private String title;
    private String checkOutUser;
    private Date checkOutDate;
    private String lifeCycleState;
    private String[] tags;
    private String description;
    private String checkOutUserFullName ;

    private List<DocumentDTO> iterations;

    private WorkflowDTO workflow;
    
    public MasterDocumentDTO() {
    }

    public MasterDocumentDTO(String workspaceId, String id, String version) {
        this.workspaceId=workspaceId;
        this.id=id;
        this.version=version;
        
    }
    
    public String getAuthor() {
        return author;
    }

    public Date getCheckOutDate() {
        return checkOutDate;
    }

    public String getCheckOutUser() {
        return checkOutUser;
    }


    public Date getCreationDate() {
        return creationDate;
    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public WorkflowDTO getWorkflow() {
        return workflow;
    }

    public void setWorkflow(WorkflowDTO workflow) {
        this.workflow = workflow;
    }

    public String getLifeCycleState() {
        return lifeCycleState;
    }

    public String[] getTags() {
        return tags;
    }

    public String getTitle() {
        return title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setCheckOutDate(Date checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public void setCheckOutUser(String checkOutUser) {
        this.checkOutUser = checkOutUser;
    }

    
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setWorkspaceID(String workspaceID) {
        this.workspaceId = workspaceID;
    }

    public void setID(String id) {
        this.id = id;
    }

    
    public void setLifeCycleState(String lifeCycleState) {
        this.lifeCycleState = lifeCycleState;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<DocumentDTO> getIterations() {
        return iterations;
    }

    public void setIterations(List<DocumentDTO> iterations) {
        this.iterations = iterations;
    }

    public void setId(String id) {
        this.id = id;
    }

    
    public DocumentDTO getLastIteration() {
        int index = iterations.size()-1;
        if(index < 0)
            return null;
        else
            return iterations.get(index);
    }

    public String getCheckOutUserFullName() {
        return checkOutUserFullName;
    }

    public void setCheckOutUserFullName(String checkOutUserFullName) {
        this.checkOutUserFullName = checkOutUserFullName;
    }

    
    
    @Override
    public String toString() {
        return workspaceId + "-" + id + "-" + version;
    }

    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof MasterDocumentDTO))
            return false;
        MasterDocumentDTO mdoc = (MasterDocumentDTO) pObj;
        return ((mdoc.id.equals(id)) && (mdoc.workspaceId.equals(workspaceId)) && (mdoc.version.equals(version)));

    }

    @Override
    public int hashCode() {
        int hash = 1;
	hash = 31 * hash + workspaceId.hashCode();
	hash = 31 * hash + id.hashCode();
        hash = 31 * hash + version.hashCode();
	return hash;
    }


    public int compareTo(MasterDocumentDTO pMDoc) {
        int wksComp = workspaceId.compareTo(pMDoc.workspaceId);
        if (wksComp != 0)
            return wksComp;
        int idComp = id.compareTo(pMDoc.id);
        if (idComp != 0)
            return idComp;
        else
            return version.compareTo(pMDoc.version);
    }

}
