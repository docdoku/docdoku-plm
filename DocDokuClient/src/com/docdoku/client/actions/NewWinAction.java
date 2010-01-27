package com.docdoku.client.actions;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.ExplorerFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class NewWinAction extends ClientAbstractAction {
    public NewWinAction(ExplorerFrame pOwner) {
        super(I18N.BUNDLE.getString("Open_title"), "/com/docdoku/client/resources/icons/window_new.png", pOwner);
        putValue(Action.SHORT_DESCRIPTION, I18N.BUNDLE.getString("Open_short_desc"));
        putValue(Action.LONG_DESCRIPTION, I18N.BUNDLE.getString("Open_long_desc"));
        putValue(Action.MNEMONIC_KEY, new Integer(I18N.getCharBundle("Open_mnemonic_key")));
    }

    public void actionPerformed(ActionEvent pAE) {
        mOwner.duplicate();
    }
}
