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

import com.docdoku.gwt.client.ui.widget.util.InputValidator;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.TextBox;
import java.util.HashSet;
import java.util.Set;

/**
 * ConstrainedTextBox is a simple TextBox providing checking features on input
 * A ConstrainedTextBox fires ConstrainedStateChangeEvent whenever its input state change.
 * @author Emmanuel Nhan <emmanuel.nhan@insa-lyon.fr>
 */
public class ConstrainedTextBox extends TextBox implements ChangeHandler, KeyPressHandler {

    private InputValidator validator;
    private String backup;
    private Set<ConstrainedStateChangeListener> observers;
    private boolean hasAcceptableInput;

    public ConstrainedTextBox() {
        backup = "";
        validator = new InputValidator() {

            @Override
            public boolean validate(String expressionToCheck) {
                return true;
            }
        };
        addChangeHandler(this);
        addKeyPressHandler(this);
        observers = new HashSet<ConstrainedStateChangeListener>();
        hasAcceptableInput = true;
    }

    @Override
    public void onChange(ChangeEvent event) {
        if (!validator.validate(super.getText())) {
            setText(backup);
        } else {
            backup = super.getText();
        }
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        backup = text;
        hasAcceptableInput = containsAcceptableInput();
    }

    @Override
    public String getText() {
        if (validator.validate(super.getText())) {
            return super.getText();
        } else {
            return backup;
        }
    }

    protected String getBackup() {
        return backup;
    }

    public void setValidator(InputValidator validator) {
        this.validator = validator;
    }

    public boolean containsAcceptableInput() {
        return validator.validate(super.getText());
    }

    public void addListener(ConstrainedStateChangeListener listener) {
        observers.add(listener);
    }

    public void removeListener(ConstrainedStateChangeListener listener) {
        observers.remove(listener);
    }

    private void fireInputStateChange() {
        ConstrainedStateChangeEvent event = new ConstrainedStateChangeEvent(this);
        for (ConstrainedStateChangeListener observer : observers) {
            observer.onInputStateChange(event);
        }
    }

    @Override
    public void onKeyPress(KeyPressEvent event) {
        if (validator.validate(super.getText()) != hasAcceptableInput) {
            hasAcceptableInput = containsAcceptableInput();
            fireInputStateChange();
        }
    }
}
