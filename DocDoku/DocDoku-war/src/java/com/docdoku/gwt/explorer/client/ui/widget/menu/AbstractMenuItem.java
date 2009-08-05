/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.docdoku.gwt.explorer.client.ui.widget.menu;

import com.docdoku.gwt.explorer.client.ui.widget.menu.MenuItemListener;
import com.docdoku.gwt.explorer.client.ui.widget.menu.MenuItem;
import com.docdoku.gwt.explorer.client.ui.widget.menu.MenuItemActivatedEvent;
import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a convinience class
 * it is designed to help in coding a MenuItem
 * As it extends Composite, in sublasses, you must call initWidget in constructor
 * @author Emmanuel Nhan
 */
public abstract class AbstractMenuItem extends Composite implements MenuItem{

    private List<MenuItemListener> observers ;
    private Command command ;

    public AbstractMenuItem() {
        observers = new ArrayList<MenuItemListener>();
    }

    public void addListener(MenuItemListener l) {
        observers.add(l);
    }

    public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
        return addDomHandler(handler, MouseOverEvent.getType());
    }

    public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
        return addDomHandler(handler, MouseOutEvent.getType());
    }

    public void removeListener(MenuItemListener l) {
        observers.remove(l);
    }

    protected abstract boolean beforeCommandCall() ;
    protected abstract void afterCommandCall() ;


    protected final void activate(){
        if(beforeCommandCall()){
            // if false ---> command call canceled 
            // execute command (if any)
            if (command != null){
                command.execute();
            }
            afterCommandCall();
        }
        // notify observers
        MenuItemActivatedEvent ev = new MenuItemActivatedEvent(this);
        for (MenuItemListener menuItemListener : observers) {
            menuItemListener.onMenuItemActivated(ev);
        }
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

}
