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

package com.docdoku.gwt.client.ui.widget.menu;

import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;

/**
 * MenuItem provides an interface to handle actions/selections
 * in ButtonMenu
 * @author Emmanuel Nhan
 */
public interface MenuItem extends HasMouseOverHandlers, HasMouseOutHandlers{
    /**
     * This method is invocked whenever the mouse is over the widget
     * It is usefull if you want to display a frame around the item, for instance
     * @param selected
     */
    public void setSelected(boolean selected) ;

    public void addListener(MenuItemListener l) ;

    public void removeListener(MenuItemListener l) ;

    /**
     * This method is called when a the menu is shown
     * You can here specify if some textboxes must be cleared
     */
    public void onShowUp() ;
    
}
