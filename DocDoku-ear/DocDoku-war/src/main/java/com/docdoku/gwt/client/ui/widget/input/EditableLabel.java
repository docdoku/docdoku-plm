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
public class EditableLabel extends ConstrainedTextBox implements MouseOverHandler, MouseOutHandler, BlurHandler, FocusHandler, KeyDownHandler {

    private final static String PRIMARY_STYLE = "docdoku-EditableLabel";
    private final static String ON_FOCUS_DEPENDENT_STYLE = "selected";
    private final static String ON_HOVER_DEPENDENT_STYLE = "hover";
    private boolean hasFocus;

    public EditableLabel() {
        setStyleName(PRIMARY_STYLE);
        addBlurHandler(this);
        addMouseOutHandler(this);
        addMouseOverHandler(this);
        addFocusHandler(this);
        addKeyDownHandler(this);
        hasFocus = false;
    }

    @Override
    public void onFocus(FocusEvent event) {
        addStyleDependentName(ON_FOCUS_DEPENDENT_STYLE);
        this.selectAll();
        removeStyleDependentName(ON_HOVER_DEPENDENT_STYLE);
        hasFocus = true;
    }

    @Override
    public void onBlur(BlurEvent event) {
        removeStyleDependentName(ON_FOCUS_DEPENDENT_STYLE);
        hasFocus = false;
    }

    @Override
    public void onMouseOver(MouseOverEvent event) {
        if (!hasFocus) {
            addStyleDependentName(ON_HOVER_DEPENDENT_STYLE);
        }
    }

    @Override
    public void onMouseOut(MouseOutEvent event) {
        removeStyleDependentName(ON_HOVER_DEPENDENT_STYLE);
    }

    @Override
    public void onKeyDown(KeyDownEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
            setText(getBackup());
            setFocus(false);
        } else if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
            setFocus(false);
        }
    }
}
