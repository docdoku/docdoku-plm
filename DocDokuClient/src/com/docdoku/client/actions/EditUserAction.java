package com.docdoku.client.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;

import com.docdoku.client.data.MainModel;
import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.ExplorerFrame;
import com.docdoku.client.ui.user.EditUserDialog;
import com.docdoku.core.entities.User;
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
