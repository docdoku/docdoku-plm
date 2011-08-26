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

package com.docdoku.gwt.client.ui.widget.spinbox;

import com.docdoku.gwt.client.ui.widget.WidgetServiceLocator;
import com.docdoku.gwt.client.ui.widget.resources.WidgetResourcesBundle;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * SpinBox is a simple widget that allows to choose an integer value
 * 
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class SpinBox extends Composite implements ClickHandler, ChangeHandler, HasValueChangeHandlers<Integer> {

    private static final String PRIMARY_STYLE = "docdoku-SpinBox";
    private int minValue;
    private int maxValue;
    private PushButton buttonUp;
    private PushButton buttonDown;
    private int value;
    private TextBox inputField;
    private int backupValue;

    /**
     * Build a default SpinBox
     * This spin box allows values between 0 and 100, initialy it shows 0
     */
    public SpinBox() {
        this(0, 100, 0);
    }

    /**
     * Build a SpinBox with specified values
     * @param min the minimum value
     * @param max the maximum value
     * @param initial the initial value
     */
    public SpinBox(int min, int max, int initial) {
        maxValue = max;
        minValue = min;
        value = initial;
        backupValue = value;
        setupUi();
        setupListeners();
        inputField.setStyleName(PRIMARY_STYLE);
    }

    /**
     * Retrieves the maximum value
     * @return the maximum value accepted by the SpinBox
     */
    public int getMaxValue() {
        return maxValue;
    }

    /**
     *
     * @param maxValue
     */
    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
        if (value > maxValue) {
            value = maxValue;
            onValueChanged();
        } else {
            onValueChangeWithoutNotification();
        }

    }

    public void setMinValue(int minValue) {
        this.minValue = minValue;
        if (value < minValue) {
            value = minValue;
            onValueChanged();
        } else {
            onValueChangeWithoutNotification();
        }

    }

    public int getValue() {
        return value;
    }

    public void setValue(int newValue) {
        value = newValue;
        onValueChanged();
    }

    private void setupUi() {
        HorizontalPanel mainPanel = new HorizontalPanel();
        VerticalPanel buttonsPanel = new VerticalPanel();
        WidgetResourcesBundle images = WidgetServiceLocator.getInstance().getImages();
        Image up = new Image(images.smallUpImage());
        Image down = new Image(images.smallDownImage());
        buttonUp = new PushButton(up);
        buttonUp.setStyleName("spinbox-button");
        buttonDown = new PushButton(down);
        buttonDown.setStyleName("spinbox-button");
        buttonsPanel.add(buttonUp);
        buttonsPanel.add(buttonDown);
        inputField = new TextBox();
        inputField.setVisibleLength(1);
        inputField.setText("" + value);
        inputField.setTextAlignment(TextBox.ALIGN_RIGHT);
        mainPanel.add(inputField);
        mainPanel.add(buttonsPanel);
        initWidget(mainPanel);
        onValueChanged();
    }

    private void setupListeners() {
        buttonUp.addClickHandler(this);
        buttonDown.addClickHandler(this);
        inputField.addChangeHandler(this);
    }

    @Override
    public void onClick(ClickEvent event) {
        if (event.getSource() == buttonUp) {
            if (value < maxValue) {
                value++;
                onValueChanged();
            }

        } else {
            if (value > minValue) {
                value--;
                onValueChanged();
            }
        }
    }

    private void onValueChangeWithoutNotification() {
        // value is the new backup now
        backupValue = value;
        if (value == maxValue && value == minValue) {
            buttonUp.setEnabled(false);
            buttonDown.setEnabled(false);
        } else if (value == maxValue) {
            buttonUp.setEnabled(false);
            buttonDown.setEnabled(true);
        } else if (value == minValue) {
            buttonUp.setEnabled(true);
            buttonDown.setEnabled(false);
        } else {
            buttonUp.setEnabled(true);
            buttonDown.setEnabled(true);
        }
        inputField.setText("" + value);
    }

    private void onValueChanged() {
        // value is the new backup now
        backupValue = value;
        if (value == maxValue && value == minValue) {
            buttonUp.setEnabled(false);
            buttonDown.setEnabled(false);
        } else if (value == maxValue) {
            buttonUp.setEnabled(false);
            buttonDown.setEnabled(true);
        } else if (value == minValue) {
            buttonUp.setEnabled(true);
            buttonDown.setEnabled(false);
        } else {
            buttonUp.setEnabled(true);
            buttonDown.setEnabled(true);
        }
        inputField.setText("" + value);
        fireChange();
    }

    @Override
    public void onChange(ChangeEvent event) {
        if (inputField.getText().matches("^[0-9]+")) {
            Integer tempValue = new Integer(inputField.getText());
            if (tempValue >= minValue && tempValue <= maxValue) {
                value = tempValue;
            } else {
                // restaure backup :
                value = backupValue;
            }
        } else {
            value = backupValue;
        }
        onValueChanged();
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Integer> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    private void fireChange() {
        ValueChangeEvent.fire(this, value);
    }
}
