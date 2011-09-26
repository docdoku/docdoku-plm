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

package com.docdoku.client.actions;

import com.docdoku.client.ui.workflow.EditActivityModelPanel;
import com.docdoku.client.ui.workflow.CreateTaskModelDialog;
import com.docdoku.core.workflow.ActivityModel;
import com.docdoku.core.workflow.TaskModel;
import com.docdoku.core.common.User;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;

public class CreateTaskActionListener implements ActionListener {


    public void actionPerformed(ActionEvent pAE) {
        final EditActivityModelPanel sourcePanel = (EditActivityModelPanel) pAE.getSource();
        Dialog owner = (Dialog) SwingUtilities.getAncestorOfClass(Dialog.class, sourcePanel);
        ActionListener action = new ActionListener() {
            public void actionPerformed(ActionEvent pAE) {
                CreateTaskModelDialog source = (CreateTaskModelDialog) pAE.getSource();
                ActivityModel activityModel = sourcePanel.getActivityModel();
                User user = source.getUser();
                String title = source.getTaskTitle();
                String instructions = source.getInstructions();
                TaskModel task = new TaskModel(activityModel, title, instructions, user);
                activityModel.addTaskModel(task);
                sourcePanel.getTasksList().addTask(task);
            }
        };

        new CreateTaskModelDialog(owner, action);
    }
}