package com.docdoku.client.ui.folder;


import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.GUIConstants;
import com.docdoku.client.ui.common.MaxLengthDocument;

import javax.swing.*;
import java.awt.*;

public class CreateFolderPanel extends JPanel {

    private JLabel mFolderLabel;
    private JTextField mFolderText;

    public CreateFolderPanel() {
        mFolderLabel = new JLabel(I18N.BUNDLE.getString("NameMandatory_label"));
        mFolderText = new JTextField(new MaxLengthDocument(50), "", 10);
        createLayout();
    }

    public JTextField getFolderText() {
        return mFolderText;
    }

    public String getFolder() {
        return mFolderText.getText();
    }

    private void createLayout() {
        setBorder(BorderFactory.createTitledBorder(I18N.BUNDLE.getString("CreateFolderPanel_border")));
        mFolderLabel.setLabelFor(mFolderText);

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
        add(mFolderLabel, constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(mFolderText, constraints);
    }
}
