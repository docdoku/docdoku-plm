package com.docdoku.client.actions;

import com.docdoku.client.ui.workflow.LifeCycleStateCanvas;
import com.docdoku.core.entities.ActivityModel;
import com.docdoku.core.entities.WorkflowModel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.docdoku.client.ui.workflow.EditLifeCycleStateDialog;
import com.docdoku.client.ui.workflow.WorkflowModelFrame;

import javax.swing.*;

public class EditLifeCycleStateActionListener implements ActionListener {


    public void actionPerformed(ActionEvent pAE) {
        final LifeCycleStateCanvas sourceCanvas = (LifeCycleStateCanvas) pAE.getSource();
        WorkflowModelFrame owner = (WorkflowModelFrame) SwingUtilities.getAncestorOfClass(WorkflowModelFrame.class, sourceCanvas);
        
        ActionListener action = new ActionListener() {
            public void actionPerformed(ActionEvent pAE) {
                EditLifeCycleStateDialog source = (EditLifeCycleStateDialog) pAE.getSource();
                String state = source.getState();
                WorkflowModel wfModel= sourceCanvas.getWorkflowModel();
                int step=sourceCanvas.getStep();
                if(step==wfModel.numberOfSteps())
                    sourceCanvas.getWorkflowModel().setFinalLifeCycleState(state);
                else{
                    ActivityModel activityModel=wfModel.getActivityModel(step);
                    activityModel.setLifeCycleState(state);
                }

                sourceCanvas.setState(state);
            }
        };
        new EditLifeCycleStateDialog(owner, sourceCanvas.getState(), action);
    }
}