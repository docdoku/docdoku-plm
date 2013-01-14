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
