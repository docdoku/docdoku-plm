/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.client.ui.notification;

import com.docdoku.client.ui.common.OKCancelPanel;
import com.docdoku.client.data.MainModel;
import com.docdoku.client.localization.I18N;
import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.common.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class NotificationDialog extends JDialog implements ActionListener {
    private NotificationPanel mNotificationPanel;
    private OKCancelPanel mOKCancelPanel;
    private ActionListener mAction;
    private DocumentMaster mNotifiedDocM;

    public NotificationDialog(Frame pOwner, DocumentMaster pNotifiedDocM, ActionListener pAction) {
        super(pOwner, I18N.BUNDLE.getString("NotificationDialog_title"), true);
        setLocationRelativeTo(pOwner);
        MainModel model = MainModel.getInstance();
        User user = model.getUser();

        mNotifiedDocM=pNotifiedDocM;
        boolean iterationStatus = MainModel.getInstance().hasIterationChangeEventSubscription(pNotifiedDocM);
        boolean stateStatus = MainModel.getInstance().hasStateChangeEventSubscription(pNotifiedDocM);
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

    public DocumentMaster getDocM() {
       return mNotifiedDocM;
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
