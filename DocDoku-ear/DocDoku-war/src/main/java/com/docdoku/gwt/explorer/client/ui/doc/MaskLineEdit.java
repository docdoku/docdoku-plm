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

package com.docdoku.gwt.explorer.client.ui.doc;

import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.TextBox;

/*
 * TODO : handler system to notify when a valid input is entered
 *
 */
/**
 *
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class MaskLineEdit extends TextBox implements KeyDownHandler, KeyUpHandler{

    public enum InputState {VALID,NOT_VALID}

    private static final char NUMBER = '_';
    private static final char CHAR = '*';
    private String mask;
    private String backup;
    private String regExp ;


    

    public MaskLineEdit() {
        addKeyDownHandler(this);
        addKeyUpHandler(this);
        mask = "" ;
        regExp = ".*" ;
    }

    public void onKeyDown(KeyDownEvent event) {
        backup = getText();
    }

    public void setMask(String pmask) {
        mask = pmask ;
        regExp = mask.replaceAll("#", "[0-9]") ;
        regExp.replaceAll("\\*", ".");
        mask = mask.replaceAll("#", "_") ;
        setText(mask);
    }

    public void onKeyUp(KeyUpEvent event) {
        if (mask != null && !mask.equals("")) {
            int curPosBackup = getCursorPos();

            if (getText().length() < backup.length()) {
                // difference is at cursor position
                if (mask.charAt(getCursorPos()) == NUMBER || mask.charAt(getCursorPos()) == CHAR) {
                    String newContent = getText().substring(0, getCursorPos()) + mask.charAt(getCursorPos()) + getText().substring(getCursorPos());
                    setText(newContent);
                    setCursorPos(curPosBackup);
                } else {
                    setText(backup);
                    setCursorPos(curPosBackup);
                }
            } else if (getText().length() > backup.length()) {
                // get the change
                int i = 0;
                boolean change = false;
                for (i = 0; i < backup.length() && !change; i++) {
                    change = backup.charAt(i) != getText().charAt(i);
                }

                if (i == backup.length() && !change) {
                    // char added at the end, restaure backup :
                    setText(backup);
                } else {

                    String added = getText().charAt(i - 1) + "";
                    String testRegExp = null;
                    if (mask.charAt(i - 1) == NUMBER) {
                        testRegExp = "[0-9]";
                    } else if (mask.charAt(i - 1) == CHAR) {
                        testRegExp = ".";
                    }

                    if (testRegExp != null && added.matches(testRegExp)) {
                        // remove next char
                        String newContent = getText().substring(0, i) + getText().substring(i + 1);
                        setText(newContent);
                        setCursorPos(i);
                    } else {
                        setText(backup);
                        setCursorPos(curPosBackup);
                    }
                }
            }
        }
    }

    public void setGeneratedId(String id){
        setText(id);
    }
}
