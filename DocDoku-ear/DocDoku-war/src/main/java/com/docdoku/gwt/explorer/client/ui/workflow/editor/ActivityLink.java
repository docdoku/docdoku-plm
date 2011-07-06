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

package com.docdoku.gwt.explorer.client.ui.workflow.editor;

import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;

/**
 * This widget is a simple horizontal line which let appear 2 images on hover.s
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class ActivityLink extends Composite implements MouseOverHandler, MouseOutHandler, ClickHandler{

    private Image serial;
    private Image parallel;
    private Image link;
    private boolean in;

    public ActivityLink() {
        HorizontalPanel mainPanel = new HorizontalPanel();
        initWidget(mainPanel);

        serial = new Image(ServiceLocator.getInstance().getExplorerImageBundle().getSerialImage());
        serial.setStyleName("workflow-addActivity");
        parallel = new Image(ServiceLocator.getInstance().getExplorerImageBundle().getParallelImage());
        parallel.setStyleName("workflow-addActivity");

        link = new Image(ServiceLocator.getInstance().getExplorerImageBundle().getHorizontalLine());

        mainPanel.add(link);
        mainPanel.add(serial);
        mainPanel.add(parallel);

        serial.setTitle(ServiceLocator.getInstance().getExplorerI18NConstants().addSerialTooltip());
        parallel.setTitle(ServiceLocator.getInstance().getExplorerI18NConstants().addParallelTooltip());

        in = false;
        serial.setVisible(false);
        parallel.setVisible(false);

        addDomHandler(this, MouseOverEvent.getType());
        addDomHandler(this, MouseOutEvent.getType());
        serial.addClickHandler(this);
        parallel.addClickHandler(this);

    }

    public void onMouseOver(MouseOverEvent event) {
        if (!in) {
            link.setVisible(false);
            parallel.setVisible(true);
            serial.setVisible(true);
            in = true;
        }
    }

    public void onMouseOut(MouseOutEvent event) {
        in = false;
        link.setVisible(true);
        serial.setVisible(false);
        parallel.setVisible(false);
    }

    

    public HandlerRegistration addActivityLinkHandler(ActivityLinkHandler handler){
        return addHandler(handler, ActivityLinkEvent.TYPE);
    }


    public void onClick(ClickEvent event) {
        if (event.getSource() == serial) {
            ActivityLinkEvent.fire(this, ActivityLinkEvent.OperationType.ADD_SERIAL);
        } else if (event.getSource() == parallel) {
            ActivityLinkEvent.fire(this, ActivityLinkEvent.OperationType.ADD_PARALLEL);
            
        }
    }

}
