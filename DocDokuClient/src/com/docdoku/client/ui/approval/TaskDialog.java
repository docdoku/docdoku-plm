package com.docdoku.client.ui.approval;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.GUIConstants;
import com.docdoku.client.ui.common.MaxLengthDocument;
import com.docdoku.client.ui.common.OKCancelPanel;
import com.docdoku.core.entities.MasterDocument;
import com.docdoku.core.entities.Task;
import java.util.Collection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TaskDialog extends JDialog implements ActionListener {
    private TaskPanel mTaskPanel;
    private OKCancelPanel mOKCancelPanel;
    private ActionListener mAction;
    private MasterDocument mMDoc;

    public TaskDialog(Frame pOwner, String pTitle, MasterDocument pMDoc, ActionListener pAction) {
        super(pOwner, pTitle, true);
        setLocationRelativeTo(pOwner);
        Collection<Task> runningTasks=pMDoc.getWorkflow().getRunningTasks();
        mTaskPanel=new TaskPanel(runningTasks);
        mOKCancelPanel = new OKCancelPanel(this, this);
        mAction = pAction;
        mMDoc=pMDoc;
        createLayout();
        setVisible(true);
    }

    private void createLayout() {
        getRootPane().setDefaultButton(mOKCancelPanel.getOKButton());
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(mTaskPanel, BorderLayout.CENTER);
        mainPanel.add(mOKCancelPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
        pack();
    }

    public Task getTask() {
        return mTaskPanel.getTask();
    }

    public MasterDocument getMDoc() {
        return mMDoc;
    }
    
    public String getComment() {
        return mTaskPanel.getComment();
    }

    public void actionPerformed(ActionEvent pAE) {
        mAction.actionPerformed(new ActionEvent(this, 0, null));
    }
}
