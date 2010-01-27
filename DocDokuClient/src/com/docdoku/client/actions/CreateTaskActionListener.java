package com.docdoku.client.actions;

import com.docdoku.client.ui.workflow.EditActivityModelPanel;
import com.docdoku.client.ui.workflow.CreateTaskModelDialog;
import com.docdoku.core.entities.ActivityModel;
import com.docdoku.core.entities.TaskModel;
import com.docdoku.core.entities.User;
import com.docdoku.core.entities.Task;

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