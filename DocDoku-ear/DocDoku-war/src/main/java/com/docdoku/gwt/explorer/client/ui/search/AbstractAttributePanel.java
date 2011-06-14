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

package com.docdoku.gwt.explorer.client.ui.search;

import com.docdoku.gwt.explorer.shared.SearchQueryDTO;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

public abstract class AbstractAttributePanel extends HorizontalPanel {

    private static final int VISIBLE_LENGTH = 10;
    private TextBox nameField;
    private Label afterName;

    public AbstractAttributePanel() {
        this(true);
    }

    public AbstractAttributePanel(boolean showEqual) {
        nameField = new TextBox();
        nameField.setVisibleLength(VISIBLE_LENGTH);
        add(nameField);
        if (showEqual) {
            afterName = new Label("=");
            add(afterName);
        }
    }

    public String getNameValue() {
        return nameField.getText();
    }

    public void setNameValue(String name) {
        nameField.setText(name);
    }

    abstract public SearchQueryDTO.AbstractAttributeQueryDTO getAttribute();
}
