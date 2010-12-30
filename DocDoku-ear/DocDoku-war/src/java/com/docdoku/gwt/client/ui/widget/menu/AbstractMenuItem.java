/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
 * This is a convinience class
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
