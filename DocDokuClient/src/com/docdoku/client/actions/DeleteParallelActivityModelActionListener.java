package com.docdoku.client.actions;

import com.docdoku.client.ui.workflow.ParallelActivityModelCanvas;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.docdoku.client.ui.workflow.EditableWorkflowModelCanvas;
import com.docdoku.client.ui.workflow.WorkflowModelFrame;
import com.docdoku.core.entities.WorkflowModel;

import javax.swing.*;

public class DeleteParallelActivityModelActionListener implements ActionListener {



    public void actionPerformed(ActionEvent pAE) {
        ParallelActivityModelCanvas source = (ParallelActivityModelCanvas) pAE.getSource();
        WorkflowModelFrame owner = (WorkflowModelFrame) SwingUtilities.getAncestorOfClass(WorkflowModelFrame.class, source);
        WorkflowModel model = owner.getWorkflowModel();
        model.removeActivityModel(source.getParalleActivityModel().getStep());
        ((EditableWorkflowModelCanvas) source.getParent()).refresh();
    }
}