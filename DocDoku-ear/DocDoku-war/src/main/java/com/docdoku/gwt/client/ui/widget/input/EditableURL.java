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

package com.docdoku.gwt.client.ui.widget.input;

import com.docdoku.gwt.client.ui.widget.util.URLValidator;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 *
 * @author Emmanuel Nhan <emmanuel.nhan@insa-lyon.fr>
 */
public class EditableURL extends Composite{

    private ConstrainedTextBox lineEdit;
    private InlineLabel link;

    public EditableURL() {
        lineEdit = new ConstrainedTextBox();
        lineEdit.setValidator(new URLValidator());
        link = new InlineLabel();
        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.add(lineEdit);
        mainPanel.add(link);
        initWidget(mainPanel);
        lineEdit.setVisible(true);
        link.addStyleName("normalLinkAction");
        link.setVisible(false);
        link.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                Window.open(link.getText(), link.getText(), "");
            }
        });

    }

    public void setEnabled(boolean enabled) {
        lineEdit.setVisible(enabled);
        link.setVisible(!enabled);
    }

    public void setText(String text) {
        lineEdit.setText(text);
        link.setText(text);
    }

    public void removeListener(ConstrainedStateChangeListener listener) {
        lineEdit.removeListener(listener);
    }

    public String getText() {
        return lineEdit.getText();
    }

    public boolean containsAcceptableInput() {
        return lineEdit.containsAcceptableInput();
    }

    public void addListener(ConstrainedStateChangeListener listener) {
        lineEdit.addListener(listener);
    }





}
