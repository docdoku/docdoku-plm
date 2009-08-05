package com.docdoku.gwt.explorer.common;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ParallelActivityDTO extends ActivityDTO implements
		Serializable {

	private int nbTaskToComplete;
	public ParallelActivityDTO(){
		
	}

	public int getNbTaskToComplete() {
		return nbTaskToComplete;
	}

	public void setNbTaskToComplete(int nbTaskToComplete) {
		this.nbTaskToComplete = nbTaskToComplete;
	}


    @Override
    public boolean isComplete() {
        return getStep() >= nbTaskToComplete ;
    }
	
	
}
