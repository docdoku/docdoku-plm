package com.docdoku.gwt.explorer.common;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SerialActivityModelDTO extends AbstractActivityModelDTO implements Serializable{
	
	public SerialActivityModelDTO(){
		super();
	}
		
	public void moveUpTask(int index){
		if (index > 0){
			TaskModelDTO t = tasks.get(index);
			this.tasks.remove(index);
			this.tasks.add(index-1, t);
		}
	}
	
	public void moveDownTask(int index){
		if (index < tasks.size() -1){
			TaskModelDTO t = tasks.get(index);
			this.tasks.remove(index);
			this.tasks.add(index+1, t);
		}
	}

}
