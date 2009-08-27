/*
 * DocdokuLineEdit.java
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

import com.docdoku.gwt.client.ui.widget.util.DocdokuChecker;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.TextBox;
import java.util.HashSet;
import java.util.Set;

/**
 * DocdokuLineEdit is a simple TextBox providing checking features on input
 * A DocdokuLineEdit fires DocdokuLineEditEvent whenever its input state change.
 * @author Emmanuel Nhan <emmanuel.nhan@insa-lyon.fr>
 */
public class DocdokuLineEdit extends TextBox implements ChangeHandler, KeyPressHandler{

    private DocdokuChecker checker ;
    private String backup ;
    private Set<DocdokuLineEditListener> observers ;
    private boolean hasAcceptableInput ;
    public DocdokuLineEdit() {
        backup = "" ;
        checker = new DocdokuChecker() {

            public boolean check(String expressionToCheck) {
                return true ;
            }
        };
        addChangeHandler(this);
//        addKeyUpHandler(this);
        addKeyPressHandler(this);
        observers = new HashSet<DocdokuLineEditListener>() ;
        hasAcceptableInput = true ;
    }

    public void onChange(ChangeEvent event) {
        if (!checker.check(super.getText())){
            setText(backup);
        }else{
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
        if(checker.check(super.getText())){
            return super.getText();
        }else{
            return backup ;
        }
    }

    protected String getBackup() {
        return backup;
    }

    public void setChecker(DocdokuChecker checker) {
        this.checker = checker;
    }

    public boolean containsAcceptableInput(){
        return checker.check(super.getText()) ;
    }


    public void addListener (DocdokuLineEditListener listener){
        observers.add(listener);
    }

    public void removeListener(DocdokuLineEditListener listener){
        observers.remove(listener);
    }

    private void fireInputStateChange(){
        DocdokuLineEditEvent event = new DocdokuLineEditEvent(this);
        for (DocdokuLineEditListener observer : observers) {
            observer.onInputStateChange(event);
        }
    }

    public void onKeyPress(KeyPressEvent event) {
        if (checker.check(super.getText()) != hasAcceptableInput){
            hasAcceptableInput = containsAcceptableInput();
            fireInputStateChange();
        }
    }

}
