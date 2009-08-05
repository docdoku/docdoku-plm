/*
 * ParallelActivityEvent.java
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

package com.docdoku.gwt.explorer.client.ui.workflow.editor.model;

import java.util.EventObject;

/**
 *
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class ParallelActivityEvent extends EventObject{

    private int newValue ;
    private ParallelActivityModelModel realSource ;

    public ParallelActivityEvent(ParallelActivityModelModel source, int value) {
        super(source);
        newValue = value;
        realSource = source ;
    }

    public int getNewValue() {
        return newValue;
    }

    public ParallelActivityModelModel getRealSource() {
        return realSource;
    }

    


    

    

}
