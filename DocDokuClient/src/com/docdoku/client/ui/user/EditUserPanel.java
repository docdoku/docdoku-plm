package com.docdoku.client.ui.user;

import com.docdoku.core.entities.User;
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
