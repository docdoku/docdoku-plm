package com.docdoku.client.ui.setting;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.GUIConstants;

public class LookAndFeelPanel extends JPanel {

    private JCheckBox mNumberedNodeCheck;

    public LookAndFeelPanel(boolean pNumberedNode) {
        mNumberedNodeCheck = new JCheckBox(I18N.BUNDLE.getString("NumberedNode_check"),pNumberedNode);
        createLayout();
    }

    private void createLayout() {
        setBorder(BorderFactory.createTitledBorder(I18N.BUNDLE.getString("Appearance_border")));
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
        add(mNumberedNodeCheck, constraints);
    }

    public boolean numberedNode(){
        return mNumberedNodeCheck.isSelected();
    }
}
