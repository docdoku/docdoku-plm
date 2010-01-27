package com.docdoku.client.actions;

import com.docdoku.client.ui.workflow.EditActivityModelPanel;
import com.docdoku.client.ui.workflow.EditTaskModelDialog;
import com.docdoku.core.entities.TaskModel;
import com.docdoku.core.entities.User;
import com.docdoku.core.entities.Task;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;

public class EditTaskActionListener implements ActionListener {


    public void actionPerformed(ActionEvent pAE) {
        final EditActivityModelPanel sourcePanel = (EditActivityModelPanel) pAE.getSource();
        Dialog owner = (Dialog) SwingUtilities.getAncestorOfClass(Dialog.class, sourcePanel);
        ActionListener action = new ActionListener() {
            public void actionPerformed(ActionEvent pAE) {
                EditTaskModelDialog source = (EditTaskModelDialog) pAE.getSource();
                User user = source.getUser();
                String title = source.getTaskTitle();
                String instructions = source.getInstructions();

                TaskModel task = source.getTaskModel();
                task.setWorker(user);
                task.setTitle(title);
                task.setInstructions(instructions);

                sourcePanel.replaceSelectedTaskModel(task);
            }
        };

        new EditTaskModelDialog(owner, action, sourcePanel.getSelectedTaskModels()[0]);
    }
}