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

import com.docdoku.client.localization.I18N;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class OKCancelPanel extends JPanel {

    private OKButton mOK;
    private CloseButton mCancel;

    public OKCancelPanel(Window pFrame, ActionListener pActionOK) {
        this(pFrame, pActionOK, true);
    }

    public OKCancelPanel(Window pFrame, ActionListener pActionOK, boolean pDisposeOnOK) {
        super(new FlowLayout(FlowLayout.CENTER));
        if (pDisposeOnOK) {
            mOK = new OKButton(pFrame, I18N.BUNDLE.getString("Ok_button"));
        } else {
            mOK = new OKButton(I18N.BUNDLE.getString("Ok_button"));
        }
        mCancel = new CloseButton(pFrame, I18N.BUNDLE.getString("Cancel_button"));
        createLayout();
        createListener(pActionOK);
    }

    @Override
    public void setEnabled(boolean pValue) {
        mOK.setEnabled(pValue);
    }

    private void createLayout() {
        add(mOK);
        add(mCancel);
    }

    private void createListener(ActionListener pActionOK) {
        mOK.addActionListener(pActionOK);
    }

    public JButton getOKButton() {
        return mOK;
    }
}
