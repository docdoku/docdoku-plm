package com.docdoku.client.actions;

import com.docdoku.core.entities.User;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;

import com.docdoku.client.data.MainModel;
import com.docdoku.client.data.Prefs;
import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.ExplorerFrame;
import com.docdoku.client.ui.setting.SettingDialog;
import javax.swing.JOptionPane;

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
