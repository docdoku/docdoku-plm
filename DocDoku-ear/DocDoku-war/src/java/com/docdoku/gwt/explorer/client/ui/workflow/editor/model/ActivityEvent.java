/*
 * ActivityEvent.java
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
public class ActivityEvent extends GwtEvent<ActivityModelHandler>{


    public final static GwtEvent.Type<ActivityModelHandler> TYPE = new GwtEvent.Type<ActivityModelHandler>();

    public static void fire (ActivityModelModel source,int position, EventType type){
        ActivityEvent ev = new ActivityEvent(position, type);
        source.fireEvent(ev);
    }

    public enum EventType {
        ADD_TASK,
        DELETE_TASK
    }

    private int position ;
    private EventType type ;
    private ActivityModelModel realSource ;


    @Deprecated
    public ActivityEvent(ActivityModelModel source, int position, EventType type) {

        realSource = source;
        this.position = position;
        this.type = type;
    }

    protected ActivityEvent(int position, EventType type){
        this.position = position ;
        this.type = type;
    }

    public int getPosition() {
        return position;
    }

    public ActivityModelModel getRealSource() {
        return realSource;
    }

    public EventType getType() {
        return type;
    }

     @Override
    public Type<ActivityModelHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ActivityModelHandler handler) {
        handler.onActivityModelChanged(this);
    }

}
