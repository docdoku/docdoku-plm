package com.docdoku.client.actions;

import com.docdoku.client.ui.ExplorerFrame;
import com.docdoku.client.ui.folder.CreateFolderDialog;
import com.docdoku.client.localization.I18N;
import com.docdoku.core.entities.Folder;

import javax.swing.*;
import java.awt.event.*;

public class CreateFolderAction extends ClientAbstractAction {
    
    public CreateFolderAction(ExplorerFrame pOwner) {
        super(I18N.BUNDLE.getString("FolderCreation_title"), "/com/docdoku/client/resources/icons/folder_new.png", pOwner);
        putValue(Action.SHORT_DESCRIPTION, I18N.BUNDLE.getString("FolderCreation_short_desc"));
        putValue(Action.LONG_DESCRIPTION, I18N.BUNDLE.getString("FolderCreation_long_desc"));
        putValue(Action.MNEMONIC_KEY, new Integer(I18N.getCharBundle("FolderCreation_mnemonic_key")));
    }
    
    public void actionPerformed(ActionEvent pAE) {
        ActionListener action = new ActionListener() {
            public void actionPerformed(ActionEvent pAE) {
                try{
                    CreateFolderDialog source = (CreateFolderDialog) pAE.getSource();
                    MainController controller = MainController.getInstance();
                    controller.createFolder(source.getParentFolder().getCompletePath(), source.getFolder());
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
        Folder parentFolder = new Folder(mOwner.getSelectedFolder().getCompletePath());
        new CreateFolderDialog(mOwner, parentFolder, action);
    }
}
