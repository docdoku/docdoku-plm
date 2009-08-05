package com.docdoku.gwt.explorer.common;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SerialActivityDTO extends ActivityDTO implements Serializable{
	

	public SerialActivityDTO(){
		
	}

    @Override
    public boolean isComplete() {
        return getStep() == getTasks().size() ;
    }

    
}
