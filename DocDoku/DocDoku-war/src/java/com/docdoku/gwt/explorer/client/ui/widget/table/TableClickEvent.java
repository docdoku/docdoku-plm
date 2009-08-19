/*
 * TableClickEvent.java
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
package com.docdoku.gwt.explorer.client.ui.widget.table;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.GwtEvent.Type;

/**
 *
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class TableClickEvent extends GwtEvent<TableClickHandler> {

    private static Type<TableClickHandler> TYPE;

    public static void fire(HasTableClickHandlers source, TableModelIndex index) {
        if (TYPE != null) {
            TableClickEvent event = new TableClickEvent(index);
            source.fireEvent(event);
        }
    }

    /**
     * Gets the type associated with this event.
     *
     * @return returns the handler type
     */
    public static Type<TableClickHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<TableClickHandler>();
        }
        return TYPE;
    }
    private TableModelIndex index;

    public TableClickEvent(TableModelIndex index) {
        this.index = index;
    }

    @Override
    public Type<TableClickHandler> getAssociatedType() {
        return (Type) TYPE;
    }

    @Override
    protected void dispatch(TableClickHandler handler) {
        handler.onClick(this);
    }

    public TableModelIndex getTableModelIndex(){
        return index ;
    }
}
