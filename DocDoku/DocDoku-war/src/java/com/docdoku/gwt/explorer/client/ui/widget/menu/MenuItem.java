/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.docdoku.gwt.explorer.client.ui.widget.menu;

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
