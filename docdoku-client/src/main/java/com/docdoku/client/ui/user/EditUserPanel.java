/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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

import com.docdoku.core.common.User;
import com.docdoku.client.ui.common.MaxLengthDocument;
import com.docdoku.client.ui.common.GUIConstants;

import javax.swing.*;
import java.awt.*;

public class EditUserPanel extends UserPanel {


    private JTextField mNameText;
    private JTextField mEmailText;

    public EditUserPanel(User pUser) {
        super(pUser);
        mNameText = new JTextField(new MaxLengthDocument(255), pUser.getName(), 10);
        mEmailText = new JTextField(new MaxLengthDocument(255), pUser.getEmail(), 10);
        createLayout();
    }

    public EditUserPanel(String pLogin) {
        super(pLogin);
        mNameText = new JTextField(new MaxLengthDocument(255), "", 10);
        mEmailText = new JTextField(new MaxLengthDocument(255), "", 10);
        createLayout();
    }

    public String getUserName() {
        return mNameText.getText();
    }

    public String getEmail() {
        return mEmailText.getText();
    }

    private void createLayout() {

        mNameLabel.setLabelFor(mNameText);
        mEmailLabel.setLabelFor(mEmailText);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 1;
        constraints.weighty = 0;
        constraints.insets = GUIConstants.INSETS;
        constraints.gridheight = 1;
        constraints.gridwidth = 1;
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(mNameText, constraints);

        constraints.gridy = 2;
        add(mEmailText, constraints);
    }
}
