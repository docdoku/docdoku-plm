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

package com.docdoku.gwt.explorer.client.ui.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Label;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Florent Garin
 */
public class TabMenuGroup implements ClickHandler {

    private Set<TabMenu> tabs;

    public TabMenuGroup(){
        tabs=new HashSet<TabMenu>();
    }

    public void addTabMenu(TabMenu tab){
        tabs.add(tab);
        tab.addClickHandler(this);
    }


    private void selectTab(TabMenu tabToSelect){
        for(TabMenu tab:tabs){
            if(tab.equals(tabToSelect))
                tab.select();
            else
                tab.unselect();
        }
    }

    public void unselect(){
        for(TabMenu tab:tabs){
            tab.unselect();
        }
    }

    public void onClick(ClickEvent event) {
        Object source = event.getSource();
        TabMenu tab = (TabMenu) ((Label)source).getParent();
        selectTab(tab);
    }
}
