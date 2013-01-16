/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.client.actions;

import com.docdoku.client.data.Config;
import com.docdoku.client.data.Prefs;
import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.util.FileIO;
import com.docdoku.core.document.DocumentMaster;
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
        final DocumentMaster docM = mOwner.getSelectedDocM();
        Thread worker = new Thread(new Runnable() {
            public void run() {
                try {
                    MainController controller = MainController.getInstance();
                    DocumentIteration doc = docM.getLastIteration();
                    for(BinaryResource fileToUpload:doc.getAttachedFiles()){
                        try{
                            File localFile = new File(Config.getCheckOutFolder(docM),fileToUpload.getName());
                            long creationDate=Prefs.getLongDocInfo(docM, localFile.getName());
                            if(localFile.exists() && localFile.lastModified()>creationDate){
                                controller.saveFile(mOwner, doc, localFile);
                            }
                        }catch (InterruptedIOException pIIOEx) {
                            
                        }
                    }
                    DocumentMaster newDocM = controller.checkInDocument(docM);
                    FileIO.rmDir(Config.getCheckOutFolder(newDocM));
                    Prefs.removeDocNode(newDocM);
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
