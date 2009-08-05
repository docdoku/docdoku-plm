/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.docdoku.gwt.explorer.client.ui;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Widget;


/**
 *
 * @author manu
 */
public class DocDragController extends PickupDragController {

    private Table table ;

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

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    
}
