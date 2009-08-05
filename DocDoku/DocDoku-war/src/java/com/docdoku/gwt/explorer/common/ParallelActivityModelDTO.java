package com.docdoku.gwt.explorer.common;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ParallelActivityModelDTO extends AbstractActivityModelDTO implements Serializable{
	
	private int tasksToComplete ;
	
	public ParallelActivityModelDTO(){
		tasksToComplete = 1 ;
	}
	

	public int getTasksToComplete() {
		return tasksToComplete;
	}

	public void setTasksToComplete(int tasksToComplete) {
		this.tasksToComplete = tasksToComplete;
	}
		
	
}
