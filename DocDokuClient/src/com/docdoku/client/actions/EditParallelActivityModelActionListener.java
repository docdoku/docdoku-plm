package com.docdoku.client.actions;

import com.docdoku.client.ui.workflow.EditParallelActivityModelDialog;
import com.docdoku.core.entities.ParallelActivityModel;
import com.docdoku.core.entities.WorkflowModel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.docdoku.client.ui.workflow.EditableParallelActivityModelCanvas;
import com.docdoku.client.ui.workflow.WorkflowModelFrame;

import javax.swing.*;

public class EditParallelActivityModelActionListener implements ActionListener {


    public void actionPerformed(ActionEvent pAE) {
        final EditableParallelActivityModelCanvas canvas = (EditableParallelActivityModelCanvas) pAE.getSource();
        final WorkflowModelFrame owner = (WorkflowModelFrame) SwingUtilities.getAncestorOfClass(WorkflowModelFrame.class, canvas);
        //clone the object in case the user cancels the action
        final ParallelActivityModel clonedActivityModel = canvas.getParalleActivityModel().clone();
        ActionListener action = new ActionListener() {
            public void actionPerformed(ActionEvent pAE) {
                EditParallelActivityModelDialog source = (EditParallelActivityModelDialog) pAE.getSource();
                int tasksToComplete = source.getNumberOfNeededCompletedTasks();
                clonedActivityModel.setTasksToComplete(tasksToComplete);
                WorkflowModel model = owner.getWorkflowModel();
                int step = canvas.getParalleActivityModel().getStep();
                model.setActivityModel(step,clonedActivityModel);
                canvas.setParallelActivityModel(clonedActivityModel);
                canvas.refresh();
            }
        };

        new EditParallelActivityModelDialog(owner, clonedActivityModel, new CreateTaskActionListener(), new EditTaskActionListener(), action);
    }
}