package com.docdoku.gwt.explorer.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressWarnings("serial")
public class WorkflowModelDTO implements Serializable {

	private String workspaceId;
	private String id;
	private String finalLifeCycleState;
	private String author;
    private Date creationDate;
    
	private List<AbstractActivityModelDTO> activities;

	public WorkflowModelDTO() {
		activities = new ArrayList<AbstractActivityModelDTO>();
	}

	public WorkflowModelDTO(String workspaceId, String id) {
		this.workspaceId = workspaceId;
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public String getWorkspaceId() {
		return workspaceId;
	}

	public void setWorkspaceId(String workspaceId) {
		this.workspaceId = workspaceId;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void addActivity(AbstractActivityModelDTO activity) {
		this.activities.add(activity);
	}

	public void removeActivity(AbstractActivityModelDTO activity) {
		this.activities.remove(activity);
	}

	public List<AbstractActivityModelDTO> getActivities() {
		return this.activities;
	}

    public AbstractActivityModelDTO[] getActivitiesArray(){
        AbstractActivityModelDTO result[] = new AbstractActivityModelDTO[activities.size()];
        for (int i = 0 ; i < result.length ; i++){
            result[i] = activities.get(i);
        }
        return result;
    }

	public void setFinalLifeCycleState(String finalLifeCycleState) {
		this.finalLifeCycleState = finalLifeCycleState;
	}

	public String getFinalLifeCycleState() {
		return finalLifeCycleState;
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

	public void setActivities(List<AbstractActivityModelDTO> activities) {
		this.activities = activities;
	}

}
