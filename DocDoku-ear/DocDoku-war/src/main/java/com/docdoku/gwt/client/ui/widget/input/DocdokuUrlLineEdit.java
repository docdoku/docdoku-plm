/*
 * DocdokuUrlLineEdit.java
 * 
 * Copyright (c) 2009 Docdoku. All rights reserved.
 * 
 * This file is part of Docdoku.
 * 
 * Docdoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Docdoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Docdoku.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.gwt.client.ui.widget.input;

import com.docdoku.gwt.client.ui.widget.util.URLChecker;
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
public class DocdokuUrlLineEdit extends Composite{

    private DocdokuLineEdit lineEdit ;
    private InlineLabel link ;

    public DocdokuUrlLineEdit() {
        lineEdit = new DocdokuLineEdit();
        lineEdit.setChecker(new URLChecker());
        link = new InlineLabel();
        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.add(lineEdit) ;
        mainPanel.add(link);
        initWidget(mainPanel);
        lineEdit.setVisible(true);
        link.addStyleName("normalLinkAction");
        link.setVisible(false);
        link.addClickHandler(new ClickHandler() {

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

    public void removeListener(DocdokuLineEditListener listener) {
        lineEdit.removeListener(listener);
    }

    public String getText() {
        return lineEdit.getText();
    }

    public boolean containsAcceptableInput() {
        return lineEdit.containsAcceptableInput();
    }

    public void addListener(DocdokuLineEditListener listener) {
        lineEdit.addListener(listener);
    }





}
