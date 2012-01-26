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

package com.docdoku.gwt.explorer.client.ui.workflow.editor;

import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 *
 * @author Emmanuel Nhan
 */
public class InstructionsPopup extends DecoratedPopupPanel{

    private TextArea instructions ;
    
    public InstructionsPopup(String text){
        super(true, true) ;
        VerticalPanel panel = new VerticalPanel();
        panel.add(new Label(ServiceLocator.getInstance().getExplorerI18NConstants().instructions()));
        instructions = new TextArea() ;
        instructions.setText(text);
        panel.add(instructions);
        setWidget(panel);
        instructions.setFocus(true);
    }

    public void setInscructions(String text){
        instructions.setText(text);
    }

    public String getInstructions() {
        return instructions.getText();
    }

}
