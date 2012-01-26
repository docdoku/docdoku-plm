/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,  
 * but WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
 * GNU General Public License for more details.  
 *  
 * You should have received a copy of the GNU General Public License  
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.  
 */

package com.docdoku.gwt.explorer.client.ui.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Label;

import org.cobogw.gwt.user.client.ui.RoundedPanel;

/**
 *
 * @author Florent Garin
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
