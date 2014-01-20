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

package com.docdoku.client.actions;

import com.docdoku.client.data.MainModel;
import com.docdoku.client.data.Prefs;
import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.ExplorerFrame;
import com.docdoku.client.ui.setting.SettingDialog;
import com.docdoku.core.common.User;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SettingAction extends ClientAbstractAction {

    public SettingAction(ExplorerFrame pOwner) {
        super(I18N.BUNDLE.getString("GlobalOptions_title"), "/com/docdoku/client/resources/icons/preferences.png", pOwner);
        putValue(Action.SHORT_DESCRIPTION, I18N.BUNDLE.getString("GlobalOptions_short_desc"));
        putValue(Action.LONG_DESCRIPTION,
                I18N.BUNDLE.getString("GlobalOptions_long_desc"));
        putValue(Action.MNEMONIC_KEY, new Integer(I18N.getCharBundle("GlobalOptions_mnemonic_key")));
    }

    public void actionPerformed(ActionEvent pAE) {
        ActionListener action = new ActionListener() {
            public void actionPerformed(ActionEvent pAE) {
                try{
                    SettingDialog source = (SettingDialog) pAE.getSource();
                    MainModel.getInstance().getElementsTreeModel().setNumbered(source.numberedNode());
                    Prefs.setLocale(source.getSelectedLocale());
                    User user = MainModel.getInstance().getUser();
                    MainController.getInstance().savePersonalInfo(user.getName(), user.getEmail(),source.getSelectedLocale().getLanguage());
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
        new SettingDialog(mOwner, action);
    }

}
