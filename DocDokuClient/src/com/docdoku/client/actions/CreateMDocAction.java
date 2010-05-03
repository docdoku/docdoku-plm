/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.client.actions;

import com.docdoku.client.data.Config;
import com.docdoku.client.data.MainModel;
import com.docdoku.core.entities.BinaryResource;
import com.docdoku.core.util.FileIO;
import com.docdoku.core.entities.MasterDocument;
import java.awt.Cursor;
import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InterruptedIOException;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.ExplorerFrame;
import com.docdoku.client.ui.doc.CreateMDocDialog;
import com.docdoku.core.entities.Folder;

public class CreateMDocAction extends ClientAbstractAction {
    
    public CreateMDocAction(ExplorerFrame pOwner) {
        super(I18N.BUNDLE.getString("MDocCreation_title"), "/com/docdoku/client/resources/icons/document_new.png", pOwner);
        putValue(Action.SHORT_DESCRIPTION, I18N.BUNDLE.getString("MDocCreation_short_desc"));
        putValue(Action.LONG_DESCRIPTION, I18N.BUNDLE.getString("MDocCreation_long_desc"));
        putValue(Action.MNEMONIC_KEY, new Integer(I18N.getCharBundle("MDocCreation_mnemonic_key")));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('N', Event.CTRL_MASK));
        setLargeIcon("/com/docdoku/client/resources/icons/document_new_large.png");
    }
    
    @Override
    public void actionPerformed(ActionEvent pAE) {
        ActionListener action = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent pAE) {
                final CreateMDocDialog source = (CreateMDocDialog) pAE.getSource();           
                Thread worker = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mOwner.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                            MainController controller = MainController.getInstance();
                            MasterDocument newMDoc = controller.createMDoc(source.getDestinationFolder().getCompletePath(), source.getMDocID(), source.getMDocTitle(), source.getDescription(), source.getMDocTemplate(), source.getWorkflowModel());
                            if(newMDoc.isCheckedOut()){
                                FileIO.rmDir(Config.getCheckOutFolder(newMDoc));
                                for(BinaryResource remoteFile:newMDoc.getLastIteration().getAttachedFiles()){
                                    try{
                                        MainModel.getInstance().getFile(mOwner,newMDoc.getLastIteration(),remoteFile);
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
                    }
                });
                worker.start();
            }
        };
        Folder destinationFolder = new Folder(mOwner.getSelectedFolder().getCompletePath());
        new CreateMDocDialog(mOwner, destinationFolder, action);
    }
}
