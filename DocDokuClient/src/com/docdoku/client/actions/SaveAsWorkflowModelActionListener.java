package com.docdoku.client.actions;

import com.docdoku.client.ui.ExplorerFrame;
import com.docdoku.client.ui.workflow.SaveWorkflowModelDialog;
import com.docdoku.client.ui.workflow.WorkflowModelFrame;
import com.docdoku.core.entities.WorkflowModel;
import java.awt.Event;

import javax.swing.*;
import java.awt.event.*;

import com.docdoku.client.localization.I18N;

public class SaveAsWorkflowModelActionListener implements ActionListener {
    
    
    public void actionPerformed(ActionEvent pAE) {
        final WorkflowModelFrame owner = (WorkflowModelFrame) pAE.getSource();
        final WorkflowModel model = owner.getWorkflowModel();
        ActionListener action = new ActionListener() {
            public void actionPerformed(ActionEvent pAE) {
                SaveWorkflowModelDialog source = (SaveWorkflowModelDialog) pAE.getSource();
                String id = source.getWorkflowModelId();
                model.setId(id);
                try{
                    MainController.getInstance().saveWorkflowModel(model);
                    owner.setTitle(source.getWorkflowModelId());
                    ExplorerFrame.unselectElementInAllFrame();
                }catch (Exception pEx) {
                    String message = pEx.getMessage()==null?I18N.BUNDLE
                            .getString("Error_unknown"):pEx.getMessage();
                    JOptionPane.showMessageDialog(null,
                            message, I18N.BUNDLE
                            .getString("Error_title"),
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        new SaveWorkflowModelDialog(owner, model, action);
    }
}
