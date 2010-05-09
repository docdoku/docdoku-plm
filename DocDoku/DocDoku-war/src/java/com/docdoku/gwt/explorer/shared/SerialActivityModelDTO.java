package com.docdoku.gwt.explorer.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SerialActivityModelDTO extends ActivityModelDTO implements Serializable{
	
	public SerialActivityModelDTO(){
		super();
	}
		
	public void moveUpTask(int index){
		if (index > 0){
			TaskModelDTO t = taskModels.get(index);
			this.taskModels.remove(index);
			this.taskModels.add(index-1, t);
		}
	}
	
	public void moveDownTask(int index){
		if (index < taskModels.size() -1){
			TaskModelDTO t = taskModels.get(index);
			this.taskModels.remove(index);
			this.taskModels.add(index+1, t);
		}
	}

}
