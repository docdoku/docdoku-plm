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

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.GwtEvent.Type;

/**
 *
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class ParallelActivityEvent extends GwtEvent<ParallelActivityModelHandler>{

    public static final GwtEvent.Type<ParallelActivityModelHandler> TYPE = new GwtEvent.Type<ParallelActivityModelHandler>();


    public static void fire (ParallelActivityModelModel source, int value){
        ParallelActivityEvent ev = new ParallelActivityEvent(value);
        source.fireEvent(ev);
    }

    private int newValue ;

    
    protected ParallelActivityEvent(int value){
        newValue =value;
    }

    public int getNewValue() {
        return newValue;
    }


    @Override
    public Type<ParallelActivityModelHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ParallelActivityModelHandler handler) {
        handler.onTaskRequiredChanged(this);
    }

    


    

    

}
