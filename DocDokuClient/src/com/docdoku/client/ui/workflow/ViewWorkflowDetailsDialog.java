package com.docdoku.client.ui.workflow;

import com.docdoku.client.ui.common.CloseButton;

import javax.swing.*;
import java.awt.*;
import com.docdoku.client.localization.I18N;
import com.docdoku.core.entities.Workflow;

public class ViewWorkflowDetailsDialog extends JDialog {
    
    private WorkflowCanvas mWorkflowCanvas;
    private JScrollPane mWorkflowScrollPane;
    private CloseButton mCloseButton;

    public ViewWorkflowDetailsDialog(Dialog pOwner, Workflow pWorkflow) {
        super(pOwner, I18N.BUNDLE.getString("ViewWorkflowDetails_title"), true);
        setLocationRelativeTo(pOwner);
        mWorkflowCanvas = new WorkflowCanvas(pWorkflow);
        mCloseButton = new CloseButton(this, I18N.BUNDLE.getString("Close_button"));
        mWorkflowScrollPane=new JScrollPane();
        createLayout();
        setVisible(true);
    }

    private void createLayout() {
        getRootPane().setDefaultButton(mCloseButton);
        JPanel mainPanel = new JPanel(new BorderLayout());       
        mWorkflowScrollPane.getViewport().add(mWorkflowCanvas);
        mainPanel.add(mWorkflowScrollPane, BorderLayout.CENTER);
        JPanel southPanel = new JPanel();
        southPanel.add(mCloseButton);
        mainPanel.add(southPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
        pack();
    }
}
