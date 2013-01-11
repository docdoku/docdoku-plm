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

package com.docdoku.client.actions;

import com.docdoku.client.ui.ExplorerFrame;

import javax.swing.*;
import java.awt.event.*;

import com.docdoku.client.localization.I18N;

public class CloseWinAction extends ClientAbstractAction {
    public CloseWinAction(ExplorerFrame pOwner) {
        super(I18N.BUNDLE.getString("Close_button"), "/com/docdoku/client/resources/icons/window_delete.png", pOwner);
        putValue(Action.SHORT_DESCRIPTION, I18N.BUNDLE.getString("Close_short_desc"));
        putValue(Action.LONG_DESCRIPTION, I18N.BUNDLE.getString("Close_long_desc"));
        putValue(Action.MNEMONIC_KEY, new Integer(I18N.getCharBundle("Close_mnemonic_key")));
    }

    public void actionPerformed(ActionEvent pAE) {
        mOwner.dispose();
    }
}
