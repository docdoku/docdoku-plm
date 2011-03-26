/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.docdoku.gwt.explorer.client.ui.widget.menu;

import com.docdoku.gwt.client.ui.widget.menu.LabelMenuItem;
import com.docdoku.gwt.explorer.client.actions.Action;
import com.docdoku.gwt.explorer.client.util.DocdokuCommand;

/**
 * This is a class provided for conveniance and usage with Docdoku GWT client side actions
 * The command in this AbstractMenuItem is a DocdokuCommand
 * @author Emmanuel Nhan
 */
public class DocdokuLabelMenuItem extends LabelMenuItem{

    private DocdokuCommand command ;

    public DocdokuLabelMenuItem(String text) {
        super(text);
        command = new DocdokuCommand() ;
        setCommand(command);
    }

    public void setAction (Action action){
        command.setAction(action);
    }

    public void setParameters(Object... params){
        command.setParameters(params);
    }

}
