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

package com.docdoku.gwt.client.ui.widget.menu;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.cobogw.gwt.user.client.ui.Button;

/**
 * Button menu offers a way to display easily a popup menu when a button
 * is clicked the popup menu items must subclass Widget and implement MenuItem.
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class ButtonMenu extends Composite implements ClickHandler{

    private PopupMenu menu;
    private Button menuButton;

    public ButtonMenu(String buttonLabel) {
        menuButton = new Button(buttonLabel + "▼") ;
        initWidget(menuButton);
        menuButton.addClickHandler(this);
        menu = new PopupMenu();
        menu.addAutoHidePartner(this.getElement());
    }

    public <T extends Widget & MenuItem> void addItem(T i) {
        menu.addMenuItem(i);
    }

    public void clear() {
        menu.clearMenu();
    }

    public <T extends Widget & MenuItem> void removeItem(T i) {
        menu.removeMenuItem(i);
    }

    public void onClick(ClickEvent event) {
        // show popup menu
        if (menu.isShowing()){
            menu.hide();
        }else{
            menu.showRelativeTo(this);
        }
        
    }

    public void showMenu(){
        menu.showRelativeTo(this);
    }

    public void hideMenu(){
        menu.hide();
    }


    private class PopupMenu extends PopupPanel implements MouseOverHandler, MouseOutHandler, MenuItemListener {

        private VerticalPanel mainPanel;


        public PopupMenu() {
            mainPanel = new VerticalPanel();
            setWidget(mainPanel);
            setAutoHideEnabled(true);
        }

        @Override
        public void onMouseOver(MouseOverEvent event) {
            MenuItem source = (MenuItem) event.getSource();
            source.setSelected(true);
        }

        @Override
        public void onMouseOut(MouseOutEvent event) {
            MenuItem source = (MenuItem) event.getSource();
            source.setSelected(false);
        }

        public <T extends Widget & MenuItem> void addMenuItem(T i) {
            mainPanel.add(i);
            i.addMouseOverHandler(this) ;
            i.addMouseOutHandler(this);
            i.addListener(this);
        }

        public void clearMenu() {
            mainPanel.clear();
        }

        public <T extends Widget & MenuItem> void removeMenuItem(T i) {
            mainPanel.remove(i);
        }

        @Override
        public void onMenuItemActivated(MenuItemActivatedEvent event) {
            this.hide();
        }



        @Override
        public void hide() {
            // un select all elements
            for(int i = 0 ; i < mainPanel.getWidgetCount() ; i++){
                MenuItem item = (MenuItem) mainPanel.getWidget(i);
                item.setSelected(false);
            }
            super.hide();
        }

        @Override
        public void show() {
            super.show();

            for(int i = 0 ; i  < mainPanel.getWidgetCount() ; i++){
                MenuItem item = (MenuItem) mainPanel.getWidget(i);
                item.onShowUp();
            }
        }




    }
}
