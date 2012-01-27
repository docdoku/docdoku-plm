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

package com.docdoku.client.ui.user;

import com.docdoku.client.localization.I18N;
import com.docdoku.core.common.User;
import com.docdoku.client.ui.common.GUIConstants;

import javax.swing.*;
import java.awt.*;

public abstract class UserPanel extends JPanel {

    private JLabel mLoginLabel;
    protected JLabel mNameLabel;
    protected JLabel mEmailLabel;
    private JLabel mLoginValueLabel;

    public UserPanel(User pUser) {
        this(pUser.getLogin());
    }

    public UserPanel(String pLogin) {
        mLoginLabel = new JLabel(I18N.BUNDLE.getString("Login_label"));
        mNameLabel = new JLabel(I18N.BUNDLE.getString("Name_label"));
        mEmailLabel = new JLabel(I18N.BUNDLE.getString("Email_label"));
        mLoginValueLabel = new JLabel(pLogin);
        createLayout();
    }

    private void createLayout() {
        setBorder(BorderFactory.createTitledBorder(I18N.BUNDLE.getString("User_border")));
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weighty = 0;
        constraints.weightx = 0;
        constraints.insets = GUIConstants.INSETS;
        constraints.gridheight = 1;
        constraints.gridwidth = 1;

        constraints.gridx = 0;
        constraints.gridy = 0;

        constraints.anchor = GridBagConstraints.EAST;
        constraints.fill = GridBagConstraints.NONE;
        add(mLoginLabel, constraints);

        constraints.gridx = 1;
        constraints.anchor = GridBagConstraints.WEST;
        add(mLoginValueLabel, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        add(mNameLabel, constraints);

        constraints.gridy = 2;
        add(mEmailLabel, constraints);
    }
}
