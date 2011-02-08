/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010 DocDoku SARL
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

import com.docdoku.client.ui.tag.ManageTagsDialog;
import com.docdoku.core.document.MasterDocument;
import com.docdoku.client.ui.ExplorerFrame;

import javax.swing.*;

import java.awt.event.*;

import com.docdoku.client.localization.I18N;

public class ManageTagsAction extends ClientAbstractAction {
    
    public ManageTagsAction(ExplorerFrame pOwner) {
        super(I18N.BUNDLE.getString("ManageTags_title"), "/com/docdoku/client/resources/icons/note.png", pOwner);
        putValue(Action.SHORT_DESCRIPTION, I18N.BUNDLE.getString("ManageTags_short_desc"));
        putValue(Action.LONG_DESCRIPTION, I18N.BUNDLE.getString("ManageTags_long_desc"));
        putValue(Action.MNEMONIC_KEY, new Integer(I18N.getCharBundle("ManageTags_mnemonic_key")));
    }

    @Override
    public void actionPerformed(ActionEvent pAE) {
        MasterDocument mdoc = mOwner.getSelectedMDoc();
        ActionListener action = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent pAE) {
                ManageTagsDialog source = (ManageTagsDialog) pAE.getSource();
                MasterDocument mdoc = source.getMDoc();
                String[] tags = source.getTags();
                MainController controller = MainController.getInstance();
                try{
                    controller.saveTags(mdoc, tags);
                }catch (Exception pEx) {
                    String message = pEx.getMessage()==null?I18N.BUNDLE
                            .getString("Error_unknown"):pEx.getMessage();
                    JOptionPane.showMessageDialog(null,
                            message, I18N.BUNDLE
                            .getString("Error_title"),
                            JOptionPane.ERROR_MESSAGE);
                }
                ExplorerFrame.unselectElementInAllFrame();
            }
        };
        new ManageTagsDialog(mOwner, mdoc, action);
    }
}
