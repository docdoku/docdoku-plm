package com.docdoku.gwt.explorer.common;

import java.io.Serializable;


@SuppressWarnings("serial")
public class TaskModelDTO implements Serializable{
	

	private String taskName ;
	//private String responsibleName ;
	private String instructions ;
	private UserDTO responsible ;

	public TaskModelDTO(){
	}
	

	
	public String getTaskName() {
		return taskName;
	}
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
		
	public String getInstructions() {
		return instructions;
	}
	public void setInstructions(String instructions) {
		this.instructions = instructions;
	}

	public UserDTO getResponsible() {
		return responsible;
	}

	public void setResponsible(UserDTO responsible) {
		this.responsible = responsible;
	}

	

}
