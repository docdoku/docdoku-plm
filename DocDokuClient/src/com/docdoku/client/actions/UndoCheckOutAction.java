package com.docdoku.client.actions;

import com.docdoku.client.data.Config;
import com.docdoku.client.data.MainModel;
import com.docdoku.client.data.Prefs;
import com.docdoku.core.util.FileIO;
import com.docdoku.core.entities.MasterDocument;
import com.docdoku.client.ui.ExplorerFrame;
import com.docdoku.client.localization.I18N;

import java.awt.*;
import java.io.File;
import javax.swing.*;
import java.awt.event.*;

public class UndoCheckOutAction extends ClientAbstractAction {
    
    public UndoCheckOutAction(ExplorerFrame pOwner) {
        super(I18N.BUNDLE.getString("UndoCheckOut_title"), "/com/docdoku/client/resources/icons/undo.png", pOwner);
        putValue(Action.SHORT_DESCRIPTION, I18N.BUNDLE.getString("UndoCheckOut_short_desc"));
        putValue(Action.LONG_DESCRIPTION,
                I18N.BUNDLE.getString("UndoCheckOut_long_desc"));
        putValue(Action.MNEMONIC_KEY, new Integer(I18N.getCharBundle("UndoCheckOut_mnemonic_key")));
        putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke('Z', Event.CTRL_MASK));
        setLargeIcon("/com/docdoku/client/resources/icons/undo_large.png");
    }
    
    public void actionPerformed(ActionEvent pAE) {
        MasterDocument mdoc = mOwner.getSelectedMDoc();
        MainController controller = MainController.getInstance();
        try {
            MasterDocument newMDoc = controller.undoCheckOut(mdoc);
            FileIO.rmDir(Config.getCheckOutFolder(newMDoc));
            Prefs.removeDocNode(newMDoc);
        } catch (Exception pEx) {
            String message = pEx.getMessage()==null?I18N.BUNDLE
                    .getString("Error_unknown"):pEx.getMessage();
            JOptionPane.showMessageDialog(null,
                    message, I18N.BUNDLE
                    .getString("Error_title"),
                    JOptionPane.ERROR_MESSAGE);
        }
        ExplorerFrame.unselectElementInAllFrame();
    }
}
