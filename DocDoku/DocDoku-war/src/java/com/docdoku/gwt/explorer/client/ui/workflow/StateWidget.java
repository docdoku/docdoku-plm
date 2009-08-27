/*
 * StateWidget.java
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
package com.docdoku.gwt.explorer.client.ui.workflow;

import com.docdoku.gwt.explorer.client.ui.widget.input.EditableLabel;
import com.docdoku.gwt.explorer.client.ui.widget.input.checker.NotEmptyChecker;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import org.cobogw.gwt.user.client.ui.RoundedPanel;

/**
 *
 * @author Emmanuel Nhan <emmanuel.nhan@insa-lyon.fr>
 */
public class StateWidget extends RoundedPanel implements ChangeHandler {

    private EditableLabel stateEdit;

    public StateWidget() {
        super(ALL, 3);

        HorizontalPanel p = new HorizontalPanel();
        p.setStyleName("editableState-Element");
        p.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        stateEdit = new EditableLabel();
        stateEdit.setNormalStyle("docdoku-editableStateWidget");
        stateEdit.setSelectedStyle("docdoku-editableStateWidget-selected");
        stateEdit.setOverStyle("docdoku-editableStateWidget-over");
        stateEdit.setTextAlignment(EditableLabel.ALIGN_CENTER);
        stateEdit.setChecker(new NotEmptyChecker());
        p.add(stateEdit);

        p.setWidth("100%");
        setWidget(p);
        setCornerStyleName("editableState-Corner");
        stateEdit.addChangeHandler(this);
    }

    public void setText(String text) {
        stateEdit.setText(text);
    }

    public String getText() {
        return stateEdit.getText();
    }

    public HandlerRegistration addChangeHandler(ChangeHandler handler) {
        return stateEdit.addChangeHandler(handler);
    }

    public EditableLabel getStateEdit() {
        return stateEdit;
    }

    public void onChange(ChangeEvent event) {
        if (stateEdit.getText().length() > stateEdit.getVisibleLength()) {
            while (stateEdit.getText().length() > stateEdit.getVisibleLength()) {
                stateEdit.setVisibleLength(stateEdit.getVisibleLength() + 1);
            }

        } else {
            while (stateEdit.getText().length() < stateEdit.getVisibleLength()) {
                stateEdit.setVisibleLength(stateEdit.getVisibleLength() - 1);
            }
        }
        setText(getText());
    }
}
