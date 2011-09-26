/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.docdoku.client.ui.login;

import com.docdoku.client.data.Prefs;
import com.docdoku.client.localization.I18N;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import com.docdoku.client.ui.common.OKButton;

public class LoginFrame extends JFrame implements ActionListener {

    private OKButton mValidButton;
    private LoginPanel mLoginPanel;
    private ActionListener mAction;

    public LoginFrame(ActionListener pAction) {
        super(I18N.BUNDLE.getString("LoginFrame_title"));
        setLocationRelativeTo(null);
        mAction = pAction;
        String lastWorkspace = Prefs.getLastWorkspace();
        String lastLogin = Prefs.getLastLogin();
        mLoginPanel = new LoginPanel(lastWorkspace, lastLogin);

        if (lastLogin != null) {
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowOpened(WindowEvent e) {
                    mLoginPanel.getPasswordText().requestFocus();
                }
            });
        }
        mValidButton = new OKButton(I18N.BUNDLE.getString("Login_title"));
        Image img =
                Toolkit.getDefaultToolkit().getImage(LoginFrame.class.getResource("/com/docdoku/client/resources/icons/key1.png"));
        setIconImage(img);
        createLayout();
        createListener();
        pack();
        setVisible(true);
    }

    public String getUser() {
        return mLoginPanel.getUser();
    }

    public char[] getPassword() {
        return mLoginPanel.getPassword();
    }

    public String getWorkspace() {
        return mLoginPanel.getWorkspace();
    }

    public void clear() {
        mLoginPanel.clear();
    }

    @Override
    public void dispose() {
        Prefs.setLastLogin(getUser());
        Prefs.setLastWorkspace(getWorkspace());
        super.dispose();
    }

    private void createLayout() {
        getRootPane().setDefaultButton(mValidButton);
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(mLoginPanel, BorderLayout.CENTER);
        JPanel southPanel = new JPanel();
        southPanel.add(mValidButton);
        mainPanel.add(southPanel, BorderLayout.SOUTH);
        getContentPane().add(mainPanel);

    }

    private void createListener() {
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent pWE) {
                System.exit(0);
            }
        });
        mValidButton.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent pAE) {
        mAction.actionPerformed(new ActionEvent(this, 0, null));
    }
}
