/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
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
