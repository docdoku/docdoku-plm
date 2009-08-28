/*
 * ActivityModelPanel.java
 * 
 * Copyright (c) 2009 Docdoku. All rights reserved.
 * 
 * This file is part of Docdoku.
 * 
 * Docdoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Docdoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Docdoku.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.docdoku.gwt.explorer.client.ui.workflow.editor;

import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.ui.workflow.editor.model.ActivityEvent;
import com.docdoku.gwt.explorer.client.ui.workflow.editor.model.ActivityModelListener;
import com.docdoku.gwt.explorer.client.ui.workflow.editor.model.ActivityModelModel;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * This class takes care of common features of panels designed to represent an activity
 * It mainly features a popup panel to add task and remove the activity
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public abstract class ActivityModelPanel extends Composite implements MouseOverHandler, MouseOutHandler, ActivityModelListener, TaskModelPanelListener {

    private static final int TIME_OUT = 200;
    protected List<TaskModelPanel> taskPanels;
    private List<ActivityModelPanelListener> observers;
    // ui :
    protected HorizontalPanel mainPanel;
    private OptionsPopup options;
    private OptionsTimer timer;
    // model
    protected ActivityModelModel model;
    // utils
    protected ScrollPanelUtil util;

    /**
     * Builds an ActivityModelPanel
     * @param util the scroll panel tool to determine whether a point is visible or not
     */
    public ActivityModelPanel(ScrollPanelUtil util) {
        this.util = util;
        mainPanel = new HorizontalPanel();
        initWidget(mainPanel);
        setStyleName("activiyModel");

        taskPanels = new LinkedList<TaskModelPanel>();
        observers = new ArrayList<ActivityModelPanelListener>();

        options = new OptionsPopup();
        timer = new OptionsTimer();

        addDomHandler(this, MouseOverEvent.getType());
        addDomHandler(this, MouseOutEvent.getType());

        options.addMouseOutHandler(this);
        options.addMouseOverHandler(this);
        
        mainPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

    }

    /**
     * hide all popup panels
     */
    public void hidePopups() {
        options.hide();
    }

    public void onMouseOver(MouseOverEvent event) {
        showPopups();
    }

    public void onMouseOut(MouseOutEvent event) {
        timer.schedule(TIME_OUT);
    }

    public abstract void setModel(ActivityModelModel model);

    public void addListener(ActivityModelPanelListener l) {
        observers.add(l);
    }

    public void removeListener(ActivityModelPanelListener l) {
        observers.add(l);
    }

    public void onDeleteClicked(TaskModelPanelEvent ev) {
        model.removeTask(taskPanels.indexOf(ev.getRealSource()));
    }

    public void onMoveUpClicked(TaskModelPanelEvent ev) {
    }

    public void onMoveDownClicked(TaskModelPanelEvent ev) {
    }

    private void fireAddClicked() {
        ActivityModelPanelEvent event = new ActivityModelPanelEvent(this);
        for (ActivityModelPanelListener listener : observers) {
            listener.onAddTaskClicked(event);
        }
    }

    private void fireDeleteClicked() {
        ActivityModelPanelEvent event = new ActivityModelPanelEvent(this);
        for (ActivityModelPanelListener listener : observers) {
            listener.onDeleteActivityClicked(event);
        }
    }

    private void showPopups() {
        timer.cancel();
        // the position of the popup must be chosen carefully as it is a popup its visibility is not managed by the scroll panel containing the editor

        DecoratedPopupPanel.PositionCallback callback = new DecoratedPopupPanel.PositionCallback() {

            public void setPosition(int offsetWidth, int offsetHeight) {
                if (util.isOverScrollPanel(getAbsoluteLeft() + getOffsetWidth() / 2 - offsetWidth / 2, getAbsoluteTop(), offsetWidth)) {
                    options.setPopupPosition(getAbsoluteLeft() + getOffsetWidth() / 2 - offsetWidth / 2, getAbsoluteTop() - offsetHeight);
                } else {
                    // find a position that fit...
                    // this is possible thanks to the ScrollPanelUtil
                    int newX = util.findAcceptableX(getAbsoluteLeft() + getOffsetWidth() / 2 - offsetWidth / 2, offsetWidth);
                    options.setPopupPosition(newX, getAbsoluteTop() - offsetHeight);
                }
            }
        };

        options.setPopupPositionAndShow(callback);
    }

    public void onActivityModelChanged(ActivityEvent event) {
        switch (event.getType()) {
            case ADD_TASK:
                addTask();
                break;
            case DELETE_TASK:
                removeTask(event.getPosition());
                break;

        }
    }

    public void onRequestedHideAllPopups() {
        hidePopups();
    }



    protected abstract void addTask();

    protected abstract void removeTask(int position);

    private class OptionsPopup extends DecoratedPopupPanel implements HasMouseOutHandlers, HasMouseOverHandlers {

        private Image add;
        private Image remove;

        public OptionsPopup() {

            HorizontalPanel panel = new HorizontalPanel();
            add = new Image();
            remove = new Image();

            ServiceLocator.getInstance().getExplorerImageBundle().getAddSmallImage().applyTo(add);
            ServiceLocator.getInstance().getExplorerImageBundle().getDeleteImage().applyTo(remove);

            add.setTitle(ServiceLocator.getInstance().getExplorerI18NConstants().taskAdd());
            remove.setTitle(ServiceLocator.getInstance().getExplorerI18NConstants().deleteActivityTooltip());
            panel.add(add);
            panel.add(remove);

            setWidget(panel);

            add.addClickHandler(new ClickHandler() {

                public void onClick(ClickEvent event) {
                    hidePopups();
                    fireAddClicked();
                }
            });

            add.addClickHandler(new ClickHandler() {

                public void onClick(ClickEvent event) {
                    model.addTask();
                }
            });

            remove.addClickHandler(new ClickHandler() {

                public void onClick(ClickEvent event) {
                    hidePopups();
                    fireDeleteClicked();
                }
            });
        }

        public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
            return addDomHandler(handler, MouseOutEvent.getType());
        }

        public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
            return addDomHandler(handler, MouseOverEvent.getType());
        }
    }

    private class OptionsTimer extends Timer {

        @Override
        public void run() {
            hidePopups();
        }
    }
}
