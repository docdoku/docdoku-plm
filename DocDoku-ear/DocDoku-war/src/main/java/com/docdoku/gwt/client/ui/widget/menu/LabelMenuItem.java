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

package com.docdoku.gwt.client.ui.widget.menu;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Label;

/**
 * This class is provided for convenience and offers a way to add label
 * to popup menus.
 * 
 * @author Emmanuel Nhan
 */
public class LabelMenuItem extends AbstractMenuItem implements ClickHandler{

    private Label label;

    public LabelMenuItem(String text) {
        label = new Label(text);
        initWidget(label);
        label.addClickHandler(this);
        setStyleName("docdoku-LabelMenuItem");
    }

    @Override
    protected boolean beforeCommandCall() {
        return true;
    }

    @Override
    protected void afterCommandCall() {       
    }

    @Override
    public void setSelected(boolean selected) {
        setStyleDependentName("selected", selected);
    }

    @Override
    public void onClick(ClickEvent event) {
        activate();
    }

    @Override
    public void onShowUp() {
        // nothing to do by default
    }



}
