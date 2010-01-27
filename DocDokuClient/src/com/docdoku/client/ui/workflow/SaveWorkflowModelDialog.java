package com.docdoku.client.ui.workflow;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.OKCancelPanel;
import com.docdoku.core.entities.WorkflowModel;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SaveWorkflowModelDialog extends JDialog implements ActionListener {
    private SaveWorkflowModelPanel mSaveWorkflowModelPanel;
    private OKCancelPanel mOKCancelPanel;
    private ActionListener mAction;

    public SaveWorkflowModelDialog(Frame pOwner, WorkflowModel pEditedWorkflowModel, ActionListener pAction) {
        super(pOwner, I18N.BUNDLE.getString("SaveWorkflowModel_title"), true);
        setLocationRelativeTo(pOwner);
        mSaveWorkflowModelPanel = new SaveWorkflowModelPanel(pEditedWorkflowModel);
        mOKCancelPanel = new OKCancelPanel(this, this);
        mAction = pAction;
        createLayout();
        createListener();
        setVisible(true);
    }

    private void createLayout() {
        getRootPane().setDefaultButton(mOKCancelPanel.getOKButton());
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(mSaveWorkflowModelPanel, BorderLayout.CENTER);
        mainPanel.add(mOKCancelPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
        mOKCancelPanel.setEnabled(true);
        pack();
    }

    private void createListener() {
        DocumentListener listener = new DocumentListener() {
            public void insertUpdate(DocumentEvent pDE) {
                mOKCancelPanel.setEnabled(true);
            }

            public void removeUpdate(DocumentEvent pDE) {
                int length = pDE.getDocument().getLength();
                if (length == 0)
                    mOKCancelPanel.setEnabled(false);
            }

            public void changedUpdate(DocumentEvent pDE) {
            }
        };
        mSaveWorkflowModelPanel.getWorkflowModelText().getDocument().addDocumentListener(listener);
    }



    public String getWorkflowModelId() {
        return mSaveWorkflowModelPanel.getWorkflowModelId();
    }

    public void actionPerformed(ActionEvent pAE) {
        mAction.actionPerformed(new ActionEvent(this, 0, null));
    }
}
