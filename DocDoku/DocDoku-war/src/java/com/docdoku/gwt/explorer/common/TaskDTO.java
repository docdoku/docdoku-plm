package com.docdoku.gwt.explorer.common;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("serial")
public class TaskDTO implements Serializable{

    public enum TaskStatus{
        APPROVED,
        IN_PROGRESS,
        NOT_STARTED,
        REJECTED
    }
	
	private String closureComment ;
	private String title ;
	private String instructions ;
	private String workerName ;
	private int targetIteration ;
	private Date closureDate ;
	private String workerMail ;
	private TaskStatus status;
	
	// 0 = approved
	// 1 = in progress
	// 2 = not started
	// 3 = rejected
	//private int status ;
	
	public TaskDTO(){
		
	}

	public String getClosureComment() {
		return closureComment;
	}

	public void setClosureComment(String closureComment) {
		this.closureComment = closureComment;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getInstructions() {
		return instructions;
	}

	public void setInstructions(String instructions) {
		this.instructions = instructions;
	}

	public String getWorkerName() {
		return workerName;
	}

	public void setWorkerName(String workerName) {
		this.workerName = workerName;
	}

	public int getTargetIteration() {
		return targetIteration;
	}

	public void setTargetIteration(int targetIteration) {
		this.targetIteration = targetIteration;
	}

//	public int getStatus() {
//		return status;
//	}
//
//	public void setStatus(int status) {
//		this.status = status;
//	}

	public Date getClosureDate() {
		return closureDate;
	}

	public void setClosureDate(java.util.Date date) {
		this.closureDate = date;
	}

	public void setWorkerMail(String workerMail) {
		this.workerMail = workerMail;
	}

	public String getWorkerMail() {
		return workerMail;
	}

	
	public TaskStatus getStatus() {
		return status;
	}

	public void setStatus(TaskStatus status) {
		this.status = status;
	}
	
	
	

}
