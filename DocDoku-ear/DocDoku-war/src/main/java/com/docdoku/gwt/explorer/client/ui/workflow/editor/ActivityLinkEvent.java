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

package com.docdoku.gwt.explorer.client.ui.workflow.editor;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.GwtEvent.Type;
/**
 * This kind of even is fired by ActivityLinks.
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class ActivityLinkEvent extends GwtEvent<ActivityLinkHandler> {

    private OperationType opType;

    public final static GwtEvent.Type<ActivityLinkHandler> TYPE = new GwtEvent.Type<ActivityLinkHandler>();

    public static void fire (ActivityLink source, OperationType op){
        ActivityLinkEvent ev = new ActivityLinkEvent(op);
        source.fireEvent(ev);
    }

    public enum OperationType{
        ADD_SERIAL,
        ADD_PARALLEL;
    }

    @Deprecated
    public ActivityLinkEvent(ActivityLink source) {
        
    }

    public OperationType getOpType() {
        return opType;
    }

    
    protected ActivityLinkEvent(OperationType type)
    {
        opType = type ;
    }


    @Override
    public Type<ActivityLinkHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ActivityLinkHandler handler) {
        handler.onAddActivityClicked(this);
    }

    
}
