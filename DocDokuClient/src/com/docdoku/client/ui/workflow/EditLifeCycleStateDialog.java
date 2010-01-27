package com.docdoku.client.ui.workflow;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.OKCancelPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class EditLifeCycleStateDialog extends JDialog implements ActionListener {

    private OKCancelPanel mOKCancelPanel;
    private EditLifeCycleStatePanel mLifeCycleStatePanel;
    private ActionListener mAction;

    public EditLifeCycleStateDialog(Frame pOwner,String pState,ActionListener pAction){
        super(pOwner,I18N.BUNDLE.getString("EditLifecycle_title"),true);
        setLocationRelativeTo(pOwner);
        mLifeCycleStatePanel=new EditLifeCycleStatePanel(pState);
        mOKCancelPanel = new OKCancelPanel(this, this);
        mAction=pAction;
        createLayout();
        setVisible(true);
    }

    public String getState(){
        return mLifeCycleStatePanel.getState();
    }

    private void createLayout() {
        getRootPane().setDefaultButton(mOKCancelPanel.getOKButton());
        JPanel mainPanel = new JPanel(new BorderLayout());
        Box centerPanel = new Box(BoxLayout.Y_AXIS);
        centerPanel.add(mLifeCycleStatePanel);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(mOKCancelPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
        pack();
    }

    public void actionPerformed(ActionEvent e) {
        mAction.actionPerformed(new ActionEvent(this, 0, null));
    }
}
