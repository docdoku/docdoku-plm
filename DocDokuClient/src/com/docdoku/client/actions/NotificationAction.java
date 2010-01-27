package com.docdoku.client.actions;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.ExplorerFrame;
import com.docdoku.client.ui.notification.NotificationDialog;

import javax.swing.*;
import java.awt.event.*;

public class NotificationAction extends ClientAbstractAction {

    public NotificationAction(ExplorerFrame pOwner) {
        super(I18N.BUNDLE.getString("EmailNotification_title"), "/com/docdoku/client/resources/icons/mail.png", pOwner);
        putValue(Action.SHORT_DESCRIPTION, I18N.BUNDLE.getString("EmailNotification_short_desc"));
        putValue(Action.LONG_DESCRIPTION, I18N.BUNDLE.getString("EmailNotification_long_desc"));
        putValue(Action.MNEMONIC_KEY, new Integer(I18N.getCharBundle("EmailNotification_mnemonic_key")));
    }

    public void actionPerformed(ActionEvent pAE) {
        ActionListener action = new ActionListener() {
            public void actionPerformed(ActionEvent pAE) {
                try{
                    NotificationDialog source = (NotificationDialog) pAE.getSource();
                    MainController controller = MainController.getInstance();
                    String command = pAE.getActionCommand();

                    if (command.equals("subscribeIteration"))
                        controller.subscribeIterationNotification(source.getMDoc());
                    else if (command.equals("unsubscribeIteration"))
                        controller.unsubscribeIterationNotification(source.getMDoc());

                    if (command.equals("subscribeState"))
                        controller.subscribeStateNotification(source.getMDoc());
                    else if (command.equals("unsubscribeState"))
                        controller.unsubscribeStateNotification(source.getMDoc());
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

        new NotificationDialog(mOwner, mOwner.getSelectedMDoc(), action);
    }
}