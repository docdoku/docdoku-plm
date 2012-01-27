/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
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
import com.docdoku.client.ui.folder.CreateFolderDialog;
import com.docdoku.client.localization.I18N;
import com.docdoku.core.document.Folder;

import javax.swing.*;
import java.awt.event.*;

public class CreateFolderAction extends ClientAbstractAction {
    
    public CreateFolderAction(ExplorerFrame pOwner) {
        super(I18N.BUNDLE.getString("FolderCreation_title"), "/com/docdoku/client/resources/icons/folder_new.png", pOwner);
        putValue(Action.SHORT_DESCRIPTION, I18N.BUNDLE.getString("FolderCreation_short_desc"));
        putValue(Action.LONG_DESCRIPTION, I18N.BUNDLE.getString("FolderCreation_long_desc"));
        putValue(Action.MNEMONIC_KEY, new Integer(I18N.getCharBundle("FolderCreation_mnemonic_key")));
    }
    
    @Override
    public void actionPerformed(ActionEvent pAE) {
        ActionListener action = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent pAE) {
                try{
                    CreateFolderDialog source = (CreateFolderDialog) pAE.getSource();
                    MainController controller = MainController.getInstance();
                    controller.createFolder(source.getParentFolder().getCompletePath(), source.getFolder());
                }catch (Exception pEx) {
                    String message = pEx.getMessage()==null?I18N.BUNDLE
                            .getString("Error_unknown"):pEx.getMessage();
                    JOptionPane.showMessageDialog(null,
                            message, I18N.BUNDLE
                            .getString("Error_title"),
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        Folder parentFolder = new Folder(mOwner.getSelectedFolder().getCompletePath());
        new CreateFolderDialog(mOwner, parentFolder, action);
    }
}
