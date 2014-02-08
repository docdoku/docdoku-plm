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
import com.docdoku.client.data.MainModel;
import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.ExplorerFrame;
import com.docdoku.client.ui.doc.CreateVersionDialog;
import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.util.FileIO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InterruptedIOException;

public class CreateVersionAction extends ClientAbstractAction {
    
    public CreateVersionAction(ExplorerFrame pOwner) {
        super(I18N.BUNDLE.getString("VersionCreation_title"),
                "/com/docdoku/client/resources/icons/documents_new.png", pOwner);
        putValue(Action.SHORT_DESCRIPTION, I18N.BUNDLE
                .getString("VersionCreation_short_desc"));
        putValue(Action.LONG_DESCRIPTION, I18N.BUNDLE
                .getString("VersionCreation_long_desc"));
        putValue(Action.MNEMONIC_KEY, new Integer(I18N.getCharBundle("VersionCreation_mnemonic_key")));
    }
    
    public void actionPerformed(ActionEvent pAE) {
        ActionListener action = new ActionListener() {
            public void actionPerformed(ActionEvent pAE) {
                final CreateVersionDialog source = (CreateVersionDialog) pAE
                        .getSource();   
                Thread worker = new Thread(new Runnable() {
                    public void run() {
                        try {
                            mOwner.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                            MainController controller = MainController.getInstance();
                            DocumentMaster[] originalAndNewDocM = controller
                                    .createVersion(source.getOriginDocM(),
                                    source.getDocMTitle(),
                                    source.getDescription(),
                                    source.getWorkflowModel());
                            if(originalAndNewDocM[1].isCheckedOut()){
                                FileIO.rmDir(Config.getCheckOutFolder(originalAndNewDocM[1]));
                                for(BinaryResource remoteFile:originalAndNewDocM[1].getLastIteration().getAttachedFiles()){
                                    try{
                                        MainModel.getInstance().getFile(mOwner,originalAndNewDocM[1].getLastIteration(),remoteFile);
                                    } catch(InterruptedIOException pIIOEx){
                                        
                                    }
                                }
                            }
                        }  catch (Exception pEx) {
                            String message = pEx.getMessage()==null?I18N.BUNDLE
                                    .getString("Error_unknown"):pEx.getMessage();
                            JOptionPane.showMessageDialog(null,
                                    message, I18N.BUNDLE
                                    .getString("Error_title"),
                                    JOptionPane.ERROR_MESSAGE);
                        }finally{
                            mOwner.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                        }
                        ExplorerFrame.unselectElementInAllFrame();
                    }
                });
                worker.start();
                
            }
        };
        new CreateVersionDialog(mOwner, mOwner.getSelectedDocM(), action);
        
    }
}
