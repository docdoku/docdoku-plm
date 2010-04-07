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
import com.docdoku.core.util.FileIO;
import com.docdoku.core.entities.BinaryResource;
import java.awt.Cursor;
import java.awt.Event;
import java.awt.event.ActionEvent;
import java.io.InterruptedIOException;

import javax.swing.*;
import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.ExplorerFrame;
import com.docdoku.core.entities.MasterDocument;

public class CheckOutAction extends ClientAbstractAction {
    
    public CheckOutAction(ExplorerFrame pOwner) {
        super(I18N.BUNDLE.getString("CheckOut_title"), "/com/docdoku/client/resources/icons/document_out.png", pOwner);
        putValue(Action.SHORT_DESCRIPTION, I18N.BUNDLE.getString("CheckOut_short_desc"));
        putValue(Action.LONG_DESCRIPTION, I18N.BUNDLE.getString("CheckOut_long_desc"));
        putValue(Action.MNEMONIC_KEY, new Integer(I18N.getCharBundle("CheckOut_mnemonic_key")));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('O', Event.CTRL_MASK));
        setLargeIcon("/com/docdoku/client/resources/icons/document_out_large.png");
    }
    
    @Override
    public void actionPerformed(ActionEvent pAE) {
        final MasterDocument mdoc = mOwner.getSelectedMDoc();
        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mOwner.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    MainController controller = MainController.getInstance();
                    MasterDocument newMDoc = controller.checkOut(mdoc);
                    FileIO.rmDir(Config.getCheckOutFolder(newMDoc));
                    for(BinaryResource remoteFile:newMDoc.getLastIteration().getAttachedFiles()){
                        try{
                            MainModel.getInstance().getFile(mOwner,newMDoc.getLastIteration(),remoteFile);
                        } catch(InterruptedIOException pIIOEx){
                            
                        }
                    }
                }catch (Exception pEx) {
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
}
