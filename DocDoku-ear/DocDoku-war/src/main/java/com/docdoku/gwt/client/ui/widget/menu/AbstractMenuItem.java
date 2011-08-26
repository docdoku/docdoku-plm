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
 * This is a convenience class
 * it is designed to help in coding a MenuItem
 * As it extends Composite, in sublasses, you must call initWidget in constructor
 * An AbstractMenuItem owns a Command, which is a generic way to handle actions through
 * GWT apis.
 * The mechanism to call the command might be sum-up like this :
 * <ul>
 * <li>activate is called by the popupmenu</li>
 * <li>beforeCommandCall is called, if this method returns true, then the command will be executed</li>
 * <li>afterCommandCall is called just after command call, to provide subclasses a way to specify behavior after a command call</li>
 * </ul>
 * @author Emmanuel Nhan
 */
public abstract class AbstractMenuItem extends Composite implements MenuItem{

    private List<MenuItemListener> observers;
    private Command command;

    public AbstractMenuItem() {
        observers = new ArrayList<MenuItemListener>();
    }

    @Override
    public void addListener(MenuItemListener l) {
        observers.add(l);
    }

    @Override
    public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
        return addDomHandler(handler, MouseOverEvent.getType());
    }

    @Override
    public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
        return addDomHandler(handler, MouseOutEvent.getType());
    }


    @Override
    public void removeListener(MenuItemListener l) {
        observers.remove(l);
    }

    /**
     * This method is called just before a command call
     * If the result is true, then the command will be executed.
     * If false, not.
     * @return
     */
    protected abstract boolean beforeCommandCall() ;

    /**
     * This method is called just after a command call.
     * It allows to specify behavior after a command call
     */
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
