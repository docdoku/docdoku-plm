/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.docdoku.gwt.explorer.client.ui.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Label;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Florent GARIN
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
