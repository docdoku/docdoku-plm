package com.docdoku.gwt.explorer.client.ui.workflow.viewer;

import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;


public class HorizontalLink extends HorizontalPanel {

    protected Image link ;

	public HorizontalLink(){
        link = new Image(ServiceLocator.getInstance().getExplorerImageBundle().getHorizontalLine());
        this.add(link);
	}

}
