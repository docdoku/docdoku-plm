package com.docdoku.client.actions;

import com.docdoku.client.ui.workflow.SerialActivityModelCanvas;
import com.docdoku.client.ui.workflow.EditableWorkflowModelCanvas;
import com.docdoku.client.ui.workflow.WorkflowModelFrame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.docdoku.core.entities.WorkflowModel;

import javax.swing.*;

public class DeleteSerialActivityModelActionListener implements ActionListener {

    public void actionPerformed(ActionEvent pAE) {
        SerialActivityModelCanvas source = (SerialActivityModelCanvas) pAE.getSource();
        WorkflowModelFrame owner = (WorkflowModelFrame) SwingUtilities.getAncestorOfClass(WorkflowModelFrame.class, source);
        WorkflowModel model = owner.getWorkflowModel();
        model.removeActivityModel(source.getSerialActivityModel().getStep());
        ((EditableWorkflowModelCanvas) source.getParent()).refresh();
    }
}