package com.docdoku.gwt.explorer.client.ui.workflow;

import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Image;

public class VerticalLink extends Image {

	public VerticalLink(){
		AbstractImagePrototype.create(ServiceLocator.getInstance().getExplorerImageBundle().getVerticalLine()).applyTo(this);
	}
	
}

