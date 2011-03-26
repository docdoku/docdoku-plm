/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
