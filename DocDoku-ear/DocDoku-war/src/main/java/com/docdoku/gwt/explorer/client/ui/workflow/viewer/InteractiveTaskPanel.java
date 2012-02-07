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

package com.docdoku.gwt.explorer.client.ui.workflow.viewer;

import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.server.rest.dto.TaskDTO;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author manu
 */
public class InteractiveTaskPanel extends ROTaskPanel implements ClickHandler, MouseOutHandler, HasMouseOutHandlers{

    private static final int TIME_OUT = 250 ;

    private Image approveImage;
    private Image rejectImage;
    private OptionsPopup options;
    private boolean active;
    private Set<TaskListener> observers;
    private Set<OptionPanelListener> optionListener ;
    private ShowTimer timer ;
    private TaskDTO dto ;
    private int activityStep ;
    private int step ;


    InteractiveTaskPanel(TaskDTO task, boolean active, int activityStep, int i) {
        super(task);
        dto = task ;
        timer = new ShowTimer();
        observers = new HashSet<TaskListener>();
        optionListener = new HashSet<OptionPanelListener>();
        this.activityStep = activityStep ;
        this.step = i ;
        this.active = active;
        // init images
        approveImage = new Image(ServiceLocator.getInstance().getExplorerImageBundle().getApproveTaskImage());
        approveImage.setTitle(ServiceLocator.getInstance().getExplorerI18NConstants().approveTaskTooltip());
        rejectImage = new Image(ServiceLocator.getInstance().getExplorerImageBundle().getRejectTaskImage());
        rejectImage.setTitle(ServiceLocator.getInstance().getExplorerI18NConstants().rejectTaskTooltip()) ;
        // init popups
        options = new OptionsPopup();
        addMouseOutHandler(this);
        approveImage.addClickHandler(this);
        rejectImage.addClickHandler(this);
        getElement().getStyle().setProperty("cursor", "pointer");
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        options.hide();
    }

    public void onClick(ClickEvent event) {
        options.hide();
        for (OptionPanelListener l : optionListener){
            l.onOptionClicked();
        }
        if (event.getSource() == approveImage) {

            String value = Window.prompt(ServiceLocator.getInstance().getExplorerI18NConstants().comment(), "");
            if (value != null) {
                options.hide();
                fireTaskApproved(value);
            }
        } else if (event.getSource() == rejectImage) {

            String value = Window.prompt(ServiceLocator.getInstance().getExplorerI18NConstants().comment(), "");
            if (value != null) {
                options.hide();
                fireTaskRejected(value);
            }
        }
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        // popup :
        PopupPanel.PositionCallback callback = new PopupPanel.PositionCallback() {

            public void setPosition(int offsetWidth, int offsetHeight) {
                int x = getAbsoluteLeft() + getOffsetWidth() - offsetWidth;
                int y = getAbsoluteTop();
                options.setPopupPosition(x, y);
            }
        };
        if (active) {
            options.setPopupPositionAndShow(callback);
        } else {
            options.hide();
        }
    }
    
    public void addTaskListener(TaskListener l) {
        this.observers.add(l);
    }

    public void removeTaskListener(TaskListener l) {
        observers.remove(l);
    }

    public void hideOptions(){
        options.hide();
    }

    void addOptionListener(ROParallelActivityPanel aThis) {
        optionListener.add(aThis);
    }

    private void fireTaskApproved(String comment) {
        TaskChangeEvent ev = new TaskChangeEvent(this, TaskChangeEvent.Type.APPROVE, dto );
        ev.setActivity(activityStep);
        ev.setStep(step);
        ev.setComment(comment);
        for (TaskListener l : observers) {
            l.onTaskStatusChange(ev);
        }
    }

    private void fireTaskRejected(String comment){
        TaskChangeEvent ev = new TaskChangeEvent(this, TaskChangeEvent.Type.REJECT, dto );
        ev.setActivity(activityStep);
        ev.setStep(step);
        ev.setComment(comment);
        for (TaskListener l : observers) {
            l.onTaskStatusChange(ev);
        }
    }

    @Override
    public void onMouseMove(MouseMoveEvent event) {
        super.onMouseMove(event);
        timer.cancel();
        if (active && !options.isShowing()) {
            PopupPanel.PositionCallback callback = new PopupPanel.PositionCallback() {

                public void setPosition(int offsetWidth, int offsetHeight) {
                    int x = getAbsoluteLeft() + getOffsetWidth() - offsetWidth;
                    int y = getAbsoluteTop()-offsetHeight;
                    options.setPopupPosition(x, y);
                }
            };
            options.setPopupPositionAndShow(callback);
        }
    }

    @Override
    public void onMouseOut(MouseOutEvent event) {
        super.onMouseOut(event);
        if (event.getSource() == this){
            timer.schedule(TIME_OUT);
        }
    }

    public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
        return addDomHandler(handler, MouseOutEvent.getType());
    }

    public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
        return addDomHandler(handler, MouseOverEvent.getType()) ;
    }

    private class OptionsPopup extends DecoratedPopupPanel implements HasMouseOutHandlers, HasMouseOverHandlers, MouseOutHandler, MouseOverHandler {

        public OptionsPopup() {
            HorizontalPanel options = new HorizontalPanel();
            options.add(approveImage);
            options.add(rejectImage);
            setWidget(options);
            addMouseOverHandler(this);
            addMouseOutHandler(this);
            getElement().getStyle().setProperty("cursor", "pointer");
        }

        public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
            return addDomHandler(handler, MouseOutEvent.getType());
        }

        public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
            return addDomHandler(handler, MouseOverEvent.getType());
        }

        public void onMouseOut(MouseOutEvent event) {
            timer.schedule(TIME_OUT);
        }

        public void onMouseOver(MouseOverEvent event) {
            timer.cancel();
        }
    }

    private class ShowTimer extends Timer{

        @Override
        public void run() {
            options.hide();
        }
        
    }

}
