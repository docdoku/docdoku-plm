package com.docdoku.client.actions;

import com.docdoku.client.ui.tag.ManageTagsDialog;
import com.docdoku.core.entities.MasterDocument;
import com.docdoku.core.entities.TaskModel;
import com.docdoku.client.ui.ExplorerFrame;
import java.util.Set;

import javax.swing.*;

import java.awt.event.*;

import com.docdoku.client.localization.I18N;

public class ManageTagsAction extends ClientAbstractAction {
    
    public ManageTagsAction(ExplorerFrame pOwner) {
        super(I18N.BUNDLE.getString("ManageTags_title"), "/com/docdoku/client/resources/icons/note.png", pOwner);
        putValue(Action.SHORT_DESCRIPTION, I18N.BUNDLE.getString("ManageTags_short_desc"));
        putValue(Action.LONG_DESCRIPTION, I18N.BUNDLE.getString("ManageTags_long_desc"));
        putValue(Action.MNEMONIC_KEY, new Integer(I18N.getCharBundle("ManageTags_mnemonic_key")));
    }
    
    public void actionPerformed(ActionEvent pAE) {
        MasterDocument mdoc = mOwner.getSelectedMDoc();
        ActionListener action = new ActionListener() {
            public void actionPerformed(ActionEvent pAE) {
                ManageTagsDialog source = (ManageTagsDialog) pAE.getSource();
                MasterDocument mdoc = source.getMDoc();
                String[] tags = source.getTags();
                MainController controller = MainController.getInstance();
                try{
                    controller.saveTags(mdoc, tags);
                }catch (Exception pEx) {
                    String message = pEx.getMessage()==null?I18N.BUNDLE
                            .getString("Error_unknown"):pEx.getMessage();
                    JOptionPane.showMessageDialog(null,
                            message, I18N.BUNDLE
                            .getString("Error_title"),
                            JOptionPane.ERROR_MESSAGE);
                }
                ExplorerFrame.unselectElementInAllFrame();
            }
        };
        new ManageTagsDialog(mOwner, mdoc, action);
    }
}
