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

package com.docdoku.client.ui.user;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.OKCancelPanel;
import com.docdoku.core.common.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class EditUserDialog extends JDialog implements ActionListener {
    private EditUserPanel mEditUserPanel;
    private OKCancelPanel mOKCancelPanel;
    private ActionListener mAction;
    private User mUser;

    public EditUserDialog(Frame pOwner, User pEditedUser, ActionListener pAction) {
        this(pOwner, pAction);
        mEditUserPanel = new EditUserPanel(pEditedUser);
        mUser=pEditedUser;
        createLayout();
        setVisible(true);
    }

    private EditUserDialog(Frame pOwner, ActionListener pAction) {
        super(pOwner, I18N.BUNDLE.getString("EditUser_title"), true);
        setLocationRelativeTo(pOwner);
        mOKCancelPanel = new OKCancelPanel(this, this);
        mAction = pAction;
    }

    private void createLayout() {
        getRootPane().setDefaultButton(mOKCancelPanel.getOKButton());
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(mEditUserPanel, BorderLayout.CENTER);
        mainPanel.add(mOKCancelPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
        pack();
    }

    public String getUserName() {
        return mEditUserPanel.getUserName();
    }

    public String getEmail() {
        return mEditUserPanel.getEmail();
    }
    
    public String getLanguage() {
        return mUser.getLanguage();
    }

    public void actionPerformed(ActionEvent pAE) {
        mAction.actionPerformed(new ActionEvent(this, 0, null));
    }
}
