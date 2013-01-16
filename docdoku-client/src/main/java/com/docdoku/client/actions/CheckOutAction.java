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
import com.docdoku.core.util.FileIO;
import com.docdoku.core.common.BinaryResource;
import java.awt.Cursor;
import java.awt.Event;
import java.awt.event.ActionEvent;
import java.io.InterruptedIOException;

import javax.swing.*;
import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.ExplorerFrame;
import com.docdoku.core.document.DocumentMaster;

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
        final DocumentMaster docM = mOwner.getSelectedDocM();
        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mOwner.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    MainController controller = MainController.getInstance();
                    DocumentMaster newDocM = controller.checkOutDocument(docM);
                    FileIO.rmDir(Config.getCheckOutFolder(newDocM));
                    for(BinaryResource remoteFile:newDocM.getLastIteration().getAttachedFiles()){
                        try{
                            MainModel.getInstance().getFile(mOwner,newDocM.getLastIteration(),remoteFile);
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
