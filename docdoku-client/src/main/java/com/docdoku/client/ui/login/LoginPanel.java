/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
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


import com.docdoku.client.localization.I18N;
import javax.swing.*;

import java.awt.*;

import com.docdoku.client.data.Config;
import com.docdoku.client.ui.common.GUIConstants;
import com.docdoku.client.ui.common.WebLink;

public class LoginPanel extends JPanel {

    private JLabel mWorkspaceLabel;
    private JLabel mUserLabel;
    private JLabel mPasswordLabel;
    private WebLink mAccountLink;

    private JTextField mWorkspaceText;
    private JTextField mUserText;
    private JPasswordField mPasswordText;
    
    
    public LoginPanel(String pWorkspace, String pLogin) {
        this();
        if(pWorkspace!=null){
            mWorkspaceText.setText(pWorkspace);
        }
        if(pLogin!=null){
            mUserText.setText(pLogin);
        }
        
    }

    public LoginPanel() {
        mWorkspaceLabel = new JLabel(I18N.BUNDLE.getString("Workspace_label"));
        mUserLabel = new JLabel(I18N.BUNDLE.getString("User_label"));
        mPasswordLabel = new JLabel(I18N.BUNDLE.getString("Password_label"));
        
        String form = "/faces/registrationForm.xhtml";
        mAccountLink = new WebLink(I18N.BUNDLE.getString("Login_link"),Config.getHTTPCodebase().toString()+form);
        
        mWorkspaceText = new JTextField("", 10);
        mUserText = new JTextField("", 10);
        mPasswordText = new JPasswordField("", 10);
        createLayout();
    }

    public String getWorkspace() {
        return mWorkspaceText.getText();
    }

    public String getUser() {
        return mUserText.getText();
    }

    public char[] getPassword() {
        return mPasswordText.getPassword();
    }

    public JTextField getPasswordText() {
        return mPasswordText;
    }
    
    public void clear() {
        mUserText.setText("");
        mUserText.setCaretPosition(0);
        mPasswordText.setText("");
        mPasswordText.setCaretPosition(0);
        mWorkspaceText.setText("");
        mWorkspaceText.setCaretPosition(0);
    }

    
    private void createLayout() {
        setBorder(BorderFactory.createTitledBorder(I18N.BUNDLE.getString("Login_border")));
        mUserLabel.setLabelFor(mUserText);
        mPasswordLabel.setLabelFor(mPasswordText);
        mWorkspaceLabel.setLabelFor(mWorkspaceText);
        mPasswordText.setEchoChar('*');

        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weighty = 0;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = GUIConstants.INSETS;
        constraints.gridheight = 1;
        constraints.gridwidth = 1;

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 0;
        constraints.fill = GridBagConstraints.NONE;
        add(mWorkspaceLabel, constraints);

        constraints.gridy = 1;
        add(mUserLabel, constraints);

        constraints.gridy = 2;
        add(mPasswordLabel, constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridy = 0;
        add(mWorkspaceText, constraints);

        constraints.gridy = 1;
        add(mUserText, constraints);

        constraints.gridy = 2;
        add(mPasswordText, constraints);
        
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.gridy = 3;
        add(mAccountLink, constraints);
    }
}