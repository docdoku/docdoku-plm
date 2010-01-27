package com.docdoku.client.actions;

import com.docdoku.client.localization.I18N;
import com.docdoku.core.entities.MasterDocument;
import com.docdoku.client.ui.ExplorerFrame;
import com.docdoku.client.ui.doc.ViewIterationsDialog;

import javax.swing.*;
import java.awt.event.*;

public class ViewIterationsAction extends ClientAbstractAction {

    public ViewIterationsAction(ExplorerFrame pOwner) {
        super(I18N.BUNDLE.getString("ViewIterationList_title"), "/com/docdoku/client/resources/icons/history2.png", pOwner);
        putValue(Action.SHORT_DESCRIPTION, I18N.BUNDLE.getString("ViewIterationList_short_desc"));
        putValue(Action.LONG_DESCRIPTION, I18N.BUNDLE.getString("ViewIterationList_long_desc"));
        putValue(Action.MNEMONIC_KEY, new Integer(I18N.getCharBundle("ViewIterationList_mnemonic_key")));
    }

    public void actionPerformed(ActionEvent pAE) {
        ActionListener downloadAction = new DownloadActionListener();
        ActionListener openAction = new OpenActionListener();

        MasterDocument mdoc = mOwner.getSelectedMDoc();
        new ViewIterationsDialog(mOwner, mdoc, downloadAction, openAction);
    }
}
