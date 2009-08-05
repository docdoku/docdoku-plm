package com.docdoku.gwt.explorer.client.ui.workflow;

import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;


public class HorizontalLink extends HorizontalPanel implements WorkflowItem/*, HasClickHandlers*/{

    protected Image link ;

	public HorizontalLink(){
        link = new Image();
		ServiceLocator.getInstance().getExplorerImageBundle().getHorizontalLine().applyTo(link);
        this.add(link);
	}

}
