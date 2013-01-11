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

import com.docdoku.client.localization.I18N;
import com.docdoku.core.document.DocumentMaster;
import com.docdoku.client.ui.ExplorerFrame;
import com.docdoku.client.ui.doc.ViewIterationsDialog;

import javax.swing.*;
import java.awt.event.*;

public class ViewIterationsAction extends ClientAbstractAction {

    public ViewIterationsAction(ExplorerFrame pOwner) {
        super(I18N.BUNDLE.getString("ViewIterationList_title"), "/com/docdoku/client/resources/icons/history2.png", pOwner);
        putValue(Action.SHORT_DESCRIPTION, I18N.BUNDLE.getString("ViewIterationList_short_desc"));
        putValue(Action.LONG_DESCRIPTION, I18N.BUNDLE.getString("ViewIterationList_long_desc"));
        putValue(Action.MNEMONIC_KEY, new Integer(I18N.getCharBundle("ViewIterationList_mnemonic_key")));
    }

    public void actionPerformed(ActionEvent pAE) {
        ActionListener downloadAction = new DownloadActionListener();
        ActionListener openAction = new OpenActionListener();

        DocumentMaster docM = mOwner.getSelectedDocM();
        new ViewIterationsDialog(mOwner, docM, downloadAction, openAction);
    }
}
