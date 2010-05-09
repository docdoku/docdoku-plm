package com.docdoku.gwt.explorer.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class WorkflowDTO implements Serializable{
	

	private List<ActivityDTO> activities;
	private String finalStateName;
	private List<String> states;
    private int currentStep ;
    private String workspaceId ;
    private int id;
	
	
	public WorkflowDTO(){
        activities = new ArrayList<ActivityDTO>();
        states = new ArrayList<String>();
	}


	public List<ActivityDTO> getActivities() {
		return activities;
	}

    public int getId() {
        return id ;
    }

	public void setActivities(List<ActivityDTO> activities) {
		this.activities = activities;
	}

	public String getFinalStateName() {
		return finalStateName;
	}


	public void setFinalStateName(String finalStateName) {
		this.finalStateName = finalStateName;
	}

    public void setId(int id) {
        this.id = id ;
    }


	public void setStates(List<String> states) {
		this.states = states;
	}


	public List<String> getStates() {
		return states;
	}

    public int getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    

    
}
