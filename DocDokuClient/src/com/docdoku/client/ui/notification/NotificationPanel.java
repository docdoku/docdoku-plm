package com.docdoku.client.ui.notification;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.GUIConstants;

import javax.swing.*;
import java.awt.*;

public class NotificationPanel extends JPanel {

    private JCheckBox mIterationCheck;
    private JCheckBox mStateCheck;

    public NotificationPanel(boolean pIterationStatus, boolean pStateStatus) {
        mIterationCheck =
                new JCheckBox(I18N.BUNDLE.getString("NotificationPanel_iteration_checkbox"),
                        pIterationStatus);
        mStateCheck =
                new JCheckBox(I18N.BUNDLE.getString("NotificationPanel_state_checkbox"),
                        pStateStatus);
        createLayout();
    }

    public boolean isIteration() {
        return mIterationCheck.isSelected();
    }

    public boolean isState() {
        return mStateCheck.isSelected();
    }

    private void createLayout() {
        setBorder(BorderFactory.createTitledBorder(I18N.BUNDLE.getString("NotificationPanel_border")));
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weighty = 0;
        constraints.weightx = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = GUIConstants.INSETS;
        constraints.gridheight = 1;
        constraints.gridwidth = 1;

        constraints.gridx = 0;
        constraints.gridy = 0;

        constraints.fill = GridBagConstraints.NONE;
        add(mIterationCheck, constraints);

        constraints.gridy = 1;
        add(mStateCheck, constraints);
    }
}
