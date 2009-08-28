package com.docdoku.gwt.explorer.client.ui.workflow;


import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Simple image to ilustrate a workflow end
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class EndPoint extends VerticalPanel{

	public  EndPoint() {
		Image im = new Image();
		ServiceLocator.getInstance().getExplorerImageBundle().getEndImage().applyTo(im);
		this.add(im);
	}
}
