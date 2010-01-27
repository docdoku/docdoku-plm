package com.docdoku.client.ui.workflow;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.OKCancelPanel;
import com.docdoku.core.entities.ParallelActivityModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class EditParallelActivityModelDialog extends JDialog implements ActionListener {

    private OKCancelPanel mOKCancelPanel;
    private EditParallelActivityModelPanel mActivityModelPanel;
    private ActionListener mAction;

    public EditParallelActivityModelDialog(Frame pOwner,ParallelActivityModel pActivityModel, ActionListener pAddTaskAction, ActionListener pEditTaskAction, ActionListener pAction){
        super(pOwner,I18N.BUNDLE.getString("EditParallelActivity_title"),true);
        setLocationRelativeTo(pOwner);
        mActivityModelPanel=new EditParallelActivityModelPanel(pActivityModel, pAddTaskAction, pEditTaskAction);
        mOKCancelPanel = new OKCancelPanel(this, this);
        mAction=pAction;
        createLayout();
        setVisible(true);
    }

    private void createLayout() {
        getRootPane().setDefaultButton(mOKCancelPanel.getOKButton());
        JPanel mainPanel = new JPanel(new BorderLayout());
        Box centerPanel = new Box(BoxLayout.Y_AXIS);
        centerPanel.add(mActivityModelPanel);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(mOKCancelPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
        pack();
    }

    public int getNumberOfNeededCompletedTasks() {
        return mActivityModelPanel.getNumberOfNeededCompletedTasks();
    }

    public void actionPerformed(ActionEvent e) {
        mAction.actionPerformed(new ActionEvent(this, 0, null));
    }
}
