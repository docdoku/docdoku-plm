package com.docdoku.gwt.explorer.client.ui.workflow;

import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;

public class StartPoint extends SimplePanel implements WorkflowItem {
	
	public StartPoint(){
		
		Image im = new Image();
		ServiceLocator.getInstance().getExplorerImageBundle().getStartImage().applyTo(im);
		this.add(im) ;
	}
	
}
