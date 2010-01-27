package com.docdoku.client.actions;

import com.docdoku.client.ui.workflow.EditSerialActivityModelDialog;
import com.docdoku.core.entities.SerialActivityModel;
import com.docdoku.core.entities.WorkflowModel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.docdoku.client.ui.workflow.EditableSerialActivityModelCanvas;
import com.docdoku.client.ui.workflow.WorkflowModelFrame;

import javax.swing.*;

public class EditSerialActivityModelActionListener implements ActionListener {


    public void actionPerformed(ActionEvent pAE) {
        final EditableSerialActivityModelCanvas canvas = (EditableSerialActivityModelCanvas) pAE.getSource();
        final WorkflowModelFrame owner = (WorkflowModelFrame) SwingUtilities.getAncestorOfClass(WorkflowModelFrame.class, canvas);
        //clone the object in case the user cancels the action
        final SerialActivityModel clonedActivityModel = canvas.getSerialActivityModel().clone();
        ActionListener action = new ActionListener() {
            public void actionPerformed(ActionEvent pAE) {
                WorkflowModel model = owner.getWorkflowModel();
                int step = canvas.getSerialActivityModel().getStep();
                model.setActivityModel(step,clonedActivityModel);
                canvas.setSerialActivityModel(clonedActivityModel);
                canvas.refresh();
            }
        };
        new EditSerialActivityModelDialog(owner, clonedActivityModel, new CreateTaskActionListener(), new EditTaskActionListener(), action);
    }

}