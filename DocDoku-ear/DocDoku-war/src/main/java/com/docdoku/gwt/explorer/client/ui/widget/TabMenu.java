package com.docdoku.gwt.explorer.client.ui.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Label;

import org.cobogw.gwt.user.client.ui.RoundedPanel;

/**
 *
 * @author Florent GARIN
 */
public class TabMenu extends RoundedPanel{

    private Label text;

    public TabMenu(String title, TabMenuGroup group){
        super(RoundedPanel.LEFT);
		setCornerStyleName("my-RP-Corner");
        setStyleName("my-RP");
		text = new Label(title);
		//text.setText(title);
		text.setStyleName("my-RP-Link");
		setWidget(text);

        group.addTabMenu(this);
        registerListeners();
    }

    public HandlerRegistration addClickHandler(ClickHandler handler){
        return text.addClickHandler(handler);
    }

    private void registerListeners(){
        text.addClickHandler(new ClickHandler(){
            public void onClick(ClickEvent event) {
				select();
            }
		});
    }

    public void select(){
        text.addStyleName("selected");
		addStyleName("selected");
    }

    public void unselect(){
        text.removeStyleName("selected");
		removeStyleName("selected");
    }
}
