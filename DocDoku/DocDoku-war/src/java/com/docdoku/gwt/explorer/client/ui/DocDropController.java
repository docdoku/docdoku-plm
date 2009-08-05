/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.docdoku.gwt.explorer.client.ui;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.drop.SimpleDropController;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author manu
 */
public class DocDropController extends SimpleDropController{


    private DocDropListener actionTarget ;

    public DocDropController(Widget dropTarget, DocDropListener actionTarget) {
        super(dropTarget);
        this.actionTarget = actionTarget ;
    }

    @Override
    public void onDrop(DragContext context) {
        super.onDrop(context);
        actionTarget.onDrop();
    }

    @Override
    public void onEnter(DragContext context) {
        super.onEnter(context);
        getDropTarget().addStyleName("dnd-overTarget");
    }

    @Override
    public void onLeave(DragContext context) {
        super.onLeave(context);
        getDropTarget().removeStyleName("dnd-overTarget");
    }



}
