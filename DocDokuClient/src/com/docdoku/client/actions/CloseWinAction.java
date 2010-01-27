package com.docdoku.client.actions;

import com.docdoku.client.ui.ExplorerFrame;

import javax.swing.*;
import java.awt.event.*;

import com.docdoku.client.localization.I18N;

public class CloseWinAction extends ClientAbstractAction {
    public CloseWinAction(ExplorerFrame pOwner) {
        super(I18N.BUNDLE.getString("Close_button"), "/com/docdoku/client/resources/icons/window_delete.png", pOwner);
        putValue(Action.SHORT_DESCRIPTION, I18N.BUNDLE.getString("Close_short_desc"));
        putValue(Action.LONG_DESCRIPTION, I18N.BUNDLE.getString("Close_long_desc"));
        putValue(Action.MNEMONIC_KEY, new Integer(I18N.getCharBundle("Close_mnemonic_key")));
    }

    public void actionPerformed(ActionEvent pAE) {
        mOwner.dispose();
    }
}
