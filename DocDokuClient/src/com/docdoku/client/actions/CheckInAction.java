package com.docdoku.client.actions;

import com.docdoku.client.data.Config;
import com.docdoku.client.data.Prefs;
import com.docdoku.core.entities.BinaryResource;
import com.docdoku.core.entities.Document;
import com.docdoku.core.util.FileIO;
import com.docdoku.core.entities.MasterDocument;
import com.docdoku.client.ui.ExplorerFrame;
import com.docdoku.client.localization.I18N;
import java.io.File;
import java.io.InterruptedIOException;

import javax.swing.*;

import java.awt.event.*;
import java.awt.*;

public class CheckInAction extends ClientAbstractAction {
    
    public CheckInAction(ExplorerFrame pOwner) {
        super(I18N.BUNDLE.getString("CheckIn_title"),
                "/com/docdoku/client/resources/icons/document_into.png", pOwner);
        putValue(Action.SHORT_DESCRIPTION, I18N.BUNDLE
                .getString("CheckIn_short_desc"));
        putValue(Action.LONG_DESCRIPTION, I18N.BUNDLE
                .getString("CheckIn_long_desc"));
        putValue(Action.MNEMONIC_KEY, new Integer(I18N.getCharBundle("CheckIn_mnemonic_key")));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('I',
                Event.CTRL_MASK));
        setLargeIcon("/com/docdoku/client/resources/icons/document_into_large.png");
    }
    
    public void actionPerformed(ActionEvent pAE) {
        final MasterDocument mdoc = mOwner.getSelectedMDoc();
        Thread worker = new Thread(new Runnable() {
            public void run() {
                try {
                    MainController controller = MainController.getInstance();
                    Document doc = mdoc.getLastIteration();
                    for(BinaryResource fileToUpload:doc.getAttachedFiles()){
                        try{
                            File localFile = new File(Config.getCheckOutFolder(mdoc),fileToUpload.getName());
                            long creationDate=Prefs.getLongDocInfo(mdoc, localFile.getName());
                            if(localFile.exists() && localFile.lastModified()>creationDate){
                                controller.saveFile(mOwner, doc, localFile);
                            }
                        }catch (InterruptedIOException pIIOEx) {
                            
                        }
                    }
                    MasterDocument newMDoc = controller.checkIn(mdoc);
                    FileIO.rmDir(Config.getCheckOutFolder(newMDoc));
                    Prefs.removeDocNode(newMDoc);
                }  catch (Exception pEx) {
                    String message = pEx.getMessage()==null?I18N.BUNDLE
                            .getString("Error_unknown"):pEx.getMessage();
                    JOptionPane.showMessageDialog(null,
                            message, I18N.BUNDLE
                            .getString("Error_title"),
                            JOptionPane.ERROR_MESSAGE);
                }
                ExplorerFrame.unselectElementInAllFrame();
            }
        });
        worker.start();
        
    }
}
