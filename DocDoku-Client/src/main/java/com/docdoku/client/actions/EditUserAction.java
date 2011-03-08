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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;

import com.docdoku.client.data.MainModel;
import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.ExplorerFrame;
import com.docdoku.client.ui.user.EditUserDialog;
import com.docdoku.core.common.User;
import javax.swing.JOptionPane;

public class EditUserAction extends ClientAbstractAction {
    public EditUserAction(ExplorerFrame pOwner) {
        super(I18N.BUNDLE.getString("EditUser_title"), "/com/docdoku/client/resources/icons/id_card.png", pOwner);
        putValue(Action.SHORT_DESCRIPTION, I18N.BUNDLE.getString("EditUser_short_desc"));
        putValue(Action.LONG_DESCRIPTION, I18N.BUNDLE.getString("EditUser_long_desc"));
        putValue(Action.MNEMONIC_KEY, new Integer(I18N.getCharBundle("EditUser_mnemonic_key")));
    }

    public void actionPerformed(ActionEvent pAE) {
        ActionListener action = new ActionListener() {
            public void actionPerformed(ActionEvent pAE) {
                try{
                    EditUserDialog source = (EditUserDialog) pAE.getSource();
                    MainController controller = MainController.getInstance();
                    controller.savePersonalInfo(source.getUserName(), source.getEmail(),source.getLanguage());
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
        MainModel model = MainModel.getInstance();
        User currentUser = model.getUser();
        new EditUserDialog(mOwner, currentUser, action);
    }
}
