/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011 DocDoku SARL
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

package com.docdoku.gwt.client.ui.widget.util;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;

/**
 * InteractiveHTML is like an HTML widget, but displays a popup tooltip on mouse over
 * 
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class InteractiveHTML extends HTML implements MouseOutHandler, MouseMoveHandler, ClickHandler {

    private TooltipPanel tooltipPanel;

    public InteractiveHTML(String html, String tooltip[], boolean hideOnClick) {
        super(html);
        this.tooltipPanel = new TooltipPanel(tooltip);
        addMouseMoveHandler(this);
        addMouseOutHandler(this);
        if (hideOnClick) {
            addClickHandler(this);
        }

    }

    public InteractiveHTML(String html, String tooltip[]){
        this(html, tooltip, true);
    }


    @Override
    public void onMouseMove(MouseMoveEvent event) {

        if (!tooltipPanel.isShowing()) {
            tooltipPanel.showRelativeTo(this);
        }
    }

    @Override
    public void onMouseOut(MouseOutEvent event) {
        tooltipPanel.hide();
    }

    @Override
    public void onClick(ClickEvent event) {
        tooltipPanel.hide();
    }

    private class TooltipPanel extends DecoratedPopupPanel {

        private HorizontalPanel panel;

        public TooltipPanel(String input[]) {
            panel = new HorizontalPanel();
            for (String str : input) {
                panel.add(new InlineLabel(str));
            }
            setWidget(panel);
        }
    }
}
