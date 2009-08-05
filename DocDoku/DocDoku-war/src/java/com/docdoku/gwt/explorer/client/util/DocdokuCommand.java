/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.docdoku.gwt.explorer.client.util;

import com.docdoku.gwt.explorer.client.actions.Action;
import com.google.gwt.user.client.Command;

/**
 * As actions in Docdoku have parameters,
 * this class encapsulate params & command call
 * for Actions.
 * @author Emmanuel Nhan
 */
public class DocdokuCommand implements Command{

    private Action action ;
    private Object parameters[] ;

    public void execute() {
        action.execute(parameters);
    }

    public void setParameters(Object... params){
        parameters = params;
    }

    public void setAction(Action a){
        action = a ;
    }

}
