package com.docdoku.gwt.explorer.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public abstract class ActivityDTO implements Serializable{
	
	private List<TaskDTO> tasks ;
    private int step ;
    private boolean stopped ;
	
	public ActivityDTO(){
		tasks = new ArrayList<TaskDTO>();
	}

	public List<TaskDTO> getTasks() {
		return tasks;
	}

	public void setTasks(List<TaskDTO> tasks) {
		this.tasks = tasks;
	}	
	
	public abstract boolean isComplete() ;
    public void addATaskComplete(){
        step ++ ;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int tasksCompleted) {
        this.step = tasksCompleted;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }
    
    
        
}
