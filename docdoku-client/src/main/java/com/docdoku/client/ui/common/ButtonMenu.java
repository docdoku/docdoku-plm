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

package com.docdoku.client.ui.common;


import javax.swing.*;

public class ButtonMenu extends JMenuItem {
    private JLabel mStatusLabel;

    public ButtonMenu(JLabel pStatusLabel) {
        mStatusLabel = pStatusLabel;
    }

    @Override
    public void menuSelectionChanged(boolean pIsIncluded) {
        if (pIsIncluded) {
            Action action = getAction();
            if (action != null) {
                Object message = action.getValue(Action.LONG_DESCRIPTION);
                mStatusLabel.setText(message.toString());
            }
        } else
            mStatusLabel.setText(" ");

        super.menuSelectionChanged(pIsIncluded);
    }

}
