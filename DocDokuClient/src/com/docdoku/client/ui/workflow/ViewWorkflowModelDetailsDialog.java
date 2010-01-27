package com.docdoku.client.ui.workflow;

import com.docdoku.client.ui.common.CloseButton;

import javax.swing.*;
import java.awt.*;
import com.docdoku.client.localization.I18N;
import com.docdoku.core.entities.WorkflowModel;

public class ViewWorkflowModelDetailsDialog extends JDialog {

    private ViewWorkflowModelCanvas mWorkflowModelCanvas;
    private JScrollPane mWorkflowScrollPane;
    private CloseButton mCloseButton;

    public ViewWorkflowModelDetailsDialog(Frame pOwner, WorkflowModel pWorkflowModel) {
        super(pOwner, I18N.BUNDLE.getString("ViewWorkflowModelDetails_title"), true);
        setLocationRelativeTo(pOwner);
        mWorkflowModelCanvas = new ViewWorkflowModelCanvas(pWorkflowModel);
        mCloseButton = new CloseButton(this, I18N.BUNDLE.getString("Close_button"));
        mWorkflowScrollPane=new JScrollPane();
        createLayout();
        setVisible(true);
    }

    private void createLayout() {
        getRootPane().setDefaultButton(mCloseButton);
        JPanel mainPanel = new JPanel(new BorderLayout());
        mWorkflowScrollPane.getViewport().add(mWorkflowModelCanvas);
        mainPanel.add(mWorkflowScrollPane, BorderLayout.CENTER);
        JPanel southPanel = new JPanel();
        southPanel.add(mCloseButton);
        mainPanel.add(southPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
        pack();
    }
}
