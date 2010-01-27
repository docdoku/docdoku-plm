package com.docdoku.client.ui.workflow;

import com.docdoku.client.ui.common.OKCancelPanel;
import com.docdoku.client.ui.workflow.EditTaskModelPanel;
import com.docdoku.core.entities.User;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;



public abstract class TaskModelDialog extends JDialog implements ActionListener {

    protected EditTaskModelPanel mEditTaskModelPanel;
    protected OKCancelPanel mOKCancelPanel;
    protected ActionListener mAction;

    public TaskModelDialog(Frame pOwner, String pTitle) {
        super(pOwner, pTitle, true);
        setLocationRelativeTo(pOwner);
    }

    public TaskModelDialog(Dialog pOwner, String pTitle) {
        super(pOwner, pTitle, true);
        setLocationRelativeTo(pOwner);
    }

    protected void init(ActionListener pAction){
        mOKCancelPanel = new OKCancelPanel(this, this);
        mAction = pAction;
        createLayout();
        createListener();
    }

    private void createLayout() {
        getRootPane().setDefaultButton(mOKCancelPanel.getOKButton());
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(mEditTaskModelPanel, BorderLayout.CENTER);
        mainPanel.add(mOKCancelPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
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
        mEditTaskModelPanel.getTitleText().getDocument().addDocumentListener(listener);
    }

    public String getTaskTitle() {
        return mEditTaskModelPanel.getTitle();
    }

    public String getInstructions() {
        return mEditTaskModelPanel.getInstructions();
    }

    public User getUser() {
        return mEditTaskModelPanel.getUser();
    }

    public void actionPerformed(ActionEvent pAE) {
        mAction.actionPerformed(new ActionEvent(this, 0, null));
    }
}
