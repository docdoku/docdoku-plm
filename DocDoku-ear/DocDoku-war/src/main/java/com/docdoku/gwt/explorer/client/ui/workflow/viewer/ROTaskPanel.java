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
import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.docdoku.gwt.explorer.client.resources.icons.ExplorerImageBundle;
import com.docdoku.gwt.explorer.client.ui.widget.DocdokuInfoPopupPanel;
import com.docdoku.server.rest.dto.TaskDTO;
import com.google.gwt.event.dom.client.HasMouseMoveHandlers;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;

public class ROTaskPanel extends Composite implements MouseMoveHandler, MouseOutHandler, HasMouseMoveHandlers {

    private Image taskImage;
    private Label taskName;
    private Label taskResponsible;
    private TaskDTO task;
    private FlexTable panel;
    private DocdokuInfoPopupPanel taskGlobalInfos;
    private DocdokuInfoPopupPanel instructionsInfos;
    private DocdokuInfoPopupPanel responsibleMail;

    public ROTaskPanel(TaskDTO model) {
        this.task = model;

        setupUi();
        setupListeners();
    }

    private void setupListeners() {
        taskResponsible.addMouseMoveHandler(this);
        taskResponsible.addMouseOutHandler(this);
        taskImage.addMouseMoveHandler(this);
        taskImage.addMouseOutHandler(this);
        taskName.addMouseMoveHandler(this);
        taskName.addMouseOutHandler(this);
        this.addDomHandler(this, MouseMoveEvent.getType());
        this.addDomHandler(this, MouseOutEvent.getType());
    }

    private void setupUi() {

        ExplorerI18NConstants constants = ServiceLocator.getInstance().getExplorerI18NConstants() ;
        ExplorerImageBundle images = ServiceLocator.getInstance().getExplorerImageBundle() ;
        panel = new FlexTable();
        CellFormatter formatter = panel.getCellFormatter();
        taskImage = new Image(images.getTaskImage());
        panel.setWidget(0, 0, taskImage);
        taskName = new Label(task.getTitle());
        if (task.getInstructions() != null && !task.getInstructions().trim().isEmpty()) {
            instructionsInfos = new DocdokuInfoPopupPanel(1, 1);
            instructionsInfos.setData(0, 0, task.getInstructions());
        }
        panel.setWidget(1, 0, taskName);
        taskResponsible = new Label(task.getWorker().getName());
        responsibleMail = new DocdokuInfoPopupPanel(1, 1);
        responsibleMail.setData(0, 0, task.getWorker().getEmail());
        panel.setWidget(2, 0, new Label(constants.responsible()));
        panel.setWidget(2, 1, taskResponsible);

        FlexCellFormatter formatterFlex = panel.getFlexCellFormatter();
        formatterFlex.setColSpan(0, 0, 2);
        formatterFlex.setColSpan(1, 0, 2);
        formatter.setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
        formatter.setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_CENTER);

        initWidget(panel);
        setupStyle();



        taskGlobalInfos = new DocdokuInfoPopupPanel(0, 2);
        if (task.getClosureComment() != null && !task.getClosureComment().isEmpty()) {
            int index = taskGlobalInfos.getRowCount();
            taskGlobalInfos.insertRow(index);
            taskGlobalInfos.setData(index, 0, constants.comment());
            taskGlobalInfos.setData(index, 1, task.getClosureComment());

        }

        if (task.getClosureDate() != null) {
            int index = taskGlobalInfos.getRowCount();
            taskGlobalInfos.insertRow(index);
            taskGlobalInfos.setData(index, 0, constants.date());
            taskGlobalInfos.setData(index, 1, DateTimeFormat.getShortDateFormat().format(task.getClosureDate()));
        }

        if (task.getTargetIteration() != 0) {
            int index = taskGlobalInfos.getRowCount();
            taskGlobalInfos.insertRow(index);
            taskGlobalInfos.setData(index, 0, constants.iteration());
            taskGlobalInfos.setData(index, 1, task.getTargetIteration() + "");
        }
    }

    private void setupStyle() {

        switch (task.getStatus()) {
            case APPROVED:
                panel.setStyleName("task-approuved");
                break;
            case IN_PROGRESS:
                panel.setStyleName("task-inprogress");
                break;
            case NOT_STARTED:
                panel.setStyleName("task-notstarted");
                break;
            case REJECTED:
                panel.setStyleName("task-rejected");
                break;
        }

    }

    public void onMouseOut(MouseOutEvent event) {
        if (event.getSource() == taskResponsible) {
            responsibleMail.hide();
            if (taskGlobalInfos.getRowCount() != 0) {
                taskGlobalInfos.setPopupPosition(event.getClientX() + 10, event.getClientY() + 10);
                taskGlobalInfos.show();
            }
        } else if (event.getSource() == this) {
            taskGlobalInfos.hide();
        } else if (event.getSource() == this.taskImage || event.getSource() == taskName) {
            instructionsInfos.hide();
            if (taskGlobalInfos.getRowCount() != 0) {
                taskGlobalInfos.setPopupPosition(event.getClientX() + 10, event.getClientY() + 10);
                taskGlobalInfos.show();
            }
        }
    }

    public void onMouseMove(MouseMoveEvent event) {
        if (event.getSource() == taskResponsible) {
            taskGlobalInfos.hide();
            responsibleMail.setPopupPosition(event.getClientX() + 10, event.getClientY() + 10);
            responsibleMail.show();
        } else if (event.getSource() == this && taskGlobalInfos.getRowCount() != 0) {
            if (!instructionsInfos.isShowing() && !responsibleMail.isShowing()) {
                taskGlobalInfos.setPopupPosition(event.getClientX() + 10, event.getClientY() + 10);
                taskGlobalInfos.show();
            }
        } else if (event.getSource() == this.taskImage || event.getSource() == this.taskName) {
            if (task.getInstructions() != null && !task.getInstructions().trim().isEmpty()) {
                taskGlobalInfos.hide();
                instructionsInfos.setPopupPosition(event.getClientX() + 10, event.getClientY() + 10);
                instructionsInfos.show();
            }

        }
    }

    public HandlerRegistration addMouseMoveHandler(MouseMoveHandler handler) {
        return this.addDomHandler(handler, MouseMoveEvent.getType());
    }

}
