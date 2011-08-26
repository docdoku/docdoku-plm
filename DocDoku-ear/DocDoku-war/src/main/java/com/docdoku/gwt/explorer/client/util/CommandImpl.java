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

package com.docdoku.gwt.explorer.client.util;

import com.docdoku.gwt.explorer.client.actions.Action;
import com.google.gwt.user.client.Command;

/**
 * As actions have parameters,
 * this class encapsulate params & command call
 * for Actions.
 * It is useful in ButtonMenu for instance, with a ExplorerLabelMenuItem
 * @author Emmanuel Nhan
 */
public class CommandImpl implements Command{

    private Action action;
    private Object parameters[];

    @Override
    public void execute() {
        action.execute(parameters);
    }

    public void setParameters(Object... params){
        parameters = params;
    }

    public void setAction(Action a){
        action = a;
    }

}
