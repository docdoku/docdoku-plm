package com.docdoku.gwt.explorer.common;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * AbstractActivityDTO is an abstract class designed to support 
 * GWT RPC serialization. This means that it implemenents
 * java.io.Serializable and uses arrays to manage collections :'(
 * 
 * @author Emmanuel Nhan
 *
 */
@SuppressWarnings("serial")
public abstract class AbstractActivityModelDTO implements Serializable{
		

	protected List<TaskModelDTO> tasks ;
	private String lifeCycleState ;
	
	public AbstractActivityModelDTO(){
		this.tasks = new ArrayList<TaskModelDTO>();
	}

	public void addTask(TaskModelDTO m){
		this.tasks.add(m);
	}
	
	public void removeTask(TaskModelDTO m){
		this.tasks.remove(m);
	}
	
	public List<TaskModelDTO> getTasks(){
		return this.tasks;
	}

	

	public void setLifeCycleState(String lifeCycleState) {
		this.lifeCycleState = lifeCycleState;
	}

	public String getLifeCycleState() {
		return lifeCycleState;
	}
	
}
