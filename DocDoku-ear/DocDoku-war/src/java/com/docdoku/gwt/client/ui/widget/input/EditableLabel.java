/*
 * EditableLabel.java
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

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;

/**
 *
 * @author Emmanuel Nhan <emmanuel.nhan@insa-lyon.fr>
 */
public class EditableLabel extends DocdokuLineEdit implements MouseOverHandler, MouseOutHandler, BlurHandler, FocusHandler, KeyDownHandler {

    private final static String DEFAULT_STYLE = "docdoku-editableLabel";
    private final static String DEFAULT_FOCUS_STYLE = "docdoku-editableLabel-selected";
    private final static String DEFAULT_OVER_STYLE = "docdoku-editableLabel-over";
    private String normalStyle;
    private String selectedStyle;
    private String overStyle;
    private boolean hasFocus ;

    public EditableLabel() {
        normalStyle = DEFAULT_STYLE;
        selectedStyle = DEFAULT_FOCUS_STYLE;
        overStyle = DEFAULT_OVER_STYLE;
        setStyleName(normalStyle);
        addBlurHandler(this);
        addMouseOutHandler(this);
        addMouseOverHandler(this);
        addFocusHandler(this);
        addKeyDownHandler(this);
        hasFocus =false ;
    }

    public void onFocus(FocusEvent event) {
        addStyleName(selectedStyle);
        this.selectAll();
        removeStyleName(overStyle);
        hasFocus = true ;
    }

    public void onBlur(BlurEvent event) {
        removeStyleName(selectedStyle);
        hasFocus = false ;
//        int x = event.getNativeEvent().getClientX();
//        int y = event.getNativeEvent().getClientY() ;
//
//        if (x < getOffsetWidth() +  getAbsoluteLeft() && x > getAbsoluteLeft() && y < getOffsetHeight() + getAbsoluteTop() && y > getAbsoluteTop()){
//            addStyleName(overStyle) ;
//        }
    }

    public void onMouseOver(MouseOverEvent event) {
        if (!hasFocus){
            addStyleName(overStyle);
        }
    }

    public void onMouseOut(MouseOutEvent event) {
        removeStyleName(overStyle);
    }

    public String getNormalStyle() {
        return normalStyle;
    }

    public void setNormalStyle(String normalStyle) {
        this.normalStyle = normalStyle;
        setStyleName(normalStyle);
    }

    public String getSelectedStyle() {
        return selectedStyle;
    }

    public void setSelectedStyle(String selectedStyle) {
        this.selectedStyle = selectedStyle;
    }

    public void onKeyDown(KeyDownEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
            setText(getBackup());
            setFocus(false);
        } else if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
            setFocus(false);
        }
    }

    public String getOverStyle() {
        return overStyle;
    }

    public void setOverStyle(String overStyle) {
        this.overStyle = overStyle;
    }
}
