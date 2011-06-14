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

package com.docdoku.gwt.explorer.client.ui;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.docdoku.gwt.client.ui.widget.table.TableWidget;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Widget;


/**
 *
 * @author manu
 */
public class DocDragController extends PickupDragController {

    private TableWidget table ;

    public DocDragController(AbsolutePanel boundaryPanel, boolean allowDroppingOnBoundaryPanel) {
        super(boundaryPanel, allowDroppingOnBoundaryPanel);

    }

    @Override
    protected void restoreSelectedWidgetsLocation() {
    }

    @Override
    protected void restoreSelectedWidgetsStyle() {
    }

    @Override
    protected void saveSelectedWidgetsLocationAndStyle() {
    }

    @Override
    protected Widget newDragProxy(DragContext context) {
        AbsolutePanel container = new AbsolutePanel();
        DOM.setStyleAttribute(container.getElement(), "overflow", "visible");
        container.add(new DragDocInfoPanel(table.getSelectedRows().size()));
        return container ;
    }

    public TableWidget getTable() {
        return table;
    }

    public void setTable(TableWidget table) {
        this.table = table;
    }

    

    
}
