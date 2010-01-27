package com.docdoku.client.actions;

import com.docdoku.client.ui.ExplorerFrame;
import com.docdoku.client.localization.I18N;
import com.docdoku.client.data.MainModel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class RefreshAction extends ClientAbstractAction {
    public RefreshAction(ExplorerFrame pOwner) {
        super(I18N.BUNDLE.getString("Refresh_title"), "/com/docdoku/client/resources/icons/refresh.png", pOwner);
        putValue(Action.SHORT_DESCRIPTION, I18N.BUNDLE.getString("Refresh_short_desc"));
        putValue(Action.LONG_DESCRIPTION, I18N.BUNDLE.getString("Refresh_long_desc"));
        putValue(Action.MNEMONIC_KEY, new Integer(I18N.getCharBundle("Refresh_mnemonic_key")));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        setLargeIcon("/com/docdoku/client/resources/icons/refresh_large.png");
    }

    public void actionPerformed(ActionEvent pAE) {
        MainModel.getInstance().updater.clear();
    }
}
