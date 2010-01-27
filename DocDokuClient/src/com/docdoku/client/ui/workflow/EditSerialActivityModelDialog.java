package com.docdoku.client.ui.workflow;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.OKCancelPanel;
import com.docdoku.core.entities.SerialActivityModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class EditSerialActivityModelDialog  extends JDialog implements ActionListener {

    private OKCancelPanel mOKCancelPanel;
    private EditSerialActivityModelPanel mActivityModelPanel;
    private ActionListener mAction;

    public EditSerialActivityModelDialog(Frame pOwner,SerialActivityModel pActivityModel,ActionListener pAddTaskAction, ActionListener pEditTaskAction, ActionListener pAction){
        super(pOwner,I18N.BUNDLE.getString("EditSerialActivity_title"),true);
        setLocationRelativeTo(pOwner);
        mActivityModelPanel=new EditSerialActivityModelPanel(pActivityModel,pAddTaskAction,pEditTaskAction);
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

    public void actionPerformed(ActionEvent e) {
        mAction.actionPerformed(new ActionEvent(this, 0, null));
    }
}
