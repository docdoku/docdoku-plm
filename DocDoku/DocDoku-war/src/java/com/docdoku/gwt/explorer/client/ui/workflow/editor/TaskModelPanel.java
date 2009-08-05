/*
 * TaskModelPanel.java
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

import com.docdoku.gwt.explorer.client.data.ExplorerConstants;
import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.docdoku.gwt.explorer.client.ui.widget.input.EditableLabel;
import com.docdoku.gwt.explorer.client.ui.widget.input.checker.NotEmptyChecker;
import com.docdoku.gwt.explorer.client.ui.workflow.editor.model.TaskModelModel;
import com.docdoku.gwt.explorer.common.UserDTO;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class TaskModelPanel extends Composite implements MouseOverHandler, MouseOutHandler, ChangeHandler {

    private static final int TIME_OUT = 200;
    // Ui
    private FlexTable mainPanel;
    private Image taskImage;
    private EditableLabel taskName;
    private ListBox responsible;
    private EditableLabel role ;
    // model
    private TaskModelModel model;
    // popups (options)
    private PopupPanel removeOption;
    private Image removeImage;
    private PopupPanel rightOption;
    private PopupPanel leftOption;
    private Image rightImage;
    private Image leftImage;
    private InstructionsPopup instructions;
    // booleans (option states)
    private boolean leftOptionEnabled;
    private boolean rightOptionEnabled;
    private boolean deleteOptionEnabled;
    private OptionsTimer timer;
    // panel util :
    private ScrollPanelUtil panelUtil;
    // observer pattern :
    private List<TaskModelPanelListener> observers;
    // bad solution to handle potential users
    // TODO : replace this stuff by roles feature when available
    private UserDTO potentialResponsibles[];

    public TaskModelPanel(boolean leftOptionEnabled, boolean rightOptionEnabled, boolean deleteOptionEnabled, ScrollPanelUtil panelUtil) {
        observers = new ArrayList<TaskModelPanelListener>();
        this.leftOptionEnabled = leftOptionEnabled;
        this.rightOptionEnabled = rightOptionEnabled;
        this.deleteOptionEnabled = deleteOptionEnabled;
        this.panelUtil = panelUtil;

        setupUi();
        setupListeners();
        addDomHandler(this, MouseOutEvent.getType());
        addDomHandler(this, MouseOverEvent.getType());

        timer = new OptionsTimer();

        potentialResponsibles = ExplorerConstants.getInstance().getWorkspaceUsers();

    }

    public TaskModelPanel(boolean deleteOptionEnabled, ScrollPanelUtil panelUtil) {
        this(false, false, deleteOptionEnabled, panelUtil);
    }

    private void setupListeners() {
        leftImage.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                hidePopups();
                fireMoveUpClicked();
            }
        });

        rightImage.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                hidePopups();
                fireMoveDownClicked();
            }
        });

        removeImage.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                hidePopups();
                fireDeleteClicked();
            }
        });

        taskName.addChangeHandler(this);

        taskImage.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                hidePopups();
                for (TaskModelPanelListener listener : observers) {
                    listener.onRequestedHideAllPopups();
                }
                PopupPanel.PositionCallback callback = new PopupPanel.PositionCallback() {

                    public void setPosition(int offsetWidth, int offsetHeight) {
                        int centerOfTaskPanelX = getAbsoluteLeft() + getOffsetWidth() / 2;
                        int centerOfTaskPanelY = getAbsoluteTop() + getOffsetHeight() / 2;
                        instructions.setPopupPosition(centerOfTaskPanelX - offsetWidth / 2, centerOfTaskPanelY - offsetHeight / 2);
                    }
                };
                instructions.setPopupPositionAndShow(callback);
            }
        });

        instructions.addCloseHandler(new CloseHandler<PopupPanel>() {

            public void onClose(CloseEvent<PopupPanel> event) {
                model.setInstructions(instructions.getInstructions());
            }
        });
    }

    private void setupUi() {
        ExplorerI18NConstants constants = ServiceLocator.getInstance().getExplorerI18NConstants();


        mainPanel = new FlexTable();
        initWidget(mainPanel);

        taskImage = new Image();
        ServiceLocator.getInstance().getExplorerImageBundle().getTaskImage().applyTo(taskImage);
        taskName = new EditableLabel();
        taskName.setChecker(new NotEmptyChecker());
        taskName.setTextAlignment(EditableLabel.ALIGN_CENTER);
//        role = new EditableLabel();
//        role.setChecker(new NotEmptyChecker()) ;
        responsible = new ListBox();
        responsible.addChangeHandler(this);
        CellFormatter formatter = mainPanel.getCellFormatter();
        mainPanel.setWidget(0, 0, taskImage);
        mainPanel.setWidget(1, 0, taskName);
        mainPanel.setWidget(2, 0, new InlineLabel(constants.responsible()));
        mainPanel.setWidget(2, 1, responsible);
//        mainPanel.setWidget(2, 1, role);

        mainPanel.getFlexCellFormatter().setColSpan(0, 0, 2);
        mainPanel.getFlexCellFormatter().setColSpan(1, 0, 2);

        formatter.setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
        formatter.setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_CENTER);

        taskImage.setTitle(constants.editInstructionTooltip());
        setStyleName("task");
        setupPopups();


    }

    private void setupPopups() {
        // images :
        removeImage = new Image();
        leftImage = new Image();
        rightImage = new Image();
        ServiceLocator.getInstance().getExplorerImageBundle().getDeleteImage().applyTo(removeImage);
        ServiceLocator.getInstance().getExplorerImageBundle().getRightImage().applyTo(rightImage);
        ServiceLocator.getInstance().getExplorerImageBundle().getLeftImage().applyTo(leftImage);

        // containers (popups)
        removeOption = new PopupPanel();
        leftOption = new PopupPanel();
        rightOption = new PopupPanel();
        removeOption.setWidget(removeImage);
        leftOption.setWidget(leftImage);
        rightOption.setWidget(rightImage);

        removeOption.setStyleName("task-option");
        leftOption.setStyleName("task-option");
        rightOption.setStyleName("task-option");

        instructions = new InstructionsPopup("");
    }

    public void setModel(TaskModelModel model) {
        this.model = model;

        taskName.setText(model.getTaskName());
        instructions.setInscructions(model.getInstructions());

        for (int i = 0; i < potentialResponsibles.length; i++) {
            responsible.addItem(potentialResponsibles[i].getName());
            if (potentialResponsibles[i].getName().equals(model.getResponsible().getName())) {
                responsible.setSelectedIndex(responsible.getItemCount() - 1);
            }
        }

    }

    public void onMouseOver(MouseOverEvent event) {
        timer.cancel();
        showPopups();
    }

    public void onMouseOut(MouseOutEvent event) {
        timer.schedule(TIME_OUT);
    }

    private void showPopups() {

        if (leftOptionEnabled && !leftOption.isShowing()) {
            PopupPanel.PositionCallback callback = new PopupPanel.PositionCallback() {

                public void setPosition(int offsetWidth, int offsetHeight) {
                    leftOption.setPopupPosition(getAbsoluteLeft(), getAbsoluteTop() + getOffsetHeight() / 2 - offsetHeight / 2);
                }
            };
            if (panelUtil.isOverScrollPanel(getAbsoluteLeft(), getAbsoluteTop(), 26)) {
                leftOption.setPopupPositionAndShow(callback);
            }
        }

        if (rightOptionEnabled && !rightOption.isShowing()) {
            PopupPanel.PositionCallback callback = new PopupPanel.PositionCallback() {

                public void setPosition(int offsetWidth, int offsetHeight) {
                    rightOption.setPopupPosition(getAbsoluteLeft() + getOffsetWidth() - offsetWidth, getAbsoluteTop() + getOffsetHeight() / 2 - offsetHeight / 2);
                }
            };
            if (panelUtil.isOverScrollPanel(getAbsoluteLeft() + getOffsetWidth() - 26, getAbsoluteTop(), 26)) {
                rightOption.setPopupPositionAndShow(callback);
            }
        }

        if (deleteOptionEnabled && !removeOption.isShowing()) {
            PopupPanel.PositionCallback callback = new PopupPanel.PositionCallback() {

                public void setPosition(int offsetWidth, int offsetHeight) {
                    removeOption.setPopupPosition(getAbsoluteLeft() + getOffsetWidth() - offsetWidth - 2, getAbsoluteTop() + 2);
                }
            };
            if (panelUtil.isOverScrollPanel(getAbsoluteLeft() + getOffsetWidth() - 26, getAbsoluteTop(), 26)) {
                removeOption.setPopupPositionAndShow(callback);
            }
        }
    }

    public void hidePopups() {
        timer.cancel();
        removeOption.hide();
        leftOption.hide();
        rightOption.hide();
    }

    public boolean isDeleteOptionEnabled() {
        return deleteOptionEnabled;
    }

    public void setDeleteOptionEnabled(boolean deleteOptionEnabled) {
        this.deleteOptionEnabled = deleteOptionEnabled;

    }

    public boolean isLeftOptionEnabled() {
        return leftOptionEnabled;
    }

    public void setLeftOptionEnabled(boolean leftOptionEnabled) {
        this.leftOptionEnabled = leftOptionEnabled;
    }

    public boolean isRightOptionEnabled() {
        return rightOptionEnabled;
    }

    public void setRightOptionEnabled(boolean rightOptionEnabled) {
        this.rightOptionEnabled = rightOptionEnabled;
    }

    public void addListener(TaskModelPanelListener l) {
        observers.add(l);
    }

    public void removeListener(TaskModelPanelListener l) {
        observers.remove(l);
    }

    private void fireDeleteClicked() {
        TaskModelPanelEvent ev = new TaskModelPanelEvent(this);
        for (TaskModelPanelListener listener : observers) {
            listener.onDeleteClicked(ev);
        }
    }

    private void fireMoveUpClicked() {
        TaskModelPanelEvent ev = new TaskModelPanelEvent(this);
        for (TaskModelPanelListener listener : observers) {
            listener.onMoveUpClicked(ev);
        }
    }

    private void fireMoveDownClicked() {
        TaskModelPanelEvent ev = new TaskModelPanelEvent(this);
        for (TaskModelPanelListener listener : observers) {
            listener.onMoveDownClicked(ev);
        }
    }

    public void onChange(ChangeEvent event) {
        if (event.getSource() == taskName) {
            model.setTaskName(taskName.getText());
        } else {
            model.setResponsible(potentialResponsibles[responsible.getSelectedIndex()]);
        }
    }

    private class OptionsTimer extends Timer {

        @Override
        public void run() {
            hidePopups();
        }
    }
}
