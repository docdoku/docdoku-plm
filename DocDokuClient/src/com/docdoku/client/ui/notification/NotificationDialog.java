package com.docdoku.client.ui.notification;

import com.docdoku.client.ui.common.OKCancelPanel;
import com.docdoku.client.data.MainModel;
import com.docdoku.client.localization.I18N;
import com.docdoku.core.entities.MasterDocument;
import com.docdoku.core.entities.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class NotificationDialog extends JDialog implements ActionListener {
    private NotificationPanel mNotificationPanel;
    private OKCancelPanel mOKCancelPanel;
    private ActionListener mAction;
    private MasterDocument mNotifiedMDoc;

    public NotificationDialog(Frame pOwner, MasterDocument pNotifiedMDoc, ActionListener pAction) {
        super(pOwner, I18N.BUNDLE.getString("NotificationDialog_title"), true);
        setLocationRelativeTo(pOwner);
        MainModel model = MainModel.getInstance();
        User user = model.getUser();

        mNotifiedMDoc=pNotifiedMDoc;
        boolean iterationStatus = MainModel.getInstance().hasIterationChangeEventSubscription(pNotifiedMDoc);
        boolean stateStatus = MainModel.getInstance().hasStateChangeEventSubscription(pNotifiedMDoc);
        mNotificationPanel = new NotificationPanel(iterationStatus, stateStatus);
        mOKCancelPanel = new OKCancelPanel(this, this);
        mAction = pAction;
        createLayout();
        setVisible(true);
    }

    private void createLayout() {
        getRootPane().setDefaultButton(mOKCancelPanel.getOKButton());
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(mNotificationPanel, BorderLayout.CENTER);
        mainPanel.add(mOKCancelPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
        pack();
    }

    public MasterDocument getMDoc() {
       return mNotifiedMDoc;
    }

    public void actionPerformed(ActionEvent pAE) {
        if (mNotificationPanel.isIteration())
            mAction.actionPerformed(new ActionEvent(this, 0, "subscribeIteration"));
        else
            mAction.actionPerformed(new ActionEvent(this, 0, "unsubscribeIteration"));
        
        if (mNotificationPanel.isState())
            mAction.actionPerformed(new ActionEvent(this, 0, "subscribeState"));
        else
            mAction.actionPerformed(new ActionEvent(this, 0, "unsubscribeState"));
    }
}
