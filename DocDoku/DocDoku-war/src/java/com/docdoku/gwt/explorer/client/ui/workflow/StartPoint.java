package com.docdoku.gwt.explorer.client.ui.workflow;

import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * Simple image to illustrate the beginning of a workflow
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class StartPoint extends SimplePanel {
	
	public StartPoint(){
		
		Image im = new Image();
		ServiceLocator.getInstance().getExplorerImageBundle().getStartImage().applyTo(im);
		this.add(im) ;
	}
	
}
